package com.example.edutitute

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edutitute.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatMessageAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var classId = ""
    private var messageEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        classId = intent.getStringExtra("classId") ?: ""

        setupRecycler()
        loadClassInfo()
        setupListeners()
    }

    private fun setupRecycler() {
        adapter = ChatMessageAdapter(messages, auth.currentUser?.uid ?: "")
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = adapter
    }

    private fun setupListeners() {
        binding.toolbarChat.setNavigationOnClickListener { finish() }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (!TextUtils.isEmpty(text) && messageEnabled) {
                sendMessage(text)
            }
        }
    }

    private fun loadClassInfo() {
        if (classId.isBlank()) return
        db.collection("classes").document(classId).get()
            .addOnSuccessListener { doc ->
                binding.toolbarChat.title = doc.getString("className") ?: "Chat"
                messageEnabled = doc.getBoolean("messageEnabled") ?: true
                binding.layoutSendBar.visibility = if (messageEnabled) View.VISIBLE else View.GONE
                binding.tvDisabled.visibility = if (!messageEnabled) View.VISIBLE else View.GONE
                listenForMessages()
            }
    }

    private fun listenForMessages() {
        db.collection("classes").document(classId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        messages.add(
                            ChatMessage(
                                senderId = doc.getString("senderId") ?: "",
                                senderName = doc.getString("senderName") ?: "",
                                messageText = doc.getString("messageText") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                    binding.recyclerChat.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage(text: String) {
        val user = auth.currentUser ?: return
        val message = hashMapOf(
            "senderId" to user.uid,
            "senderName" to (user.displayName ?: "User"),
            "messageText" to text,
            "timestamp" to System.currentTimeMillis()
        )

        val classRef = db.collection("classes").document(classId)
        classRef.collection("messages").add(message)
            .addOnSuccessListener {
                binding.etMessage.text?.clear()
                classRef.update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to System.currentTimeMillis()
                    )
                )
            }
    }
}
