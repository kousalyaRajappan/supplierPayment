package za.co.topitup.suppliers

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import io.realm.BuildConfig
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.ActivityCashManBinding
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.fin_balance
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository
import za.co.topitup.suppliers.ui.history.HistoryFragment
import za.co.topitup.suppliers.ui.payment.ReceiptDialogFragment
import za.co.topitup.suppliers.ui.supplier.SupplierFragment
import za.co.topitup.suppliers.ui.supplier.SupplierViewModel
import za.co.topitup.suppliers.ui.supplier.manage.AddSupplierFragment
import za.co.topitup.suppliers.utils.AppPreferences
import za.co.topitup.suppliers.utils.Constants
import za.co.topitup.suppliers.utils.Print
import za.co.topitup.suppliers.utils.toast
import java.text.SimpleDateFormat
import java.util.*

//const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

/**
 * Pass in the following IntentExtras:
 * * [licence] of type [String]
 * * [posUser] of type [String]
 * * [deviceType] of type [String]
 * * [retailerId] of type [String]
 * * [liveEnv] of type [Boolean]
 */
class CashManActivity : FragmentActivity(), NavigationHost, //LifecycleOwner,
    ReceiptDialogFragment.ReceiptDialogListener {

    private lateinit var binding: ActivityCashManBinding

    private lateinit var sharedPreferences: AppPreferences
//    private var vendorActivated: Boolean = false

    private lateinit var vendorLatitude: String
    private lateinit var vendorLongitude: String
    var movies = ArrayList<MyItem>()

    //    private var locationEnabled: Boolean = false
    private var locationEnabled: Boolean = true

    lateinit var printer: Print

    private lateinit var licence: String
    private lateinit var posUser: String
    private lateinit var deviceType: String
    private lateinit var retailerId: String
    private lateinit var account_number: String
    private lateinit var user_name: String

    private var liveEnv: Boolean = false
    private var liveEnv1: String = "false"
    private var loginPass: String = ""


    private val supplierViewModel: SupplierViewModel by viewModels()

    private lateinit var tiu_title_balance: TextView
    private lateinit var tiu_title_balance_cash: TextView

    private lateinit var tiu_title_outlet: TextView


//    private var liveEnv: Boolean = false

//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showExitDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Register callback for onBackPressed
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val intent = intent
        licence = intent.getStringExtra("licence").toString()
        posUser = intent.getStringExtra("posUser").toString()
        deviceType = intent.getStringExtra("deviceType").toString()
        retailerId = intent.getStringExtra("retailerId").toString()
        liveEnv1 = intent.getStringExtra("liveEnv").toString()

//        loginPass = intent.getStringExtra("password").toString()



        loginPass = "1111"
        var testEnv:String = intent.getStringExtra("testEnv").toString()

        showDialog("licence :  $licence \n posUser : $posUser \n deviceType : $deviceType \n retailer  : $retailerId  \nliveenv : $liveEnv")
//        Log.e("live env","live............"+deviceType)
        Retailer.create(retailerId, licence, posUser, deviceType, liveEnv1)
        Constants.RETAILER_ID = retailerId
       /* sharedPreferences = AppPreferences(applicationContext)
        //vendorActivated = sharedPreferences.vendorActivated
        sharedPreferences.RETAILERID = retailerId*/
        // Initialize Realm
        Realm.init(this)

        // Init Coil
        ImageLoader.Builder(applicationContext)
            .components {
                add(SvgDecoder.Factory())
            }
            .addLastModifiedToFileCacheKey(true)
            .fallback(R.drawable.ic_suppliers)
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.1)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.25)
                    .build()
            }.respectCacheHeaders(false)
            .build()

        /*--------------------------------------------------------------------------------------*/

        binding = ActivityCashManBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainerView, SupplierFragment())
                .commit()
        }

        val topAppBar = binding.topAppBar
        val tabLayout = binding.tabLayout

        //Tab Navigation
        val tab1Text = getString(R.string.title_supplier)
        val tab2Text = getString(R.string.title_management)
        val tab3Text = getString(R.string.title_history)
        val addSupplierButton = topAppBar.menu.findItem(R.id.addSupplierMenuButton)
        val refreshButton = topAppBar.menu.findItem(R.id.supplierListRefreshButton)
        val tiu_clock = findViewById(R.id.tiu_clock) as TextView
        tiu_title_balance = findViewById(R.id.tiu_title_balance) as TextView

        tiu_title_balance_cash = findViewById(R.id.tiu_title_balance_cash) as TextView

        //Toast.makeText(activity_main.this, "in="+TIMEOUT_IN_MILLI,Toast.LENGTH_LONG).show();
         tiu_title_outlet = findViewById(R.id.tiu_title_outlet) as TextView
        //        tiu_batt = (TextView) findViewById(R.id.tiu_batt);
        val tiu_user_name = findViewById(R.id.tiu_user_name) as TextView


