package app.fueled.dinein.paging

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import app.fueled.dinein.models.VenueItemModel
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit

/**
 * @author Annsh Singh
 *
 * This is the DataSource class for the paged list builder
 * @param latLong The coordinates of user's current location
 * @param version The version Foursquare API requires for authentication
 * @param retrofit Retrofit instance for network calls
 * @param sharedPreferencesEditor To edit values in sharedPreferences
 * @param compositeDisposable A container for other disposables
 */

class FeedDataSource(
    latLong: String?,
    version: String?,
    retrofit: Retrofit,
    sharedPreferencesEditor: SharedPreferences.Editor,
    compositeDisposable: CompositeDisposable) : DataSource.Factory<Unit, VenueItemModel>() {

    /**
     * Create an instance of keyed data source here to handle the return type of this data source class
     * and also to get the value of status to control various UI elements depending upon the
     * value of status
     */
    private var feedKeyedDataSource = FeedPagingDataSource(latLong, version, retrofit, sharedPreferencesEditor,
        compositeDisposable)

    override fun create(): DataSource<Unit, VenueItemModel> {
        return feedKeyedDataSource
    }

    fun getStatus(): LiveData<String>? {
        return feedKeyedDataSource.getStatus()
    }
}