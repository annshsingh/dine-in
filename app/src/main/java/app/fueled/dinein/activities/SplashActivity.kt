package app.fueled.dinein.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import app.fueled.dinein.MyApplication
import app.fueled.dinein.R
import app.fueled.dinein.utils.visible
import kotlinx.android.synthetic.main.activity_splash.*
import javax.inject.Inject

/**
 * @author Annsh Singh
 *
 * This activity will be show to the user every time the user opens up the app.
 * Here we check if the user has visited the app for the first time and direct
 * the user accordingly.
 */

class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var showMainTextAnim: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MyApplication.getComponent()
            .injectSplashActivity(this)

        checkIfFirstTime()
        startAnimation()
    }

    /**
     * Method to decide whether to show onBoarding screen or not
     */

    private fun checkIfFirstTime() {
        if(sharedPreferences.getBoolean("isFirstTime", true)){
            Handler().postDelayed({
            val intent = Intent(this, OnBoarding::class.java)
            startActivity(intent)
            finish()
            }, 1500)
        }else{
            Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            }, 1500)
        }
    }

    /**
     * Mehtod to set up the animation
     */

    private fun startAnimation() {
        text_icon.visible()
        //setting up an animation on the main logo
        showMainTextAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in_splash)
        text_icon.startAnimation(showMainTextAnim)
    }
}
