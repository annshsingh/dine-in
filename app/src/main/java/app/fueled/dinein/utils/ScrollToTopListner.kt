package app.fueled.dinein.utils

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import app.fueled.dinein.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @author Annsh Singh
 *
 * Class that extends OnScrollListner of [RecyclerView]
 * Here we control the visibility of the [FloatingActionButton] that is shown
 * when the user scrolls UP on the list on home screen and is hidden when
 * the user is scrolling down.
 *
 * Clicking on the fab will take the user to the top of the list without any delay
 */

open class ScrollToTopListner constructor(context: Context?) :
        RecyclerView.OnScrollListener() {

    private val IMMEDIATELY = 0
    private val DELAYED = 1
    private val fab: FloatingActionButton = (context as AppCompatActivity).findViewById(R.id.fab_scroll_up)


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        //Look for changes in the Fab behavior
        try {
            updateFab(dy)
        } catch (e: Exception) {
        }

    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (RecyclerView.SCROLL_STATE_IDLE == newState) {
            try {
                hideFab(DELAYED)
            } catch (e: Exception) {
            }

        }
    }

    @Throws(Exception::class)
    private fun updateFab(dy: Int) {
        if (dy >= 0) {
            hideFab(IMMEDIATELY)
        } else {
            fab.show()
        }
    }

    @Throws(Exception::class)
    private fun hideFab(TIME: Int) {
        if (TIME == IMMEDIATELY && fab.visibility == View.VISIBLE) {
            fab.hide()
        } else if (TIME == DELAYED && fab.visibility == View.VISIBLE) {
            Handler().postDelayed({ fab.hide() }, 500)
        }
    }
}