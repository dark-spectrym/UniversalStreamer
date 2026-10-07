package com.streamdev.aiostreamer.core.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import com.streamdev.aiostreamer.baseline.BaselineConfig
import com.streamdev.aiostreamer.baseline.model.LoginStatus
import com.streamdev.aiostreamer.baseline.security.HashInformation
import com.streamdev.aiostreamer.baseline.security.HashSigner
import com.streamdev.aiostreamer.baseline.security.RsaHashSigner

/**
 * Android-backed [HashSigner] for the v9 API.
 *
 * Builds the [HashInformation] the backend expects, supplying the platform values
 * the baseline cannot know: the SHA-256 digests of every signing certificate, the
 * android id, the TV flag, and the current login snapshot. The RSA encryption and
 * JSON serialisation stay in the baseline ([RsaHashSigner]).
 *
 * Uses `GET_SIGNING_CERTIFICATES` on API 28+ and the deprecated `GET_SIGNATURES`
 * below that (minSdk 24).
 */
object AndroidHashSigner {

    fun create(context: Context, loginStatusProvider: () -> LoginStatus): HashSigner {
        val appContext = context.applicationContext
        return RsaHashSigner(
            infoProvider = { buildInfo(appContext, loginStatusProvider()) },
            serverPublicKeyBase64 = BaselineConfig.SERVER_PUBLIC_KEY,
        )
    }

    @SuppressLint("HardwareIds")
    private fun buildInfo(context: Context, login: LoginStatus): HashInformation = HashInformation(
        id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "",
        isTV = isTelevision(context),
        loginStatus = login,
        packageName = context.packageName,
        signatures = signingCertSha256(context),
        time = System.currentTimeMillis() / 1000L,
        version = BaselineConfig.VERSION_CODE,
    )

    private fun signingCertSha256(context: Context): List<String> {
        val pm = context.packageManager
        val pkg = context.packageName
        val certs: List<ByteArray> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            @Suppress("PackageManagerGetSignatures")
            val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
            (info.signingInfo?.apkContentsSigners ?: emptyArray()).map { it.toByteArray() }
        } else {
            @Suppress("DEPRECATION", "PackageManagerGetSignatures")
            val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            (info.signatures ?: emptyArray()).map { it.toByteArray() }
        }
        return certs.map(RsaHashSigner::certDigest)
    }

    private fun isTelevision(context: Context): Boolean {
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
            pm.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
        ) {
            return true
        }
        val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
    }
}