/*
        tiu_user_name.setText(user_name)
        tiu_title_outlet.setText(account_number)*/

        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM YY  @  HH:mm")
        val strDate = sdf.format(c.time)
        tiu_clock.text = strDate
        get_balance()
//        get_update_users()
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val c = Calendar.getInstance()
                val sdf = SimpleDateFormat("dd MMM YY  @  HH:mm")
                val strDate = sdf.format(c.time)
                tiu_clock.text = strDate
                handler.postDelayed(this, 60000) //now is every 2 minutes
            }
        }, 60000)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {


            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    addSupplierButton.isVisible = false
                    refreshButton.isVisible = false
                    when (tab.text) {
                        tab1Text -> {
                            navigateTo(SupplierFragment(), false)
                            addSupplierButton.isVisible = true
                        }
                        tab2Text -> {
                            navigateTo(AddSupplierFragment(), false)
                            refreshButton.isVisible = true
                        }
                        tab3Text -> navigateTo(HistoryFragment(), false)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
        for (item in 0..tabLayout.tabCount) {
            tabLayout.getTabAt(item)?.setCustomView(R.layout.tab_item)
        }

        // Shared Preferences and Location
        sharedPreferences = AppPreferences(applicationContext)
        //vendorActivated = sharedPreferences.vendorActivated
        locationEnabled = sharedPreferences.locationEnabled
        vendorLatitude = sharedPreferences.vendorLatitude.toString()
        vendorLongitude = sharedPreferences.vendorLongitude.toString()


//        fusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(applicationContext)
//        if (vendorLatitude == "0.0" || vendorLongitude == "0.0") {
//
//            //TODO sharedPreferences location Enabled
//
//            if (permissionApproved()) {
//                getCurrentLocation()
//            } else {
//                requestPermissions()
//            }
//        }

//        TODO run this after getting location
//         TODO only update data
//        val supplierList = SupplierRepository().getSuppliers(vendorLatitude, vendorLongitude)
//        supplierList.observe(this) { suppliersList ->
//            lifecycleScope.launch {
//                SupplierDatabaseOperations().insertSuppliers(suppliersList)
//            }
//        }
//

        //topAppBar
        topAppBar.setNavigationOnClickListener {
            //onBackPressed()
            onBackPressedCallback.handleOnBackPressed()
        }

        topAppBar.setOnLongClickListener {
            val verName = BuildConfig.VERSION_NAME

            MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.versionDialogText, verName))
                .setNegativeButton(getString(R.string.dialogCloseText), null)
                .show()
            true
        }


        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.settingsMenu -> {
//                    navigateTo(SettingsFragment(), true)
//                    true
//                }
                R.id.addSupplierMenuButton -> {
                    tabLayout.getTabAt(1)!!.select()
                    true
                }
                R.id.supplierListRefreshButton -> {
                    updateSuppliers()
                    true
                }
                else -> false
            }
        }
        printer = Print(this)
    }

    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private fun updateSuppliers() {
        Log.e("update suppliers","........update suppliers")
        val supplierList = SupplierRepository().getSuppliersFromAPI(vendorLatitude, vendorLongitude)
        binding.AppProgressBar.visibility = View.VISIBLE
        supplierList.observe(this@CashManActivity) { suppliersList ->
            lifecycleScope.launch {
                if (suppliersList[0].id != 0) {
                    SupplierDatabaseOperations().insertSuppliers(suppliersList)
                }
                else {
                    toast(getString(R.string.network_error_basic), Toast.LENGTH_LONG)
                }
                binding.AppProgressBar.visibility = View.INVISIBLE
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        //supportFragmentManager.popBackStack()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        //supportFragmentManager.popBackStack()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.exitDialogTitle))
            .setMessage(getString(R.string.exitDialogMessage))
            .setPositiveButton(getString(R.string.dialogPositiveText)) { _, _ -> finish() }
            .setNegativeButton(getString(R.string.dialogNegativeText), null)
            .show()
    }

//  Location functions
//  /* ----------------------------------------------------------------------------------------- */

//    private fun getCurrentLocation() {
//        lifecycleScope.launch(Dispatchers.Default) {
//            GetLocation(fusedLocationProviderClient, applicationContext)
//                .fetchUpdates()
//                .collect { currentLocation ->
//                    sharedPreferences.vendorLatitude =
//                        currentLocation.latitude.toString()
//                    sharedPreferences.vendorLongitude =
//                        currentLocation.longitude.toString()
//                    this.cancel("Done")
//                }
//        }
//    }

//    private fun permissionApproved(): Boolean {
//        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//            this,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    }

    // Request Location Permissions
//    private fun requestPermissions() {
//        val provideRationale = permissionApproved()
//
//        // If the user denied a previous request, but didn't check "Don't ask again", provide
//        // additional rationale.
//        if (provideRationale) {
//            Snackbar.make(
//                findViewById(R.id.activity_cash_man),
//                R.string.permission_rationale,
//                Snackbar.LENGTH_LONG
//            )
//                .setAction(R.string.ok) {
//                    // Request permission
//                    ActivityCompat.requestPermissions(
//                        this@CashManActivity,
//                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//                    )
//                }
//                .show()
//        } else {
//            ActivityCompat.requestPermissions(
//                this@CashManActivity,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
//                grantResults.isEmpty() ->
//                    // If user interaction was interrupted, the permission request
//                    // is cancelled and you receive empty arrays.
//                    Log.d("Location", "User interaction was cancelled.")
//
//                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
//                    // Permission was granted.
//                {
//                    locationEnabled = true
//                    sharedPreferences.locationEnabled = true
//                    getCurrentLocation()
//                }
//
//                else -> {
//                    // Permission denied.
//                    Snackbar.make(
//                        findViewById(R.id.activity_cash_man),
//                        R.string.permission_denied_explanation,
//                        Snackbar.LENGTH_LONG
//                    )
//                        .setAction(R.string.settings) {
//                            // Build intent that displays the App settings screen.
//                            val intent = Intent()
//                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            val uri = Uri.fromParts(
//                                "package",
//                                BuildConfig.LIBRARY_PACKAGE_NAME,
//                                null
//                            )
//                            intent.data = uri
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            startActivity(intent)
//                        }
//                        .show()
//                }
//            }
//        }
//    }

    /* ----------------------------------------------------------------------------------------- */
    private fun showDialog(desc :String) {

        val dialog = Dialog(this@CashManActivity)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.setCancelable(false)
        dialog.show()
        val txt_desc = dialog.findViewById<View>(R.id.txt_desc) as TextView
        val txt_back = dialog.findViewById<View>(R.id.txt_back) as TextView
        txt_desc.text = desc
        txt_back.setOnClickListener {

            dialog.dismiss()

//            finish()
        }


    }
    fun get_balance() {
        Log.e("call api","......getBalance...")
        lifecycleScope.launch(Dispatchers.IO) {

            supplierViewModel.getBalanceInfo().collect { result ->
                if (result.status == ApiStatus.SUCCESS) {

                    Log.e("response","res"+result.data)
                    val finBalance: fin_balance = result.data as fin_balance


                    finBalance.balance?.let { finBalance.balance = it }
                    finBalance.balance_cash?.let { finBalance.balance_cash = it }
                    finBalance.acn1?.let { finBalance.acn1 = it }

                    runOnUiThread {
                        tiu_title_balance.text =
                            "Standard R " + finBalance.balance
                        tiu_title_balance_cash.text = " Bills R " +finBalance.balance_cash
                        sharedPreferences.accountNumber = finBalance.acn1
                        tiu_title_outlet.setText(sharedPreferences.accountNumber)
                        // Stuff that updates the UI
                    }


                }
            }


        }

    }

    private fun get_update_users() {

            val posUsersList = SupplierRepository().getPosuserListAPI()
            binding.AppProgressBar.visibility = View.VISIBLE
        posUsersList.observe(this@CashManActivity) { posusresList ->

                lifecycleScope.launch {
                    Log.e("pos users","size...."+posusresList.size)

                    for (i in 0 until posusresList.size) {

                        if(posusresList[i].posuser_id.equals(loginPass)){
                            Log.e("pos users......."+i,"${posusresList[i].posuser_firstname}size...."+posusresList[i].posuser_id)
                        }
                    }

//                    binding.AppProgressBar.visibility = View.INVISIBLE
                }
            }
        }





}