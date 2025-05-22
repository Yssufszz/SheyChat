package com.yusuf.sheychat.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yusuf.sheychat.R
import com.yusuf.sheychat.data.model.Message
import com.yusuf.sheychat.databinding.ItemChatMessageBinding
import com.yusuf.sheychat.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

class EnhancedChatAdapter : RecyclerView.Adapter<EnhancedChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()
    private val currentUserId = FirebaseHelper.getCurrentUserId()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun updateMessages(newMessages: List<Message>) {
        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MessageViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(
        private val binding: ItemChatMessageBinding,
        private val viewType: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            setupMessageBubble(message)
            setupContent(message)
            setupLayout()
        }

        private fun setupMessageBubble(message: Message) {
            val isCurrentUser = viewType == VIEW_TYPE_SENT

            if (isCurrentUser) {
                binding.messageContainer.setBackgroundResource(R.drawable.message_bubble_sent)
                binding.tvMessage.setTextColor(itemView.context.getColor(R.color.on_primary))
                binding.tvSenderName.visibility = View.GONE
            } else {
                binding.messageContainer.setBackgroundResource(R.drawable.message_bubble_received)
                binding.tvMessage.setTextColor(itemView.context.getColor(R.color.on_surface))
                binding.tvSenderName.visibility = View.VISIBLE
                binding.tvSenderName.text = message.senderName
            }
        }

        private fun setupContent(message: Message) {
            binding.tvMessage.text = message.message
            binding.tvTimestamp.text = formatTimestamp(message.timestamp)
        }

        private fun setupLayout() {
            val isCurrentUser = viewType == VIEW_TYPE_SENT
            val layoutParams = binding.messageContainer.layoutParams as ViewGroup.MarginLayoutParams

            if (isCurrentUser) {
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_RTL
                layoutParams.marginStart = 64
                layoutParams.marginEnd = 16
            } else {
                binding.root.layoutDirection = View.LAYOUT_DIRECTION_LTR
                layoutParams.marginStart = 16
                layoutParams.marginEnd = 64
            }

            binding.messageContainer.layoutParams = layoutParams
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "now" // less than 1 minute
                diff < 3600000 -> "${diff / 60000}m ago" // less than 1 hour
                diff < 86400000 -> { // less than 1 day
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class MessageDiffCallback(
        private val oldList: List<Message>,
        private val newList: List<Message>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].messageId == newList[newItemPosition].messageId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}