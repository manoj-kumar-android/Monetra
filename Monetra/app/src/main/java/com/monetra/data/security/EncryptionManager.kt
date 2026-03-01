package com.monetra.data.security

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor() {

    private val transformation = "AES/GCM/NoPadding"
    private val algorithm = "PBKDF2WithHmacSHA256"
    private val salt = "monetra_stable_salt_v2".toByteArray() 
    private val iterationCount = 2000
    private val keyLength = 256

    private fun getSecretKey(password: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(algorithm)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun encrypt(data: String, password: String): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password))
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return iv + encryptedData
    }

    fun decrypt(encryptedByteArray: ByteArray, password: String): String {
        if (encryptedByteArray.size < 12) throw Exception("Invalid backup file format")
        
        val iv = encryptedByteArray.sliceArray(0 until 12)
        val encryptedData = encryptedByteArray.sliceArray(12 until encryptedByteArray.size)
        
        val cipher = Cipher.getInstance(transformation)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password), spec)
        
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }
}
