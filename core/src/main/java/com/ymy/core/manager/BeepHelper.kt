package com.ymy.core.manager

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Vibrator
import com.ymy.core.R
import com.ymy.core.permission.Weak
import java.io.IOException


/**
 * Created on 2020/10/8 11:56.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class BeepHelper(context: Context, private val beepRawId: Int = R.raw.alarmcoming) {
    /****************提示声音 震动  发送成功之后显示 */
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep = false
    private val BEEP_VOLUME = 1f
    private var vibrate = false
    private var context by Weak {
        context
    }

    private fun initBeepSound() {
        context?.run {
            if (playBeep && mediaPlayer == null) {
                (context as Activity).volumeControlStream = AudioManager.STREAM_MUSIC
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setOnCompletionListener(beepListener)
                    val file: AssetFileDescriptor =
                        resources.openRawResourceFd(beepRawId)
                    try {
                        setDataSource(file.fileDescriptor, file.startOffset, file.length)
                        file.close()
                        setVolume(BEEP_VOLUME, BEEP_VOLUME)
                        prepare()
                    } catch (e: IOException) {
                        mediaPlayer = null
                    }
                }
            }
        }
    }

    private val beepListener: MediaPlayer.OnCompletionListener =
        MediaPlayer.OnCompletionListener { mediaPlayer -> mediaPlayer.seekTo(0) }

    private val VIBRATE_DURATION = 200L


    fun playBeepSoundAndVibrate() {
        context?.run {
            mediaPlayer?.run {
                if (playBeep) {
                    start()
                }
            }
            if (vibrate) {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VIBRATE_DURATION)
            }
        }
    }


    fun initVoice() {
        context?.run {
            playBeep = true
            val audioService = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                playBeep = false
            }
            initBeepSound()
            vibrate = true
        }
    }
}