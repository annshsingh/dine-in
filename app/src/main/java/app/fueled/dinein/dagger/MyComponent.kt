package app.fueled.dinein.dagger
import app.fueled.dinein.activities.MainActivity
import app.fueled.dinein.activities.SplashActivity
import dagger.Component

/**
 * @author Annsh Singh
 * @modules - List of all the Dagger modules
 *
 * This interface consists of all the activities/fragments where injection
 * of methods provided by dagger is required
 */

@AppScope
@Component(modules = [ContextModule::class, NetworkModule::class, RepositoryModule::class, StorageModule::class, ViewModelModule::class])
interface MyComponent {

    fun injectSplashActivity(splashActivity: SplashActivity)

    fun injectMainActivity(mainActivity: MainActivity)
}