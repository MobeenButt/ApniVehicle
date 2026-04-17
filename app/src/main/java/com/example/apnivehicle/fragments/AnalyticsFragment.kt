package com.example.apnivehicle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.adapters.MarketTrendAdapter
import com.example.apnivehicle.databinding.FragmentAnalyticsBinding
import com.example.apnivehicle.utils.AnalyticsManager
import java.text.NumberFormat
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAnalytics()
    }

    private fun loadAnalytics() {
        try {
            val sellerAnalytics = AnalyticsManager.getSellerAnalytics(requireContext())
            
            // Check if user has any listings
            if (sellerAnalytics.totalListings == 0) {
                showEmptyState()
                return
            }
            
            hideEmptyState()
            
            // Overview
            binding.textTotalViews.text = sellerAnalytics.totalViews.toString()
            binding.textTotalListings.text = sellerAnalytics.activeListings.toString()
            binding.textTotalFavorites.text = sellerAnalytics.totalFavorites.toString()
            binding.textTotalContacts.text = sellerAnalytics.totalContacts.toString()
            
            // Performance Score
            val score = sellerAnalytics.performanceScore.toInt()
            binding.textPerformanceScore.text = score.toString()
            binding.progressPerformance.progress = score
            
            binding.textPerformanceDescription.text = when {
                score >= 80 -> "Excellent! Your listings are performing great!"
                score >= 60 -> "Good performance! Keep optimizing your listings."
                score >= 40 -> "Average performance. Consider improving photos and descriptions."
                score >= 20 -> "Below average. Try updating your listings for better visibility."
                else -> "Low performance. Review your pricing and listing quality."
            }
            
            // Top Performers
            binding.textMostViewed.text = sellerAnalytics.mostViewedVehicle?.let {
                "${it.title} (${AnalyticsManager.getVehicleAnalyticsData(requireContext(), it.id).viewCount} views)"
            } ?: "No data yet"
            
            binding.textLeastViewed.text = sellerAnalytics.leastViewedVehicle?.let {
                "${it.title} (${AnalyticsManager.getVehicleAnalyticsData(requireContext(), it.id).viewCount} views)"
            } ?: "No data yet"
            
            binding.textBestCategory.text = sellerAnalytics.bestPerformingCategory
            
            // Market Position
            binding.textAveragePrice.text = priceFormatter.format(sellerAnalytics.averagePrice)
            binding.textMarketPosition.text = sellerAnalytics.marketPosition
            
            binding.textMarketInsight.text = when {
                sellerAnalytics.marketPosition.contains("Below") -> 
                    "Your prices are competitive. You may attract more buyers."
                sellerAnalytics.marketPosition.contains("Above") -> 
                    "Your prices are higher than average. Ensure quality justifies the price."
                else -> 
                    "Your pricing is well-positioned in the market."
            }
            
            // Market Trends
            val trends = AnalyticsManager.getMarketTrends(requireContext())
            if (trends.isNotEmpty()) {
                binding.recyclerMarketTrends.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerMarketTrends.adapter = MarketTrendAdapter(trends)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AnalyticsFragment", "Error loading analytics", e)
            showEmptyState()
        }
    }
    
    private fun showEmptyState() {
        binding.textEmptyAnalytics.visibility = View.VISIBLE
        // Hide all cards
        binding.root.findViewById<View>(com.example.apnivehicle.R.id.text_analytics_title)?.parent?.let {
            if (it is ViewGroup) {
                for (i in 2 until it.childCount) {
                    it.getChildAt(i).visibility = View.GONE
                }
            }
        }
    }
    
    private fun hideEmptyState() {
        binding.textEmptyAnalytics.visibility = View.GONE
        // Show all cards
        binding.root.findViewById<View>(com.example.apnivehicle.R.id.text_analytics_title)?.parent?.let {
            if (it is ViewGroup) {
                for (i in 2 until it.childCount - 1) { // -1 to exclude empty state text
                    it.getChildAt(i).visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAnalytics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
