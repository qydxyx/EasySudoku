package com.antigravity.sudoku.data.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * 触觉振动与轻量即时合成音效管理器
 * 无需外部臃肿资源，纯代码合成柔和清脆的声音，支持动态开关
 */
class HapticAndSoundManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    // ==========================================
    // 触觉反馈 (Haptics)
    // ==========================================

    fun vibrateClick() {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    fun vibrateSuccess() {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 60, 45)
            val amplitudes = intArrayOf(0, 160, 0, 220)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(80)
        }
    }

    fun vibrateError() {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 50, 40)
            val amplitudes = intArrayOf(0, 220, 0, 220)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120)
        }
    }

    // ==========================================
    // 轻量合成音效 (Synthesized Audio)
    // ==========================================

    fun playNumberSound() {
        if (!soundEnabled) return
        // 587 Hz (D5) 短促清脆的正弦波带平滑衰减
        playTone(frequency = 587.33, durationMs = 45, volume = 0.28f)
    }

    fun playNoteSound() {
        if (!soundEnabled) return
        // 880 Hz (A5) 极短轻微敲击声
        playTone(frequency = 880.0, durationMs = 25, volume = 0.15f)
    }

    fun playEraseSound() {
        if (!soundEnabled) return
        // 330 Hz (E4) 低调柔和擦除音
        playTone(frequency = 329.63, durationMs = 50, volume = 0.25f)
    }

    fun playErrorSound() {
        if (!soundEnabled) return
        // 220 Hz 警示低音
        playTone(frequency = 220.0, durationMs = 120, volume = 0.35f)
    }

    fun playVictoryChord() {
        if (!soundEnabled) return
        audioScope.launch {
            // C 大三和弦琶音 C5 -> E5 -> G5 -> C6
            val notes = listOf(523.25, 659.25, 783.99, 1046.50)
            for (freq in notes) {
                launch { playToneDirect(freq, durationMs = 120, volume = 0.32f) }
                delay(80)
            }
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        audioScope.launch {
            playToneDirect(frequency, durationMs, volume)
        }
    }

    private suspend fun playToneDirect(frequency: Double, durationMs: Int, volume: Float) {
        var audioTrack: AudioTrack? = null
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val sinValue = sin(2.0 * Math.PI * frequency * time)
                // 3ms 平滑淡入 + 线性衰减封套，消除起音爆音
                val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
                val decay = 1.0 - (i.toDouble() / numSamples)
                val sample = (sinValue * attack * decay * Short.MAX_VALUE * volume).toInt().toShort()
                buffer[i] = sample
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay(durationMs.toLong() + 20)
            audioTrack.stop()
        } catch (_: Exception) {
            // 静默处理音频通道异常
        } finally {
            runCatching { audioTrack?.release() }
        }
    }
}
