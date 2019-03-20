package app.fueled.dinein.network

import app.fueled.dinein.models.DetailsModel
import app.fueled.dinein.models.FeedModel
import io.reactivex.Observable
import retrofit2.http.*

/**
 * @author Annsh Singh
 *
 * This is the interface for Retrofit to make network calls - GET POST PUT etc
 *
 */

interface FeedInterface {

    /**
     * This method is used by the retrofit instance to fetch list of feed from the network
     * @param coordinates Location coordinates required to fetch feed
     * @param category Category to which the list items must belong to
     * @param offset Used to page through results
     * @param limit Number of results to return in the response
     * @param clientId Required by Foursquare API for authentication
     * @param clientSecret Required by Foursquare API for authentication
     * @param version Required by Foursquare API for authentication
     */

    @Headers("Accept: application/json")
    @GET("venues/explore")
    fun getFeed(@Query("ll") coordinates: String?,
                        @Query("section") category: String?,
                        @Query("offset") offset: Int?,
                        @Query("limit") limit: Int?,
                        @Query("client_id") clientId: String?,
                        @Query("client_secret") clientSecret: String?,
                        @Query("v") version: String?): Observable<FeedModel>

    /**
     * This method is used by the retrofit instance to fetch venue details from the network
     * @param id Venue id for which the user wants to fetch more details for
     * @param clientId Required by Foursquare API for authentication
     * @param clientSecret Required by Foursquare API for authentication
     * @param version Required by Foursquare API for authentication
     */
    @Headers("Accept: application/json")
    @GET("venues/{id}")
    fun getDetails(@Path("id") id: String?,
                @Query("client_id") clientId: String?,
                @Query("client_secret") clientSecret: String?,
                @Query("v") version: String?): Observable<DetailsModel>
}