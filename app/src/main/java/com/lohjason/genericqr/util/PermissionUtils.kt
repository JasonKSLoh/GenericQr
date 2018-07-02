package com.lohjason.genericqr.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import com.lohjason.genericqr.BuildConfig

/**
 * PermissionUtils
 * Created by jason on 25/6/18.
 */
class PermissionUtils {

    companion object {
        const val REQUEST_CODE_CAMERA = 101

        fun hasCameraPermission(context: Context):Boolean{
            val hasPermission: Int = if(Build.VERSION.SDK_INT < 23){
                PermissionChecker.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            } else {
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            }
            return hasPermission == PackageManager.PERMISSION_GRANTED
        }

        fun requestCameraPermission(appCompatActivity: AppCompatActivity){
            if(Build.VERSION.SDK_INT >= 23){
                ActivityCompat.requestPermissions(appCompatActivity, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
            }
        }

        fun canRequestCameraPermission(appCompatActivity: AppCompatActivity): Boolean{
            return if(Build.VERSION.SDK_INT >= 23){
                ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, android.Manifest.permission.CAMERA)
            } else {
                false
            }
        }

        fun openSettingsPage(appCompatActivity: AppCompatActivity){
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            appCompatActivity.startActivity(intent)


        }
    }

}