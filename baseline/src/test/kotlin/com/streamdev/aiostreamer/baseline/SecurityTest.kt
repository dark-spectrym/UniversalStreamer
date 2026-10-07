package com.streamdev.aiostreamer.baseline

import com.google.gson.Gson
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.security.HashInformation
import com.streamdev.aiostreamer.baseline.security.PasswordHasher
import com.streamdev.aiostreamer.baseline.security.RsaHashSigner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

class SecurityTest {

    @Test
    fun sha3_matchesKnownVector() {
        assertEquals(
            "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a",
            PasswordHasher.sha3Hex(""),
        )
    }

    @Test
    fun certDigest_isSha256Base64() {
        val cert = byteArrayOf(1, 2, 3, 4, 5)
        val expected = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(cert),
        )
        assertEquals(expected, RsaHashSigner.certDigest(cert))
    }

    @Test
    fun v9Hash_encryptsHashInformationJsonAndIsHeaderSafe() {
        // 4096-bit to match the production server key's plaintext capacity; the
        // HashInformation JSON exceeds a 2048-bit key's PKCS1 limit.
        val kp = KeyPairGenerator.getInstance("RSA").apply { initialize(4096) }.generateKeyPair()
        val pubB64 = Base64.getEncoder().encodeToString(kp.public.encoded)

        val info = HashInformation(
            id = "android-xyz",
            isTV = false,
            loginStatus = LoginStatus(pro = 0, status = 0, token = "", unixtime = 1700000000),
            packageName = "com.streamdev.aiostreamer",
            signatures = listOf(RsaHashSigner.certDigest(byteArrayOf(9, 9, 9))),
            time = 1700000000,
            version = 6719,
        )
        val signer = RsaHashSigner(infoProvider = { info }, serverPublicKeyBase64 = pubB64)

        val hash = signer.sign()
        assertTrue("header-safe", !hash.contains("\n") && !hash.contains("\r"))

        // The server would RSA-decrypt and parse the JSON; emulate that round-trip.
        val decrypted = Cipher.getInstance("RSA/ECB/PKCS1PADDING").run {
            init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA")
                .generatePrivate(PKCS8EncodedKeySpec(kp.private.encoded)))
            String(doFinal(Base64.getDecoder().decode(hash)))
        }
        val parsed = Gson().fromJson(decrypted, HashInformation::class.java)
        assertEquals("com.streamdev.aiostreamer", parsed.packageName)
        assertEquals(6719, parsed.version)
        assertEquals(info.signatures, parsed.signatures)
    }
}
