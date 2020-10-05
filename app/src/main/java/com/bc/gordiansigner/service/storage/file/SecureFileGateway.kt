package com.bc.gordiansigner.service.storage.file

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File

class SecureFileGateway internal constructor(context: Context) : FileGateway(context) {

    private val MASTER_KEY_ALIAS = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    override fun write(path: String, name: String, data: ByteArray) {
        val file = getEncryptedFile("$path/$name")
        file.openFileOutput().apply {
            write(data)
            flush()
            close()
        }
    }

    override fun writeOnFilesDir(name: String, data: ByteArray) {
        write(context.filesDir.absolutePath, name, data)
    }

    override fun read(path: String): ByteArray {
        val file = getEncryptedFile(path)
        val inputStream = file.openFileInput()
        val os = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            os.write(nextByte)
            nextByte = inputStream.read()
        }
        return os.toByteArray()
    }

    override fun readOnFilesDir(name: String): ByteArray =
        read(File(context.filesDir, name).absolutePath)

    private fun getEncryptedFile(path: String) = File(path).let { f ->
        if (f.isDirectory) throw IllegalArgumentException("invalid file")
        EncryptedFile.Builder(
            f,
            context,
            MASTER_KEY_ALIAS,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }
}