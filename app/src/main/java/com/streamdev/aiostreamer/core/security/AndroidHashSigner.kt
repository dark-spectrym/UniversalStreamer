package com.streamdev.aiostreamer.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.streamdev.aiostreamer.baseline.BaselineConfig
import com.streamdev.aiostreamer.baseline.security.HashSigner
import com.streamdev.aiostreamer.baseline.security.RsaHashSigner
import java.security.MessageDigest

/**
 * Android-backed [HashSigner].
 *
 * Supplies the platform-specific input to [RsaHashSigner]: the SHA-1 digest of
 * the app's own signing certificate, read from [PackageManager]. The RSA
 * encryption and payload assembly live in the platform-agnostic baseline.
 *
 * Replaces the reflection/`generateHash` logic in the old `HelperClass`, using
 * the modern `GET_SIGNING_CERTIFICATES` API on API 28+ and falling back to the
 * deprecated `GET_SIGNATURES` on older devices (minSdk 21).
 */
object AndroidHashSigner {

    fun create(context: Context): HashSigner {
        val appContext = context.applicationContext
        val packageName = appContext.packageName
        return RsaHashSigner(
            certSha1DigestProvider = { sha1OfSigningCert(appContext) },
            packageName = packageName,
            serverPublicKeyBase64 = BaselineConfig.SERVER_PUBLIC_KEY,
        )
    }

    private fun sha1OfSigningCert(context: Context): ByteArray {
        val pm = context.packageManager
        val packageName = context.packageName
        val certBytes: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("PackageManagerGetSignatures")
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signers = info.signingInfo?.apkContentsSigners
            requireNotNull(signers?.firstOrNull()) { "no signing certificate" }.toByteArray()
        } else {
            @Suppress("DEPRECATION", "PackageManagerGetSignatures")
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            info.signatures!!.first().toByteArray()
        }
        return MessageDigest.getInstance("SHA").digest(certBytes)
    }
}
