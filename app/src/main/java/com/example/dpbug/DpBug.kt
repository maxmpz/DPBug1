package com.example.dpbug

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.DynamicsProcessing
import android.util.Log

private const val TAG = "DpBug"


/** Tries to create DP with the given numBands postEq and returns true on success */
fun createAndDestroyDP(context: Context, numBands: Int): Boolean {
    try {
        val am = context.getSystemService<AudioManager>(AudioManager::class.java)

        Log.w(TAG, "createAndDestroyDP trying numBands=$numBands")

        val cfg = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            2,
            false,
            0,
            false,
            0,
            true,
            numBands,
            false
        ).build()

        val dp = DynamicsProcessing(1, am.generateAudioSessionId(), cfg)

        // Pixel9ProXL @15 BP1A.250305.020:
        // ..
        // getEqBandConfig not able to find channel 0 band 1023
        // handleGetParameter error ret -22, effect_param_t: { status: -22, p: 12 (padded: 12), v: 0, dataAddr: 0xb4000077824e2fdc},
        // paramROffset: 12, valueROffset: 12, paramWOffset: 0, valueWOffset: 12

        val eq = dp.getPostEqByChannelIndex(0)

        Log.w(TAG, "createAndDestroyDP OK requested numBands=$numBands GOT eq.bandCount=${eq.bandCount}")

        val lastBand = eq.getBand(numBands - 1)
        val preLastBand = eq.getBand(numBands - 2)

        lastBand.gain = 10f
        preLastBand.gain = 5f

        // These seemingly change it in DP, but not in the engine.
        // Bands 32-1023 effectively clamped to the single band #31
        eq.setBand(numBands - 1, lastBand)
        eq.setBand(numBands - 2, preLastBand)

        val lastBand2 = eq.getBand(numBands - 1)
        val preLastBand2 = eq.getBand(numBands - 2)

        Log.w(TAG, "createAndDestroyDP lastBand2.gain=${lastBand2.gain} preLastBand2.gain=${preLastBand2.gain}")

        dp.release()

        return true
    } catch(th: Throwable) {
        Log.e(TAG, "FAILED numBands=$numBands", th)
    }
    return false
}


/** Create AudioTrack, attach DP to it, return both or null */
fun createATAndDPAndSetLastBandsTo5and10dB(context: Context, numBands: Int): Pair<AudioTrack, DynamicsProcessing>? {
    var at: AudioTrack? = null
    try {
        Log.w(TAG, "createATAndDP trying numBands=$numBands")

        at = AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(48000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build()).build()
        val cfg = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            2,
            false,
            0,
            false,
            0,
            true,
            numBands,
            false
        ).build()

        // Giving 1234 priority to find it faster in the "dumpsys media.audio_flinger" log
        val dp = DynamicsProcessing(1234, at.audioSessionId, cfg)

        // Pixel9ProXL @15 BP1A.250305.020:
        // ..
        // getEqBandConfig not able to find channel 0 band 1023
        // handleGetParameter error ret -22, effect_param_t: { status: -22, p: 12 (padded: 12), v: 0, dataAddr: 0xb4000077824e2fdc},
        // paramROffset: 12, valueROffset: 12, paramWOffset: 0, valueWOffset: 12

        val eq = dp.getPostEqByChannelIndex(0)

        Log.w(TAG, "createAndDestroyDP OK requested numBands=$numBands GOT eq.bandCount=${eq.bandCount}")

        val lastBand = eq.getBand(numBands - 1)
        val preLastBand = eq.getBand(numBands - 2)

        lastBand.gain = 10f
        preLastBand.gain = 5f

        // These seemingly change it in DP, but not in the engine.
        // Bands 32-1023 effectively clamped to the single band #31
        eq.setBand(numBands - 1, lastBand)
        eq.setBand(numBands - 2, preLastBand)

        val lastBand2 = eq.getBand(numBands - 1)
        val preLastBand2 = eq.getBand(numBands - 2)

        Log.w(TAG, "createAndDestroyDP lastBand2.gain=${lastBand2.gain} preLastBand2.gain=${preLastBand2.gain}")

        return Pair(at, dp)
    } catch(th: Throwable) {
        Log.e(TAG, "FAILED numBands=$numBands", th)
    }
    return null
}
