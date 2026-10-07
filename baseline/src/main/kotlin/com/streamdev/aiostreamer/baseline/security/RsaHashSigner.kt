package com.streamdev.aiostreamer.baseline.security

import com.google.gson.Gson
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

/**
 * Default [HashSigner] for the v9 API.
 *
 * Reconstructs the v6.7.1 algorithm:
 * ```
 * hash = base64( RSA/ECB/PKCS1( gson.toJson(HashInformation) ) )   // server public key
 * ```
 * with all base64 CR/LF stripped so the value is header-safe.
 *
 * The [HashInformation] is rebuilt per call by [infoProvider] so time and the
 * current login state are always fresh. The provider is supplied by the Android
 * layer (which reads the signing-cert SHA-256 digests, android id and login
 * state); this class stays free of Android dependencies.
 */
class RsaHashSigner(
    private val infoProvider: () -> HashInformation,
    private val serverPublicKeyBase64: String,
    private val gson: Gson = Gson(),
) : HashSigner {

    override fun sign(): String {
        val json = gson.toJson(infoProvider())
        return encrypt(json).replace("\n", "").replace("\r", "")
    }

    private fun encrypt(data: String): String {
        val keyBytes = Base64.getDecoder().decode(serverPublicKeyBase64)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(keyBytes))
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray(Charsets.UTF_8)))
    }

    companion object {
        /**
         * SHA-256 digest of a signing certificate, standard base64 (padded, no line
         * wraps) — equivalent to Android's `Base64.encodeToString(sha, Base64.NO_WRAP)`,
         * which is the form the v9 backend expects in `HashInformation.signatures`.
         */
        fun certDigest(certBytes: ByteArray): String {
            val sha = java.security.MessageDigest.getInstance("SHA-256").digest(certBytes)
            return Base64.getEncoder().encodeToString(sha)
        }
    }
}
