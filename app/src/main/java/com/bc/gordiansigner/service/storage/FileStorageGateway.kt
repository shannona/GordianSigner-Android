package com.bc.gordiansigner.service.storage

import android.content.Context
import java.io.File

class FileStorageGateway internal constructor(private val context: Context) {

    fun save(path: String, name: String, data: ByteArray): File {
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, name)
        if (!file.exists()) file.createNewFile()
        file.writeBytes(data)
        return file
    }

    fun saveOnFilesDir(name: String, data: ByteArray) =
        save(context.filesDir.absolutePath, name, data)

    fun isExistingOnFilesDir(name: String) = File(context.filesDir, name).exists()


    fun isExisting(path: String) = File(path).exists()

    fun read(path: String) = File(path).readBytes()

    fun readOnFilesDir(name: String) = read(File(context.filesDir, name).absolutePath)

    fun filesDir() = context.filesDir

    fun firstFile(path: String) =
        listFiles(path).let { files -> if (files.isNotEmpty()) files[0] else null }

    fun listFiles(path: String) = File(path).let { file ->
        when {
            !file.exists() -> listOf()
            file.isFile -> listOf(file)
            else -> file.listFiles()?.toList() ?: listOf()
        }
    }

    fun delete(path: String) = File(path).let { file ->
        if (!file.exists()) true
        else if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }
}