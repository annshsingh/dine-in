package app.fueled.dinein.utils

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Annsh Singh
 * This class is used to handle the "scroll to top" action speed of various lists
 */

class SpeedyLayoutManager(context: Context?) : LinearLayoutManager(context) {

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?,
                                        position: Int) {

        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@SpeedyLayoutManager.computeScrollVectorForPosition(targetPosition)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
        }

        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    companion object {
        private val MILLISECONDS_PER_INCH = 3f //default is 25f (bigger = slower)
    }
}