package com.streamdev.aiostreamer.baseline.security

import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

/**
 * Default [HashSigner]. Reconstructs the `generateHash` algorithm from the
 * original `HelperClass`:
 *
 * ```
 * payload   = base64( SHA1(signingCertificate) ) + packageName + unixSeconds
 * hash      = base64( RSA/ECB/PKCS1( payload ) )   // encrypted with the server public key
 * ```
 *
 * All CR/LF produced by the base64 encoders are stripped so the value is header-safe.
 *
 * The signing-certificate SHA-1 digest and the package name are platform values,
 * supplied by the caller, which keeps this class free of any Android dependency.
 *
 * @param certSha1DigestProvider returns the raw SHA-1 digest of the app's signing certificate
 * @param packageName            the application id (e.g. `com.streamdev.aiostreamer`)
 * @param serverPublicKeyBase64  the backend RSA public key (see [BaselineConfig.SERVER_PUBLIC_KEY])
 * @param clockSeconds           current unix time in seconds; injectable for testing
 */
class RsaHashSigner(
    private val certSha1DigestProvider: () -> ByteArray,
    private val packageName: String,
    private val serverPublicKeyBase64: String,
    private val clockSeconds: () -> Long = { System.currentTimeMillis() / 1000L },
) : HashSigner {

    override fun sign(): String {
        val certDigestB64 = Base64.getEncoder()
            .encodeToString(certSha1DigestProvider())
            .stripNewlines()
        val payload = certDigestB64 + packageName + clockSeconds().toString()
        return encrypt(payload).stripNewlines()
    }

    private fun encrypt(data: String): String {
        val keyBytes = Base64.getDecoder().decode(serverPublicKeyBase64)
        val publicKey = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(keyBytes))
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.toByteArray(Charsets.UTF_8)))
    }

    private fun String.stripNewlines(): String = replace("\n", "").replace("\r", "")
}
