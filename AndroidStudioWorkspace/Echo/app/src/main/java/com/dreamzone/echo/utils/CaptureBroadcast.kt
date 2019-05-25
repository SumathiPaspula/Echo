package com.dreamzone.echo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.dreamzone.echo.MainActivity
import com.dreamzone.echo.R
import com.dreamzone.echo.fragments.PlayingFragment

class CaptureBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            intent?.action == Intent.ACTION_NEW_OUTGOING_CALL -> {
                try {
                    MainActivity.Statified.notificationManager?.cancel(1111)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                try {
                    if (PlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                        PlayingFragment.Statified.mediaPlayer?.pause()
                        PlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                when (tm.callState) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        try {
                            MainActivity.Statified.notificationManager?.cancel(1111)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        try {
                            if (PlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                                PlayingFragment.Statified.mediaPlayer?.pause()
                                PlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }
}