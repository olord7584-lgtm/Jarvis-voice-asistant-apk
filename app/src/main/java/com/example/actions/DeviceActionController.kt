package com.example.actions

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val health: String
)

data class SystemMemoryInfo(
    val freeMemMB: Long,
    val totalMemMB: Long,
    val usedPercentage: Int
)

class DeviceActionController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var isTorchOn = false

    fun toggleTorch(): Result<Boolean> {
        val cm = cameraManager ?: return Result.failure(IllegalStateException("Camera service unavailable"))
        return try {
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val characteristics = cm.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cm.cameraIdList.firstOrNull() ?: return Result.failure(IllegalStateException("No camera with flash found"))

            isTorchOn = !isTorchOn
            cm.setTorchMode(cameraId, isTorchOn)
            Result.success(isTorchOn)
        } catch (e: Exception) {
            Log.e("DeviceActionController", "Flashlight error", e)
            Result.failure(e)
        }
    }

    fun setTorch(on: Boolean): Result<Boolean> {
        val cm = cameraManager ?: return Result.failure(IllegalStateException("Camera service unavailable"))
        return try {
            val cameraId = cm.cameraIdList.firstOrNull() ?: return Result.failure(IllegalStateException("No camera found"))
            cm.setTorchMode(cameraId, on)
            isTorchOn = on
            Result.success(isTorchOn)
        } catch (e: Exception) {
            Log.e("DeviceActionController", "Torch set error", e)
            Result.failure(e)
        }
    }

    fun getBatteryInfo(): BatteryInfo {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            85
        }

        return BatteryInfo(
            percentage = batteryPct,
            isCharging = isCharging,
            health = if (batteryPct > 20) "Nominal" else "Low"
        )
    }

    fun getMemoryInfo(): SystemMemoryInfo {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalMB = memInfo.totalMem / (1024 * 1024)
        val availMB = memInfo.availMem / (1024 * 1024)
        val usedMB = totalMB - availMB
        val pct = if (totalMB > 0) ((usedMB * 100) / totalMB).toInt() else 42

        return SystemMemoryInfo(
            freeMemMB = availMB,
            totalMemMB = totalMB,
            usedPercentage = pct
        )
    }

    fun getNetworkStatus(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "Sensors Offline"
        val activeNet = cm.activeNetwork ?: return "Disconnected"
        val capabilities = cm.getNetworkCapabilities(activeNet) ?: return "Disconnected"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "High-Bandwidth Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Satellite Uplink"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Direct Interface"
            else -> "Connected"
        }
    }

    fun launchCamera(): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchYouTube(query: String? = null): Boolean {
        return try {
            val uri = if (!query.isNullOrBlank()) {
                Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            } else {
                Uri.parse("https://www.youtube.com")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchMaps(locationQuery: String? = null): Boolean {
        return try {
            val uri = if (!locationQuery.isNullOrBlank()) {
                Uri.parse("geo:0,0?q=${Uri.encode(locationQuery)}")
            } else {
                Uri.parse("geo:0,0?q=current+location")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchWebSearch(query: String): Boolean {
        return try {
            val uri = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTimer(seconds: Int, message: String = "JARVIS Protocol"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun adjustVolume(increase: Boolean): Int {
        val am = audioManager ?: return -1
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        return am.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}
