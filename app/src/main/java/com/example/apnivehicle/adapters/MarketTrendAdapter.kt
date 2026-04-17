package com.example.apnivehicle.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.databinding.ItemMarketTrendBinding
import com.example.apnivehicle.models.MarketTrend
import com.example.apnivehicle.models.TrendDirection
import java.text.NumberFormat
import java.util.Locale

class MarketTrendAdapter(
    private val trends: List<MarketTrend>
) : RecyclerView.Adapter<MarketTrendAdapter.TrendViewHolder>() {

    private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))

    inner class TrendViewHolder(private val binding: ItemMarketTrendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trend: MarketTrend) {
            binding.apply {
                textTrendCategory.text = trend.category
                textTrendDetails.text = "Avg: ${priceFormatter.format(trend.averagePrice)} • ${trend.totalListings} listings"

                when (trend.trendDirection) {
                    TrendDirection.UP -> {
                        textTrendIndicator.text = "↑"
                        textTrendIndicator.setTextColor(Color.parseColor("#4CAF50")) // Green
                    }
                    TrendDirection.DOWN -> {
                        textTrendIndicator.text = "↓"
                        textTrendIndicator.setTextColor(Color.parseColor("#F44336")) // Red
                    }
                    TrendDirection.STABLE -> {
                        textTrendIndicator.text = "→"
                        textTrendIndicator.setTextColor(Color.parseColor("#FF9800")) // Orange
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val binding = ItemMarketTrendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        holder.bind(trends[position])
    }

    override fun getItemCount() = trends.size
}
