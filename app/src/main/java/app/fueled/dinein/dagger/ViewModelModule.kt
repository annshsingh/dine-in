package app.fueled.dinein.dagger

import android.content.SharedPreferences
import app.fueled.dinein.repositories.FeedRepository
import app.fueled.dinein.viewmodels.FeedViewModel
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit

/**
 * @author Annsh Singh
 *
 * This module consists of all the ViewModels listed in this app and
 * does the work of providing instances of them
 */
@Module(includes = [(RepositoryModule::class)])
class ViewModelModule {

    /**
     * This is to provide instance of FeedViewModel
     * @param feedRepository
     * @param retrofit
     * @param sharedPreferencesEditor
     * @param compositeDisposable
     */

    @Provides
    @AppScope
    fun provideFeedViewModel(
        retrofit: Retrofit,
        sharedPreferencesEditor: SharedPreferences.Editor,
        compositeDisposable: CompositeDisposable): FeedViewModel = FeedViewModel(retrofit, sharedPreferencesEditor,
        compositeDisposable
    )
}