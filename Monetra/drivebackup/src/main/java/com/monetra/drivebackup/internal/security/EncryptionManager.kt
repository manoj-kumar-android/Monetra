package com.monetra.drivebackup.internal.security

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class EncryptionManager @Inject constructor() {

    private val algorithm = "AES/GCM/NoPadding"
    private val tagLength = 128
    private val ivLength = 12
    private val saltLength = 16
    private val iterations = 10000
    private val keyLength = 256
    
    // This secret is baked into the app and combined with the Google ID
    private val appSecret = "MonetraBackupSecretV1"

    /**
     * Derives a stable AES key from the Google User ID.
     */
    private fun deriveKey(googleUserId: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            (googleUserId + appSecret).toCharArray(),
            salt,
            iterations,
            keyLength
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Encrypts a file using AES-256 GCM.
     */
    fun encrypt(googleUserId: String, inputFile: File, outputFile: File) {
        val salt = ByteArray(saltLength).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(ivLength).apply { SecureRandom().nextBytes(this) }
        val key = deriveKey(googleUserId, salt)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(tagLength, iv))

        inputFile.inputStream().use { input ->
            outputFile.outputStream().use { output ->
                // Write salt and IV to the beginning of the file
                output.write(salt)
                output.write(iv)
                
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encryptedChunk = cipher.update(buffer, 0, bytesRead)
                    if (encryptedChunk != null) {
                        output.write(encryptedChunk)
                    }
                }
                val finalChunk = cipher.doFinal()
                if (finalChunk != null) {
                    output.write(finalChunk)
                }
            }
        }
    }

    /**
     * Decrypts a file using AES-256 GCM.
     */
    fun decrypt(googleUserId: String, inputFile: File, outputFile: File) {
        inputFile.inputStream().use { input ->
            val salt = ByteArray(saltLength).apply { input.read(this) }
            val iv = ByteArray(ivLength).apply { input.read(this) }
            val key = deriveKey(googleUserId, salt)

            val cipher = Cipher.getInstance(algorithm)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(tagLength, iv))

            outputFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val decryptedChunk = cipher.update(buffer, 0, bytesRead)
                    if (decryptedChunk != null) {
                        output.write(decryptedChunk)
                    }
                }
                val finalChunk = cipher.doFinal()
                if (finalChunk != null) {
                    output.write(finalChunk)
                }
            }
        }
    }
}
