package app.fueled.dinein.dagger

import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * @author Annsh Singh
 *
 * Here we provide the application context of My Application
 * @param context Application context
 */

@Module
class ContextModule(val context: Context) {
    @Provides
    @AppScope
    fun providesContext(): Context = context
}