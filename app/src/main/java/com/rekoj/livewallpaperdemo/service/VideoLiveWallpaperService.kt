package com.rekoj.livewallpaperdemo.service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import com.rekoj.livewallpaperdemo.opengl.MyBaseRenderer
import java.io.IOException

class VideoLiveWallpaperService : WallpaperService() {
    internal inner class VideoEngine : Engine() {
        private var broadcastReceiver: BroadcastReceiver? = null
        private var glSurfaceView: WallpaperGLSurfaceView? = null
        private var rendererSet = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            val supportsEs3 = configurationInfo.reqGlEsVersion >= 0x30000

            if (supportsEs3) {
                val myRenderer = MyBaseRenderer(this@VideoLiveWallpaperService)
                glSurfaceView = WallpaperGLSurfaceView(this@VideoLiveWallpaperService)
                glSurfaceView?.setEGLContextClientVersion(3)
                glSurfaceView?.setRenderer(myRenderer)
                rendererSet = true
            } else {
                Toast.makeText(
                    this@VideoLiveWallpaperService, "This device does not support OpenGL ES 3.0.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        inner class WallpaperGLSurfaceView(context: Context) : GLSurfaceView(context) {
            init {
                setEGLContextClientVersion(3)
            }

            override fun getHolder(): SurfaceHolder {
                return super.getHolder()
            }

            fun onWallpaperDestroy() {
                super.onDetachedFromWindow()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.e("DatPV", rendererSet.toString())
            if (rendererSet) {
                glSurfaceView?.onResume()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (rendererSet) {
                if (visible) {
                    glSurfaceView?.onResume()
                } else {
                    glSurfaceView?.onPause()
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            if (rendererSet) {
                glSurfaceView?.onPause()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            glSurfaceView?.onWallpaperDestroy()
            unregisterReceiver(broadcastReceiver)
        }
    }

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    companion object {
        const val VIDEO_PARAMS_CONTROL_ACTION = "moe.cyunrei.livewallpaper"
        private const val KEY_ACTION = "music"
        private const val ACTION_MUSIC_UNMUTE = false
        private const val ACTION_MUSIC_MUTE = true

        fun muteMusic(context: Context) {
            Intent(VIDEO_PARAMS_CONTROL_ACTION).apply {
                putExtra(KEY_ACTION, ACTION_MUSIC_MUTE)
            }.also { context.sendBroadcast(it) }
        }

        fun unmuteMusic(context: Context) {
            Intent(VIDEO_PARAMS_CONTROL_ACTION).apply {
                putExtra(KEY_ACTION, ACTION_MUSIC_UNMUTE)
            }.also {
                context.sendBroadcast(it)
            }
        }

        fun setToWallPaper(context: Context) {
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, VideoLiveWallpaperService::class.java)
                )
            }.also {
                context.startActivity(it)
            }
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}