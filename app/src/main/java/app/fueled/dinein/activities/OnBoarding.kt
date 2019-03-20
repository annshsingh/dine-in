package app.fueled.dinein.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import app.fueled.dinein.R
import com.ramotion.paperonboarding.PaperOnboardingFragment
import com.ramotion.paperonboarding.PaperOnboardingPage

/**
 * @Author Annsh Singh
 * @see <a href="https://github.com/Ramotion/paper-onboarding-android">PaperOnboarding by Ramotion</a>
 *
 * OnBoarding class for a basic app introduction
 * This Activity will be shown to the user only once.
 */

class OnBoarding : AppCompatActivity() {

    private val elements = ArrayList<PaperOnboardingPage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //This must be called before adding content
        //Setting screen to be in full screen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(app.fueled.dinein.R.layout.activity_onboarding)
        prepareData()
        setUpViews()
    }

    /**
     * Method to prepare data for the OnBoarding pages
     */

    private fun prepareData() {
        val scr1 = PaperOnboardingPage(
           getString(R.string.onBoardingOneTitle),
            getString(R.string.onBoardingOneSubTitle),
            Color.parseColor("#ffffff"), R.drawable.ic_onboarding_one, R.drawable.ic_obkey_one
        )
        val scr2 = PaperOnboardingPage(
            getString(R.string.onBoardingTwoTitle),
            getString(R.string.onBoardingTwoSubTitle),
            Color.parseColor("#ffffff"), R.drawable.ic_onboarding_two, R.drawable.ic_obkey_two
        )
        val scr3 = PaperOnboardingPage(
            getString(R.string.onBoardingThreeTitle),
            getString(R.string.onBoardingThreeSubTitle),
            Color.parseColor("#ffffff"), R.drawable.ic_onboarding_three, R.drawable.ic_obkey_three
        )

        elements.add(scr1)
        elements.add(scr2)
        elements.add(scr3)
    }

    /**
     * Mehtod to set OnBoarding pages on the activity and
     * also defining any gesture listeners if any.
     */

    private fun setUpViews() {
        val onBoardingFragment = PaperOnboardingFragment.newInstance(elements)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(android.R.id.content, onBoardingFragment)
        fragmentTransaction.commit()

        onBoardingFragment.setOnRightOutListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
