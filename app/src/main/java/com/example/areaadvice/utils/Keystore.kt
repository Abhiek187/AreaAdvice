package com.example.areaadvice.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

// Source: https://stackoverflow.com/a/42858285
@Suppress("DEPRECATION")
fun getSignature(context: Context?): String? {
    if (context == null) return null
    val packageManager = context.packageManager
    val packageName = context.packageName

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = packageInfo.signingInfo

            return if (signingInfo == null) {
                null
            } else if (signingInfo.hasMultipleSigners()) {
                signatureDigest(signingInfo.apkContentsSigners.last())
            } else {
                signatureDigest(signingInfo.signingCertificateHistory.last())
            }
        } else {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            val signatures = packageInfo.signatures

            return if (signatures == null || signatures.isEmpty()) {
                null
            } else {
                signatureDigest(signatures.last())
            }
        }
    } catch (_: Exception) {
        return null
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun signatureDigest(signature: Signature): String? {
    val signatureBytes = signature.toByteArray()

    try {
        val messageDigest = MessageDigest.getInstance("SHA1")
        val digest = messageDigest.digest(signatureBytes)
        return digest.toHexString()
    } catch (_: Exception) {
        return null
    }
}
