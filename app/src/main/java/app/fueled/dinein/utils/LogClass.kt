package app.fueled.dinein.utils

import android.util.Log
import app.fueled.dinein.BuildConfig

/**
 * @author Annsh Singh
 *
 * Class being used to perform all the logging operations in the application
 * Note: Logs will all be displayed in the error section
 */
object LogClass {
    fun displayLog(TAG: String, Message: String) {
        if (BuildConfig.DEBUG) {
            try {
                Log.e(TAG, Message)
            } catch (e: Exception) {
                Log.e("LogClass", e.message)
            }

        }
    }
}
