package app.fueled.dinein.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import app.fueled.dinein.models.DetailsModel
import app.fueled.dinein.models.VenueItemModel
import app.fueled.dinein.network.FeedInterface
import app.fueled.dinein.paging.FeedDataSource
import app.fueled.dinein.repositories.FeedRepository
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * @author Annsh Singh
 *
 * This is the ViewModel class for the MainActivity
 * It handles the data flow from datasource to the view
 * @param retrofit [Retrofit] instance for network calls
 * @param sharedPreferencesEditor [SharedPreferences.Editor] instance to edit values in sharedPreferences
 * @param compositeDisposable A container for other disposables
 */

class FeedViewModel @Inject constructor(
    val retrofit: Retrofit,
    var sharedPreferencesEditor: SharedPreferences.Editor,
    var compositeDisposable: CompositeDisposable
) : ViewModel() {

    var status: LiveData<String>? = null

    var detailStatus = MutableLiveData<String>()

    var venueDetails = MutableLiveData<DetailsModel>()

    //Paged list configuration for home feed
    private var pagelistConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setInitialLoadSizeHint(10)
        .setPageSize(10)
        .build()

    /**
     * Mehtod to fetch data from network and create a [PagedList]
     * @param latLong Coordinates required by the Foursquare API
     * @param version Version value required by the Foursquare API for authentication
     *
     * @return [LivePagedListBuilder] that can be observed on the view
     */

    fun getFeedFromNetwork(
        latLong: String?,
        version: String?): LiveData<PagedList<VenueItemModel>> {
        val feedDataSource = FeedDataSource(
                latLong, version, retrofit, sharedPreferencesEditor,
                compositeDisposable)
        status = feedDataSource.getStatus()
        return LivePagedListBuilder(feedDataSource, pagelistConfig)
            .build()

    }

    /**
     * Method to get more details for a particular value
     * @param venueId Id of the venue for which user wants to fetch more details
     * @param version Version value required by the Foursquare API for authentication
     */

    fun getMoreDetailsFromNetwork(venueId: String?, version: String?) {
        detailStatus.postValue("loading")
        retrofit.create(FeedInterface::class.java)
            .getDetails(
                id = venueId,
                clientId = "ZY0LXPXG3RJERMXZTLPZEP4GGFT4IFO4KGPCHR4W4HK4ZUM1",
                clientSecret = "DA1FHUVENJBZ5125KIPA5VS5JW1SENELATIQR5NGS2IBC0WS",
                version = version
            )
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(object : Observer<DetailsModel> {
                override fun onComplete() {
                    detailStatus.postValue("completed")
                }

                override fun onError(e: Throwable) {
                    detailStatus.postValue("quota error")
                }

                override fun onNext(t: DetailsModel) {
                    when {
                        t.meta.code == 200 -> venueDetails.postValue(t)
                        t.meta.code == 429 -> detailStatus.postValue("quota error")
                        else -> detailStatus.postValue("error")
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }
            })
    }

    /**
     * Method called after the activity is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}