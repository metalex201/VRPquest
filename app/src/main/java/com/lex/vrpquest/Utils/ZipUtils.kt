package com.lex.vrpquest.Utils

import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.ExtractAskMode
import net.sf.sevenzipjbinding.ExtractOperationResult
import net.sf.sevenzipjbinding.IArchiveExtractCallback
import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback
import net.sf.sevenzipjbinding.ICryptoGetTextPassword
import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.ISequentialOutStream
import net.sf.sevenzipjbinding.PropID
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipException
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.VolumedArchiveInStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

//======================================= SevenZipExtract =======================================

class ExtractionException : Exception {
    constructor(msg: String?) : super(msg)
    constructor(msg: String?, e: Exception?) : super(msg, e)
}

class ExtractCallback(private val inArchive: IInArchive, outputDirectoryFile: File, pass: String, progress: (Float) -> Unit) :
    IArchiveExtractCallback, ICryptoGetTextPassword {
    private var index = 0
    private var outputStream: OutputStream? = null
    private var file: File? = null
    private var isFolder = false
    private var outputDirectoryFile = outputDirectoryFile
    private var pass = pass
    //progress callback
    private var totalval = 0L
    private var progress = progress
    override fun setTotal(total: Long) {totalval = total}
    override fun setCompleted(completeValue: Long) {
        if (completeValue != 0L) {
            progress((((completeValue) * 100L) / totalval) / 100F)
        }
    }

    @Throws(SevenZipException::class)
    override fun getStream(index: Int, extractAskMode: ExtractAskMode): ISequentialOutStream? {
        closeOutputStream()

        this.index = index
        this.isFolder = inArchive.getProperty(
            index,
            PropID.IS_FOLDER
        ) as Boolean

        val path = inArchive.getProperty(index, PropID.PATH) as String
        file = File(outputDirectoryFile, path)
        if (isFolder) {
            createDirectory(file!!)
            return null
        }

        createDirectory(file!!.parentFile)

        try {
            outputStream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            throw SevenZipException(
                "Error opening file: "
                        + file!!.absolutePath, e
            )
        }

        return ISequentialOutStream { data ->
            try {
                outputStream!!.write(data)
            } catch (e: IOException) {
                throw SevenZipException(
                    "Error writing to file: "
                            + file!!.absolutePath
                )
            }
            data.size // Return amount of consumed data
        }
    }

    @Throws(SevenZipException::class)
    private fun createDirectory(parentFile: File) {
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw SevenZipException(
                    "Error creating directory: "
                            + parentFile.absolutePath
                )
            }
        }
    }

    @Throws(SevenZipException::class)
    private fun closeOutputStream() {
        if (outputStream != null) {
            try {
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                throw SevenZipException(
                    "Error closing file: "
                            + file!!.absolutePath
                )
            }
        }
    }

    override fun prepareOperation(extractAskMode: ExtractAskMode) {}
    @Throws(SevenZipException::class)
    override fun setOperationResult(
        extractOperationResult: ExtractOperationResult
    ) {
        closeOutputStream()
        val path = inArchive.getProperty(index, PropID.PATH) as String
        //Log.i(TAG, path);
        //Log.i(TAG,"extractOperationResult: " + extractOperationResult.toString());
        if (extractOperationResult != ExtractOperationResult.OK) {
            throw SevenZipException("Invalid file: $path")
        }
    }

    override fun cryptoGetTextPassword(): String {
        return pass
    }
}

//MULTIPART
class ArchiveOpenVolumeCallback : IArchiveOpenVolumeCallback {
    private val openedRandomAccessFileList: MutableMap<String, RandomAccessFile> = HashMap()

    @Throws(SevenZipException::class)
    override fun getProperty(propID: PropID) {
    }

