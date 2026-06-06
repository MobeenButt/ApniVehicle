package com.example.apnivehicle.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.databinding.ActivityChatBinding
import com.example.apnivehicle.models.ChatMessage
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.ChatRepository
import com.example.apnivehicle.utils.NetworkMonitor
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAT_ID = "extra_chat_id"
        const val EXTRA_VEHICLE_TITLE = "extra_vehicle_title"
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        const val EXTRA_SELLER_ID = "extra_seller_id"
    }

    private lateinit var binding: ActivityChatBinding
    private var chatId: String = ""
    private var sellerId: String = ""
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: MessageAdapter
    private var messagesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityChatBinding.inflate(layoutInflater)
            setContentView(binding.root)

            chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: ""
            sellerId = intent.getStringExtra(EXTRA_SELLER_ID) ?: ""
            val vehicleTitle = intent.getStringExtra(EXTRA_VEHICLE_TITLE) ?: "Chat"

            // Safety check: if chatId is empty, something went wrong
            if (chatId.isEmpty()) {
                Toast.makeText(this, "Error: Invalid chat session", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            try {
                setSupportActionBar(binding.toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = vehicleTitle
            } catch (e: Exception) {
                android.util.Log.e("ChatActivity", "Toolbar setup failed", e)
            }

            setupRecyclerView()
            setupSendButton()
            observeNetwork()

            if (chatId.isNotEmpty()) {
                startListening()
                markAsRead()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error opening chat", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = AuthRepository.getCurrentUser()?.id ?: ""
        adapter = MessageAdapter(messages, currentUserId)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerMessages.adapter = adapter
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.inputMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            if (!NetworkMonitor.isCurrentlyOnline()) {
                Toast.makeText(this, "You are offline. Cannot send messages.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = AuthRepository.getCurrentUser()?.id ?: return@setOnClickListener
            val message = ChatMessage(senderId = currentUserId, text = text)

            binding.inputMessage.text?.clear()
            binding.btnSend.isEnabled = false

            lifecycleScope.launch {
                val result = ChatRepository.sendMessage(chatId, message, sellerId)
                binding.btnSend.isEnabled = true
                result.onFailure {
                    Toast.makeText(this@ChatActivity, "Failed to send: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startListening() {
        try {
            messagesListener = ChatRepository.listenToMessages(chatId) { updatedMessages ->
                try {
                    // Safety check for null messages
                    if (updatedMessages == null) return@listenToMessages

                    // Fix: instead of clearing and re-adding everything (which causes full redraw/flicker),
                    // only notify about genuinely new items appended at the end.
                    val previousSize = messages.size
                    val newItems = updatedMessages.drop(previousSize)
                    if (newItems.isNotEmpty()) {
                        messages.addAll(newItems)
                        adapter.notifyItemRangeInserted(previousSize, newItems.size)
                    } else if (updatedMessages.size != messages.size) {
                        // Fallback for out-of-order or deleted messages
                        messages.clear()
                        messages.addAll(updatedMessages)
                        adapter.notifyDataSetChanged()
                    }
                    if (messages.isNotEmpty()) {
                        binding.recyclerMessages.scrollToPosition(messages.size - 1)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ChatActivity", "Error updating messages", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatActivity", "Error starting message listener", e)
            Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markAsRead() {
        val userId = AuthRepository.getCurrentUser()?.id ?: return
        lifecycleScope.launch {
            ChatRepository.markAsRead(chatId, userId)
        }
    }

    private fun observeNetwork() {
        NetworkMonitor.isOnline.observe(this) { online ->
            binding.offlineBanner?.visibility = if (online) View.GONE else View.VISIBLE
            binding.btnSend.isEnabled = online
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }

    // ===== Message Adapter =====

    inner class MessageAdapter(
        private val items: List<ChatMessage>,
        private val currentUserId: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_SENT = 1
        private val VIEW_RECEIVED = 2
        private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        override fun getItemViewType(position: Int): Int {
            return if (items[position].senderId == currentUserId) VIEW_SENT else VIEW_RECEIVED
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = android.view.LayoutInflater.from(parent.context)
            return if (viewType == VIEW_SENT) {
                val v = inflater.inflate(com.example.apnivehicle.R.layout.item_message_sent, parent, false)
                SentViewHolder(v)
            } else {
                val v = inflater.inflate(com.example.apnivehicle.R.layout.item_message_received, parent, false)
                ReceivedViewHolder(v)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            try {
                val msg = items.getOrNull(position) ?: return
                val time = if (msg.timestamp > 0) {
                    timeFormat.format(Date(msg.timestamp))
                } else {
                    "N/A"
                }
                when (holder) {
                    is SentViewHolder -> {
                        holder.tvText.text = msg.text ?: ""
                        holder.tvTime.text = time
                    }
                    is ReceivedViewHolder -> {
                        holder.tvText.text = msg.text ?: ""
                        holder.tvTime.text = time
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatActivity", "Error binding message", e)
            }
        }

        override fun getItemCount() = items.size

        inner class SentViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: android.widget.TextView = v.findViewById(com.example.apnivehicle.R.id.tv_message_text)
            val tvTime: android.widget.TextView = v.findViewById(com.example.apnivehicle.R.id.tv_message_time)
        }

        inner class ReceivedViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvText: android.widget.TextView = v.findViewById(com.example.apnivehicle.R.id.tv_message_text)
            val tvTime: android.widget.TextView = v.findViewById(com.example.apnivehicle.R.id.tv_message_time)
        }
    }
}
