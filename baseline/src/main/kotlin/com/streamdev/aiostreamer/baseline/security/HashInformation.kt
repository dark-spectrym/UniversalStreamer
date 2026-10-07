package com.streamdev.aiostreamer.baseline.security

import com.google.gson.annotations.SerializedName
import com.streamdev.aiostreamer.baseline.model.LoginStatus

/**
 * Payload serialized to JSON and RSA-encrypted to form the `hash` request header
 * in the v9 API (reconstructed from v6.7.1 `HashInformation` + hash generator).
 *
 * Replaces the v7 scheme (base64(SHA1(cert)) + packageName + unixSeconds). v9
 * sends a structured object including the current login state, all signing-cert
 * digests (SHA-256, base64 no-wrap), the android id, package, client version and
 * whether the device is a TV.
 */
data class HashInformation(
    @SerializedName("id") var id: String = "",
    @SerializedName("isTV") var isTV: Boolean = false,
    @SerializedName("loginStatus") var loginStatus: LoginStatus = LoginStatus(),
    @SerializedName("packageName") var packageName: String = "",
    @SerializedName("signatures") var signatures: List<String> = emptyList(),
    @SerializedName("time") var time: Long = 0L,
    @SerializedName("version") var version: Int = 0,
)
