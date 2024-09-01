package com.lex.vrpquest

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IArchiveOpenCallback
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun MetadataInitialize(context:Context, text: (String) -> Unit, progress: (Float) -> Unit) {
    GlobalScope.launch {
        Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
        val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
        if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
        if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
        Log.i(context.applicationInfo.name, "Downloading meta.7z")
        val testjson = JSONObject(URL("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json").readText())
        val baseUri = testjson.getString("baseUri")
        val password = String(Base64.decode(testjson.getString("password")))

        var startTime = System.currentTimeMillis()

        text(downloadFile("$baseUri" + "meta.7z", "$externalFilesDir/meta.7z","rclone/v69", { progress(it) }).toString())

        var endTime = System.currentTimeMillis()
        var elapsedTime = endTime - startTime
        Log.i(context.applicationInfo.name,"Elapsed time: $elapsedTime milliseconds")
        Log.i(context.applicationInfo.name,"password $password")
        //unzip(File("$externalFilesDir/meta.7z"), password)
        ExtractExample.Extract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", password)
        //rcloneDownload("$baseUri/meta.7z", "$externalFilesDir/meta.7z")
        //SevenUnzip("$externalFilesDir/meta.7z", "$externalFilesDir/meta", password)
    }
}

fun downloadFile(url: String, destinationPath: String, customHeader: String, progress: (Float) -> Unit): Boolean {
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

    if (!response.isSuccessful) {
        Log.e("com.lex.vrpquest", "Download failed: ${response.code}")
        return false
    }


    val file = File(destinationPath)
    if (file.parentFile!!.exists()) {
        file.parentFile!!.mkdirs()
    }

    val sourceBytes = response.body!!.source()
    val sink = file.sink().buffer()

    val contentLength = response.body!!.contentLength()
    var totalRead: Long = 0
    var lastRead: Long
    var count: Long = 0
    while (sourceBytes
            .read(sink.buffer, 8L * 1024)
            .also { lastRead = it } != -1L
    ) {
        totalRead += lastRead
        sink.emitCompleteSegments()

        count++
        if (count % 100 == 0L) {
            progress((((totalRead) * 100L) / contentLength) / 100F)
        }
        //progress((((totalRead) * 100L)  / contentLength) /100F)
    }

    sink.writeAll(response.body!!.source())
    progress(1.0F)
    sink.close()
    response.body!!.close()

    Log.i("com.lex.vrpquest", "Download successful: $destinationPath")
    return true
}



fun unzip(zipdir:File, pass:String) {
    val ver = SevenZip.getSevenZipVersion();
    try {
        val randomAccessFile = RandomAccessFile(zipdir, "r")
        val inStream = RandomAccessFileInStream(randomAccessFile)
        val callback: IArchiveOpenCallback = ArchiveOpenCallback()
        val inArchive: IInArchive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, inStream, pass)

        var extractCallback: IArchiveExtractCallback = ArchiveExtractCallback()
        inArchive.extract(null, false, extractCallback)

        inArchive.close()
        inStream.close()
    } catch (e: FileNotFoundException) {
        Log.e(TAG, e.message!!)
    } catch (e: SevenZipException) {
        Log.e(TAG, e.message!!)
    } catch (e: IOException) {
        Log.e(TAG, e.message!!)
    }
}

class ArchiveOpenCallback : IArchiveOpenCallback {
    override fun setTotal(files: Long, bytes: Long) {
        Log.i(TAG, "Archive open, total work: $files files, $bytes bytes")
    }

    override fun setCompleted(files: Long, bytes: Long) {
        Log.i(TAG, "Archive open, completed: $files files, $bytes bytes")
    }
}

class ArchiveExtractCallback : IArchiveExtractCallback {
    override fun setTotal(total: Long) {

    }

    override fun setCompleted(complete: Long) {

    }

    override fun getStream(index: Int, extractAskMode: ExtractAskMode?): ISequentialOutStream {
        Log.i(TAG, "Extract archive, get stream: $index to: $extractAskMode")
        val stream: ISequentialOutStream = SequentialOutStream()
        return stream
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode?) {
    }

    override fun setOperationResult(extractOperationResult: ExtractOperationResult?) {

    }

}
private class SequentialOutStream : ISequentialOutStream {
    @Throws(SevenZipException::class)
    override fun write(data: ByteArray): Int {
        if (data == null || data.size == 0) {
            throw SevenZipException("null data")
        }
        Log.i(TAG, "Data to write: " + data.size)
        return data.size
    }
}