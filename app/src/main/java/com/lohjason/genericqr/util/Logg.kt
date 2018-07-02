package com.lohjason.genericqr.util

import android.util.Log
import com.lohjason.genericqr.BuildConfig

/**
 * Logg
 * Created by jason on 27/6/18.
 */
class Logg{
    companion object {
        private val shouldShowLogs = BuildConfig.DEBUG

        fun d(tag: String, message: String?, throwable: Throwable?){
            if(shouldShowLogs){
                Log.d(tag, message, throwable)
            }
        }

        fun d(tag: String, message: String?){
            Logg.d(tag, message, null)
        }

        fun e(tag: String, message: String?, throwable: Throwable?){
            if(shouldShowLogs){
                Log.e(tag, message, throwable)
            }
        }

        fun e(tag: String, message: String?){
            Logg.e(tag, message, null)
        }
    }
}