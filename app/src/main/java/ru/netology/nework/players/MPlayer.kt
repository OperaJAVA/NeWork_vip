package ru.netology.nework.players

import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

class MPlayer {
    private var player: MediaPlayer? = null

    interface GetInfo {
        fun getDuration(dut: Int)
        fun onCompletionPlay()
    }

    fun play(link: String, dt: GetInfo) {
        try {
            if (player == null) {
                player = MediaPlayer().apply {
                    setDataSource(link)
                    setOnPreparedListener {
                        dt.getDuration(it.duration)
                        it.start()
                    }
                    prepareAsync()
                    setOnCompletionListener {
                        dt.onCompletionPlay()
                    }
                }
            } else {
                player?.start()
            }

        } catch (e: IOException) {
            Log.e("MPlayer", "IOException while playing media", e)
        }
    }

    fun pausePlayer() {
        try {
            if (player?.isPlaying == true) player?.pause()
        } catch (e: IOException) {
            Log.e("MPlayer", "IOException while pausing media", e)
        }
    }

    fun stopPlayer() {
        try {
            player?.release()
            player = null
        } catch (e: IOException) {
            Log.e("MPlayer", "IOException while stopping media", e)
        }
    }
}