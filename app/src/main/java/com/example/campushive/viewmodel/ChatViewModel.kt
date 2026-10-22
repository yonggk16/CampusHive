package com.example.campushive.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.model.ChatConversation
import com.example.campushive.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    init {
        listenToConversations()
    }

    /**
     * Stream real-time conversations from Cloud Firestore
     */
    private fun listenToConversations() {
        viewModelScope.launch {
            getConversationsFlow().collect { liveChats ->
                _conversations.value = liveChats
            }
        }
    }

    private fun getConversationsFlow(): Flow<List<ChatConversation>> = callbackFlow {
        val listener = conversationsRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error listening to conversations", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val chats = snapshot.documents.mapNotNull { doc ->
                    try {
                        val studentName = doc.getString("studentName") ?: "Student"
                        val serviceTitle = doc.getString("serviceTitle") ?: "Campus Service"
                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                        val isVerified = doc.getBoolean("isVerifiedStudent") ?: true

                        val timeFormatted = doc.getTimestamp("lasttimestamp")?.toDate()?.let { date ->
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
                        } ?: "Just now"

                        ChatConversation(
                            id = doc.id,
                            studentName = studentName,
                            serviceTitle = serviceTitle,
                            lastMessage = lastMessage,
                            timestamp = timeFormatted,
                            unreadCount = unreadCount,
                            isVerifiedStudent = isVerified
                        )
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing conversation ${doc.id}", e)
                        null
                    }
                }

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Stream real-time messages for a specific conversation from Cloud Firestore
     */
    fun getMessagesFlow(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = conversationsRef.document(conversationId)
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

                val currentUid = Firebase.auth.currentUser?.uid ?: ""
                val messages = snapshot.documents.mapNotNull { doc ->
                    try {
                        val text = doc.getString("text") ?: ""
                        val senderUid = doc.getString("senderUid") ?: ""
                        val time = doc.getString("time") ?: "Just now"
                        val isFromMe = doc.getBoolean("isFromMe") ?: (senderUid == currentUid)

                        ChatMessage(
                            id = doc.id,
                            text = text,
                            time = time,
                            isFromMe = isFromMe
                        )
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error parsing message ${doc.id}", e)
                        null
                    }
                }

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Send a new message to Cloud Firestore in real time
     */
    fun sendMessage(
        conversationId: String,
        text: String,
        studentName: String = "Student",
        serviceTitle: String = "Campus Service"
    ) {
        if (text.isBlank()) return

        val currentUid = Firebase.auth.currentUser?.uid ?: "usr_daniel"
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                val convDoc = if (conversationId.isBlank()) {
                    conversationsRef.document()
                } else {
                    conversationsRef.document(conversationId)
                }

                // 1. Ensure Conversation Parent Document exists/updates
                val convData = hashMapOf(
                    "conversationId" to convDoc.id,
                    "studentName" to studentName,
                    "serviceTitle" to serviceTitle,
                    "lastMessage" to text.trim(),
                    "lasttimestamp" to Timestamp.now(),
                    "unreadCount" to 0,
                    "isVerifiedStudent" to true
                )
                convDoc.set(convData).await()

                // 2. Write Message Subcollection Document
                val msgRef = convDoc.collection("messages").document()
                val msgData = hashMapOf(
                    "messageId" to msgRef.id,
                    "conversationId" to convDoc.id,
                    "senderUid" to currentUid,
                    "text" to text.trim(),
                    "time" to timeNow,
                    "isFromMe" to true,
                    "timestamp" to Timestamp.now()
                )
                msgRef.set(msgData).await()

                Log.d("ChatViewModel", "Message sent successfully to conversation: ${convDoc.id}")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message: ${e.message}", e)
            }
        }
    }
}