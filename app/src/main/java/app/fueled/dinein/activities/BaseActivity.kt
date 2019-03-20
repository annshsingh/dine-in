package app.fueled.dinein.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

/**
 * @author Annsh Singh
 *
 * Every activity must extend this activity if it wants to notifiy user of network changes
 */

abstract class BaseActivity : AppCompatActivity() {

    private var networkErrorSnackbar: Snackbar? = null
    private var networkConnectedSnackbar: Snackbar? = null
    var context: Context? = null
    private var errorMessageEnabled = false
    private var connectedMessageEnabled = false
    private var errorMessageParentView: View? = null


    /**
     * This method should be called once in onCreate() to setup the error Snackbar.
     * @param context to set an action to open the wifi settings from the snackbar itself
     * activities to let the user open wifi settings)
     */
    fun enableErrorMessageSnackbar(context: Context) {
        this.context = context
        if (!errorMessageEnabled) {
            errorMessageEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        registerNetworkCheckReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkStatusBroadcastReceiver)
    }

    /**
     * Registers a BroadcastReceiver with filters for network state.
     * This can also be done in manifest file for a broadcast receiver.
     * But we have done this here to keep it all together
     */
    private fun registerNetworkCheckReceiver() {
        val networkStatusFilter = IntentFilter()
        //Broadcast intent action indicating that the state of Wi-Fi connectivity has changed
        networkStatusFilter.addAction("android.net.wifi.STATE_CHANGE")
        //A change in network connectivity has occurred. A default connection has either been established or lost.
        networkStatusFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        //Register a broadcast receiver with filters for network state
        registerReceiver(networkStatusBroadcastReceiver, networkStatusFilter)
    }

    /**
     * BroadcastReceiver to handle changes in network state.
     */
    private val networkStatusBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (NetworkUtil.isNetworkConnected(context)) {
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                hideErrorMessageSnackbar()
                showConnectedMessageSnackbar()
                // hideConnectedMessageSnackbar()
            } else {
                //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                showErrorMessageSnackbar()
            }
        }
    }

    /**
     * Shows the error message Snackbar, if enabled
     */
    private fun showErrorMessageSnackbar() {
        errorMessageEnabled = true
        if (errorMessageEnabled) {
            initializeNetworkErrorSnackbar()
            networkErrorSnackbar?.show()
        }
    }

    private fun showConnectedMessageSnackbar() {
        if (errorMessageEnabled && connectedMessageEnabled) {
            initializeNetworkConnectedSnackbar()
            errorMessageEnabled = false
            networkConnectedSnackbar?.show()
        }
    }

    /**
     * Hides the error message Snackbar if currently shown
     */
    private fun hideErrorMessageSnackbar() {
        if (networkErrorSnackbar != null && networkErrorSnackbar!!.isShown) {
            connectedMessageEnabled = true
            networkErrorSnackbar?.dismiss()
        }
    }

//    private fun hideConnectedMessageSnackbar() {
//        if (networkConnectedSnackbar != null && networkConnectedSnackbar!!.isShown) {
//            networkConnectedSnackbar?.dismiss()
//        }
//    }

    /**
     * Creates an instance of the error Snackbar if none exists
     */
    private fun initializeNetworkErrorSnackbar() {
        if (networkErrorSnackbar == null) {
            networkErrorSnackbar =
                    DesignUtil.makeErrorMessageSnackbar(getErrorMessageDisplayView(), context!!)
        }
    }

    private fun initializeNetworkConnectedSnackbar() {
        if (networkConnectedSnackbar == null) {
            networkConnectedSnackbar =
                    DesignUtil.makeConnectedMessageSnackbar(errorMessageParentView!!)
        }
    }

    /**
     * Gets the view the Snackbar should be displayed in
     * @return The root view of this activity
     */
    private fun getErrorMessageDisplayView(): View {
        if (errorMessageParentView == null) {
            errorMessageParentView = DesignUtil.getRootViewFromActivity(this)
        }
        return errorMessageParentView!!
    }

    /**
     * Provides utility methods for networking.
     */
    object NetworkUtil {
        /**
         * Checks if the device is currently connected to the network.
         * @param context Context of the activity from which this is being called
         * @return Whether the network state is connected or not
         */
        fun isNetworkConnected(context: Context?): Boolean {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val ni = cm.activeNetworkInfo
            return ni != null && ni.isConnected
        }
    }

    /**
     * Here we design our snackbar and we get the root view of a particular activity
     */
    object DesignUtil {
        /**
         * Creates a Snackbar for displaying error messages with red background, white text, and indefinite display length.
         * @param view View to create the Snackbar within
         * @param context Context of the activity
         * @return An error Snackbar with the provided message
         */
        fun makeErrorMessageSnackbar(view: View, context: Context): Snackbar {
            val errorMessageSnackbar =
                    Snackbar.make(view, "No internet connection", Snackbar.LENGTH_INDEFINITE)
            val snackbarView = errorMessageSnackbar.view
            snackbarView.setBackgroundColor((Color.parseColor("#7D0708")))
            val textView = snackbarView.findViewById<View>(
                com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)

            errorMessageSnackbar.setAction("SETTINGS") {
                val wifiSettingsIntent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                context.startActivity(wifiSettingsIntent)
            }
            errorMessageSnackbar.setActionTextColor(Color.WHITE)


            return errorMessageSnackbar
        }

        fun makeConnectedMessageSnackbar(view: View): Snackbar {
            val connectedMessageSnackbar = Snackbar.make(view, "Connected", Snackbar.LENGTH_SHORT)
            val snackbarView = connectedMessageSnackbar.view
            snackbarView.setBackgroundColor((Color.parseColor("#13cb86")))
            val textView = snackbarView.findViewById<View>(
                com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)
            return connectedMessageSnackbar
        }

        /**
         * Gets the root view from an activity.
         * @param activity Activity to get root view from
         * @return The root view from the given activity
         */
        fun getRootViewFromActivity(activity: Activity): View {
            return activity.window.decorView.findViewById(android.R.id.content)
        }
    }
}