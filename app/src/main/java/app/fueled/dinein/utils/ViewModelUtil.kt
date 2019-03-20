package app.fueled.dinein.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Creates a one off view model factory for the given view model instance.
 * I used the code provided in the official android documentation for ViewModel here
 */

object ViewModelUtil {
    fun <T : ViewModel> createFor(model: T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(model.javaClass)) {
                    return model as T
                }
                throw IllegalArgumentException("unexpected model class $modelClass")
            }
        }
    }
}