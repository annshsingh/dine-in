package app.fueled.dinein.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import app.fueled.dinein.activities.MainActivity
import kotlinx.android.synthetic.main.global_dialog_layout.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Annsh Singh
 *
 * Class with all the Utility functions
 */

object MyUtils {

    /**
     * Method to check if the user device is connected to the internet
     * @param context
     */
    fun isConnectedToInternet(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.state == NetworkInfo.State.CONNECTED
    }

    /**
     * Method to create an Alert Dialog to ask user to grant necessary permission to the App from settings
     * @param context
     * @param setting Name of the setting to be displayed on the alert dialog
     */

    @SuppressLint("SetTextI18n")
    fun showGrantPermissionsDialog(context: Context?, setting: String?) {
        val alertDialogBuilder = AlertDialog.Builder(context!!)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(app.fueled.dinein.R.layout.global_dialog_layout, null)
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(false)
        dialogView.dialog_close.gone()
        dialogView.dialog_image.setImageResource(app.fueled.dinein.R.drawable.ic_error)
        dialogView.dialog_cancel_text.text = "Exit App"
        dialogView.dialog_cancel_button.setOnClickListener {
            alertDialog?.dismiss()
            (context as MainActivity).finish()
            System.exit(0)
        }
        dialogView.dialog_text.text = "Please allow Dine In access to your $setting to use this feature." +
                " This option is available under 'Permissions' in your App settings"

        dialogView.dialog_ok_text.text = "Go to Settings"
        dialogView.dialog_ok_button.setOnClickListener {
            openAppSettings(context)
            alertDialog.dismiss()
        }
        alertDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun openAppSettings(context: Context) {
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:" + context.packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        context.startActivity(i)
    }

    /**
     * Method used to
     *        -> clip latitude and longitude values to use in the Foursquare API
     *        -> clip value in km on feed item on Home Page
     */

    fun clipDecimal(double: Double?): String{
        return DecimalFormat("#.##").format(double)
    }

    /**
     * Method to get the version value used in the Foursquare API
     */

    fun getVersion(): String{
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(System.currentTimeMillis())
    }
}