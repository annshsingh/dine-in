package app.fueled.dinein.paging

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import app.fueled.dinein.models.FeedModel
import app.fueled.dinein.models.VenueItemModel
import app.fueled.dinein.network.FeedInterface
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit

/**
 * @author Annsh Singh
 *
 * This is the data loader class for paged list that is shown on the home screen
 * @param latLong The coordinates of user's current location
 * @param version The version Foursquare API requires for authentication
 * @param retrofit Retrofit instance for network calls
 * @param sharedPreferencesEditor To edit values in sharedPreferences
 * @param compositeDisposable A container for other disposables
 */
class FeedPagingDataSource(
    val latLong: String?,
    val version: String?,
    val retrofit: Retrofit,
    val sharedPreferencesEditor: SharedPreferences.Editor,
    val compositeDisposable: CompositeDisposable
) : ItemKeyedDataSource<Unit, VenueItemModel>() {


    //To keep track of the API response
    private var status = MutableLiveData<String>()

    var i: Int = 0

    /**
     * Method called to load data before the first element in the list
     */
    override fun loadBefore(params: LoadParams<Unit>, callback: LoadCallback<VenueItemModel>) {
        //do nothing
    }


    /**
     * Method called to load initial data to be shown
     */
    override fun loadInitial(
        params: LoadInitialParams<Unit>,
        callback: LoadInitialCallback<VenueItemModel>
    ) {
        status.postValue("initial loading")
        retrofit.create(FeedInterface::class.java)
            .getFeed(
                coordinates = latLong,
                category = "feed",
                offset = i,
                limit = 10,
                clientId = "ZY0LXPXG3RJERMXZTLPZEP4GGFT4IFO4KGPCHR4W4HK4ZUM1",
                clientSecret = "DA1FHUVENJBZ5125KIPA5VS5JW1SENELATIQR5NGS2IBC0WS",
                version = version
            )
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(object : io.reactivex.Observer<FeedModel> {
                override fun onComplete() {
                    status.postValue("completed")
                }

                override fun onError(e: Throwable) {
                    status.postValue("error initial")
                }

                override fun onNext(feed: FeedModel) {
                    if (feed.meta.code == 200) {
                        callback.onResult(feed.response.groups[0].items)
                        sharedPreferencesEditor.putInt("totalResults", feed.response.totalResults).commit()
                    } else {
                        status.postValue("error initial")
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }
            })
    }

    /**
     * Method called to load the data after the initial load is completed and the user scrolls
     * further down
     */

    override fun loadAfter(params: LoadParams<Unit>, callback: LoadCallback<VenueItemModel>) {
        status.postValue("loading after")
        i += 10
        retrofit.create(FeedInterface::class.java)
            .getFeed(
                coordinates = latLong,
                category = "feed",
                offset = i,
                limit = 10,
                clientId = "ZY0LXPXG3RJERMXZTLPZEP4GGFT4IFO4KGPCHR4W4HK4ZUM1",
                clientSecret = "DA1FHUVENJBZ5125KIPA5VS5JW1SENELATIQR5NGS2IBC0WS",
                version = version
            )
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(object : io.reactivex.Observer<FeedModel> {
                override fun onComplete() {
                    status.postValue("completed")
                }

                override fun onError(e: Throwable) {
                    status.postValue("error after")
                }

                override fun onNext(feed: FeedModel) {
                    if (feed.meta.code == 200) {
                        callback.onResult(feed.response.groups[0].items)
                    } else {
                        status.postValue("error after")
                    }
                }

                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }
            })

    }

    /**
     * This method is used to use an item property as a way to fetch the next set of data in loadAfter
     * or load Before methods. In our case we use an integer value (defined above as i) to fetch data
     * Hence return type is Unit
     */
    override fun getKey(item: VenueItemModel) {

    }

    /**
     * Method to return the status value
     */

    fun getStatus(): LiveData<String> {
        return status
    }
}