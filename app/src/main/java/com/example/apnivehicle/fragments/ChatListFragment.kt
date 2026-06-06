package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.activities.ChatActivity
import com.example.apnivehicle.models.Chat
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.ChatRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.FormatUtils
import com.google.firebase.firestore.ListenerRegistration

class ChatListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var emptyText: TextView? = null
    private var chatsListener: ListenerRegistration? = null

    // Pre-processed display items — resolved once when data arrives, NOT inside onBindViewHolder.
    private val displayItems = mutableListOf<ChatDisplayItem>()
    private lateinit var adapter: ChatListAdapter

    /**
     * All data needed to render one chat row, resolved upfront so onBindViewHolder
     * never touches repositories or does any string computation during scrolling.
     */
    data class ChatDisplayItem(
        val chat: Chat,
        val safeChatId: String,
        val sellerId: String,
        val displayTitle: String,      // e.g. "Ali Hassan"
        val displayMeta: String,       // e.g. "Honda Civic • 2021 • PKR 42,00,000"
        val lastMessage: String,
        val timeLabel: String,
        val unreadCount: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)
        recyclerView = view.findViewById(R.id.recycler_chats)
        emptyText = view.findViewById(R.id.text_empty_chats)
        emptyText?.text = "No inbox conversations yet.\nStart chatting with a seller!"
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            adapter = ChatListAdapter(displayItems) { item ->
                try {
                    if (item.safeChatId.isBlank()) {
                        android.util.Log.e("ChatListFragment", "safeChatId is blank, cannot open chat")
                        return@ChatListAdapter
                    }
                    val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                        putExtra(ChatActivity.EXTRA_CHAT_ID, item.safeChatId)
                        putExtra(ChatActivity.EXTRA_VEHICLE_TITLE, item.chat.vehicleTitle)
                        putExtra(ChatActivity.EXTRA_VEHICLE_ID, item.chat.vehicleId)
                        putExtra(ChatActivity.EXTRA_SELLER_ID, item.sellerId)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("ChatListFragment", "Error opening chat", e)
                }
            }
            recyclerView?.layoutManager = LinearLayoutManager(requireContext())
            recyclerView?.adapter = adapter

            val currentUser = AuthRepository.getCurrentUser()
            if (currentUser == null) {
                emptyText?.visibility = View.VISIBLE
                emptyText?.text = "Please login to view chats"
                return
            }

            val userId = currentUser.id
            if (userId.isBlank()) {
                emptyText?.visibility = View.VISIBLE
                emptyText?.text = "Error: Invalid user ID"
                return
            }

            chatsListener = ChatRepository.listenToUserChats(userId) { updatedChats ->
                try {
                    // Resolve all display data HERE (on the callback thread / main thread),
                    // not inside onBindViewHolder. This keeps the RecyclerView draw pass fast.
                    val currentUserId = AuthRepository.getCurrentUser()?.id ?: ""
                    val newItems = updatedChats.map { chat -> buildDisplayItem(chat, currentUserId) }

                    val diff = DiffUtil.calculateDiff(ChatDiffCallback(displayItems, newItems))
                    displayItems.clear()
                    displayItems.addAll(newItems)
                    diff.dispatchUpdatesTo(adapter)

                    emptyText?.visibility = if (displayItems.isEmpty()) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    android.util.Log.e("ChatListFragment", "Error updating chats", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatListFragment", "Error in onViewCreated", e)
            emptyText?.visibility = View.VISIBLE
            emptyText?.text = "Error loading chats"
        }
    }

    /** Build a self-contained display item from raw Chat data. Called once per chat update. */
    private fun buildDisplayItem(chat: Chat, currentUserId: String): ChatDisplayItem {
        val isCurrentUserSeller = currentUserId.isNotBlank() && currentUserId == chat.sellerId

        val sellerId = when {
            chat.sellerId.isNotBlank() && chat.sellerId != currentUserId -> chat.sellerId
            chat.buyerId.isNotBlank() && chat.buyerId != currentUserId -> chat.buyerId
            else -> chat.participants.firstOrNull { it != currentUserId } ?: ""
        }

        val safeChatId = chat.chatId.ifBlank {
            if (currentUserId.isNotBlank() && sellerId.isNotBlank() && chat.vehicleId.isNotBlank())
                ChatRepository.buildChatId(currentUserId, sellerId, chat.vehicleId)
            else ""
        }

        // Resolve names from repositories — done ONCE here, not on every scroll frame.
        val otherPartyName = when {
            isCurrentUserSeller -> chat.buyerName.ifBlank {
                AuthRepository.getUserById(chat.buyerId)?.username ?: "Buyer"
            }
            chat.sellerName.isNotBlank() -> chat.sellerName
            chat.buyerName.isNotBlank() -> chat.buyerName
            else -> AuthRepository.getUserById(
                if (chat.sellerId.isNotBlank()) chat.sellerId else chat.buyerId
            )?.username ?: "Conversation"
        }

        val vehicle = VehicleRepository.getVehicleById(chat.vehicleId)
        val vehicleTitle = vehicle?.title?.takeIf { it.isNotBlank() } ?: chat.vehicleTitle.ifBlank { "Vehicle" }
        val vehicleBrand = vehicle?.brand?.takeIf { it.isNotBlank() } ?: chat.vehicleBrand
        val vehicleModel = vehicle?.model?.takeIf { it.isNotBlank() } ?: chat.vehicleModel
        val vehicleYear  = if (vehicle != null && vehicle.year > 0) vehicle.year else if (chat.vehicleYear > 0) chat.vehicleYear else 0
        val vehiclePrice = if (vehicle != null && vehicle.price > 0L) vehicle.price else if (chat.vehiclePrice > 0L) chat.vehiclePrice else 0L

        val summaryParts = buildList {
            if (vehicleBrand.isNotBlank()) add(vehicleBrand)
            if (vehicleModel.isNotBlank()) add(vehicleModel)
            if (vehicleYear > 0) add(vehicleYear.toString())
            if (vehiclePrice > 0L) add("PKR ${String.format("%,d", vehiclePrice)}")
        }
        val meta = buildString {
            append(vehicleTitle)
            if (summaryParts.isNotEmpty()) { append(" • "); append(summaryParts.joinToString(" • ")) }
        }

        val timeLabel = if (chat.lastMessageTimestamp > 0)
            try { FormatUtils.getRelativeTime(chat.lastMessageTimestamp) } catch (_: Exception) { "" }
        else ""

        val unread = chat.unreadCount[currentUserId] ?: 0

        return ChatDisplayItem(
            chat = chat,
            safeChatId = safeChatId,
            sellerId = sellerId,
            displayTitle = otherPartyName,
            displayMeta = meta,
            lastMessage = if (chat.lastMessage.isNotBlank()) chat.lastMessage else "No messages yet",
            timeLabel = timeLabel,
            unreadCount = unread
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatsListener?.remove()
        recyclerView = null
        emptyText = null
    }

    // ===== DiffUtil =====

    private class ChatDiffCallback(
        private val old: List<ChatDisplayItem>,
        private val new: List<ChatDisplayItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(o: Int, n: Int) = old[o].chat.chatId == new[n].chat.chatId
        override fun areContentsTheSame(o: Int, n: Int): Boolean {
            val a = old[o]; val b = new[n]
            return a.lastMessage == b.lastMessage && a.unreadCount == b.unreadCount && a.timeLabel == b.timeLabel
        }
    }

    // ===== Inner Adapter =====

    inner class ChatListAdapter(
        private val items: List<ChatDisplayItem>,
        private val onClick: (ChatDisplayItem) -> Unit
    ) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

        inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById(R.id.tv_chat_title)
            val tvMeta: TextView = itemView.findViewById(R.id.tv_chat_meta)
            val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
            val tvTime: TextView = itemView.findViewById(R.id.tv_chat_time)
            val tvUnread: TextView = itemView.findViewById(R.id.tv_unread_badge)

            // Click listener set ONCE in init — not on every bind call.
            init {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) onClick(items[pos])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(v)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            // Pure view population — zero repository calls, zero string formatting.
            val item = items.getOrNull(position) ?: return
            holder.tvTitle.text = item.displayTitle
            holder.tvMeta.text = item.displayMeta
            holder.tvLastMessage.text = item.lastMessage
            holder.tvTime.text = item.timeLabel
            if (item.unreadCount > 0) {
                holder.tvUnread.visibility = View.VISIBLE
                holder.tvUnread.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
            } else {
                holder.tvUnread.visibility = View.GONE
            }
        }

        override fun getItemCount() = items.size
    }
}
