package app.fueled.dinein.activities

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.text.bold
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.fueled.dinein.MyApplication
import app.fueled.dinein.R
import app.fueled.dinein.adapters.HomeFeedAdapter
import app.fueled.dinein.database.IsDislikedDao
import app.fueled.dinein.models.VenueItemModel
import app.fueled.dinein.utils.*
import app.fueled.dinein.viewmodels.FeedViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.global_dialog_layout.view.*
import kotlinx.android.synthetic.main.scroll_up_fab.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*
import javax.inject.Inject

/**
 * @author Annsh Singh
 *
 * This is the main screen of the app with which the user interacts
 * It extends
 *      -> BaseActivity - To capture network events and notify user of the same
 *      -> LocationListner - To detect user's location to fetch data from network
 */

class MainActivity : BaseActivity(), LocationListener {

    /* All the injected dependencies */
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @Inject
    lateinit var feedViewModel: FeedViewModel

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var isDislikedDao: IsDislikedDao

    @Inject
    lateinit var diffCallback: DiffUtil.ItemCallback<VenueItemModel>
    /* ---------------------------------- */

    /* Values for coroutines */
    //A failure or cancellation of a child does not cause the supervisor job
    //to fail and does not affect its other children
    private val coroutineJob = SupervisorJob()

    private val ioScope = CoroutineScope(Dispatchers.IO + coroutineJob)
    /* ---------------------------------- */

    /* Values and variables for permission requests and location */
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationManager: LocationManager? = null
    private var rxPermissions: RxPermissions? = null
    private var permissionGranted: Boolean? = null
    private var mCurrentLocation: Location? = null
    private var alertDialog: AlertDialog? = null
    private var mGeocoder: Geocoder? = null
    private var addresses: List<Address>? = null
    private var locality: Address? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var area: String? = null
    private val REQUEST_CHECK_SETTINGS = 1
    private val GPS_ENABLE_REQUEST = 2
    private val REQUEST_WIFI = 11
    private var cancled = 0
    private var denied = 0
    /* ---------------------------------- */

    /* Values and variables for setting up views */
    private var linearLayoutManager = SpeedyLayoutManager(this)
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    var isTotalResultsShown = MutableLiveData<Boolean>()
    private var adapter: HomeFeedAdapter? = null
    var anim: ObjectAnimator? = null
    /* ---------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Enabling capturing of network state in this activity
        enableErrorMessageSnackbar(this)
        //Enabling dependency injection for this activity
        MyApplication.getComponent()
            .injectMainActivity(this)

        //initialising all the variables
        initvars()

        //User has entered the app once, so don't show the onBoarding screen again
        sharedPreferencesEditor.putBoolean("isFirstTime", false).apply()

        fab_scroll_up.setOnClickListener {
            linearLayoutManager.smoothScrollToPosition(feed_recycler_view, null, 0)
        }

        refresh_feed.setOnClickListener {
            feed_retry_layout.gone()
            requestPermission()
        }

        //setting up swipe container view
        swipe_container.setColorSchemeResources(R.color.black)
        swipe_container.setOnRefreshListener {
            requestPermission()
        }
    }


    /**
     * Method to initialise variables
     */

