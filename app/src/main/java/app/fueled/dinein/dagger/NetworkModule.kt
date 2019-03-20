package app.fueled.dinein.dagger

import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * @author Annsh Singh
 *
 * This module is used to provide an instance of All network related methods
 */

@Module(includes = [(ContextModule::class)])
class NetworkModule {

    /**
     * This method provides the instance of Retrofit
     * @param client
     */
    @Provides
    @AppScope
    fun provideRetrofit(client: OkHttpClient?): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.foursquare.com/v2/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(client!!)
        .build()

    /**
     * This method provides the instance of OkHttpClient for logging purposes
     */
    @Provides
    @AppScope
    fun provideInterceptor(): OkHttpClient? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .build()
        return client
    }

    /**
     * This method is used to provide an instance of Picasso
     * @param context Application context
     */
    @Provides
    @AppScope
    fun providePicasso(context: Context): Picasso = Picasso.Builder(context)
        .listener { _, uri, exception -> Log.e("!!!", "uri was {$uri} and exception = $exception", exception) }
        .build()
}
