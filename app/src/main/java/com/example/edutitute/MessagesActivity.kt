package com.example.edutitute

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edutitute.databinding.ActivityMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatGroupAdapter
    private val chatGroups = mutableListOf<ChatGroup>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecycler()
        loadChatGroups()

        binding.toolbarMessages.setNavigationOnClickListener { finish() }
    }

    private fun setupRecycler() {
        adapter = ChatGroupAdapter(chatGroups) { selectedGroup ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("classId", selectedGroup.classId)
            startActivity(intent)
        }
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerMessages.adapter = adapter
    }

    private fun loadChatGroups() {
        val user = auth.currentUser ?: return

        db.collection("users").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) return@addOnSuccessListener

                val userDoc = docs.first()
                val role = userDoc.getString("role") ?: "Student"
                val schoolId = userDoc.getString("schoolId") ?: ""
                val classId = userDoc.getString("classId") ?: ""

                chatGroups.clear()

                when (role) {
                    "Student" -> {
                        // Student → only their class chat
                        if (classId.isNotBlank()) {
                            db.collection("classes").document(classId).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        chatGroups.add(
                                            ChatGroup(
                                                classId = doc.id,
                                                className = doc.getString("className") ?: "Class Chat",
                                                lastMessage = doc.getString("lastMessage") ?: "No messages yet",
                                                lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                                            )
                                        )
                                        updateUI()
                                    }
                                }
                        } else updateUI()
                    }

                    "Teacher" -> {
                        // Teacher → their class + teachers_chat
                        db.collection("classes").whereEqualTo("teacherId", user.uid).get()
                            .addOnSuccessListener { classDocs ->
                                for (doc in classDocs) {
                                    chatGroups.add(
                                        ChatGroup(
                                            classId = doc.id,
                                            className = doc.getString("className") ?: "My Class",
                                            lastMessage = doc.getString("lastMessage") ?: "No messages yet",
                                            lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                                        )
                                    )
                                }

                                // Add teacher staff chat
                                chatGroups.add(
                                    ChatGroup(
                                        classId = "teachers_chat",
                                        className = "Teachers Group",
                                        lastMessage = "Staff-only discussion",
                                        lastMessageTime = System.currentTimeMillis()
                                    )
                                )

                                updateUI()
                            }
                    }

                    "Moderator", "Headmaster" -> {
                        // Can view all class chats
                        db.collection("classes").whereEqualTo("schoolId", schoolId).get()
                            .addOnSuccessListener { classDocs ->
                                for (doc in classDocs) {
                                    chatGroups.add(
                                        ChatGroup(
                                            classId = doc.id,
                                            className = doc.getString("className") ?: "Unnamed Class",
                                            lastMessage = doc.getString("lastMessage") ?: "No messages yet",
                                            lastMessageTime = doc.getLong("lastMessageTime") ?: 0L
                                        )
                                    )
                                }

                                // Add staff chat
                                chatGroups.add(
                                    ChatGroup(
                                        classId = "teachers_chat",
                                        className = "Teachers Group",
                                        lastMessage = "Staff-only discussion",
                                        lastMessageTime = System.currentTimeMillis()
                                    )
                                )

                                updateUI()
                            }
                    }
                }
            }
    }

    private fun updateUI() {
        adapter.notifyDataSetChanged()
        binding.tvEmpty.visibility = if (chatGroups.isEmpty()) View.VISIBLE else View.GONE
    }
}
