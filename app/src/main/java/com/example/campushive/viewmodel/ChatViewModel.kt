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
import com.google.firebase.firestore.Query
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

class ChatViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val conversationsRef = db.collection("conversations")
    private val listingsRef = db.collection("listings")
    private val usersRef = db.collection("users")
    private val auth: FirebaseAuth = Firebase.auth

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    // Cached maps from Firestore to resolve providerUID, providerName, and user fullNames
    private var listingsProviderUidMap = mapOf<String, String>()
    private var listingsProviderNameMap = mapOf<String, String>()
    private var usersNameMap = mapOf<String, String>()
    private var latestConversationDocs = listOf<DocumentSnapshot>()

    private var conversationsRegistration: ListenerRegistration? = null
    private var listingsRegistration: ListenerRegistration? = null
    private var usersRegistration: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        syncCurrentUserToFirestore(firebaseAuth)
        rebuildConversationsList()
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

    private fun getCurrentUserName(): String {
        val user = auth.currentUser
        return user?.displayName?.trim()?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.trim()?.takeIf { it.isNotBlank() }
            ?: "Student"
    }

    private fun syncCurrentUserToFirestore(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser ?: return
        val name = getCurrentUserName()
        val data = hashMapOf(
            "uid" to user.uid,
            "fullName" to name,
            "email" to (user.email ?: ""),
            "isVerified" to true
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
                }
                listingsProviderUidMap = uidMap
                listingsProviderNameMap = nameMap
                rebuildConversationsList()
            }
        }

        usersRegistration = usersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val uMap = mutableMapOf<String, String>()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid")?.takeIf { it.isNotBlank() } ?: doc.id
                    val fullName = doc.getString("fullName")
                        ?.takeIf { it.isNotBlank() }
                        ?: doc.getString("email")?.substringBefore("@")
                        ?: ""
                    if (fullName.isNotBlank()) {
                        uMap[uid] = fullName
                    }
                }
                usersNameMap = uMap
                rebuildConversationsList()
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
     * Inspects conversation messages to backfill missing seekerUid / seekerName / providerUID
     * (for example, when an older conversation overwrote seekerName with the provider's name).
     */
    private fun repairMissingConversationParticipants(docs: List<DocumentSnapshot>) {
        val currentUid = auth.currentUser?.uid ?: ""
        val myName = getCurrentUserName()

        for (doc in docs) {
            val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                ?: doc.id.removePrefix("chat_")
            val providerUID = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                ?: listingsProviderUidMap[listingId]
                ?: ""
            val providerName = doc.getString("providerName")?.takeIf { it.isNotBlank() }
                ?: listingsProviderNameMap[listingId]
                ?: usersNameMap[providerUID]
                ?: ""

            val storedSeekerUid = doc.getString("seekerUid") ?: ""
            val storedSeekerName = doc.getString("seekerName") ?: ""

            val seekerNameInvalid = storedSeekerName.isBlank() ||
                (providerName.isNotBlank() && storedSeekerName.equals(providerName, ignoreCase = true))

            if (storedSeekerUid.isBlank() || seekerNameInvalid || doc.getString("providerUID").isNullOrBlank()) {
                doc.reference.collection("messages").get().addOnSuccessListener { msgSnap ->
                    val updates = hashMapOf<String, Any>()
                    if (providerUID.isNotBlank() && doc.getString("providerUID").isNullOrBlank()) {
                        updates["providerUID"] = providerUID
                    }
                    if (providerName.isNotBlank() && doc.getString("providerName").isNullOrBlank()) {
                        updates["providerName"] = providerName
                    }

                    // Find a message sent by the Seeker (anyone whose senderUid != providerUID)
                    val seekerMsg = msgSnap.documents.firstOrNull { m ->
                        val sUid = m.getString("senderUid") ?: ""
                        val sRole = m.getString("senderRole") ?: ""
                        (sRole.equals("SEEKER", ignoreCase = true)) ||
                            (sUid.isNotBlank() && providerUID.isNotBlank() && sUid != providerUID)
                    }

                    val resolvedSeekerUid = storedSeekerUid.ifBlank {
                        seekerMsg?.getString("senderUid") ?: ""
                    }
                    if (resolvedSeekerUid.isNotBlank() && storedSeekerUid != resolvedSeekerUid) {
                        updates["seekerUid"] = resolvedSeekerUid
                    }

                    val msgSeekerName = seekerMsg?.getString("senderName")
                        ?.takeIf { it.isNotBlank() && !it.equals(providerName, ignoreCase = true) }
                    val userDocSeekerName = usersNameMap[resolvedSeekerUid]
                        ?.takeIf { it.isNotBlank() && !it.equals(providerName, ignoreCase = true) }
                    val currentUserAsSeekerName = if (
                        currentUid.isNotBlank() &&
                        (currentUid == resolvedSeekerUid || (providerUID.isNotBlank() && currentUid != providerUID)) &&
                        !myName.equals(providerName, ignoreCase = true)
                    ) {
                        myName
                    } else {
                        null
                    }

                    val resolvedSeekerName = msgSeekerName ?: userDocSeekerName ?: currentUserAsSeekerName
                    if (!resolvedSeekerName.isNullOrBlank() && storedSeekerName != resolvedSeekerName) {
                        updates["seekerName"] = resolvedSeekerName
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

        val chats = latestConversationDocs.mapNotNull { doc ->
            try {
                val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                    ?: doc.id.removePrefix("chat_")

                val providerUID = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                    ?: listingsProviderUidMap[listingId]
                    ?: ""
                val seekerUid = doc.getString("seekerUid") ?: ""

                val providerName = doc.getString("providerName")?.takeIf { it.isNotBlank() }
                    ?: listingsProviderNameMap[listingId]
                    ?: usersNameMap[providerUID]
                    ?: ""

                val rawSeekerName = doc.getString("seekerName") ?: ""
                val seekerName = rawSeekerName
                    .takeIf { it.isNotBlank() && !it.equals(providerName, ignoreCase = true) }
                    ?: usersNameMap[seekerUid]?.takeIf { !it.equals(providerName, ignoreCase = true) }
                    ?: ""

                val fallbackStudentName = doc.getString("studentName") ?: "Student"

                val amIProvider = (currentUid.isNotBlank() && providerUID.isNotBlank() && currentUid == providerUID) ||
                    (myName.isNotBlank() && providerName.isNotBlank() && myName.equals(providerName, ignoreCase = true))

                // If I am the Provider, always display the Seeker's name (never my own name).
                // If I am the Seeker, always display the Provider's name.
                val displayStudentName = if (amIProvider) {
                    when {
                        seekerName.isNotBlank() -> seekerName
                        fallbackStudentName.isNotBlank() && !fallbackStudentName.equals(myName, ignoreCase = true) && !fallbackStudentName.equals(providerName, ignoreCase = true) -> fallbackStudentName
                        else -> "Student Seeker"
                    }
                } else {
                    when {
                        providerName.isNotBlank() -> providerName
                        fallbackStudentName.isNotBlank() && !fallbackStudentName.equals(myName, ignoreCase = true) -> fallbackStudentName
                        else -> "Service Provider"
                    }
                }

                val serviceTitle = doc.getString("serviceTitle") ?: "Campus Service"
                val lastMessage = doc.getString("lastMessage") ?: ""
                val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                val isVerified = doc.getBoolean("isVerifiedStudent") ?: true

                val timeFormatted = doc.getTimestamp("lasttimestamp")?.toDate()?.let { date ->
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                } ?: "Just now"

                ChatConversation(
                    id = doc.id,
                    studentName = displayStudentName,
                    serviceTitle = serviceTitle,
                    lastMessage = lastMessage,
                    timestamp = timeFormatted,
                    unreadCount = unreadCount,
                    isVerifiedStudent = isVerified,
                    listingId = listingId,
                    seekerUid = seekerUid,
                    providerUID = providerUID
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
        var cachedSeekerUid = ""

        // Keep conversation role metadata (seekerUid & providerUID) updated
        val convListener = convDocRef.addSnapshotListener { convSnap, _ ->
            if (convSnap != null && convSnap.exists()) {
                cachedSeekerUid = convSnap.getString("seekerUid") ?: ""
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
                                    val updates = hashMapOf<String, Any>(
                                        "seekerUid" to currentUid,
                                        "seekerName" to myName,
                                        "providerUID" to pUid
                                    )
                                    if (pName.isNotBlank()) updates["providerName"] = pName
                                    convDocRef.set(updates, SetOptions.merge())
                                }
                            }
                    }
                }

                // Also backfill Seeker info immediately if cachedProviderUid is already known and current user is Seeker
                val currentUid = auth.currentUser?.uid ?: ""
                val myName = getCurrentUserName()
                if (currentUid.isNotBlank() && cachedProviderUid.isNotBlank() && currentUid != cachedProviderUid) {
                    val storedSeekerName = convSnap.getString("seekerName") ?: ""
                    val storedProviderName = convSnap.getString("providerName") ?: ""
                    if (storedSeekerName.isBlank() || storedSeekerName.equals(storedProviderName, ignoreCase = true)) {
                        convDocRef.set(
                            mapOf(
                                "seekerUid" to currentUid,
                                "seekerName" to myName
                            ),
                            SetOptions.merge()
                        )
                    }
                }
            }
        }

        val listener = convDocRef
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
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

                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        val text = doc.getString("text") ?: ""
                        val senderUid = doc.getString("senderUid") ?: ""
                        val senderRole = doc.getString("senderRole") ?: ""
                        val time = doc.getString("time")
                            ?: doc.getTimestamp("timestamp")?.toDate()?.let { date ->
                                SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                            }
                            ?: "Just now"

                        val isFromMe = when {
                            senderUid.isNotBlank() && currentUid.isNotBlank() -> senderUid == currentUid
                            senderRole.isNotBlank() && myRole.isNotBlank() -> senderRole.equals(myRole, ignoreCase = true)
                            else -> false
                        }

                        ChatMessage(
                            id = doc.id,
                            text = text,
                            time = time,
                            isFromMe = isFromMe,
                            senderRole = senderRole
                        )
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing message ${doc.id}", e)
                        null
                    }
                }

                trySend(messages)
            }

        awaitClose {
            convListener.remove()
            listener.remove()
        }
    }

    /**
     * Send a new message to Cloud Firestore in real time matching the ERD schema
     */
    fun sendMessage(
        conversationId: String,
        text: String,
        studentName: String = "Student",
        serviceTitle: String = "Campus Service"
    ) {
        if (text.isBlank()) return

        val currentUser = auth.currentUser
        val currentUid = currentUser?.uid ?: "usr_daniel"
        val currentUserName = getCurrentUserName()
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                val convDoc = if (conversationId.isBlank()) {
                    conversationsRef.document()
                } else {
                    conversationsRef.document(conversationId)
                }

                val existingConv = convDoc.get().await()
                val listingId = existingConv.getString("listingId")
                    ?.takeIf { it.isNotBlank() }
                    ?: convDoc.id.removePrefix("chat_")

                // Resolve providerUID and providerName from conversation or LISTING document
                var providerUID = existingConv.getString("providerUID")
                    ?.takeIf { it.isNotBlank() }
                    ?: listingsProviderUidMap[listingId]
                    ?: ""
                var providerName = existingConv.getString("providerName")
                    ?.takeIf { it.isNotBlank() }
                    ?: listingsProviderNameMap[listingId]
                    ?: ""

                if ((providerUID.isBlank() || providerName.isBlank()) && listingId.isNotBlank()) {
                    val listingSnap = listingsRef.document(listingId).get().await()
                    if (listingSnap.exists()) {
                        if (providerUID.isBlank()) {
                            providerUID = listingSnap.getString("providerUID") ?: ""
                        }
                        if (providerName.isBlank()) {
                            providerName = listingSnap.getString("providerName") ?: ""
                        }
                    }
                }

                val isProvider = (providerUID.isNotBlank() && currentUid == providerUID) ||
                    (providerName.isNotBlank() && currentUserName.equals(providerName, ignoreCase = true))

                val senderRole = if (isProvider) "PROVIDER" else "SEEKER"

                val resolvedProviderUid = if (isProvider && providerUID.isBlank()) currentUid else providerUID
                val resolvedProviderName = when {
                    isProvider -> currentUserName
                    providerName.isNotBlank() -> providerName
                    !studentName.equals(currentUserName, ignoreCase = true) -> studentName
                    else -> "Service Provider"
                }

                // Resolve Seeker UID & Seeker Name — never overwrite Seeker with Provider's own name/UID
                var resolvedSeekerUid = existingConv.getString("seekerUid")
                    ?.takeIf { it.isNotBlank() && it != resolvedProviderUid }
                    ?: ""
                var resolvedSeekerName = existingConv.getString("seekerName")
                    ?.takeIf { it.isNotBlank() && !it.equals(resolvedProviderName, ignoreCase = true) }
                    ?: ""

                if (!isProvider) {
                    // The current sender IS the Seeker
                    resolvedSeekerUid = currentUid
                    resolvedSeekerName = currentUserName
                } else {
                    // The current sender is the Provider replying to the Seeker.
                    // If seekerUid or seekerName is still missing, inspect existing messages or users collection.
                    if (resolvedSeekerUid.isBlank() || resolvedSeekerName.isBlank()) {
                        val msgsSnap = convDoc.collection("messages").get().await()
                        val seekerMsg = msgsSnap.documents.firstOrNull { m ->
                            val sUid = m.getString("senderUid") ?: ""
                            sUid.isNotBlank() && sUid != currentUid
                        }
                        if (resolvedSeekerUid.isBlank() && seekerMsg != null) {
                            resolvedSeekerUid = seekerMsg.getString("senderUid") ?: ""
                        }
                        if (resolvedSeekerName.isBlank() && seekerMsg != null) {
                            resolvedSeekerName = seekerMsg.getString("senderName")
                                ?.takeIf { it.isNotBlank() && !it.equals(resolvedProviderName, ignoreCase = true) }
                                ?: ""
                        }
                        if (resolvedSeekerName.isBlank() && resolvedSeekerUid.isNotBlank()) {
                            val seekerUserDoc = usersRef.document(resolvedSeekerUid).get().await()
                            resolvedSeekerName = seekerUserDoc.getString("fullName")
                                ?.takeIf { it.isNotBlank() && !it.equals(resolvedProviderName, ignoreCase = true) }
                                ?: usersNameMap[resolvedSeekerUid]
                                ?: ""
                        }
                    }
                    if (resolvedSeekerName.isBlank() && !studentName.equals(resolvedProviderName, ignoreCase = true) && !studentName.equals(currentUserName, ignoreCase = true)) {
                        resolvedSeekerName = studentName
                    }
                }

                // 1. Update Conversation Parent Document with ERD fields + both participant names
                val convData = hashMapOf<String, Any>(
                    "conversationId" to convDoc.id,
                    "listingId" to listingId,
                    "seekerUid" to resolvedSeekerUid,
                    "providerUID" to resolvedProviderUid,
                    "providerName" to resolvedProviderName,
                    "serviceTitle" to serviceTitle,
                    "lastMessage" to text.trim(),
                    "lasttimestamp" to Timestamp.now(),
                    "unreadCount" to 0,
                    "isVerifiedStudent" to true
                )
                if (resolvedSeekerName.isNotBlank()) {
                    convData["seekerName"] = resolvedSeekerName
                    convData["studentName"] = resolvedSeekerName
                }

                convDoc.set(convData, SetOptions.merge()).await()

                // 2. Write Message Subcollection Document with ERD fields + senderName
                val msgRef = convDoc.collection("messages").document()
                val msgData = hashMapOf(
                    "messageId" to msgRef.id,
                    "conversationId" to convDoc.id,
                    "senderRole" to senderRole,
                    "senderUid" to currentUid,
                    "senderName" to currentUserName,
                    "text" to text.trim(),
                    "time" to timeNow,
                    "timestamp" to Timestamp.now()
                )
                msgRef.set(msgData).await()

                Log.d("ChatViewModel", "Message sent ($senderRole) by $currentUserName to conversation: ${convDoc.id}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message: ${e.message}", e)
            }
        }
    }
}
