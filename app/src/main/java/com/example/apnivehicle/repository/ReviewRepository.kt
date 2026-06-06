package com.example.apnivehicle.repository

import android.util.Log
import com.example.apnivehicle.models.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object ReviewRepository {

    private const val TAG = "ReviewRepository"
    private const val COLLECTION_REVIEWS = "reviews"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun addReview(review: Review): Result<Review> {
        return try {
            db.collection(COLLECTION_REVIEWS).document(review.reviewId).set(review).await()
            // Update seller's average rating
            updateSellerRating(review.targetId)
            Result.success(review)
        } catch (e: Exception) {
            Log.e(TAG, "addReview failed", e)
            Result.failure(e)
        }
    }

    suspend fun getReviewsForTarget(targetId: String): List<Review> {
        return try {
            val snapshot = db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("targetId", targetId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "getReviewsForTarget failed", e)
            emptyList()
        }
    }

    fun listenToReviews(targetId: String, onReviews: (List<Review>) -> Unit): ListenerRegistration {
        return db.collection(COLLECTION_REVIEWS)
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { Log.w(TAG, "listenToReviews error", error); return@addSnapshotListener }
                val reviews = snapshot?.documents?.mapNotNull { it.toObject(Review::class.java) } ?: emptyList()
                onReviews(reviews)
            }
    }

    suspend fun addSellerReply(reviewId: String, reply: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_REVIEWS).document(reviewId).update(
                mapOf("sellerReply" to reply, "sellerReplyAt" to System.currentTimeMillis())
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "addSellerReply failed", e)
            Result.failure(e)
        }
    }

    private suspend fun updateSellerRating(sellerId: String) {
        try {
            val reviews = getReviewsForTarget(sellerId)
            if (reviews.isEmpty()) return
            val avgRating = reviews.map { it.rating }.average().toFloat()
            db.collection("users").document(sellerId).update(
                mapOf("rating" to avgRating, "reviewCount" to reviews.size)
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "updateSellerRating failed", e)
        }
    }

    suspend fun hasUserReviewedSeller(reviewerId: String, targetId: String): Boolean {
        return try {
            val snapshot = db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("reviewerId", reviewerId)
                .whereEqualTo("targetId", targetId)
                .get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
