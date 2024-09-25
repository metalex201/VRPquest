package com.lex.vrpquest.ui.theme

import android.annotation.SuppressLint
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.CountingOutputStream
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.NullOutputStream
import okio.IOException
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.SocketException


fun testfpt(user:String, pass:String, host:String):Boolean {

    val client = FTPClient();
    try {
        val parts = host.split(":").toTypedArray()
        if(parts.size < 2) {return false}
        println(parts[1])
        client.connect(parts[0], parts[1].toInt());
        client.login(user, pass);
        client.logout();
        return true
    } catch (e: SocketException ) {
        return false
    } catch (e: IOException ) {
        return false
    }
    return false
}

fun FTPconnect(user:String, pass:String, host:String):FTPClient? {
    val client = FTPClient();
    try {
        val parts = host.split(":").toTypedArray()
        if(parts.size < 2) {return null}
        println(parts[1])
        client.connect(parts[0], parts[1].toInt())
        client.login(user, pass);
        client.setFileType(FTP.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode()
        client.setBufferSize(1024);
        return client
    } catch (e: SocketException ) {
        return null
    } catch (e: IOException ) {
        return null
    }
}


fun FTPdownloadFile(client: FTPClient, localDir:String, remoteDir:String, progress: (Float) -> Unit):Boolean {
    val remoteFile = File(remoteDir)
    val directory = remoteFile.parentFile?.absolutePath
    val fileName = remoteFile.name

    val localdir = File(localDir).parentFile!!.absolutePath

    if(!File(localdir).exists()) {File(localdir).mkdirs()}

    client.enterLocalPassiveMode()
    client.changeWorkingDirectory(directory)
    client.setFileType(FTP.BINARY_FILE_TYPE);

    val fullsize = client.getSize(fileName).toLong()

    val fos = FileOutputStream(localDir)
    fos.flush()

    var count = 0
    val updatetime = 1000

    val cos: CountingOutputStream = object : CountingOutputStream(fos) {
        override fun beforeWrite(n: Int) {
            super.beforeWrite(n)
            count++
            if (count >= updatetime) {
                val progress = getByteCount() / (fullsize /100) / 100F
                progress(progress)
                println("Downloaded " +  getByteCount() + "/" + fullsize)
                println(progress)
                count = 0
            }

        }
    }




    println("DOWNLOAD STARTED")

    //benchmark(1, {client.retrieveFile(fileName, fos)})
    val success = client.retrieveFile(fileName, cos)
    progress(1F)

    fos.close()
    println("DOWNLOAD STATE : "+ success)
    return success
}

fun FTPdownloadRecursive(client: FTPClient, localDir:String, remoteDir:String, progress: (Float) -> Unit):Boolean {
    var size = FTPgetFolderSize(client, remoteDir)

    var currentSize = 0L
    var finishedSize = 0L

    if(!File(localDir).exists()) {File(localDir).mkdirs()}

    @SuppressLint("SuspiciousIndentation")
    fun file(client: FTPClient, localDir:String, remoteDir:String):Boolean {

        val remoteFile = File(remoteDir)
        val fileName = remoteFile.name

        val fos = FileOutputStream(localDir)
        fos.flush()

        val fullsize = client.getSize(fileName).toLong()
        var count = 0
        val updatetime = 1000

        val cos: CountingOutputStream = object : CountingOutputStream(fos) {
            override fun beforeWrite(n: Int) {
                super.beforeWrite(n)
                count++
                if (count >= updatetime) {
                    currentSize = finishedSize + getByteCount()
                    val progress = (currentSize) / (size /100 ) / 100F
                    progress(progress)
                    println("Downloaded " +  getByteCount() + "/" + fullsize)
                    count = 0
                }

            }
        }

        val success = client.retrieveFile(fileName, cos)
        finishedSize += fullsize

        fos.close()
        return success
    }

    fun recursive(client: FTPClient, localDir:String, remoteDir:String):Boolean {
        client.changeWorkingDirectory(remoteDir)
        val remoteDirFile = File(remoteDir)
        if (!remoteDirFile.exists()) {remoteDirFile.mkdirs()}

        val fileListing = client.listFiles()

        // Print the file and directory names
        for (file in fileListing) {
            println(file.name)
            if (file.isDirectory) {
                if(!File("$localDir/${file.name}").exists()) {
                    File("$localDir/${file.name}").mkdirs()
                }

                client.changeWorkingDirectory("$localDir/${file.name}")

                recursive(client, "$localDir/${file.name}", "$remoteDir/${file.name}")
            } else {
                file(client, "$localDir/${file.name}", "$remoteDir/${file.name}")
            }
        }
        return true
    }

    recursive(client, localDir, remoteDir)

    return true
}



fun FTPfileExists(client: FTPClient, file:String):Boolean {
    val remoteFile = File(file)
    val directory = remoteFile.parentFile?.absolutePath
    val fileName = remoteFile.name

    client.changeWorkingDirectory(directory)

    for(files in client.listFiles()) {
        if(files.name == fileName) {return true}
    }
    return false
}

fun FTPfindApk(client: FTPClient, folder:String):String {

    client.changeWorkingDirectory(folder)

    for(file in client.listFiles()) {
        if (file.name.endsWith(".apk")){
            return folder + "/" + file.name
        }
    }
    return ""
}

fun FTPgetFolderSize(client: FTPClient, folder:String):Long {

    client.changeWorkingDirectory(folder)
    var size = 0L

    for(file in client.listFiles()) {
        if (file.isDirectory){
            size += FTPgetFolderSize(client, folder + "/" + file.name)
        } else {
            size += file.size
        }
    }
    return size
}









fun benchmark(times: Int,  benchfunc: () -> Unit) {
    var startTime: Long
    var endTime: Long
    var elapsedTime = 0L
    repeat(times) {
        startTime = System.currentTimeMillis()
        benchfunc()
        endTime = System.currentTimeMillis()
        elapsedTime += endTime - startTime
    }
    elapsedTime = elapsedTime / times
    println("Elapsed time: $elapsedTime milliseconds")
}