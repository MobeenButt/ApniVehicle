package com.example.apnivehicle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.models.Review
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.ReviewRepository
import com.example.apnivehicle.utils.FormatUtils
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ReviewsFragment : Fragment() {

    companion object {
        const val ARG_TARGET_ID = "target_id"
        const val ARG_TARGET_TYPE = "target_type"

        fun newInstance(targetId: String, targetType: String = "seller"): ReviewsFragment {
            return ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TARGET_ID, targetId)
                    putString(ARG_TARGET_TYPE, targetType)
                }
            }
        }
    }

    private var recyclerView: RecyclerView? = null
    private var emptyText: TextView? = null
    private var tvAverageRating: TextView? = null
    private var ratingBar: RatingBar? = null
    private var reviewsListener: ListenerRegistration? = null
    private val reviews = mutableListOf<Review>()
    private lateinit var adapter: ReviewAdapter
    private var targetId: String = ""
    private var targetType: String = "seller"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        recyclerView = view.findViewById(R.id.recycler_reviews)
        emptyText = view.findViewById(R.id.text_empty_reviews)
        tvAverageRating = view.findViewById(R.id.tv_average_rating)
        ratingBar = view.findViewById(R.id.rating_bar_average)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        targetId = arguments?.getString(ARG_TARGET_ID) ?: ""
        targetType = arguments?.getString(ARG_TARGET_TYPE) ?: "seller"

        val currentUserId = AuthRepository.getCurrentUser()?.id ?: ""
        val isSeller = currentUserId == targetId

        adapter = ReviewAdapter(reviews, isSeller) { review, reply ->
            lifecycleScope.launch {
                val result = ReviewRepository.addSellerReply(review.reviewId, reply)
                result.onSuccess { Toast.makeText(requireContext(), "Reply posted", Toast.LENGTH_SHORT).show() }
                result.onFailure { Toast.makeText(requireContext(), "Failed: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
        }
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        if (targetId.isNotEmpty()) {
            reviewsListener = ReviewRepository.listenToReviews(targetId) { updatedReviews ->
                // Use DiffUtil instead of notifyDataSetChanged() to prevent full relayout on updates.
                val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = reviews.size
                    override fun getNewListSize() = updatedReviews.size
                    override fun areItemsTheSame(o: Int, n: Int) =
                        reviews[o].reviewId == updatedReviews[n].reviewId
                    override fun areContentsTheSame(o: Int, n: Int) =
                        reviews[o] == updatedReviews[n]
                })
                reviews.clear()
                reviews.addAll(updatedReviews)
                diff.dispatchUpdatesTo(adapter)
                emptyText?.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
                updateAverageRating()
            }
        }
    }

    private fun updateAverageRating() {
        if (reviews.isEmpty()) return
        val avg = reviews.map { it.rating }.average().toFloat()
        tvAverageRating?.text = String.format("%.1f / 5.0 (%d reviews)", avg, reviews.size)
        ratingBar?.rating = avg
    }

    override fun onDestroyView() {
        super.onDestroyView()
        reviewsListener?.remove()
        recyclerView = null
        emptyText = null
    }

    inner class ReviewAdapter(
        private val items: List<Review>,
        private val isSeller: Boolean,
        private val onReply: (Review, String) -> Unit
    ) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvReviewer: TextView = v.findViewById(R.id.tv_reviewer_name)
            val ratingBar: RatingBar = v.findViewById(R.id.rating_bar_review)
            val tvText: TextView = v.findViewById(R.id.tv_review_text)
            val tvDate: TextView = v.findViewById(R.id.tv_review_date)
            val tvReply: TextView = v.findViewById(R.id.tv_seller_reply)
            val btnReply: com.google.android.material.button.MaterialButton = v.findViewById(R.id.btn_reply)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = items[position]
            holder.tvReviewer.text = review.reviewerName.ifEmpty { "Anonymous" }
            holder.ratingBar.rating = review.rating
            holder.tvText.text = review.text
            holder.tvDate.text = FormatUtils.getRelativeTime(review.createdAt)

            if (review.sellerReply.isNotEmpty()) {
                holder.tvReply.visibility = View.VISIBLE
                holder.tvReply.text = "Seller: ${review.sellerReply}"
            } else {
                holder.tvReply.visibility = View.GONE
            }

            if (isSeller && review.sellerReply.isEmpty()) {
                holder.btnReply.visibility = View.VISIBLE
                // Set listener only when needed — use tag to avoid re-attaching the same listener
                holder.btnReply.setOnClickListener { showReplyDialog(review) }
            } else {
                holder.btnReply.visibility = View.GONE
                holder.btnReply.setOnClickListener(null)
            }
        }

        override fun getItemCount() = items.size

        private fun showReplyDialog(review: Review) {
            val input = android.widget.EditText(requireContext()).apply {
                hint = "Write your reply..."
                minLines = 2
            }
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reply to Review")
                .setView(input)
                .setPositiveButton("Post Reply") { _, _ ->
                    val reply = input.text.toString().trim()
                    if (reply.isNotEmpty()) onReply(review, reply)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
