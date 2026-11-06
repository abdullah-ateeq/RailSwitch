package com.example.edgepulse.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Procedural retro-arcade sound synthesizer using Android AudioTrack.
 * Generates low-latency sound effects on demand with zero external asset files.
 */
class SoundManager {

    var isSoundEnabled: Boolean = true

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    // Pre-generated PCM audio buffers for instant playback
    private val tapPcm: ShortArray by lazy { generateSweepPcm(800.0, 1300.0, 0.055) }
    private val lapPcm: ShortArray by lazy { generateChordPcm(587.33, 880.0, 0.20) }
    private val collisionPcm: ShortArray by lazy { generateImpactPcm(180.0, 55.0, 0.28) }

    fun playReverse() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(tapPcm)
        }
    }

    fun playLap() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(lapPcm)
        }
    }

    fun playCollision() {
        if (!isSoundEnabled) return
        scope.launch {
            playPcm(collisionPcm)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Release after playback finished
            scope.launch {
                val durationMs = (buffer.size.toFloat() / sampleRate * 1000).toLong() + 50
                kotlinx.coroutines.delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Audio output device busy or unavailable
        }
    }

    private fun generateSweepPcm(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        val twoPi = 2.0 * PI

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val envelope = (1.0 - progress) * (1.0 - progress) // Smooth decay
            phase += twoPi * currentFreq / sampleRate
            val sample = (sin(phase) * envelope * Short.MAX_VALUE * 0.75).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChordPcm(freq1: Double, freq2: Double, durationSec: Double): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val twoPi = 2.0 * PI

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress) * (1.0 - progress)
            phase1 += twoPi * freq1 / sampleRate
            phase2 += twoPi * freq2 / sampleRate
            val tone = 0.5 * sin(phase1) + 0.5 * sin(phase2)
            val sample = (tone * envelope * Short.MAX_VALUE * 0.8).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateImpactPcm(startFreq: Double, endFreq: Double, durationSec: Double): ShortArray {
        val numSamples = (durationSec * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        val twoPi = 2.0 * PI

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val freq = startFreq - (startFreq - endFreq) * progress
            phase += twoPi * freq / sampleRate
            val envelope = (1.0 - progress)
            // Blend triangle wave with subtle noise for punchy tactile impact
            val triangle = (abs((phase / PI) % 2.0 - 1.0) * 2.0 - 1.0)
            val sample = (triangle * envelope * Short.MAX_VALUE * 0.9).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
