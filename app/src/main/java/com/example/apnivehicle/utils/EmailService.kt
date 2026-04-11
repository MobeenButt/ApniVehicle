package com.example.apnivehicle.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailService {
    
    private const val TAG = "EmailService"
    
    /**
     * Send verification email using device's email client
     */
    fun sendVerificationEmailViaClient(
        context: Context,
        toEmail: String,
        userName: String,
        verificationCode: String
    ) {
        val subject = "Verify Your ApniVehicle Account"
        val body = buildVerificationEmailBody(userName, verificationCode)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Send verification email"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open email client", e)
        }
    }
    
    /**
     * Send CNIC verification email
     */
    fun sendCnicVerificationEmail(
        context: Context,
        toEmail: String,
        userName: String,
        cnicNumber: String
    ) {
        val subject = "CNIC Verification Request - ApniVehicle"
        val body = buildCnicVerificationEmailBody(userName, cnicNumber)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Send CNIC verification"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open email client", e)
        }
    }
    
    /**
     * Send phone verification email
     */
    fun sendPhoneVerificationEmail(
        context: Context,
        toEmail: String,
        userName: String,
        phoneNumber: String,
        verificationCode: String
    ) {
        val subject = "Phone Number Verification - ApniVehicle"
        val body = buildPhoneVerificationEmailBody(userName, phoneNumber, verificationCode)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Send phone verification"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open email client", e)
        }
    }
    
    /**
     * Send seller verification complete email
     */
    fun sendVerificationCompleteEmail(
        context: Context,
        toEmail: String,
        userName: String
    ) {
        val subject = "Congratulations! You're Now a Verified Seller"
        val body = buildVerificationCompleteEmailBody(userName)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Verification complete"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open email client", e)
        }
    }
    
    private fun buildVerificationEmailBody(userName: String, code: String): String {
        return """
            Dear $userName,
            
            Welcome to ApniVehicle - Pakistan's #1 Vehicle Marketplace!
            
            To complete your email verification, please use the following code:
            
            Verification Code: $code
            
            This code will expire in 15 minutes.
            
            Why verify your account?
            ✓ Get a verified badge on your profile
            ✓ Build trust with buyers
            ✓ Increase your listing visibility
            ✓ Access premium features
            
            If you didn't request this verification, please ignore this email.
            
            Best regards,
            ApniVehicle Team
            
            ---
            Need help? Contact us at support@apnivehicle.com
        """.trimIndent()
    }
    
    private fun buildCnicVerificationEmailBody(userName: String, cnicNumber: String): String {
        return """
            Dear $userName,
            
            Thank you for submitting your CNIC for verification!
            
            CNIC Number: $cnicNumber
            
            Our verification team will review your documents within 24-48 hours.
            
            What happens next?
            1. Our team verifies your CNIC details
            2. We check the uploaded images for authenticity
            3. You'll receive a confirmation email once approved
            4. Your profile will display a "Verified Seller" badge
            
            Benefits of CNIC Verification:
            ✓ Verified Seller Badge
            ✓ Higher buyer trust
            ✓ Priority listing placement
            ✓ Access to premium features
            ✓ Faster response from buyers
            
            Status: Under Review
            Expected completion: 24-48 hours
            
            You can check your verification status in the app under Profile > Verification.
            
            Best regards,
            ApniVehicle Verification Team
            
            ---
            Questions? Contact us at verification@apnivehicle.com
        """.trimIndent()
    }
    
    private fun buildPhoneVerificationEmailBody(
        userName: String,
        phoneNumber: String,
        code: String
    ): String {
        return """
            Dear $userName,
            
            You've requested to verify your phone number on ApniVehicle.
            
            Phone Number: $phoneNumber
            Verification Code: $code
            
            Please enter this code in the app to verify your phone number.
            This code will expire in 15 minutes.
            
            Why verify your phone?
            ✓ Buyers can contact you directly
            ✓ Receive SMS notifications for inquiries
            ✓ Build trust with potential buyers
            ✓ Faster communication
            
            If you didn't request this, please secure your account immediately.
            
            Best regards,
            ApniVehicle Team
            
            ---
            Need help? Contact us at support@apnivehicle.com
        """.trimIndent()
    }
    
    private fun buildVerificationCompleteEmailBody(userName: String): String {
        return """
            Dear $userName,
            
            🎉 Congratulations! Your seller verification is complete!
            
            You are now a VERIFIED SELLER on ApniVehicle!
            
            Your Profile Benefits:
            ✓ Verified Seller Badge displayed on all your listings
            ✓ Higher visibility in search results
            ✓ Increased buyer trust and confidence
            ✓ Priority customer support
            ✓ Access to seller analytics
            ✓ Featured listing opportunities
            
            What's Next?
            • Start posting your vehicle listings
            • Respond quickly to buyer inquiries (aim for <2 hours)
            • Maintain accurate vehicle information
            • Upload high-quality photos
            • Build your seller rating
            
            Tips for Success:
            1. Be honest and transparent in your listings
            2. Respond to inquiries within 2 hours
            3. Keep your contact information updated
            4. Upload clear photos from multiple angles
            5. Price competitively based on market rates
            
            Your verified status helps buyers trust you more, leading to faster sales!
            
            Happy Selling!
            ApniVehicle Team
            
            ---
            Track your performance: Profile > Seller Dashboard
            Need help? Contact us at support@apnivehicle.com
        """.trimIndent()
    }
    
    /**
     * Generate a 6-digit verification code
     */
    fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }
    
    /**
     * Validate verification code format
     */
    fun isValidVerificationCode(code: String): Boolean {
        return code.matches(Regex("^\\d{6}$"))
    }
}
