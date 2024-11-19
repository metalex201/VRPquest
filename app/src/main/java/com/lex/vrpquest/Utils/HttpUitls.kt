package com.lex.vrpquest.Utils

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.lex.vrpquest.Managers.disableSSLCertificateChecking
import com.lex.vrpquest.Managers.enableSSLCertificateChecking
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.json.JSONObject
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


fun downloadFile(url: String, destinationPath: String, customHeader: String, progress: (Float) -> Unit, IsStopping: MutableState<Boolean> = mutableStateOf(false)): Boolean {
    println("$url START")
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()
    println("REQUEST $url")
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return false
    }
    println("BODY $url ")

    val file = File(destinationPath)
    if (file.parentFile!!.exists()) {
        file.parentFile!!.mkdirs()
    }

    val sourceBytes = response.body!!.source()
    val sink = file.sink().buffer()
    println("DOWNLOAD $url ")
    val contentLength = response.body!!.contentLength()
    var totalRead: Long = 0
    var lastRead: Long
    var count: Long = 0
    while (sourceBytes
            .read(sink.buffer, 4L * 1024)
            .also { lastRead = it } != -1L && !IsStopping.value
    ) {

        totalRead += lastRead
        sink.emitCompleteSegments()

        count++
        //only report progress every 100th write, to be more efficient
        if (count > 100) {
            progress((((totalRead) * 100L) / contentLength) / 100F)
            count = 0
        }
    }

    var totalBytesRead = 0L

    while (true) {
        if(IsStopping.value) { break }

        val readCount = response.body!!.source().read(sink.buffer,4L * 1024)
        if (readCount == -1L) break
        totalBytesRead += readCount
    }

    progress(1.0F)
    sink.close()
    response.body!!.close()

    println("Download finished: $destinationPath")
    return true
}


fun getUrlFileList(url: String, customHeader: String): String {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return ""
    }

    val result = response.body!!.string()
    response.body!!.close()
    println(result)
    return result
}

fun getUrlFileSize(url: String, customHeader: String): Long {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return 0
    }

    val result = response.body!!.contentLength()
    response.body!!.close()
    return result
}

fun getUrlFileSizeDir(url: String, customHeader: String): List<String>  {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return listOf()
    }

    val result = response.body!!.string()
    response.body!!.close()

    println(result)
    val regex = "<a href=\"([^\"]+)\">[^<]+</a>".toRegex()
    val matches = regex.findAll(result)

    return matches.drop(1).map { it.groupValues[1] }.toList()
}


fun postMetrics(packageName: String, versionCode: String): String {
    val apiUrl = "https://api.vrpirates.wiki/metrics/add"
    val token = "dnJwcXVlc3Q6ZmZtQmg4cEhlb0VLajU1NTgxcmNWSXZv"

    disableSSLCertificateChecking()
    val url = URL(apiUrl)
    val connection = url.openConnection() as HttpURLConnection
    try {
        // Setup the connection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true

        // Set headers
        connection.setRequestProperty("Authorization", token)
        connection.setRequestProperty("Origin", "vrpquest")
        connection.setRequestProperty("Content-Type", "application/json")

        // Create JSON body
        val requestBody = JSONObject().apply {
            put("packagename", packageName)
            put("versioncode", versionCode)
        }

        // Write the body
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        // Read the response
        return if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorResponse = connection.errorStream.bufferedReader().use { it.readText() }
            throw Exception("HTTP ${connection.responseCode}: $errorResponse")
        }
    } finally {
        connection.disconnect()
        enableSSLCertificateChecking()
    }
}