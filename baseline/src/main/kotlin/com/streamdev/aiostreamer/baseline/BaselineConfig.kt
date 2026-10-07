package com.streamdev.aiostreamer.baseline

/**
 * Central configuration for every connection the app makes to the outside world.
 *
 * Reconstructed from the clean v6.7.1 build (versionCode 6719): the backend moved
 * from the v7 flat API to the v9 RESTful API, the request-integrity hash scheme
 * and its RSA key changed, and two additional typed backends were introduced
 * (NSFW swipe feed and RedGifs).
 */
object BaselineConfig {

    /** Primary backend REST base URL. Typed endpoints in [ApiService] are relative to this. */
    const val DEFAULT_BASE_URL: String = "https://porn-app.com/api/"

    /** Swipe-feed backend (NSFW swipe feature). */
    const val SWIPE_BASE_URL: String = "https://nsfwswipe.com/api/"

    /** RedGifs public API (swipe/gif integration). */
    const val REDGIFS_BASE_URL: String = "https://api.redgifs.com/v2/"

    /** Current API version segment. Endpoints are declared as `v9/...`. */
    const val API_VERSION: String = "v9"

    /** Client version code sent in the integrity hash and version endpoints. */
    const val VERSION_CODE: Int = 6719

    /** Connect/read/write timeout applied to the backend client (seconds). */
    const val TIMEOUT_SECONDS: Long = 90L

    /** Longer timeout used for stream-resolution and direct site scraping (seconds). */
    const val SCRAPE_TIMEOUT_SECONDS: Long = 60L

    /**
     * RSA public key of the backend (v6.7.1), used to encrypt the per-request
     * integrity hash (see [com.streamdev.aiostreamer.baseline.security.RsaHashSigner]).
     * Public key shipped in every client build; not a secret.
     */
    const val SERVER_PUBLIC_KEY: String =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA2e/sX/U3UNOsCJQHkEK7IF+VG5D1jMSr" +
        "el8NDYTKVhV5etQg8hW4Lo5wQckpB8mbDz9ZVgy1z647Csh/vSqnpT1Rb3F35xNERrz87WzeoVGq" +
        "ABDNU4l+yqREmjgeyUYyMgAlIGVzhXwZJjOcZl7zcGEuH0H24aZhiQX5XBfcG4Rugnc0QbxUbnbl" +
        "TJD2vNHn4nzEJbz0eBE81YyF/Wkc1P4a55lD3CzDMoqjGNgESyb+9AO2yhY3ux20k7RUkLg62B65" +
        "6kYIQjGBu7tSyLVL08htpQOs/GDpi31sB2a32NPzgj85TNIOXQQ5ZtOZHssYGADtbBKbREYR6mtK" +
        "QKWG2qf58ns9wYZ2ATqwQAS/brTJYard0pThOh/71ik8aAeyw0jbL5jAhz0wSs679PwTUwvbD6oq" +
        "d2w8MDr2YG4lyK7jPma1QqzMpKCn/N2YKOU0jjXcj/twaXKSUCr+LiCu7MxBl76j3WoyaI4FsPGX" +
        "AKFPHQU6bixMY/0XmEezLzwlJ7cf24tLBqADm8ooy92xM6nfALY2bWMgAufqXwPRCfjlec/sOiST" +
        "O53P9XiaEUPLGpHUOqCZRFb1vc3v16B4Z1R+B6rYyaVJ9hkQ+x09yExDFxLQOuG7YqJkwq3az1CM" +
        "0zhMtK48vJrBUkgLzVMWnt3Tycn73ZEINcR183XyFI0CAwEAAQ=="

    /** iOS Safari UA used for most mobile-oriented site requests. */
    const val USER_AGENT_MOBILE: String =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
        "(KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"

    /** Desktop Firefox UA (matches the v6.7.1 default stream user-agent). */
    const val USER_AGENT_DESKTOP: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:152.0) Gecko/20100101 Firefox/152.0"
}
