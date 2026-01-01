package com.example.edutitute

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edutitute.databinding.ActivityApproveUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ApproveUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApproveUsersBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ApproveUsersAdapter
    private val pendingUsers = mutableListOf<DocumentSnapshot>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApproveUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecycler()
        loadPendingUsers()
    }

    /** Setup RecyclerView */
    private fun setupRecycler() {
        binding.recyclerPendingUsers.layoutManager = LinearLayoutManager(this)
        adapter = ApproveUsersAdapter(pendingUsers, ::approveUser, ::rejectUser)
        binding.recyclerPendingUsers.adapter = adapter
    }

    /** Load users that are not approved yet (same school only) */
    private fun loadPendingUsers() {
        val currentUser = auth.currentUser ?: return
        binding.recyclerPendingUsers.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        db.collection("users").whereEqualTo("uid", currentUser.uid).get()
            .addOnSuccessListener { userDocs ->
                if (userDocs.isEmpty) {
                    Toast.makeText(this, "⚠️ Current user not found in Firestore.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val approverSchoolId = userDocs.first().getString("schoolId") ?: ""
                if (approverSchoolId.isBlank()) {
                    Toast.makeText(this, "⚠️ No school ID found for approver.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereEqualTo("approved", false)
                    .whereEqualTo("schoolId", approverSchoolId)
                    .get()
                    .addOnSuccessListener { docs ->
                        pendingUsers.clear()
                        pendingUsers.addAll(docs.documents)
                        adapter.notifyDataSetChanged()

                        if (docs.isEmpty) {
                            binding.tvEmpty.visibility = View.VISIBLE
                        } else {
                            binding.recyclerPendingUsers.visibility = View.VISIBLE
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "⚠️ Error loading users: ${it.message}", Toast.LENGTH_SHORT).show()
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "⚠️ Failed to fetch approver details.", Toast.LENGTH_SHORT).show()
            }
    }

    /** Approve user */
    private fun approveUser(userDoc: DocumentSnapshot) {
        userDoc.reference.update("approved", true)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ ${userDoc.getString("firstName")} approved!", Toast.LENGTH_SHORT).show()
                loadPendingUsers()
            }
            .addOnFailureListener {
                Toast.makeText(this, "⚠️ Failed to approve user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /** Reject user */
    private fun rejectUser(userDoc: DocumentSnapshot) {
        userDoc.reference.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "❌ ${userDoc.getString("firstName")} rejected.", Toast.LENGTH_SHORT).show()
                loadPendingUsers()
            }
            .addOnFailureListener {
                Toast.makeText(this, "⚠️ Failed to reject user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
