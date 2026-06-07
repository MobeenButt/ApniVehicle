package com.example.apnivehicle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ItemImageBinding
import java.io.File

class ImagePagerAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUri: String) {
            // Clear any previous tint so placeholder never bleeds onto real image
            binding.imageView.clearColorFilter()
            binding.imageView.colorFilter = null

            val source: Any = when {
                // Remote Firebase Storage URL
                imageUri.startsWith("http") -> imageUri
                // Local absolute file path
                imageUri.startsWith("/") -> {
                    val f = File(imageUri)
                    if (f.exists()) f else R.drawable.ic_car_rental
                }
                // content:// or file:// URI string
                else -> try {
                    android.net.Uri.parse(imageUri)
                } catch (_: Exception) {
                    R.drawable.ic_car_rental
                }
            }

            Glide.with(binding.root.context)
                .load(source)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_car_rental)
                .error(R.drawable.ic_car_rental)
                .centerCrop()
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(
                        e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imageView.clearColorFilter(); return false
                    }
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable, model: Any,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean
                    ): Boolean {
                        binding.imageView.clearColorFilter(); return false
                    }
                })
                .into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}

