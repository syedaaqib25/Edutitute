package com.example.edutitute

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.edutitute.databinding.ItemMessageReceivedBinding
import com.example.edutitute.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter(
    private val messages: List<ChatMessage>,
    private val currentUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int =
        if (messages[position].senderId == currentUid) TYPE_SENT else TYPE_RECEIVED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedViewHolder(binding)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
        if (holder is SentViewHolder) {
            holder.binding.tvMessage.text = msg.messageText
            holder.binding.tvTime.text = time
        } else if (holder is ReceivedViewHolder) {
            holder.binding.tvSender.text = msg.senderName
            holder.binding.tvMessage.text = msg.messageText
            holder.binding.tvTime.text = time
        }
    }

    override fun getItemCount() = messages.size

    inner class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)
}
