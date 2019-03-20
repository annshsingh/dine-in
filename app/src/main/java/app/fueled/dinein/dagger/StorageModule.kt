package app.fueled.dinein.dagger

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.recyclerview.widget.DiffUtil
import app.fueled.dinein.database.IsDislikedDao
import app.fueled.dinein.database.MyDatabase
import app.fueled.dinein.models.VenueItemModel
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable


/**
 * @author Annsh Singh
 *
 * Here we provide the instance of our main database along with
 * all the DAOs that are in our application and the Diffcallbacks logic required
 * to handle data in Paging
 */

@Module(includes = [(ContextModule::class)])
class StorageModule {

    /**
     * This method provides the instance of SharedPreferences
     * @param context Application context
     */
    @Provides
    @AppScope
    fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("default", Context.MODE_PRIVATE)

    /**
     *T his method provides the instance of SharedPreferencesEditor
     * @param context Application context
     */
    @SuppressLint("CommitPrefEdits")
    @Provides
    @AppScope
    fun getSharedPrefrencesEditor(context: Context): SharedPreferences.Editor =
        getSharedPreferences(context).edit()

    /**
     * This method provides the instance of MyDatabase
     * @param context Application context
     */
    @Provides
    @AppScope
    fun provideMyDatabase(context: Context): MyDatabase = MyDatabase.getDatabase(context)

    /**
     * This method provides the instance of IsDislikedDao
     * @param myDatabase [MyDatabase] instance
     */
    @Provides
    @AppScope
    fun provideIsDislikedDao(
        myDatabase: MyDatabase
    ): IsDislikedDao = myDatabase.getIsDislikedDao()

    /**
     * This method provides the instance of DiffUtil callback for the type VenueItemModel
     */
    @Provides
    @AppScope
    fun provideYourFeedCallBack(): DiffUtil.ItemCallback<VenueItemModel> {
        return object : DiffUtil.ItemCallback<VenueItemModel>() {
            override fun areContentsTheSame(oldItem: VenueItemModel, newItem: VenueItemModel): Boolean {
                return oldItem.venue.id == newItem.venue.id
            }

            override fun areItemsTheSame(oldItem: VenueItemModel, newItem: VenueItemModel): Boolean {
                return oldItem.equals(newItem)
            }
        }
    }

    /**
     * Provides an instance of composite disposable
     */
    @Provides
    @AppScope
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }
}