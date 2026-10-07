package com.streamdev.aiostreamer.baseline

import com.streamdev.aiostreamer.baseline.security.PasswordHasher
import com.streamdev.aiostreamer.baseline.security.RsaHashSigner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64
import javax.crypto.Cipher
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class SecurityTest {

    @Test
    fun sha3_matchesKnownVector() {
        // SHA3-256("") known digest.
        assertEquals(
            "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a",
            PasswordHasher.sha3Hex(""),
        )
    }

    @Test
    fun sha3_isDeterministic() {
        assertEquals(PasswordHasher.sha3Hex("hunter2"), PasswordHasher.sha3Hex("hunter2"))
    }

    @Test
    fun rsaHashSigner_producesHeaderSafeDecryptableToken() {
        // Generate a throwaway RSA keypair so the test can decrypt and assert the payload.
        val kpg = java.security.KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }
        val kp = kpg.generateKeyPair()
        val pubB64 = Base64.getEncoder().encodeToString(
            KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(kp.public.encoded)).encoded,
        )

        val signer = RsaHashSigner(
            certSha1DigestProvider = { byteArrayOf(1, 2, 3, 4, 5) },
            packageName = "com.streamdev.aiostreamer",
            serverPublicKeyBase64 = pubB64,
            clockSeconds = { 1700000000L },
        )

        val hash = signer.sign()

        assertTrue("hash must be newline-free for use as an HTTP header", !hash.contains("\n") && !hash.contains("\r"))

        // Decrypt with the private key and confirm the reconstructed payload.
        val decrypted = Cipher.getInstance("RSA/ECB/PKCS1PADDING").run {
            init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(kp.private.encoded)))
            String(doFinal(Base64.getDecoder().decode(hash)))
        }
        val expectedCertB64 = Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4, 5))
        assertEquals(expectedCertB64 + "com.streamdev.aiostreamer" + "1700000000", decrypted)
    }
}
