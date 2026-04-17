package com.example.apnivehicle.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object JsonFileHandler {
    
    // Make gson public so inline function can access it
    val gson = Gson()
    
    // Save data to JSON file
    fun <T> saveToFile(context: Context, filename: String, data: T): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            val writer = FileWriter(file)
            gson.toJson(data, writer)
            writer.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // Load data from JSON file
    inline fun <reified T> loadFromFile(context: Context, filename: String): T? {
        return try {
            val file = File(context.filesDir, filename)
            if (!file.exists()) return null
            
            val reader = FileReader(file)
            val type = object : TypeToken<T>() {}.type
            val data: T = gson.fromJson(reader, type)
            reader.close()
            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Check if file exists
    fun fileExists(context: Context, filename: String): Boolean {
        val file = File(context.filesDir, filename)
        return file.exists()
    }
    
    // Delete file
    fun deleteFile(context: Context, filename: String): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
