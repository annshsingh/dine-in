package app.fueled.dinein.utils

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

/**
 * @author Annsh Singh
 *
 * File for extension functions used in the app
 */

/**
 * Extension function for inflaing a layout in Adapters
 */
fun ViewGroup.inflate(layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}

/**
 * Extension functions for Visibility
 */

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

/**
 * Extension function for the delay
 */

fun withDelay(delay: Long, block: () -> Unit) {
    Handler().postDelayed(Runnable(block), delay)
}

/**
 * Extension function for toast
 */

fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

