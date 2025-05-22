package com.yusuf.sheychat.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yusuf.sheychat.data.model.Message
import com.yusuf.sheychat.databinding.ItemChatMessageBinding
import com.yusuf.sheychat.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()
    private val currentUserId = FirebaseHelper.getCurrentUserId()

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val isCurrentUser = message.senderId == currentUserId

            binding.tvMessage.text = message.message
            binding.tvSenderName.text = if (isCurrentUser) "You" else message.senderName
            binding.tvTimestamp.text = formatTimestamp(message.timestamp)

            // Adjust layout based on sender
            if (isCurrentUser) {
                binding.root.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
            } else {
                binding.root.layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}