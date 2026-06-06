package com.example.apnivehicle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ItemVehicleCardBinding
import com.example.apnivehicle.models.Vehicle

class VehicleAdapter(
    private val onItemClick: (Vehicle) -> Unit,
    private val onFavoriteClick: (Vehicle) -> Unit,
    private val onEditClick: (Vehicle) -> Unit = {},
    private val onDeleteClick: (Vehicle) -> Unit = {},
    private val showOwnerActions: Boolean = false
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var vehicles: List<Vehicle> = emptyList()

    // ViewHolder wires click listeners ONCE in init — never inside bind().
    // Setting listeners on every bind() call during scroll is wasteful and can cause
    // stale-closure bugs where the listener captures the wrong vehicle reference.
    inner class VehicleViewHolder(
        val binding: ItemVehicleCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(vehicles[pos])
            }
            binding.iconFavorite.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onFavoriteClick(vehicles[pos])
            }
            binding.buttonEdit.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onEditClick(vehicles[pos])
            }
            binding.buttonDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onDeleteClick(vehicles[pos])
            }
        }

        fun bind(vehicle: Vehicle) {
            binding.textTitle.text = vehicle.title
            binding.textPrice.text = "PKR ${String.format("%,d", vehicle.price)}"
            binding.textCity.text = vehicle.city
            binding.textYear.text = vehicle.year.toString()

            // Always clear any previous tint so the placeholder never bleeds into a real image
            binding.imageVehicle.clearColorFilter()
            binding.imageVehicle.colorFilter = null

            val imageSource: Any? = when {
                // Firebase Storage / remote URL
                !vehicle.imageUri.isNullOrEmpty() && vehicle.imageUri!!.startsWith("http") ->
                    vehicle.imageUri

                // Local file path — wrap in File so Glide can load it reliably
                !vehicle.imageUri.isNullOrEmpty() -> {
                    val f = java.io.File(vehicle.imageUri!!)
                    if (f.exists()) f else null
                }

                // First item in imageList
                vehicle.imageList.isNotEmpty() -> {
                    val uri = vehicle.imageList[0]
                    when {
                        uri.startsWith("http") -> uri
                        else -> {
                            val f = java.io.File(uri)
                            if (f.exists()) f else null
                        }
                    }
                }

                else -> null
            }

            if (imageSource != null) {
                com.bumptech.glide.Glide.with(binding.imageVehicle.context)
                    .load(imageSource)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_car_rental)
                    .error(R.drawable.ic_car_rental)
                    .centerCrop()
                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: com.bumptech.glide.load.engine.GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.imageVehicle.clearColorFilter()
                            return false
                        }
                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.imageVehicle.clearColorFilter()
                            return false
                        }
                    })
                    .into(binding.imageVehicle)
            } else if (vehicle.image != 0) {
                binding.imageVehicle.clearColorFilter()
                binding.imageVehicle.setImageResource(vehicle.image)
            } else {
                binding.imageVehicle.clearColorFilter()
                binding.imageVehicle.setImageResource(R.drawable.ic_car_rental)
            }

            val favoriteTint = if (vehicle.isFavorite) R.color.primary else R.color.text_secondary
            binding.iconFavorite.setColorFilter(
                ContextCompat.getColor(binding.root.context, favoriteTint)
            )

            binding.buttonEdit.visibility = if (showOwnerActions) View.VISIBLE else View.GONE
            binding.buttonDelete.visibility = if (showOwnerActions) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return VehicleViewHolder(ItemVehicleCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    /**
     * Update the list using DiffUtil so only changed items are redrawn.
     * This replaces both submitList() and updateList() — both methods are kept for
     * call-site compatibility but now do the same efficient thing.
     */
    fun submitList(newItems: List<Vehicle>) {
        val diff = DiffUtil.calculateDiff(VehicleDiffCallback(vehicles, newItems))
        vehicles = newItems
        diff.dispatchUpdatesTo(this)
    }

    // Keep this alias so existing call sites don't need to change
    fun updateList(newItems: List<Vehicle>) = submitList(newItems)

    private class VehicleDiffCallback(
        private val old: List<Vehicle>,
        private val new: List<Vehicle>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) = old[oldPos].id == new[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val o = old[oldPos]; val n = new[newPos]
            return o.title == n.title && o.price == n.price && o.isFavorite == n.isFavorite
                    && o.imageUri == n.imageUri
        }
    }
}