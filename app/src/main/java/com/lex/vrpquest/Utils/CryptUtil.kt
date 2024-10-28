package com.lex.vrpquest.Utils

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


fun decryptPassword(encryptedPassword: String): String? {
    val key = byteArrayOf(
        0x9c.toByte(), 0x93.toByte(), 0x5b.toByte(), 0x48.toByte(), 0x73.toByte(), 0x0a.toByte(), 0x55.toByte(), 0x4d.toByte(),
        0x6b.toByte(), 0xfd.toByte(), 0x7c.toByte(), 0x63.toByte(), 0xc8.toByte(), 0x86.toByte(), 0xa9.toByte(), 0x2b.toByte(),
        0xd3.toByte(), 0x90.toByte(), 0x19.toByte(), 0x8e.toByte(), 0xb8.toByte(), 0x12.toByte(), 0x8a.toByte(), 0xfb.toByte(),
        0xf4.toByte(), 0xde.toByte(), 0x16.toByte(), 0x2b.toByte(), 0x8b.toByte(), 0x95.toByte(), 0xf6.toByte(), 0x38.toByte()
    )

    val decoded = Base64.getUrlDecoder().decode(encryptedPassword)
    if (decoded.size < 16) {
        return null // Input too short
    }

    val iv = decoded.slice(0 until 16).toByteArray()
    val data = decoded.slice(16 until decoded.size).toByteArray()

    val cipher = Cipher.getInstance("AES/CTR/NoPadding")
    val secretKeySpec = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)

    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

    val decrypted = cipher.doFinal(data)

    return String(decrypted)
}