    @Throws(SevenZipException::class)
    override fun getStream(filename: String): RandomAccessFileInStream? {
        try {
            var randomAccessFile = openedRandomAccessFileList[filename]
            if (randomAccessFile != null) {
                randomAccessFile.seek(0)
                return RandomAccessFileInStream(randomAccessFile)
            }
            randomAccessFile = RandomAccessFile(filename, "r")
            openedRandomAccessFileList[filename] = randomAccessFile
            return RandomAccessFileInStream(randomAccessFile)
        } catch (fileNotFoundException: FileNotFoundException) {
            return null
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}



@Throws(ExtractionException::class)
fun SevenZipExtract(archive: String,
                     outputDirectory: String,
                     IsSingularZip: Boolean,
                     pass: String,
                     progress: (Float) -> Unit,
                     IsStopping: MutableState<Boolean> = mutableStateOf(false))
{
    //prepareOutputDirectory
    var outputDirectoryFile = File(outputDirectory)
    if (!outputDirectoryFile.exists()) {
        outputDirectoryFile.mkdirs()
    } else {
        if (outputDirectoryFile.listFiles() != null) {
            throw ExtractionException(
                "Output directory not empty: "
                        + outputDirectory
            )
        }
    }
    if (IsSingularZip) {
        //extractArchiveSingle
        val randomAccessFile: RandomAccessFile
        val inArchive: IInArchive

        try {
            randomAccessFile = RandomAccessFile(archive, "r")
            inArchive = SevenZip.openInArchive(
                ArchiveFormat.SEVEN_ZIP,
                RandomAccessFileInStream(randomAccessFile), pass
            )
            GlobalScope.launch {
                while (!IsStopping.value) {
                    delay(1)
                }
                inArchive.close()
            }
        } catch (e: FileNotFoundException) {
            throw ExtractionException("File not found", e)
        } catch (e: SevenZipException) {
            throw ExtractionException("Error opening archive", e)
        }

        try {
            inArchive.extract(null, false, ExtractCallback(inArchive, outputDirectoryFile, pass, progress))
            inArchive.close()
        } catch (e: SevenZipException) {
            val message = "error message:" + e.message + "," + (e.cause?.message ?: "")
            throw ExtractionException(message, e)
        }
    } else {
        //extractArchiveMulti
        val inArchive: IInArchive
        try {
            inArchive = SevenZip.openInArchive(
                ArchiveFormat.SEVEN_ZIP,
                VolumedArchiveInStream(archive, ArchiveOpenVolumeCallback()), pass
            )
            inArchive.extract(null, false, ExtractCallback(inArchive, outputDirectoryFile, pass, progress))
            inArchive.close()
        } catch (e: SevenZipException) {
            val message = "error message:" + e.message + "," + (e.cause?.message ?: "")
            throw ExtractionException(message, e)
        }
    }
}


//ZIPPING NORMAL

//ZIPPING

class ZipUtil {
    val BUFFER_SIZE = 4096
    public fun zip(listFiles: List<File>, destZipFile: File?, customName: Pair<File, File> = Pair(File(""), File("")), progress: (Float) -> Unit) {
        val fullsize = getSize(listFiles)
        var tempSize:Long = 0
        val fileOutputStream = FileOutputStream(destZipFile)
        val zos = ZipOutputStream(fileOutputStream)

        for (file in listFiles) {
            if (file.isDirectory) {
                zipDirectory(file, file.name, zos) {
                    tempSize += it
                    progress((((tempSize) * 100L) / fullsize) / 100F)
                }
            } else {
                var name = file.name
                if (file == customName.first) {
                    println("CUSTOMNAME SET PAIR FOUND")
                    name = customName.second.name
                }
                zipFile(file, zos, {tempSize += it
                    progress((((tempSize) * 100L) / fullsize) / 100F) }, name)
            }
        }
        zos.flush()
        zos.close()
    }

    private fun zipDirectory(folder: File, parentFolder: String, zos: ZipOutputStream, progress: (Long) -> Unit) {
        for (file in folder.listFiles()) {
            if (file.isDirectory) {
                zipDirectory(file, parentFolder + File.separator + file.name, zos, progress)
                continue
            }
            zos.putNextEntry(ZipEntry(parentFolder + File.separator + file.name))

            var totalRead: Long = 0
            var count: Long = 0

            val bis = BufferedInputStream(FileInputStream(file))
            val bytesIn = ByteArray(BUFFER_SIZE)
            var read = 0

            while ((bis.read(bytesIn).also { read = it }) != -1) {
                zos.write(bytesIn, 0, read)
                count++
                totalRead += read
                if (count % 100 == 0L) {
                    progress(totalRead )
                    totalRead = 0
                }
            }

            progress(totalRead)
            zos.closeEntry()
        }
    }

    private fun zipFile(file: File, zos: ZipOutputStream, progress: (Long) -> Unit, fileName:String = file.name) {
        println(file.absolutePath)
        var totalRead: Long = 0
        var count: Long = 0

        zos.putNextEntry(ZipEntry(fileName))
        val bis = BufferedInputStream(FileInputStream(file))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read = 0
        while ((bis.read(bytesIn).also { read = it }) != -1) {
            zos.write(bytesIn, 0, read)

            count++
            totalRead += read
            if (count % 100 == 0L) {
                progress(totalRead )
                totalRead = 0
            }
        }
        progress(totalRead)

        zos.closeEntry()
    }

    fun getSize(listFiles: List<File>): Long {
        var size: Long = 0
        for (file in listFiles) {
            if (file.isDirectory) {
                if(file.listFiles() != null) {
                    for (files in file.listFiles()) {
                        size += getSize(listOf(files))
                    }
                }
            } else {
                size += file.length()
            }
        }
        return size
    }
}
