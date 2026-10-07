package com.streamdev.aiostreamer.baseline

/**
 * Central configuration for every connection the app makes to the outside world.
 *
 * In the original v6.4.5 build these values were scattered as string literals
 * across `RetrofitClient`, `GLOBALVARS`, `GetStream` and `GetDataRows`. They are
 * consolidated here so the whole external-connection surface is defined in one
 * place and can be pointed at a staging backend for testing.
 */
object BaselineConfig {

    /** Backend REST base URL. All typed endpoints in [ApiService] are relative to this. */
    const val DEFAULT_BASE_URL: String = "https://porn-app.com/api/"

    /** Current API version segment. Endpoints are declared as `v7/...`. */
    const val API_VERSION: String = "v7"

    /** Connect/read/write timeout applied to the backend client (seconds). */
    const val TIMEOUT_SECONDS: Long = 90L

    /** Longer timeout used for stream-resolution and direct site scraping (seconds). */
    const val SCRAPE_TIMEOUT_SECONDS: Long = 60L

    /**
     * RSA public key of the backend, used to encrypt the per-request integrity
     * hash (see [com.streamdev.aiostreamer.baseline.security.RsaHashSigner]).
     * This is a public key shipped in every client build; it is not a secret.
     */
    const val SERVER_PUBLIC_KEY: String =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAsnKCLt4+khc/JxFKF3Vzz" +
        "E9JH9brTc8BgfnI34lm21L9yLnI2t+urGqCihcIEYwUMDsXN4bMvkaB99UA3KCU1Ka" +
        "NQD2yrJXcLu5Gz3iLQHtaVL0Xky/BPunzIXctPh3E1IPCpuXzI2JRgzKvmkrnY2UQj" +
        "7a2luquCTTHXkfXETZC1sXEklDCjyf+kxRyx9rYELCAY3Mzuo9CWw/xP+10RH7lrFj" +
        "2NfHE/zNd955YJnh5XdwzfAdK7YPSbL/1lF2r8YjYRcFPr8OdvZ3CisDZRtWUtA6VL" +
        "A0Z6KLm047Qm/swqfJUoGzfnkFEahnXaxi2nxVinGH4CzZGmIhFYQB3w2KpTXKo8JH" +
        "P9C/gz7FGQH1BVbe0K4LZ1BV1ffW9vzVzHdS7gY7ND8r/gKNZeaVU8YVcyflg0nb/U" +
        "IkyWKM5Ap8HucAb8djXT08i/FBsfc0yo0v15X2mYIjtAo+3/Cmc1BY54VojOomWt/X" +
        "t35CfJXMyt5LU1T4eFVxdojF4K/wYcRXCxsAl7DWfPwGz8gCVmrnls3wzpiehmuP52" +
        "W6iV7nBlh9eoDKWMOURmPRFkLpVkRHfc59zQACZdhdUzVntIgNFU2S0m3XnoxUM1FM" +
        "JUMyEmGdDW7w/vSyub5v89XukrNHR87yNDXusCAOCersR7qcDCc1UbafL2/214NP1Y" +
        "yMCAwEAAQ=="

    /** iOS Safari UA used for most mobile-oriented site requests. */
    const val USER_AGENT_MOBILE: String =
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
        "(KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"

    /** Desktop Chrome UA used where a site blocks or degrades the mobile UA. */
    const val USER_AGENT_DESKTOP: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
}
