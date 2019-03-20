package app.fueled.dinein

import android.app.Activity
import android.app.Application
import app.fueled.dinein.dagger.MyComponent
import app.fueled.dinein.dagger.ContextModule
import app.fueled.dinein.dagger.DaggerMyComponent

/**
 * @author Annsh Singh
 *
 * Application class created to setup dagger and provide an
 * access to [MyComponent] interface to views for injection
 */

class MyApplication : Application() {
    companion object {
        lateinit var myComponent: MyComponent

        //to get instance of application in a particular activity
        fun get(activity: Activity): MyApplication {
            return activity.application as MyApplication
        }

        //to get the component anywhere in the app
        fun getComponent(): MyComponent {
            return myComponent
        }
    }

    override fun onCreate() {
        super.onCreate()
        //Only include the modules with constructors
        //No need to include modules without constructors
        myComponent = DaggerMyComponent.builder()
            .contextModule(ContextModule(this))
            .build()
    }
}