package com.example.sensor

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log2
import kotlin.math.max

data class CameraLuminanceData(
    val histogramBins: FloatArray = FloatArray(64) { 0f },
    val averageLuminance: Float = 118f,
    val centerSpotLuminance: Float = 118f,
    val estimatedEv100: Float = 12f,
    val estimatedLux: Float = 1000f,
    val shadowClippingRatio: Float = 0f,
    val highlightClippingRatio: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CameraLuminanceData
        return histogramBins.contentEquals(other.histogramBins) &&
                averageLuminance == other.averageLuminance &&
                centerSpotLuminance == other.centerSpotLuminance &&
                estimatedEv100 == other.estimatedEv100
    }

    override fun hashCode(): Int {
        var result = histogramBins.contentHashCode()
        result = 31 * result + averageLuminance.hashCode()
        result = 31 * result + centerSpotLuminance.hashCode()
        result = 31 * result + estimatedEv100.hashCode()
        return result
    }
}

class CameraLuminanceAnalyzer : ImageAnalysis.Analyzer {

    private val _luminanceData = MutableStateFlow(CameraLuminanceData())
    val luminanceData: StateFlow<CameraLuminanceData> = _luminanceData.asStateFlow()

    private val rawBins = IntArray(64)

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes[0]
            val buffer = plane.buffer
            val rowStride = plane.rowStride
            val pixelStride = plane.pixelStride
            val width = image.width
            val height = image.height

            rawBins.fill(0)
            var totalLuminance = 0L
            var totalPixels = 0

            // Spot metering center region: central 20% x 20%
            val spotLeft = (width * 0.40).toInt()
            val spotRight = (width * 0.60).toInt()
            val spotTop = (height * 0.40).toInt()
            val spotBottom = (height * 0.60).toInt()
            var spotLuminanceSum = 0L
            var spotPixelCount = 0

            var shadowClippedCount = 0
            var highlightClippedCount = 0

            // Subsample with step 3 for high framerate & ultra low CPU usage
            val step = 3
            for (row in 0 until height step step) {
                val rowOffset = row * rowStride
                val isSpotRow = row in spotTop..spotBottom

                for (col in 0 until width step step) {
                    val index = rowOffset + col * pixelStride
                    if (index < buffer.limit()) {
                        val y = buffer.get(index).toInt() and 0xFF
                        val bin = (y / 4).coerceIn(0, 63)
                        rawBins[bin]++
                        totalLuminance += y
                        totalPixels++

                        if (isSpotRow && col in spotLeft..spotRight) {
                            spotLuminanceSum += y
                            spotPixelCount++
                        }

                        if (y <= 5) shadowClippedCount++
                        if (y >= 250) highlightClippedCount++
                    }
                }
            }

            if (totalPixels > 0) {
                // Find peak bin for normalization
                var maxCount = 1
                for (b in rawBins) {
                    if (b > maxCount) maxCount = b
                }

                val normalizedBins = FloatArray(64) { i ->
                    (rawBins[i].toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                }

                val avgY = (totalLuminance.toFloat() / totalPixels).coerceIn(1f, 254f)
                val spotY = if (spotPixelCount > 0) {
                    (spotLuminanceSum.toFloat() / spotPixelCount).coerceIn(1f, 254f)
                } else avgY

                // 18% standard middle gray is Y ≈ 118 in sRGB/Rec709
                // Standard calibration: if scene is at middle gray (Y=118), typical indoor office EV100 is ~7-8, daylight is ~12-15
                // Formula: EV100 = BaseEv + log2(spotY / 118)
                val baseEv = 12.0f
                val deltaEv = log2(spotY / 118.0f)
                val estimatedEv = (baseEv + deltaEv).coerceIn(-4.0f, 18.0f)
                val estimatedLux = (2.5f * Math.pow(2.0, estimatedEv.toDouble())).toFloat()

                val shadowRatio = shadowClippedCount.toFloat() / totalPixels
                val highlightRatio = highlightClippedCount.toFloat() / totalPixels

                _luminanceData.value = CameraLuminanceData(
                    histogramBins = normalizedBins,
                    averageLuminance = avgY,
                    centerSpotLuminance = spotY,
                    estimatedEv100 = estimatedEv,
                    estimatedLux = estimatedLux,
                    shadowClippingRatio = shadowRatio,
                    highlightClippingRatio = highlightRatio
                )
            }
        } catch (e: Exception) {
            // Protect against buffer concurrency during camera shutdown
        } finally {
            image.close()
        }
    }
}
