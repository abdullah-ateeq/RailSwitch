package com.example.railswitch.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure procedural audio synthesizer providing low-latency arcade sound effects
 * without external audio assets.
 */
class SoundManager {

    var isEnabled: Boolean = true

    private val audioExecutor = Executors.newSingleThreadExecutor()
    private val sampleRate = 22050

    fun playSwitchSound() {
        if (!isEnabled) return
        audioExecutor.execute {
            // High-tech quick frequency glide (480Hz -> 760Hz, 35ms)
            val durationMs = 35
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = 480f + (760f - 480f) * progress
                val angle = 2.0 * PI * i * freq / sampleRate
                val envelope = 1f - progress // linear fade-out
                samples[i] = (sin(angle) * envelope * 0.45 * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(samples)
        }
    }

    fun playDodgeSound() {
        if (!isEnabled) return
        audioExecutor.execute {
            // Bright rewarding coin/chime blip (880Hz -> 1320Hz, 45ms)
            val durationMs = 45
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = 880f + (1320f - 880f) * progress
                val angle = 2.0 * PI * i * freq / sampleRate
                val envelope = 1f - progress * progress
                samples[i] = (sin(angle) * envelope * 0.5 * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(samples)
        }
    }

    fun playMilestoneSound() {
        if (!isEnabled) return
        audioExecutor.execute {
            // Triumphant double-beep harmonic arpeggio (70ms)
            val durationMs = 80
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = if (progress < 0.5f) 784f else 1046.5f // G5 -> C6
                val angle = 2.0 * PI * i * freq / sampleRate
                val envelope = 1f - (progress % 0.5f) * 2f
                samples[i] = (sin(angle) * envelope * 0.6 * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(samples)
        }
    }

    fun playCollisionSound() {
        if (!isEnabled) return
        audioExecutor.execute {
            // Crunchy low-frequency impact with slight distortion (140ms)
            val durationMs = 140
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toFloat() / numSamples
                val freq = 160.0 * (1.0 - progress * 0.6)
                val fundamental = sin(2.0 * PI * i * freq / sampleRate)
                val noise = ((i % 17) - 8) / 8.0 * 0.35
                val mixed = (fundamental + noise).coerceIn(-1.0, 1.0)
                val envelope = (1.0 - progress) * (1.0 - progress)
                samples[i] = (mixed * envelope * 0.7 * Short.MAX_VALUE).toInt().toShort()
            }
            playPcm(samples)
        }
    }

    private fun playPcm(samples: ShortArray) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep((samples.size * 1000L / sampleRate) + 20L)
        } catch (_: Exception) {
            // Suppress audio hardware errors
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }

    fun release() {
        try {
            audioExecutor.shutdownNow()
        } catch (_: Exception) {}
    }
}
