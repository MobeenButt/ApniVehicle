package com.example.apnivehicle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.utils.AppNotificationManager.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private val onDismiss: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NotificationItem>() {
            override fun areItemsTheSame(a: NotificationItem, b: NotificationItem) = a.id == b.id
            override fun areContentsTheSame(a: NotificationItem, b: NotificationItem) = a == b
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_notif_title)
        val tvMessage: TextView = itemView.findViewById(R.id.tv_notif_message)
        val tvTime: TextView = itemView.findViewById(R.id.tv_notif_time)
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_notif_icon)
        val btnDismiss: ImageButton = itemView.findViewById(R.id.btn_dismiss_notif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text = relativeTime(item.timestamp)

        // Pick icon tint based on title keyword
        val iconRes = when {
            item.title.contains("Added", ignoreCase = true) ||
            item.title.contains("Post", ignoreCase = true)  -> R.drawable.ic_add
            item.title.contains("Deleted", ignoreCase = true) ||
            item.title.contains("Removed", ignoreCase = true) -> R.drawable.ic_delete
            item.title.contains("Favorite", ignoreCase = true) -> R.drawable.ic_favorite
            item.title.contains("Battery", ignoreCase = true) -> R.drawable.ic_notifications
            else -> R.drawable.ic_notifications
        }
        holder.ivIcon.setImageResource(iconRes)

        holder.btnDismiss.setOnClickListener { onDismiss(item) }
    }

    private fun relativeTime(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < TimeUnit.MINUTES.toMillis(1)  -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1)    -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1)     -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(7)     -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
            else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(ts))
        }
    }
}
