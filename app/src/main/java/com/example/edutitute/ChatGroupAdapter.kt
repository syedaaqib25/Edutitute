package com.example.edutitute

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edutitute.databinding.ItemChatGroupBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatGroupAdapter(
    private val chatGroups: List<ChatGroup>,
    private val onClick: (ChatGroup) -> Unit
) : RecyclerView.Adapter<ChatGroupAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(private val binding: ItemChatGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: ChatGroup) {
            binding.tvClassName.text = group.className
            binding.tvLastMessage.text = group.lastMessage
            binding.tvTime.text = formatTime(group.lastMessageTime)

            binding.root.setOnClickListener { onClick(group) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatGroups[position])
    }

    override fun getItemCount() = chatGroups.size

    private fun formatTime(timestamp: Long): String {
        return if (timestamp == 0L) "" else {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
