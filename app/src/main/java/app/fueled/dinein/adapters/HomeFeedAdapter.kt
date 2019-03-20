package app.fueled.dinein.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.fueled.dinein.R
import app.fueled.dinein.activities.MainActivity
import app.fueled.dinein.database.IsDislikedDao
import app.fueled.dinein.database.RestaurantListModel
import app.fueled.dinein.models.VenueItemModel
import app.fueled.dinein.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.single_home_feed.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author Annsh Singh
 *
 * Adapter for the list on home screen.
 * The list shows restaurants near the user along with other useful information
 * @param mainActivity To get activity context
 * @param picasso [Picasso] instance
 * @param diffCallback Diffutil for the list items
 * @param ioScope [CoroutineScope] to perform operations on other thread (for eg database operations)
 * @param isDislikedDao [IsDislikedDao] instance to access all the CRUD
 * operation methods for [RestaurantListModel] table
 */

class HomeFeedAdapter(
    val mainActivity: MainActivity,
    val picasso: Picasso,
    diffCallback: DiffUtil.ItemCallback<VenueItemModel>,
    val ioScope: CoroutineScope,
    val isDislikedDao: IsDislikedDao
) : PagedListAdapter<VenueItemModel, RecyclerView.ViewHolder>(diffCallback) {

    private var networkState: Boolean? = null
    private val VIEWTYPE_PLACEHOLDER = 0
    private val VIEWTYPE_FEED = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEWTYPE_FEED ->
                HomeFeedAdapter.FeedsViewHolder(
                    parent.inflate(app.fueled.dinein.R.layout.single_home_feed)
                )
            else -> {
                val view = parent.inflate(app.fueled.dinein.R.layout.feed_placeholder)
                HomeFeedAdapter.PlaceHolderViewHolder(view)
            }
        }
    }

    /**
     * Method to set values on various ViewHolder elements
     */

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedsViewHolder) {
            val currentItem = getItem(holder.adapterPosition)

            //setting view to notify user if the user downvoted the restaurant
            isDislikedDao.isDisliked(currentItem?.venue?.id)
                .observe(mainActivity, Observer<Int> { t ->
                    when {
                        t != null -> {
                            holder.hideRecommendation.setOnClickListener {
                                ioScope.launch {
                                    isDislikedDao.delete(currentItem?.venue?.id)
                                }

                                withDelay(500) {
                                    holder.hideIcon.setImageResource(R.drawable.ic_thumbs_down)
                                    holder.hideText.text = "NOT INTERESTED"
                                    holder.hideText.setTextColor(Color.parseColor("#b71c1c"))
                                    holder.topLayout.visible()
                                }
                            }
                            holder.hideIcon.setImageResource(R.drawable.ic_undo)
                            holder.hideText.text = "UNDO (You are not interested in this venue)"
                            holder.hideText.setTextColor(Color.parseColor("#13cb86"))
                            holder.topLayout.gone()
                        }
                        else -> {
                            holder.hideRecommendation.setOnClickListener {
                                ioScope.launch {
                                    isDislikedDao.insertDisliked(currentItem?.venue?.id?.let { id ->
                                        RestaurantListModel(id)
                                    })
                                }
                                withDelay(500) {
                                    holder.hideIcon.setImageResource(R.drawable.ic_undo)
                                    holder.hideText.text = "UNDO (You are not interested in this venue)"
                                    holder.hideText.setTextColor(Color.parseColor("#13cb86"))
                                    holder.topLayout.visible()
                                }
                            }
                            holder.hideIcon.setImageResource(R.drawable.ic_thumbs_down)
                            holder.hideText.text = "NOT INTERESTED"
                            holder.hideText.setTextColor(Color.parseColor("#b71c1c"))
                            holder.topLayout.gone()
                        }
                    }
                })

            picasso.load(
                "${currentItem?.venue?.categories?.get(0)?.icon?.prefix}bg_64" +
                        "${currentItem?.venue?.categories?.get(0)?.icon?.suffix}"
            )
                .into(holder.categoryIcon)

            holder.restaurantName.text = currentItem?.venue?.name
            holder.categoryName.text = currentItem?.venue?.categories?.get(0)?.name
            if (!currentItem?.venue?.location?.crossStreet.isNullOrBlank()) {
                holder.restaurantAddress.text =
                    "${currentItem?.venue?.location?.address} (${currentItem?.venue?.location?.crossStreet})"
            } else if (!currentItem?.venue?.location?.address.isNullOrBlank()) {
                holder.restaurantAddress.text =
                    "${currentItem?.venue?.location?.address}"
            } else {
                holder.restaurantAddress.text =
                    "${currentItem?.venue?.location?.formattedAddress?.get(0)}"
            }
            holder.distance.text =
                "${MyUtils.clipDecimal(currentItem?.venue?.location?.distance?.times(0.001))} km away"

            if (!currentItem?.venue?.location?.lat.isNullOrBlank() && !currentItem?.venue?.location?.lng.isNullOrBlank()) {
                holder.bottomLayout.setOnClickListener {
                    val uri =
                        "https://www.google.com/maps/dir/?api=1&destination=${currentItem?.venue?.location?.lat}," +
                                "${currentItem?.venue?.location?.lng}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    mainActivity.startActivity(intent)
                }
            }

            holder.learnmore.setOnClickListener {
                mainActivity.openMoreDetailsSheet(currentItem?.venue?.id, currentItem?.venue?.name)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * This method is used to set network state to control visibility of placeholder at list's end
     */

    fun setNetworkState(state: Boolean) {
        val previousState = this.networkState
        val previousExtraRow = hasExtraRow()
        this.networkState = state
        val newExtraRow = hasExtraRow()
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(itemCount)

            } else {
                notifyItemInserted(itemCount)

            }
        } else if (newExtraRow && previousState != state) {
            notifyItemChanged(itemCount - 1)
        }
    }

    /**
     * Setting placeholder view ot be shown after every 10 feed when next batch is being loaded
     */
    private class PlaceHolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeHolder = itemView.placeholder_view
    }

    /**
     * View Holder for individual feed item to access layout elements
     * It extends
     *      -> LayoutContainer - This is to use Kotlin android extension for views
     * To use this add this ->
     *      androidExtensions {
     *           experimental = true
     *      }
     * to build.gradle file
     */

    class FeedsViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val topLayout: ConstraintLayout = top_layout
        val categoryIcon: AppCompatImageView = category_icon
        val restaurantName: TextView = restaurant_name
        val categoryName: TextView = category
        val restaurantAddress: TextView = address
        val distance: TextView = distance_text
        val bottomLayout: RelativeLayout = bottom_layout
        val learnmore: TextView = learn_more
        val hideRecommendation: LinearLayout = hide_recommendation
        val hideIcon: AppCompatImageView = hide_icon
        val hideText: TextView = hide_text
    }

    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState != true
    }

    /**
     * Method to differentiate between the views
     *      -> Placeholder
     *      -> Feed item
     */
    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            VIEWTYPE_PLACEHOLDER
        } else {
            VIEWTYPE_FEED
        }
    }
}