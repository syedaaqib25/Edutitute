package com.example.edutitute

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edutitute.databinding.ItemPendingUserCardBinding
import com.google.firebase.firestore.DocumentSnapshot

class ApproveUsersAdapter(
    private val users: List<DocumentSnapshot>,
    private val onApprove: (DocumentSnapshot) -> Unit,
    private val onReject: (DocumentSnapshot) -> Unit
) : RecyclerView.Adapter<ApproveUsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemPendingUserCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(doc: DocumentSnapshot) {
            val name = "${doc.getString("firstName")} ${doc.getString("lastName")}".trim()
            val email = doc.getString("email") ?: "N/A"
            val role = doc.getString("role") ?: "Unknown"

            binding.tvUserName.text = name
            binding.tvUserEmail.text = "Email: $email"
            binding.tvUserRole.text = "Role: $role"

            binding.btnApprove.setOnClickListener { onApprove(doc) }
            binding.btnReject.setOnClickListener { onReject(doc) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemPendingUserCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size
}