    private fun initvars() {
        setSupportActionBar(bookmarks_screen_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        anim = ObjectAnimator.ofFloat(total_results_layout, View.ALPHA, 1f)

        isTotalResultsShown.postValue(false)

        val viewModelFactory = ViewModelUtil.createFor(feedViewModel)
        feedViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(FeedViewModel::class.java)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        rxPermissions = RxPermissions(this)
        mGeocoder = Geocoder(this, Locale.getDefault())
        requestPermission()

        setUpBottomSheet()

        //Scroll to top functionality
        feed_recycler_view.addOnScrollListener(object : ScrollToTopListner(this) {})
    }

    /**
     * Handle all the required permissions
     */

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun requestPermission() {
        feed_recycler_view.gone()
        if (!MyUtils.isConnectedToInternet(this)) {
            showNoConnectionDialog()
        } else {
            rxPermissions?.request(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                ?.subscribe { granted ->
                    if (granted) {
                        permissionGranted = true
                        handleLocationServices()
                    } else {
                        LogClass.displayLog(TAG, "Here in else with denied value ----> $denied")
                        denied++
                        if (denied <= 1) {
                            LogClass.displayLog(TAG, "Here in else with denied value ----> $denied")
                            MyUtils.showGrantPermissionsDialog(this, "location")
                        } else denied = 0
                    }
                }?.let {
                    compositeDisposable.add(it)
                }
        }
    }

    /**
     * Method to check if GPS is turned on and take action depending on the condition
     */

    private fun handleLocationServices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //If location services enabled then connect and fetch current location
                getLocation()
            } else {
                //If google FeedLocation Services are disabled then show layout for disabled
                showDisabledLocationLayout("lollipop")
            }
        } else {
            //This condition is to handle location services is for old android versions
            val locationProviders =
                Settings.Secure.getString(
                    this.contentResolver,
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
                )
            if (!locationProviders.isEmpty()) {
                //If location services enabled then connect and fetch current location
                getLocation()
            } else {
                //If they are disabled then show location services disabled layout
                showDisabledLocationLayout("old")
            }
        }
    }

    /**
     * Method to handle location services and fetch current location from GPS
     */

    @SuppressWarnings("MissingPermission")
    private fun getLocation() {
        if (mLocationRequest == null) {
            setUpLocationRequest()
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            mLocationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    onLocationChanged(locationResult?.lastLocation)
                }

            }, Looper.myLooper()
        )
    }


    /**
     * Method to set up location request
     */

    private fun setUpLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest?.interval = 5000
        mLocationRequest?.numUpdates = 1
    }

    /**
     * Method called when device's GPS is disabled
     */

    private fun showDisabledLocationLayout(version: String) {
        if (version == "lollipop") {
            buildAlertMessageNoGps()
        } else if (version == "old") {
            showLocationDialog()
        }
    }


    /**
     * Method to handle GPS enable service for pre lollipop devices
     */

    private fun showLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("GPS Disabled!")
        builder.setMessage("Please enable GPS")
        builder.setPositiveButton("Enable") { _, _ ->
            val gpsSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(gpsSettingsIntent, GPS_ENABLE_REQUEST)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    /**
     * Method to handle GPS enable service for post lollipop devices
     */

    private fun buildAlertMessageNoGps() {
        setUpLocationRequest()
        val builder = LocationSettingsRequest.Builder()

        builder.addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener(this) {
            requestPermission()
        }

        task.addOnFailureListener(this) { e ->
            val statusCode = (e as ApiException).statusCode
            when (statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED ->
                    // FeedLocation settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        val resolvable = e as ResolvableApiException
                        resolvable.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }

    }


    /**
     * Method to handle current location if it changes
     */

    override fun onLocationChanged(location: Location?) {

        if (location != null) {
            mCurrentLocation = location
            mFusedLocationClient?.removeLocationUpdates(object : LocationCallback() {})
            fetchDataForCurrentLocation()
        } else {
            toast("Cannot fetch location at this time")
        }
    }

    /**
     * Method to fetch the location name and then fetch nearby feed from the network
     */

    @SuppressLint("SetTextI18n")
    private fun fetchDataForCurrentLocation() {
        if (mCurrentLocation != null) {
            //location_disabled_dialog.gone()
            latitude = mCurrentLocation?.latitude
            longitude = mCurrentLocation?.longitude

            try {
                if (MyUtils.isConnectedToInternet(this)) {
                    addresses = latitude?.let {
                        longitude?.let { it1 ->
                            mGeocoder?.getFromLocation(it, it1, 1)
                        }
                    }
                    locality = addresses?.get(0)
                    area = locality?.subLocality
                } else {
                    toast("Please turn on your network services")
                }
            } catch (e: Exception) {
                //do nothing
            }

            try {
                if (!addresses?.get(0)?.getAddressLine(0).isNullOrBlank()) {
                    location_name.text = addresses?.get(0)?.getAddressLine(0)
                    toolbar_bottom_layout.visible()
                } else {
                    location_name.text = "Cannot fetch location name"
                    toolbar_bottom_layout.visible()
                }
            } catch (e: Exception) {
                location_name.text = "Cannot fetch location name"
                toolbar_bottom_layout.visible()
            }

            fetchHomeFeed()
        }
    }

    /**
     * Alert for no Internet Connection
     */
    @SuppressLint("SetTextI18n")
    private fun showNoConnectionDialog() {

        val alertDialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.global_dialog_layout, null)
        alertDialogBuilder.setView(dialogView)
        alertDialog = alertDialogBuilder.create()
        alertDialog?.setCancelable(false)

        dialogView.dialog_image.setImageResource(R.drawable.ic_error)
        dialogView.dialog_close.gone()
        dialogView.dialog_cancel_text.text = "Exit App"
        dialogView.dialog_cancel_button.setOnClickListener {
            alertDialog?.dismiss()
            onBackPressed()
        }
        dialogView.dialog_text.text = "We need access to internet to fetch data"
        /*
        On pressing the Submit button we have to control the color of the button and also send only the
        Username to the local variable that is to be appended to the BASE URL when user finally clicks on
        save button
         */
        dialogView.dialog_ok_text.text = "Enable Wifi"
        dialogView.dialog_ok_button.setOnClickListener {
            alertDialog?.dismiss()
            startActivityForResult(Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_WIFI)
        }
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()
    }

    /**
     * Method to handle various responses from device hardware like GPS, WIFI etc
     */

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS ->
                when (resultCode) {
                    RESULT_OK -> {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermission()
                        }
                        requestPermission()
                    }
                    RESULT_CANCELED -> {
                        cancled++
                        if (cancled < 2) {
                            toast("To let us fetch your current location you need to enable location services!")
                            buildAlertMessageNoGps()//Ask one more time
                        } else {
                            cancled = 0
                            feed_recycler_view.gone()
                            placeholder_view.gone()
                            feed_retry_layout.visible()
                        }
                    }
                }

            REQUEST_WIFI -> if (MyUtils.isConnectedToInternet(this)) {
                requestPermission()
            }

            GPS_ENABLE_REQUEST -> requestPermission()
        }
    }

    /**
     * Method to fetch feed from the network and populate layout according to the status
     */

    @SuppressLint("SetTextI18n")
    private fun fetchHomeFeed() {
        feedViewModel.getFeedFromNetwork(
            latLong = "${MyUtils.clipDecimal(latitude)},${
            MyUtils.clipDecimal(longitude)}", version = MyUtils.getVersion()
        )
            .observe(this, Observer { pagedList ->
                adapter?.submitList(pagedList)
            })

        setAdapter()

        feedViewModel.status?.observe(this, Observer<String> { t ->
            when (t) {
                "error after" -> {
                    swipe_container.isRefreshing = false
                    if (MyUtils.isConnectedToInternet(this) && adapter?.currentList?.isEmpty() == true) {
                        feed_server_error.visible()
                        placeholder_view.gone()
                        feed_recycler_view.gone()
                    } else if (adapter?.currentList?.isEmpty() == true) {
                        toast("Something went wrong. Please check your network connection!")
                    }
                }

                "error initial" -> {
                    swipe_container.isRefreshing = false
                    if (MyUtils.isConnectedToInternet(this) && adapter?.currentList?.isEmpty() == true) {
                        feed_server_error.visible()
                        placeholder_view.gone()
                        feed_recycler_view.gone()
                    } else {
                        placeholder_view.gone()
                        feed_retry_layout.visible()
                    }
                }

                "completed" -> {
                    feed_server_error.gone()
                    placeholder_view.gone()
                    feed_recycler_view.visible()
                    adapter?.setNetworkState(true)
                    swipe_container.isRefreshing = false

                    //Block to control visibility of the total results layout element
                    if (sharedPreferences.getInt("totalResults", 0) > 0 && isTotalResultsShown.value == false) {
                        val result = SpannableStringBuilder()
                            .bold { append(sharedPreferences.getInt("totalResults", 0).toString()) }
                            .append(" places near you")
                        total_results?.text = result
                        anim?.start()
                        total_results_layout?.visible()
                        isTotalResultsShown.postValue(true)
                    }

                    if (adapter?.currentList?.isEmpty() == true || adapter?.currentList?.size == null) {
                        //show empty layout here
                        no_feed_layout.visible()
                    }
                }
                "initial loading" -> placeholder_view.visible()
                else -> adapter?.setNetworkState(false)

            }
        })

        feed_recycler_view.addOnScrollListener(object :
            ScrollToTopListner(this) {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                isTotalResultsShown.observe(this@MainActivity, Observer {
                    if (it == true) {
                        anim?.reverse()
                        total_results_layout?.gone()
                        isTotalResultsShown.postValue(false)
                    }
                })
            }
        })
    }

    /**
     * Setting up adapter for the recycler view to show a list of feed
     */

    private fun setAdapter() {
        linearLayoutManager = SpeedyLayoutManager(this)
        feed_recycler_view.layoutManager = linearLayoutManager
        adapter = HomeFeedAdapter(
            this, picasso, diffCallback, ioScope, isDislikedDao
        )
        feed_recycler_view.adapter = adapter
    }


    /**
     * Method to handle the bottom sheet layout for individual restaurant details
     */

    private fun setUpBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        sheetBehavior?.isHideable = true
        sheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        shadow.visible()
                        shadow.setOnClickListener {
                            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        shadow.gone()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })
    }

    /**
     * Method called when user clicks on more... on a list element layout
     */

    fun openMoreDetailsSheet(id: String?, name: String?) {
        if (sheetBehavior?.state !== BottomSheetBehavior.STATE_EXPANDED) {
            bottom_res_name.text = name

            feedViewModel.getMoreDetailsFromNetwork(venueId = id, version = MyUtils.getVersion())

            if (MyUtils.isConnectedToInternet(this)) {
                feedViewModel.detailStatus.observe(this, Observer { status ->

                    when (status) {
                        "completed" -> {
                            details_progress.gone()
                            feedViewModel.venueDetails.observe(this, Observer { detailsModel ->
                                if (detailsModel.response.venue.description != null) {
                                    description_layout.visible()
                                    bottom_res_desc.text = detailsModel.response.venue.description
                                } else {
                                    description_layout.gone()
                                }
                                if (detailsModel.response.venue.contact?.phone != null) {
                                    phone_layout.visible()
                                    bottom_res_phone.text = detailsModel.response.venue.contact.phone
                                    phone_layout.setOnClickListener {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.fromParts(
                                                "tel", detailsModel.response.venue.contact.phone,
                                                null
                                            )
                                        )
                                        startActivity(intent)
                                    }
                                } else {
                                    phone_layout.gone()
                                }
                                if (detailsModel.response.venue.hours != null) {
                                    if (detailsModel.response.venue.hours.timeframes?.get(0)?.open != null) {
                                        if (detailsModel.response.venue.hours.timeframes?.get(0)?.open?.get(0)?.renderedTime != null) {
                                            operationhrs_layout.visible()
                                            bottom_res_operatinghrs.text =
                                                detailsModel.response.venue.hours.timeframes[0].open?.get(0)
                                                    ?.renderedTime
                                        }
                                    }
                                } else {
                                    operationhrs_layout.gone()
                                }
                                if (detailsModel.response.venue.canonicalUrl != null) {
                                    bottom_res_url.visible()
                                    bottom_res_url.setOnClickListener {
                                        val browserIntent =
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(detailsModel.response.venue.canonicalUrl)
                                            )
                                        startActivity(browserIntent)
                                    }
                                } else {
                                    bottom_res_url.gone()
                                }
                            })
                        }

                        "error" -> {
                            toast("Cannot fetch details")
                            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        }

                        "quota error" -> {
                            toast("API Quota Exceeded")
                            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                })

                sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                toast("Turn on your internet connection")
            }
        } else {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    /**
     * Get rid of all resources here when user exits the app
     */

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        coroutineJob.cancel()
    }

    companion object {
        //Tag used to log events on this activity
        private val TAG = MainActivity::class.java.simpleName
    }
}
