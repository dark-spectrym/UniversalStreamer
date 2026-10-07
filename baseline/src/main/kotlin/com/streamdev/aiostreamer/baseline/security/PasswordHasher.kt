package com.streamdev.aiostreamer.baseline.security

import org.bouncycastle.crypto.digests.SHA3Digest
import org.bouncycastle.util.encoders.Hex

/**
 * SHA3-256 password digest, hex encoded.
 *
 * Mirrors `UserData.generateSHA` from the original build: the plaintext password
 * is never sent to the backend, only its SHA3-256 hex digest.
 */
object PasswordHasher {

    fun sha3Hex(plaintext: String): String {
        val digest = SHA3Digest(256)
        val input = plaintext.toByteArray(Charsets.UTF_8)
        digest.update(input, 0, input.size)
        val out = ByteArray(digest.digestSize)
        digest.doFinal(out, 0)
        return Hex.toHexString(out)
    }
}
