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

    private val _userOnlineStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val userOnlineStatus: StateFlow<Map<String, Boolean>> = _userOnlineStatus.asStateFlow()

    private val _userLastSeenMillis = MutableStateFlow<Map<String, Long>>(emptyMap())
    val userLastSeenMillis: StateFlow<Map<String, Long>> = _userLastSeenMillis.asStateFlow()

    // Cached maps from Firestore to resolve providerUID, providerName, and user usernames
    private var allKnownListingIds = setOf<String>()
    private var listingsProviderUidMap = mapOf<String, String>()
    private var listingsProviderNameMap = mapOf<String, String>()
    private var usersNameMap = mapOf<String, String>()
    private val discoveredSeekerUidMap = mutableMapOf<String, String>()
    private val discoveredSeekerNameMap = mutableMapOf<String, String>()
    private val optimisticConversations = mutableMapOf<String, ChatConversation>()
    private var latestConversationDocs = listOf<DocumentSnapshot>()

    private var conversationsRegistration: ListenerRegistration? = null
    private var listingsRegistration: ListenerRegistration? = null
    private var usersRegistration: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        optimisticConversations.clear()
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
            "isVerifiedStudent" to true,
            "isOnline" to true,
            "lastSeen" to Timestamp.now()
        )
        usersRef.document(user.uid).set(data, SetOptions.merge())
    }

    private fun startFirestoreListeners() {
        listingsRegistration = listingsRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val knownIds = mutableSetOf<String>()
                val uidMap = mutableMapOf<String, String>()
                val nameMap = mutableMapOf<String, String>()
                for (doc in snapshot.documents) {
                    val id = doc.getString("listingId")?.takeIf { it.isNotBlank() } ?: doc.id
                    knownIds.add(id)
                    knownIds.add(doc.id)
                    val pUid = doc.getString("providerUID") ?: ""
                    val pName = doc.getString("providerName") ?: ""
                    if (pUid.isNotBlank()) uidMap[id] = pUid
                    if (pName.isNotBlank()) nameMap[id] = pName
                    if (pUid.isNotBlank()) uidMap[doc.id] = pUid
                    if (pName.isNotBlank()) nameMap[doc.id] = pName
                }
                allKnownListingIds = knownIds
                listingsProviderUidMap = uidMap
                listingsProviderNameMap = nameMap
                rebuildConversationsList()
                repairMissingConversationParticipants(latestConversationDocs)
            }
        }

        usersRegistration = usersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val uMap = mutableMapOf<String, String>()
                val onlineMap = mutableMapOf<String, Boolean>()
                val lastSeenMap = mutableMapOf<String, Long>()
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid")?.takeIf { it.isNotBlank() } ?: doc.id
                    val username = extractUsernameFromDoc(doc)
                    val isOnline = doc.getBoolean("isOnline") ?: false
                    val lastSeenMillis = doc.getTimestamp("lastSeen")?.toDate()?.time ?: 0L

                    onlineMap[uid] = isOnline
                    onlineMap[doc.id] = isOnline
                    if (lastSeenMillis > 0L) {
                        lastSeenMap[uid] = lastSeenMillis
                        lastSeenMap[doc.id] = lastSeenMillis
                    }

                    if (username.isNotBlank()) {
                        uMap[uid] = username
                        uMap[doc.id] = username
                        val nameKey = username.trim().lowercase()
                        onlineMap[nameKey] = isOnline
                        if (lastSeenMillis > (lastSeenMap[nameKey] ?: 0L)) {
                            lastSeenMap[nameKey] = lastSeenMillis
                        }
                    }
                }
                usersNameMap = uMap
                _userOnlineStatus.value = onlineMap
                _userLastSeenMillis.value = lastSeenMap
                rebuildConversationsList()
                repairMissingConversationParticipants(latestConversationDocs)
            }
        }

        conversationsRegistration = conversationsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatViewModel", "Error listening to conversations", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                latestConversationDocs = snapshot.documents
                rebuildConversationsList()
                repairMissingConversationParticipants(snapshot.documents)
            }
        }
    }

    private fun isUsersIdentifier(target: String?): Boolean {
        if (target.isNullOrBlank()) return false
        val user = auth.currentUser ?: return false
        val cleanTarget = target.trim().lowercase()

        val myUid = user.uid.trim().lowercase()
        val myEmail = (user.email ?: "").trim().lowercase()
        val myEmailPrefix = myEmail.substringBefore("@").trim().lowercase()
        val myDisplayName = (user.displayName ?: "").trim().lowercase()
        val myName = getCurrentUserName().trim().lowercase()

        return cleanTarget == myUid ||
                (myEmail.isNotBlank() && cleanTarget == myEmail) ||
                (myEmailPrefix.isNotBlank() && cleanTarget == myEmailPrefix) ||
                (myDisplayName.isNotBlank() && !isGenericPlaceholderName(myDisplayName) && cleanTarget == myDisplayName) ||
                (myName.isNotBlank() && !isGenericPlaceholderName(myName) && cleanTarget == myName)
    }

    /**
     * Inspects conversation messages to discover and backfill the real seekerUid and seekerName
     * so the Provider always sees the Seeker's username (e.g., "sdawd") and the Seeker sees the Provider's name ("lusane").
     */
    private fun repairMissingConversationParticipants(docs: List<DocumentSnapshot>) {
        if (docs.isEmpty()) return
        val currentUid = auth.currentUser?.uid ?: ""
        val myName = getCurrentUserName()
        val knownIds = allKnownListingIds + listingsProviderUidMap.keys

        for (doc in docs) {
            val convId = doc.id
            val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                ?: extractListingIdFromChatId(convId, knownIds)
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
                ?: extractSeekerUidFromChatId(convId, knownIds).takeIf { it != providerUID }
                ?: ""
            val storedSeekerName = doc.getString("seekerName")
                ?.takeIf { !isGenericPlaceholderName(it) && !it.equals(providerName, ignoreCase = true) }
                ?: discoveredSeekerNameMap[convId]
                ?: ""

            if (doc.getString("lastMessage").isNullOrBlank() || storedSeekerUid.isBlank() || storedSeekerName.isBlank() || doc.getString("providerUID").isNullOrBlank()) {
                doc.reference.collection("messages").get().addOnSuccessListener { msgSnap ->
                    val updates = hashMapOf<String, Any>()
                    if (providerUID.isNotBlank() && doc.getString("providerUID").isNullOrBlank()) {
                        updates["providerUID"] = providerUID
                    }
                    if (providerName.isNotBlank() && doc.getString("providerName").isNullOrBlank()) {
                        updates["providerName"] = providerName
                    }

                    if (doc.getString("lastMessage").isNullOrBlank() && msgSnap.documents.isNotEmpty()) {
                        val latestMsg = msgSnap.documents.maxByOrNull {
                            it.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                        }
                        val lastMsgText = latestMsg?.getString("text")?.takeIf { it.isNotBlank() } ?: "Started a conversation"
                        val lastTs = latestMsg?.getTimestamp("timestamp") ?: Timestamp.now()
                        updates["lastMessage"] = lastMsgText
                        updates["lasttimestamp"] = lastTs
                    }

                    // Check if current user sent any message in this conversation
                    val myMsg = msgSnap.documents.firstOrNull { m ->
                        val sUid = m.getString("senderUid") ?: ""
                        isUsersIdentifier(sUid)
                    }
                    if (myMsg != null && currentUid.isNotBlank() && currentUid != providerUID) {
                        discoveredSeekerUidMap[convId] = currentUid
                        if (!isGenericPlaceholderName(myName)) {
                            discoveredSeekerNameMap[convId] = myName
                        }
                        updates["seekerUid"] = currentUid
                        if (!isGenericPlaceholderName(myName)) {
                            updates["seekerName"] = myName
                        }
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
                        resolvedSeekerUid.isNotBlank() &&
                        currentUid == resolvedSeekerUid &&
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
        val user = auth.currentUser ?: return
        val currentUid = user.uid
        val myName = getCurrentUserName()
        val knownIds = allKnownListingIds + listingsProviderUidMap.keys

        val myUserDocs = latestConversationDocs.filter { doc ->
            val convId = doc.id
            val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                ?: extractListingIdFromChatId(convId, knownIds)
            val pUid = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                ?: listingsProviderUidMap[listingId]
                ?: ""
            val sUid = doc.getString("seekerUid")?.takeIf { it.isNotBlank() && it != pUid }
                ?: discoveredSeekerUidMap[convId]
                ?: extractSeekerUidFromChatId(convId, knownIds).takeIf { it != pUid }
                ?: ""
            val pName = doc.getString("providerName") ?: ""
            val sName = doc.getString("seekerName") ?: ""

            val matchesProvider = isUsersIdentifier(pUid) ||
                    (!isGenericPlaceholderName(pName) && isUsersIdentifier(pName))

            val extractedSeekerInChatId = extractSeekerUidFromChatId(convId, knownIds)
            val matchesSeeker = isUsersIdentifier(sUid) ||
                    (!isGenericPlaceholderName(sName) && isUsersIdentifier(sName)) ||
                    isUsersIdentifier(extractedSeekerInChatId) ||
                    (convId.contains("_") && isUsersIdentifier(convId.substringAfterLast("_"))) ||
                    (!matchesProvider && currentUid.isNotBlank())

            matchesProvider || matchesSeeker
        }

        val firestoreChats = myUserDocs.mapNotNull { doc ->
            try {
                val convId = doc.id
                val listingId = doc.getString("listingId")?.takeIf { it.isNotBlank() }
                    ?: extractListingIdFromChatId(convId, knownIds)

                val providerUID = doc.getString("providerUID")?.takeIf { it.isNotBlank() }
                    ?: listingsProviderUidMap[listingId]
                    ?: ""
                val seekerUid = doc.getString("seekerUid")?.takeIf { it.isNotBlank() && it != providerUID }
                    ?: discoveredSeekerUidMap[convId]
                    ?: extractSeekerUidFromChatId(convId, knownIds).takeIf { it != providerUID }
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

                val counterpartUid = if (amIProvider) seekerUid else providerUID
                val onlineMap = _userOnlineStatus.value
                val lastSeenMap = _userLastSeenMillis.value
                val isCounterpartOnline = when {
                    counterpartUid.isNotBlank() && onlineMap.containsKey(counterpartUid) -> onlineMap[counterpartUid] == true
                    displayStudentName.isNotBlank() -> onlineMap[displayStudentName.trim().lowercase()] == true
                    else -> false
                }
                val counterpartLastSeen = when {
                    counterpartUid.isNotBlank() && (lastSeenMap[counterpartUid] ?: 0L) > 0L -> lastSeenMap[counterpartUid] ?: 0L
                    displayStudentName.isNotBlank() -> lastSeenMap[displayStudentName.trim().lowercase()] ?: 0L
                    else -> doc.getTimestamp("lasttimestamp")?.toDate()?.time ?: 0L
                }

                // Clear optimistic entry once Firestore has the conversation with its latest message
                val opt = optimisticConversations[convId]
                if (opt != null && (lastMessage.isNotBlank() || opt.lastMessage.isBlank())) {
                    optimisticConversations.remove(convId)
                }

                ChatConversation(
                    id = convId,
                    studentName = displayStudentName,
                    serviceTitle = serviceTitle,
                    lastMessage = if (lastMessage.isBlank() && opt != null) opt.lastMessage else lastMessage,
                    timestamp = timeFormatted,
                    unreadCount = unreadCount,
                    isVerifiedStudent = isVerified,
                    listingId = listingId,
                    seekerUid = seekerUid,
                    providerUID = providerUID,
                    providerName = providerName.ifBlank { displayStudentName },
                    seekerName = seekerName,
                    isProvider = amIProvider,
                    isOnline = isCounterpartOnline,
                    lastSeenMillis = counterpartLastSeen
                )
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error parsing conversation ${doc.id}", e)
                null
            }
        }

        val firestoreIds = firestoreChats.map { it.id }.toSet()
        val pendingOptimistic = optimisticConversations.values.filter { opt ->
            currentUid.isNotBlank() &&
                !firestoreIds.contains(opt.id) &&
                (opt.seekerUid == currentUid || opt.providerUID == currentUid)
        }

        _conversations.value = pendingOptimistic + firestoreChats
    }

    /**
     * Initializes or ensures a conversation exists when a Seeker clicks "Chat with Provider"
     * on the Service Detail screen so it immediately appears in the Chats list.
     */
    fun startConversation(
        conversationId: String,
        listingId: String,
        providerUID: String,
        providerName: String,
        serviceTitle: String
    ) {
        if (conversationId.isBlank()) return
        val currentUid = auth.currentUser?.uid ?: return
        val currentUserName = getCurrentUserName()

        val resolvedProviderUid = providerUID.ifBlank { listingsProviderUidMap[listingId].orEmpty() }
        val resolvedProviderName = providerName.ifBlank { listingsProviderNameMap[listingId].orEmpty().ifBlank { "Provider" } }

        discoveredSeekerUidMap[conversationId] = currentUid
        if (!isGenericPlaceholderName(currentUserName)) {
            discoveredSeekerNameMap[conversationId] = currentUserName
        }

        // Add optimistic conversation if not already in Firestore conversations list
        if (_conversations.value.none { it.id == conversationId }) {
            optimisticConversations[conversationId] = ChatConversation(
                id = conversationId,
                studentName = resolvedProviderName,
                serviceTitle = serviceTitle,
                lastMessage = "Tap to start chatting",
                timestamp = "Just now",
                unreadCount = 0,
                isVerifiedStudent = true,
                listingId = listingId,
                seekerUid = currentUid,
                providerUID = resolvedProviderUid,
                providerName = resolvedProviderName,
                seekerName = currentUserName,
                isProvider = false
            )
            rebuildConversationsList()
        }

        // Always persist & merge participant metadata into the Firestore conversation document
        val convDocRef = conversationsRef.document(conversationId)
        val updateData = hashMapOf<String, Any>(
            "conversationId" to conversationId,
            "listingId" to listingId,
            "seekerUid" to currentUid,
            "seekerName" to currentUserName,
            "providerUID" to resolvedProviderUid,
            "providerName" to resolvedProviderName,
            "serviceTitle" to serviceTitle,
            "isVerifiedStudent" to true
        )
        convDocRef.set(updateData, SetOptions.merge())

        convDocRef.get().addOnSuccessListener { snap ->
            if (!snap.exists() || snap.getString("lastMessage").isNullOrBlank()) {
                convDocRef.collection("messages").get().addOnSuccessListener { msgSnap ->
                    val latestMsg = msgSnap.documents.maxByOrNull {
                        it.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    }
                    val lastMsgText = latestMsg?.getString("text")?.takeIf { it.isNotBlank() } ?: "Started a conversation"
                    val lastTs = latestMsg?.getTimestamp("timestamp") ?: Timestamp.now()

                    val initData = hashMapOf<String, Any>(
                        "lastMessage" to lastMsgText,
                        "lasttimestamp" to lastTs,
                        "unreadCount" to 0
                    )
                    convDocRef.set(initData, SetOptions.merge())
                }
            }
        }
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
        val knownIds = allKnownListingIds + listingsProviderUidMap.keys
        val listingIdFromChatId = extractListingIdFromChatId(conversationId, knownIds)
        val seekerUidFromChatId = extractSeekerUidFromChatId(conversationId, knownIds)
        var cachedProviderUid = listingsProviderUidMap[listingIdFromChatId] ?: ""
        var cachedSeekerUid = discoveredSeekerUidMap[conversationId]
            ?.takeIf { it.isNotBlank() }
            ?: seekerUidFromChatId

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
                                // Only backfill Seeker info if this conversation does not already belong to another Seeker
                                val currentUid = auth.currentUser?.uid ?: ""
                                val myName = getCurrentUserName()
                                val canClaimSeeker = (sUid.isBlank() || sUid == currentUid) &&
                                    (seekerUidFromChatId.isBlank() || seekerUidFromChatId == currentUid)
                                if (currentUid.isNotBlank() && pUid.isNotBlank() && currentUid != pUid && canClaimSeeker) {
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

                // Only backfill Seeker info if cachedProviderUid is known and this conversation belongs to currentUid
                val currentUid = auth.currentUser?.uid ?: ""
                val myName = getCurrentUserName()
                val canClaimSeeker = (sUid.isBlank() || sUid == currentUid) &&
                    (seekerUidFromChatId.isBlank() || seekerUidFromChatId == currentUid)
                if (currentUid.isNotBlank() && cachedProviderUid.isNotBlank() && currentUid != cachedProviderUid && canClaimSeeker) {
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

                // Prevent leaking messages from a conversation that belongs to a different Seeker
                val effectiveSeekerUid = cachedSeekerUid.ifBlank {
                    snapshot.documents.firstOrNull { doc ->
                        val sUid = doc.getString("senderUid") ?: ""
                        sUid.isNotBlank() && cachedProviderUid.isNotBlank() && sUid != cachedProviderUid
                    }?.getString("senderUid").orEmpty()
                }
                if (currentUid.isNotBlank() &&
                    effectiveSeekerUid.isNotBlank() &&
                    currentUid != cachedProviderUid &&
                    currentUid != effectiveSeekerUid
                ) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val myRole = when {
                    currentUid.isNotBlank() && currentUid == cachedProviderUid -> "PROVIDER"
                    currentUid.isNotBlank() && currentUid == effectiveSeekerUid -> "SEEKER"
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

                // If messages exist in subcollection but parent conversation document is missing, repair parent doc
                if (sortedDocs.isNotEmpty() && latestConversationDocs.none { it.id == conversationId }) {
                    val lastDoc = sortedDocs.last()
                    val lastText = lastDoc.getString("text").orEmpty().ifBlank { "[Attachment]" }
                    val lastTs = lastDoc.getTimestamp("timestamp") ?: Timestamp.now()
                    val sUid = effectiveSeekerUid.ifBlank { seekerUidFromChatId.ifBlank { currentUid } }
                    val sName = discoveredSeekerNameMap[conversationId].orEmpty().ifBlank { getCurrentUserName() }
                    val pUid = cachedProviderUid.ifBlank { listingsProviderUidMap[listingIdFromChatId].orEmpty() }
                    val pName = listingsProviderNameMap[listingIdFromChatId].orEmpty()
                    val repairData = hashMapOf<String, Any>(
                        "conversationId" to conversationId,
                        "listingId" to listingIdFromChatId,
                        "seekerUid" to sUid,
                        "seekerName" to sName,
                        "studentName" to sName,
                        "providerUID" to pUid,
                        "lastMessage" to lastText,
                        "lasttimestamp" to lastTs,
                        "unreadCount" to 0,
                        "isVerifiedStudent" to true
                    )
                    if (pName.isNotBlank()) repairData["providerName"] = pName
                    convDocRef.set(repairData, SetOptions.merge())
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

        val convDoc = if (conversationId.isBlank()) {
            conversationsRef.document()
        } else {
            conversationsRef.document(conversationId)
        }

        val knownIds = allKnownListingIds + listingsProviderUidMap.keys
        val initialListingId = extractListingIdFromChatId(convDoc.id, knownIds)
        val initialProviderUid = listingsProviderUidMap[initialListingId].orEmpty()
        val initialProviderName = listingsProviderNameMap[initialListingId]
            ?.takeIf { !isGenericPlaceholderName(it) }
            ?: studentName.takeIf { !isGenericPlaceholderName(it) && !it.equals(currentUserName, ignoreCase = true) }
            ?: "Provider"
        val initialAmIProvider = initialProviderUid.isNotBlank() && currentUid == initialProviderUid
        val initialSeekerUid = if (!initialAmIProvider) {
            currentUid
        } else {
            discoveredSeekerUidMap[convDoc.id]
                ?: extractSeekerUidFromChatId(convDoc.id, knownIds).takeIf { it != initialProviderUid }
                ?: ""
        }
        val initialSeekerName = if (!initialAmIProvider) {
            currentUserName
        } else {
            discoveredSeekerNameMap[convDoc.id].orEmpty().ifBlank { studentName }
        }

        // Optimistically update conversations list and write parent conversation immediately (0ms lag, immune to back-navigation cancellation)
        if (!initialAmIProvider) {
            discoveredSeekerUidMap[convDoc.id] = currentUid
            discoveredSeekerNameMap[convDoc.id] = currentUserName
        }
        optimisticConversations[convDoc.id] = ChatConversation(
            id = convDoc.id,
            studentName = if (initialAmIProvider) initialSeekerName.ifBlank { "Student" } else initialProviderName,
            serviceTitle = serviceTitle,
            lastMessage = lastMessagePreview,
            timestamp = timeNow,
            unreadCount = 0,
            isVerifiedStudent = true,
            listingId = initialListingId,
            seekerUid = initialSeekerUid,
            providerUID = initialProviderUid,
            providerName = initialProviderName,
            seekerName = initialSeekerName,
            isProvider = initialAmIProvider
        )
        rebuildConversationsList()

        // Queue immediate local write to parent conversation + message subcollection so neither can be lost if user presses Back
        val msgRef = convDoc.collection("messages").document()
        val initialSenderRole = if (initialAmIProvider) "PROVIDER" else "SEEKER"
        val immediateMsgData = hashMapOf<String, Any>(
            "messageId" to msgRef.id,
            "conversationId" to convDoc.id,
            "senderRole" to initialSenderRole,
            "senderUid" to currentUid,
            "senderName" to currentUserName,
            "text" to text.trim(),
            "time" to timeNow,
            "timestamp" to nowTimestamp
        )
        if (attachmentType.isNotBlank()) {
            immediateMsgData["attachmentType"] = attachmentType
            immediateMsgData["attachmentName"] = attachmentName
            immediateMsgData["attachmentSize"] = attachmentSize
            immediateMsgData["attachmentBase64"] = attachmentBase64
        }
        msgRef.set(immediateMsgData)

        val immediateConvData = hashMapOf<String, Any>(
            "conversationId" to convDoc.id,
            "listingId" to initialListingId,
            "seekerUid" to initialSeekerUid,
            "providerUID" to initialProviderUid,
            "providerName" to initialProviderName,
            "serviceTitle" to serviceTitle,
            "lastMessage" to lastMessagePreview,
            "lasttimestamp" to nowTimestamp,
            "unreadCount" to 0,
            "isVerifiedStudent" to true
        )
        if (initialSeekerName.isNotBlank()) {
            immediateConvData["seekerName"] = initialSeekerName
            immediateConvData["studentName"] = initialSeekerName
        }
        convDoc.set(immediateConvData, SetOptions.merge())

        viewModelScope.launch {
            try {
                val existingConv = runCatching { convDoc.get().await() }.getOrNull()
                val listingId = existingConv?.getString("listingId")
                    ?.takeIf { it.isNotBlank() }
                    ?: initialListingId

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
                    ?: extractSeekerUidFromChatId(convDoc.id, knownIds).takeIf { it != resolvedProviderUid }
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

                // Update Conversation Parent Document with resolved ERD fields + participant usernames
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

    companion object {
        /**
         * Builds a unique conversation ID scoped to both the service listing and the seeker's UID
         * so each seeker gets their own isolated conversation thread with the provider.
         */
        fun buildConversationId(listingId: String, seekerUid: String): String {
            val cleanListingId = listingId.trim()
            val cleanSeekerUid = seekerUid.trim()
            return if (cleanSeekerUid.isNotBlank()) {
                "chat_${cleanListingId}_${cleanSeekerUid}"
            } else {
                "chat_$cleanListingId"
            }
        }

        /**
         * Extracts the listingId from either a per-seeker chatId ("chat_<listingId>_<seekerUid>")
         * or a legacy chatId ("chat_<listingId>").
         */
        fun extractListingIdFromChatId(
            chatId: String,
            knownListingIds: Set<String> = emptySet()
        ): String {
            val body = chatId.removePrefix("chat_")
            if (body.isBlank()) return ""

            if (knownListingIds.contains(body)) return body

            val matchedKnown = knownListingIds
                .filter { body.startsWith("${it}_") }
                .maxByOrNull { it.length }
            if (matchedKnown != null) return matchedKnown

            // Legacy "chat_srv_<digits>" without a seekerUid suffix
            val legacySrvOnlyRegex = Regex("^srv_\\d+$")
            if (legacySrvOnlyRegex.matches(body)) {
                return body
            }

            val srvRegex = Regex("^(srv_[^_]+)(?:_(.+))?$")
            val srvMatch = srvRegex.matchEntire(body)
            if (srvMatch != null) {
                return srvMatch.groupValues[1]
            }

            if (body.contains("_")) {
                return body.substringBeforeLast("_")
            }

            return body
        }

        /**
         * Extracts the seekerUid from a per-seeker chatId ("chat_<listingId>_<seekerUid>"),
         * or returns an empty string for legacy chatIds.
         */
        fun extractSeekerUidFromChatId(
            chatId: String,
            knownListingIds: Set<String> = emptySet()
        ): String {
            val body = chatId.removePrefix("chat_")
            if (body.isBlank()) return ""
            val listingId = extractListingIdFromChatId(chatId, knownListingIds)
            if (listingId.isNotBlank() && body.startsWith("${listingId}_")) {
                return body.removePrefix("${listingId}_")
            }
            return ""
        }
    }
}
