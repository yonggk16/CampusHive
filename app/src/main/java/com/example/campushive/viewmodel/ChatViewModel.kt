package com.example.campushive.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.model.ChatConversation
import com.example.campushive.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val conversationsRef = db.collection("conversations")
    private val listingsRef = db.collection("listings")
    private val usersRef = db.collection("users")
    private val auth: FirebaseAuth = Firebase.auth

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // Cached maps from Firestore to resolve providerUID, providerName, and user usernames
    private var listingsProviderUidMap = mapOf<String, String>()
    private var listingsProviderNameMap = mapOf<String, String>()
    private var usersNameMap = mapOf<String, String>()
    private val discoveredSeekerUidMap = mutableMapOf<String, String>()
    private val discoveredSeekerNameMap = mutableMapOf<String, String>()
    private var latestConversationDocs = listOf<DocumentSnapshot>()

    private var conversationsRegistration: ListenerRegistration? = null
    private var listingsRegistration: ListenerRegistration? = null
    private var usersRegistration: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        syncCurrentUserToFirestore(firebaseAuth)
        rebuildConversationsList()
        repairMissingConversationParticipants(latestConversationDocs)
    }

    init {
        syncCurrentUserToFirestore(auth)
        auth.addAuthStateListener(authStateListener)
        startFirestoreListeners()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        conversationsRegistration?.remove()
        listingsRegistration?.remove()
        usersRegistration?.remove()
    }

    private fun isGenericPlaceholderName(name: String?): Boolean {
        if (name.isNullOrBlank()) return true
        val trimmed = name.trim()
        return trimmed.equals("Student", ignoreCase = true) ||
            trimmed.equals("Student User", ignoreCase = true) ||
            trimmed.equals("Student Seeker", ignoreCase = true) ||
            trimmed.equals("Student Peer", ignoreCase = true) ||
            trimmed.equals("Service Provider", ignoreCase = true)
    }

    private fun extractUsernameFromDoc(doc: DocumentSnapshot): String {
        val displayName = doc.getString("displayName")?.trim()
        if (!isGenericPlaceholderName(displayName)) return displayName!!

        val fullName = doc.getString("fullName")?.trim()
        if (!isGenericPlaceholderName(fullName)) return fullName!!

        val handle = doc.getString("handle")?.trim()
        if (!isGenericPlaceholderName(handle)) return handle!!

        val emailPrefix = doc.getString("email")?.substringBefore("@")?.trim()
        if (!emailPrefix.isNullOrBlank()) return emailPrefix

        return displayName ?: fullName ?: ""
    }

    private fun getCurrentUserName(): String {
        val user = auth.currentUser ?: return "Student"
        val uid = user.uid
        val fromUsersMap = usersNameMap[uid]?.takeIf { !isGenericPlaceholderName(it) }
        val fromAuthDisplay = user.displayName?.trim()?.takeIf { !isGenericPlaceholderName(it) }
        val fromEmail = user.email?.substringBefore("@")?.trim()?.takeIf { it.isNotBlank() }
        return fromUsersMap ?: fromAuthDisplay ?: fromEmail ?: "Student"
    }

    private fun formatLocalTime(date: Date): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    }

    private fun syncCurrentUserToFirestore(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser ?: return
        val name = getCurrentUserName()
        val data = hashMapOf(
            "uid" to user.uid,
            "displayName" to name,
            "fullName" to name,
            "email" to (user.email ?: ""),
            "isVerifiedStudent" to true
        )
        usersRef.document(user.uid).set(data, SetOptions.merge())
    }

    private fun startFirestoreListeners() {
        listingsRegistration = listingsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val uidMap = mutableMapOf<String, String>()
                val nameMap = mutableMapOf<String, String>()
                for (doc in snapshot.documents) {
                    val id = doc.getString("listingId")?.takeIf { it.isNotBlank() } ?: doc.id
                    val pUid = doc.getString("providerUID") ?: ""
                    val pName = doc.getString("providerName") ?: ""
                    if (pUid.isNotBlank()) uidMap[id] = pUid
                    if (pName.isNotBlank()) nameMap[id] = pName
                    if (pUid.isNotBlank()) uidMap[doc.id] = pUid
                    if (pName.isNotBlank()) nameMap[doc.id] = pName
                }
                listingsProviderUidMap = uidMap
                listingsProviderNameMap = nameMap
                rebuildConversationsList()
                repairMissingConversationParticipants(latestConversationDocs)
            }
        }

        usersRegistration = usersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val uMap = mutableMapOf<String, String>()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid")?.takeIf { it.isNotBlank() } ?: doc.id
                    val username = extractUsernameFromDoc(doc)
                    if (username.isNotBlank()) {
                        uMap[uid] = username
                        uMap[doc.id] = username
                    }
                }
                usersNameMap = uMap
                rebuildConversationsList()
                repairMissingConversationParticipants(latestConversationDocs)
            }
        }

        conversationsRegistration = conversationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatViewModel", "Error listening to conversations", error)
                _conversations.value = emptyList()
                return@addSnapshotListener
            }
            if (snapshot != null) {
                latestConversationDocs = snapshot.documents
                rebuildConversationsList()
                repairMissingConversationParticipants(snapshot.documents)
            }
        }
    }

    /**
     * Inspects conversation messages to discover and backfill the real seekerUid and seekerName
     * so the Provider always sees the Seeker's username (e.g., "sdawd") and the Seeker sees the Provider's name ("lusane").
     */
    private fun repairMissingConversationParticipants(docs: List<DocumentSnapshot>) {
        if (docs.isEmpty()) return
        val currentUid = auth.currentUser?.uid ?: ""
        val myName = getCurrentUserName()

        for (doc in docs) {
            val convId = doc.id
            val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                ?: convId.removePrefix("chat_")
            val providerUID = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                ?: listingsProviderUidMap[listingId]
                ?: ""
            val providerName = doc.getString("providerName")?.takeIf { !isGenericPlaceholderName(it) }
                ?: listingsProviderNameMap[listingId]
                ?: usersNameMap[providerUID]
                ?: ""

            val storedSeekerUid = doc.getString("seekerUid")
                ?.takeIf { it.isNotBlank() && it != providerUID }
                ?: discoveredSeekerUidMap[convId]
                ?: ""
            val storedSeekerName = doc.getString("seekerName")
                ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                ?: discoveredSeekerNameMap[convId]
                ?: ""

            if (storedSeekerUid.isBlank() || storedSeekerName.isBlank() || doc.getString("providerUID").isNullOrBlank()) {
                doc.reference.collection("messages").get().addOnSuccessListener { msgSnap ->
                    val updates = hashMapOf<String, Any>()
                    if (providerUID.isNotBlank() && doc.getString("providerUID").isNullOrBlank()) {
                        updates["providerUID"] = providerUID
                    }
                    if (providerName.isNotBlank() && doc.getString("providerName").isNullOrBlank()) {
                        updates["providerName"] = providerName
                    }

                    // Find any message sent by the Seeker (senderUid != providerUID)
                    val seekerMsg = msgSnap.documents.firstOrNull { m ->
                        val sUid = m.getString("senderUid") ?: ""
                        val sRole = m.getString("senderRole") ?: ""
                        val sName = m.getString("senderName") ?: ""
                        (sRole.equals("SEEKER", ignoreCase = true)) ||
                            (sUid.isNotBlank() && providerUID.isNotBlank() && sUid != providerUID) ||
                            (sName.isNotBlank() && providerName.isNotBlank() && !sName.equals(providerName, ignoreCase = true))
                    }

                    val resolvedSeekerUid = storedSeekerUid.ifBlank {
                        seekerMsg?.getString("senderUid")?.takeIf { it != providerUID } ?: ""
                    }
                    if (resolvedSeekerUid.isNotBlank()) {
                        discoveredSeekerUidMap[convId] = resolvedSeekerUid
                        if (doc.getString("seekerUid") != resolvedSeekerUid) {
                            updates["seekerUid"] = resolvedSeekerUid
                        }
                    }

                    val msgSeekerName = seekerMsg?.getString("senderName")
                        ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                    val userDocSeekerName = usersNameMap[resolvedSeekerUid]
                        ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                    val currentUserAsSeekerName = if (
                        currentUid.isNotBlank() &&
                        (currentUid == resolvedSeekerUid || (providerUID.isNotBlank() && currentUid != providerUID)) &&
                        !isGenericPlaceholderName(myName) &&
                        !myName.equals(providerName, ignoreCase = true)
                    ) {
                        myName
                    } else {
                        null
                    }

                    val resolvedSeekerName = msgSeekerName ?: userDocSeekerName ?: currentUserAsSeekerName
                    if (!resolvedSeekerName.isNullOrBlank()) {
                        discoveredSeekerNameMap[convId] = resolvedSeekerName
                        if (doc.getString("seekerName") != resolvedSeekerName) {
                            updates["seekerName"] = resolvedSeekerName
                        }
                        rebuildConversationsList()
                    } else if (resolvedSeekerUid.isNotBlank()) {
                        // Fetch the seeker's user profile directly from Firestore if not in cache yet
                        usersRef.document(resolvedSeekerUid).get().addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val fetchedName = extractUsernameFromDoc(userDoc)
                                if (fetchedName.isNotBlank() && !fetchedName.equals(providerName, ignoreCase = true)) {
                                    discoveredSeekerNameMap[convId] = fetchedName
                                    doc.reference.set(mapOf("seekerName" to fetchedName), SetOptions.merge())
                                    rebuildConversationsList()
                                }
                            }
                        }
                    }

                    if (updates.isNotEmpty()) {
                        doc.reference.set(updates, SetOptions.merge())
                    }
                }
            }
        }
    }

    private fun rebuildConversationsList() {
        val currentUid = auth.currentUser?.uid ?: ""
        val myName = getCurrentUserName()

        val myUserDocs = latestConversationDocs.filter { doc ->
            if (currentUid.isBlank()) return@filter true
            val convId = doc.id
            val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                ?: convId.removePrefix("chat_")
            val pUid = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                ?: listingsProviderUidMap[listingId]
                ?: ""
            val sUid = doc.getString("seekerUid")?.takeIf { it.isNotBlank() && it != pUid }
                ?: discoveredSeekerUidMap[convId]
                ?: ""
            val pName = doc.getString("providerName") ?: ""
            val sName = doc.getString("seekerName") ?: ""

            currentUid == pUid || currentUid == sUid ||
            (myName.isNotBlank() && (myName.equals(pName, ignoreCase = true) || myName.equals(sName, ignoreCase = true)))
        }

        val chats = myUserDocs.mapNotNull { doc ->
            try {
                val convId = doc.id
                val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                    ?: convId.removePrefix("chat_")

                val providerUID = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                    ?: listingsProviderUidMap[listingId]
                    ?: ""
                val seekerUid = doc.getString("seekerUid")?.takeIf { it.isNotBlank() && it != providerUID }
                    ?: discoveredSeekerUidMap[convId]
                    ?: ""

                val providerName = doc.getString("providerName")?.takeIf { !isGenericPlaceholderName(it) }
                    ?: listingsProviderNameMap[listingId]?.takeIf { !isGenericPlaceholderName(it) }
                    ?: usersNameMap[providerUID]?.takeIf { !isGenericPlaceholderName(it) }
                    ?: ""

                val rawSeekerName = doc.getString("seekerName") ?: ""
                val seekerName = rawSeekerName
                    .takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                    ?: discoveredSeekerNameMap[convId]?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                    ?: usersNameMap[seekerUid]?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                    ?: ""

                val fallbackStudentName = doc.getString("studentName") ?: ""

                val amIProvider = when {
                    currentUid.isNotBlank() && providerUID.isNotBlank() -> currentUid == providerUID
                    myName.isNotBlank() && providerName.isNotBlank() -> myName.equals(providerName, ignoreCase = true)
                    else -> false
                }

                // If I am the Provider (e.g. lusane), ALWAYS display the Seeker's username (e.g. sdawd).
                // If I am the Seeker (e.g. sdawd), ALWAYS display the Provider's username (e.g. lusane).
                val displayStudentName = if (amIProvider) {
                    when {
                        seekerName.isNotBlank() -> seekerName
                        !isGenericPlaceholderName(fallbackStudentName) &&
                            !fallbackStudentName.equals(myName, ignoreCase = true) &&
                            !fallbackStudentName.equals(providerName, ignoreCase = true) -> fallbackStudentName
                        seekerUid.isNotBlank() -> usersNameMap[seekerUid] ?: seekerUid.take(8)
                        else -> "Student"
                    }
                } else {
                    when {
                        providerName.isNotBlank() -> providerName
                        !isGenericPlaceholderName(fallbackStudentName) &&
                            !fallbackStudentName.equals(myName, ignoreCase = true) -> fallbackStudentName
                        else -> "Provider"
                    }
                }

                val serviceTitle = doc.getString("serviceTitle") ?: "Campus Service"
                val lastMessage = doc.getString("lastMessage") ?: ""
                val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                val isVerified = doc.getBoolean("isVerifiedStudent") ?: true

                val timeFormatted = doc.getTimestamp("lasttimestamp")?.toDate()?.let { date ->
                    formatLocalTime(date)
                } ?: "Just now"

                ChatConversation(
                    id = convId,
                    studentName = displayStudentName,
                    serviceTitle = serviceTitle,
                    lastMessage = lastMessage,
                    timestamp = timeFormatted,
                    unreadCount = unreadCount,
                    isVerifiedStudent = isVerified,
                    listingId = listingId,
                    seekerUid = seekerUid,
                    providerUID = providerUID,
                    providerName = providerName.ifBlank { displayStudentName },
                    seekerName = seekerName,
                    isProvider = amIProvider
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error parsing conversation ${doc.id}", e)
                null
            }
        }

        _conversations.value = chats
    }

    /**
     * Stream real-time messages for a specific conversation from Cloud Firestore
     */
    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val convDocRef = conversationsRef.document(conversationId)
        val listingIdFromChatId = conversationId.removePrefix("chat_")
        var cachedProviderUid = listingsProviderUidMap[listingIdFromChatId] ?: ""
        var cachedSeekerUid = discoveredSeekerUidMap[conversationId] ?: ""

        // Keep conversation role metadata (seekerUid & providerUID) updated
        val convListener = convDocRef.addSnapshotListener { convSnap, _ ->
            if (convSnap != null && convSnap.exists()) {
                val sUid = convSnap.getString("seekerUid") ?: ""
                if (sUid.isNotBlank()) cachedSeekerUid = sUid

                val convProviderUid = convSnap.getString("providerUID") ?: ""
                if (convProviderUid.isNotBlank()) {
                    cachedProviderUid = convProviderUid
                } else {
                    val listingId = convSnap.getString("listingId")
                        ?.takeIf { it.isNotBlank() }
                        ?: listingIdFromChatId
                    if (listingId.isNotBlank()) {
                        listingsRef.document(listingId).get()
                            .addOnSuccessListener { listingSnap ->
                                val pUid = listingSnap.getString("providerUID") ?: ""
                                val pName = listingSnap.getString("providerName") ?: ""
                                if (pUid.isNotBlank()) {
                                    cachedProviderUid = pUid
                                }
                                // Backfill Seeker info if the currently logged-in user is the Seeker opening this chat
                                val currentUid = auth.currentUser?.uid ?: ""
                                val myName = getCurrentUserName()
                                if (currentUid.isNotBlank() && pUid.isNotBlank() && currentUid != pUid) {
                                    discoveredSeekerUidMap[conversationId] = currentUid
                                    if (!isGenericPlaceholderName(myName)) {
                                        discoveredSeekerNameMap[conversationId] = myName
                                    }
                                    val updates = hashMapOf<String, Any>(
                                        "seekerUid" to currentUid,
                                        "seekerName" to myName,
                                        "providerUID" to pUid
                                    )
                                    if (pName.isNotBlank()) updates["providerName"] = pName
                                    convDocRef.set(updates, SetOptions.merge())
                                    rebuildConversationsList()
                                }
                            }
                    }
                }

                // Also backfill Seeker info immediately if cachedProviderUid is already known and current user is Seeker
                val currentUid = auth.currentUser?.uid ?: ""
                val myName = getCurrentUserName()
                if (currentUid.isNotBlank() && cachedProviderUid.isNotBlank() && currentUid != cachedProviderUid) {
                    discoveredSeekerUidMap[conversationId] = currentUid
                    if (!isGenericPlaceholderName(myName)) {
                        discoveredSeekerNameMap[conversationId] = myName
                    }
                    val storedSeekerName = convSnap.getString("seekerName") ?: ""
                    val storedProviderName = convSnap.getString("providerName") ?: ""
                    if (isGenericPlaceholderName(storedSeekerName) || storedSeekerName.equals(storedProviderName, ignoreCase = true)) {
                        convDocRef.set(
                            mapOf(
                                "seekerUid" to currentUid,
                                "seekerName" to myName
                            ),
                            SetOptions.merge()
                        )
                        rebuildConversationsList()
                    }
                }
            }
        }

        // Listen to all messages in the subcollection and sort in Kotlin so documents are never dropped
        val listener = convDocRef
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error listening to messages for $conversationId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val currentUid = auth.currentUser?.uid ?: ""
                val myRole = when {
                    currentUid.isNotBlank() && currentUid == cachedProviderUid -> "PROVIDER"
                    currentUid.isNotBlank() && currentUid == cachedSeekerUid -> "SEEKER"
                    currentUid.isNotBlank() && cachedProviderUid.isNotBlank() && currentUid != cachedProviderUid -> "SEEKER"
                    else -> ""
                }

                val sortedDocs = snapshot.documents.sortedBy { doc ->
                    doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                }

                val messages = sortedDocs.mapNotNull { doc ->
                    try {
                        val text = doc.getString("text") ?: ""
                        val senderUid = doc.getString("senderUid") ?: ""
                        val senderRole = doc.getString("senderRole") ?: ""
                        val senderName = doc.getString("senderName") ?: ""
                        val attachmentType = doc.getString("attachmentType") ?: ""
                        val attachmentName = doc.getString("attachmentName") ?: ""
                        val attachmentSize = doc.getString("attachmentSize") ?: ""
                        val attachmentBase64 = doc.getString("attachmentBase64") ?: ""

                        // If we see a message from the Seeker, cache their UID and username immediately
                        if (senderUid.isNotBlank() && cachedProviderUid.isNotBlank() && senderUid != cachedProviderUid) {
                            discoveredSeekerUidMap[conversationId] = senderUid
                            if (!isGenericPlaceholderName(senderName)) {
                                discoveredSeekerNameMap[conversationId] = senderName
                            }
                        }

                        val time = doc.getTimestamp("timestamp")?.toDate()?.let { date ->
                            formatLocalTime(date)
                        } ?: doc.getString("time") ?: formatLocalTime(Date())

                        val isFromMe = when {
                            senderUid.isNotBlank() && currentUid.isNotBlank() -> senderUid == currentUid
                            senderRole.isNotBlank() && myRole.isNotBlank() -> senderRole.equals(myRole, ignoreCase = true)
                            else -> false
                        }

                        val isEdited = doc.getBoolean("isEdited") ?: false
                        val isDeleted = doc.getBoolean("isDeleted") ?: false

                        ChatMessage(
                            id = doc.id,
                            text = text,
                            time = time,
                            isFromMe = isFromMe,
                            senderRole = senderRole,
                            attachmentType = attachmentType,
                            attachmentName = attachmentName,
                            attachmentSize = attachmentSize,
                            attachmentBase64 = attachmentBase64,
                            isEdited = isEdited,
                            isDeleted = isDeleted
                        )
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing message ${doc.id}", e)
                        null
                    }
                }

                rebuildConversationsList()
                trySend(messages)
            }

        awaitClose {
            convListener.remove()
            listener.remove()
        }
    }

    /**
     * Send a new message (text and/or image/file attachment) to Cloud Firestore in real time matching the ERD schema
     */
    fun sendMessage(
        conversationId: String,
        text: String,
        studentName: String = "Student",
        serviceTitle: String = "Campus Service",
        attachmentType: String = "",
        attachmentName: String = "",
        attachmentSize: String = "",
        attachmentBase64: String = ""
    ) {
        if (text.isBlank() && attachmentType.isBlank()) return

        val currentUser = auth.currentUser
        val currentUid = currentUser?.uid ?: "usr_daniel"
        val currentUserName = getCurrentUserName()
        val nowTimestamp = Timestamp.now()
        val timeNow = formatLocalTime(nowTimestamp.toDate())

        val lastMessagePreview = when {
            text.isNotBlank() && attachmentType == "IMAGE" -> "[Photo] ${text.trim()}"
            text.isNotBlank() && attachmentType == "FILE" -> "[File] ${text.trim()}"
            text.isNotBlank() -> text.trim()
            attachmentType == "IMAGE" -> "[Photo]"
            attachmentType == "FILE" -> "[File] ${attachmentName.ifBlank { "Attachment" }}"
            else -> ""
        }

        viewModelScope.launch {
            try {
                val convDoc = if (conversationId.isBlank()) {
                    conversationsRef.document()
                } else {
                    conversationsRef.document(conversationId)
                }

                val existingConv = runCatching { convDoc.get().await() }.getOrNull()
                val listingId = existingConv?.getString("listingId")
                    ?.takeIf { it.isNotBlank() }
                    ?: convDoc.id.removePrefix("chat_")

                // Resolve providerUID and providerName from conversation or LISTING document
                var providerUID = existingConv?.getString("providerUID")
                    ?.takeIf { it.isNotBlank() }
                    ?: listingsProviderUidMap[listingId]
                    ?: ""
                var providerName = existingConv?.getString("providerName")
                    ?.takeIf { !isGenericPlaceholderName(it) }
                    ?: listingsProviderNameMap[listingId]
                    ?: ""

                if ((providerUID.isBlank() || providerName.isBlank()) && listingId.isNotBlank()) {
                    val listingSnap = runCatching { listingsRef.document(listingId).get().await() }.getOrNull()
                    if (listingSnap != null && listingSnap.exists()) {
                        if (providerUID.isBlank()) {
                            providerUID = listingSnap.getString("providerUID") ?: ""
                        }
                        if (providerName.isBlank()) {
                            providerName = listingSnap.getString("providerName") ?: ""
                        }
                    }
                }

                val isProvider = when {
                    providerUID.isNotBlank() && currentUid.isNotBlank() -> currentUid == providerUID
                    providerName.isNotBlank() && currentUserName.isNotBlank() -> currentUserName.equals(providerName, ignoreCase = true)
                    else -> false
                }

                val senderRole = if (isProvider) "PROVIDER" else "SEEKER"

                val resolvedProviderUid = if (isProvider && providerUID.isBlank()) currentUid else providerUID
                val resolvedProviderName = when {
                    isProvider -> currentUserName
                    providerName.isNotBlank() -> providerName
                    !isGenericPlaceholderName(studentName) && !studentName.equals(currentUserName, ignoreCase = true) -> studentName
                    else -> "Provider"
                }

                // Resolve Seeker UID & Seeker Name — never overwrite Seeker with Provider's own name/UID
                var resolvedSeekerUid = existingConv?.getString("seekerUid")
                    ?.takeIf { it.isNotBlank() && it != resolvedProviderUid }
                    ?: discoveredSeekerUidMap[convDoc.id]
                    ?: ""
                var resolvedSeekerName = existingConv?.getString("seekerName")
                    ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(resolvedProviderName, ignoreCase = true) }
                    ?: discoveredSeekerNameMap[convDoc.id]
                    ?: ""

                if (!isProvider) {
                    // The current sender IS the Seeker
                    resolvedSeekerUid = currentUid
                    resolvedSeekerName = currentUserName
                    discoveredSeekerUidMap[convDoc.id] = currentUid
                    discoveredSeekerNameMap[convDoc.id] = currentUserName
                } else {
                    // The current sender is the Provider replying to the Seeker.
                    if (resolvedSeekerUid.isBlank() || resolvedSeekerName.isBlank()) {
                        val msgsSnap = runCatching { convDoc.collection("messages").get().await() }.getOrNull()
                        val seekerMsg = msgsSnap?.documents?.firstOrNull { m ->
                            val sUid = m.getString("senderUid") ?: ""
                            sUid.isNotBlank() && sUid != currentUid
                        }
                        if (resolvedSeekerUid.isBlank() && seekerMsg != null) {
                            resolvedSeekerUid = seekerMsg.getString("senderUid") ?: ""
                        }
                        if (resolvedSeekerName.isBlank() && seekerMsg != null) {
                            resolvedSeekerName = seekerMsg.getString("senderName")
                                ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(resolvedProviderName, ignoreCase = true) }
                                ?: ""
                        }
                        if (resolvedSeekerName.isBlank() && resolvedSeekerUid.isNotBlank()) {
                            val seekerUserDoc = runCatching { usersRef.document(resolvedSeekerUid).get().await() }.getOrNull()
                            if (seekerUserDoc != null && seekerUserDoc.exists()) {
                                resolvedSeekerName = extractUsernameFromDoc(seekerUserDoc)
                                    .takeIf { !isGenericPlaceholderName(it) && !it.equals(resolvedProviderName, ignoreCase = true) }
                                    ?: ""
                            }
                            if (resolvedSeekerName.isBlank()) {
                                resolvedSeekerName = usersNameMap[resolvedSeekerUid]
                                    ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(resolvedProviderName, ignoreCase = true) }
                                    ?: ""
                            }
                        }
                    }
                    if (resolvedSeekerName.isBlank() &&
                        !isGenericPlaceholderName(studentName) &&
                        !studentName.equals(resolvedProviderName, ignoreCase = true) &&
                        !studentName.equals(currentUserName, ignoreCase = true)
                    ) {
                        resolvedSeekerName = studentName
                    }
                }

                // 1. Write Message Subcollection Document first so the message is delivered immediately
                val msgRef = convDoc.collection("messages").document()
                val msgData = hashMapOf<String, Any>(
                    "messageId" to msgRef.id,
                    "conversationId" to convDoc.id,
                    "senderRole" to senderRole,
                    "senderUid" to currentUid,
                    "senderName" to currentUserName,
                    "text" to text.trim(),
                    "time" to timeNow,
                    "timestamp" to nowTimestamp
                )
                if (attachmentType.isNotBlank()) {
                    msgData["attachmentType"] = attachmentType
                    msgData["attachmentName"] = attachmentName
                    msgData["attachmentSize"] = attachmentSize
                    msgData["attachmentBase64"] = attachmentBase64
                }
                msgRef.set(msgData).await()

                // 2. Update Conversation Parent Document with ERD fields + participant usernames
                val convData = hashMapOf<String, Any>(
                    "conversationId" to convDoc.id,
                    "listingId" to listingId,
                    "seekerUid" to resolvedSeekerUid,
                    "providerUID" to resolvedProviderUid,
                    "providerName" to resolvedProviderName,
                    "serviceTitle" to serviceTitle,
                    "lastMessage" to lastMessagePreview,
                    "lasttimestamp" to nowTimestamp,
                    "unreadCount" to 0,
                    "isVerifiedStudent" to true
                )
                if (resolvedSeekerName.isNotBlank()) {
                    convData["seekerName"] = resolvedSeekerName
                    convData["studentName"] = resolvedSeekerName
                }

                convDoc.set(convData, SetOptions.merge()).await()

                Log.d("ChatViewModel", "Message sent ($senderRole) by $currentUserName to conversation: ${convDoc.id}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message: ${e.message}", e)
            }
        }
    }

    /**
     * Edit a sent message in Cloud Firestore in real time
     */
    fun editMessage(conversationId: String, messageId: String, newText: String) {
        if (newText.isBlank() || conversationId.isBlank() || messageId.isBlank()) return

        viewModelScope.launch {
            try {
                val msgRef = conversationsRef.document(conversationId).collection("messages").document(messageId)
                msgRef.update(
                    mapOf(
                        "text" to newText.trim(),
                        "isEdited" to true
                    )
                ).await()

                // Update parent conversation preview
                conversationsRef.document(conversationId).update("lastMessage", newText.trim()).await()
                Log.d("ChatViewModel", "Successfully edited message: $messageId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to edit message: ${e.message}", e)
            }
        }
    }

    /**
     * Delete a sent message in Cloud Firestore in real time
     */
    fun deleteMessage(conversationId: String, messageId: String) {
        if (conversationId.isBlank() || messageId.isBlank()) return

        viewModelScope.launch {
            try {
                val msgRef = conversationsRef.document(conversationId).collection("messages").document(messageId)
                msgRef.update(
                    mapOf(
                        "text" to "This message was deleted",
                        "isDeleted" to true
                    )
                ).await()

                // Update parent conversation preview
                conversationsRef.document(conversationId).update("lastMessage", "This message was deleted").await()
                Log.d("ChatViewModel", "Successfully deleted message: $messageId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to delete message: ${e.message}", e)
            }
        }
    }
}
