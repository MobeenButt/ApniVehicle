package com.example.apnivehicle.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apnivehicle.R

/**
 * Shows a horizontal strip of selected image thumbnails in AddVehicleFragment.
 * First item gets a "Cover" badge. Each item has an X button to remove it.
 */
class ImageThumbnailAdapter(
    private val uris: MutableList<Uri>,
    private val onRemove: (Int) -> Unit,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageThumbnailAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumb: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btn_remove_image)
        val tvCover: TextView = itemView.findViewById(R.id.tv_cover_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_thumbnail, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val uri = uris[position]

        holder.ivThumb.clearColorFilter()
        Glide.with(holder.ivThumb.context)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_car_rental)
            .into(holder.ivThumb)

        // Show "Cover" badge only on first image
        holder.tvCover.visibility = if (position == 0) View.VISIBLE else View.GONE

        holder.btnRemove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onRemove(pos)
        }

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onClick(pos)
        }
    }

    override fun getItemCount(): Int = uris.size
}
