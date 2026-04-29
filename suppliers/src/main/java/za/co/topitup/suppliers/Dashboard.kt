package za.co.topitup.suppliers

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.fin_balance
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.ui.supplier.SupplierViewModel
import za.co.topitup.suppliers.ui.supplier.TAG
import za.co.topitup.suppliers.utils.AppPreferences
import za.co.topitup.suppliers.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private val supplierViewModel: SupplierViewModel by viewModels()
    private lateinit var sharedPreferences: AppPreferences

    private lateinit var tiu_title_balance: TextView
    private lateinit var tiu_title_balance_cash: TextView
    private lateinit var tiu_title_outlet: TextView
    private lateinit var tiu_clock: TextView
    private lateinit var txt_version: TextView

    private lateinit var licence: String
    private lateinit var posUser: String
    private lateinit var deviceType: String
    private lateinit var retailerId: String
    private var liveEnv: Boolean = false
    private var liveEnv1: String = "false"
    private var loginPass: String = ""
    private var connected: String = ""
    private var lastDeviceAddress: String = ""
    private val clockHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Immersive status bar
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        sharedPreferences = AppPreferences(applicationContext)

        // Read intent extras (same as CashManActivity)
        licence = intent.getStringExtra("licence").toString()

        // Bind header views
        tiu_title_balance = findViewById(R.id.tiu_title_balance)
        tiu_title_balance_cash = findViewById(R.id.tiu_title_balance_cash)
        tiu_title_outlet = findViewById(R.id.tiu_title_outlet)
        tiu_clock = findViewById(R.id.tiu_clock)
        txt_version = findViewById(R.id.txt_version)

        // Set app version
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            txt_version.text = packageInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        licence = intent.getStringExtra("licence").toString()
        posUser = intent.getStringExtra("posUser").toString()
        deviceType = intent.getStringExtra("deviceType").toString()
        retailerId = intent.getStringExtra("retailerId").toString()
        liveEnv1 = intent.getStringExtra("liveEnv").toString()
        connected = intent.getStringExtra("connected").toString()
        lastDeviceAddress = intent.getStringExtra("lastDeviceAddress").toString()

//        loginPass = intent.getStringExtra("password").toString()


        loginPass = "1111"
        var testEnv: String = intent.getStringExtra("testEnv").toString()

//        showDialog("licence :  $licence \n posUser : $posUser \n deviceType : $deviceType \n retailer  : $retailerId  \nliveenv : $liveEnv")
        Log.e("live env", "live......connected......" + connected)
        Retailer.create(retailerId, licence, posUser, deviceType, liveEnv1, connected)
        Constants.RETAILER_ID = retailerId



        // Start clock
        startClock()

        // Load balance
        getBalance()

        // Long press on header to show version dialog (same as CashManActivity)
        findViewById<android.view.View>(R.id.head).setOnLongClickListener {
            val verName = za.co.topitup.suppliers.BuildConfig.VERSION_NAME
            MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.versionDialogText, verName))
                .setNegativeButton(getString(R.string.dialogCloseText), null)
                .show()
            true
        }

        // Cards
        val cardSupplier = findViewById<CardView>(R.id.cardSupplier)
        val cardWholesaler = findViewById<CardView>(R.id.cardWholesaler)
        val cardScanQR = findViewById<CardView>(R.id.cardScanQR)
        val cardPaymentHistory = findViewById<CardView>(R.id.cardPaymentHistory)
        val cardPaymentSummary = findViewById<CardView>(R.id.cardPaymentSummary)
        val cardTransferToBank = findViewById<CardView>(R.id.cardTransferToBank)

        animateCards(cardSupplier, cardWholesaler, cardScanQR, cardPaymentHistory, cardPaymentSummary, cardTransferToBank)

        cardSupplier.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()

            val intent = Intent(this, CashManActivity::class.java).apply {
                // Pass all extras forward so CashManActivity works correctly
                putExtra("licence", this@DashboardActivity.intent.getStringExtra("licence"))
                putExtra("posUser", this@DashboardActivity.intent.getStringExtra("posUser"))
                putExtra("deviceType", this@DashboardActivity.intent.getStringExtra("deviceType"))
                putExtra("retailerId", this@DashboardActivity.intent.getStringExtra("retailerId"))
                putExtra("liveEnv", this@DashboardActivity.intent.getStringExtra("liveEnv"))
                putExtra("connected", this@DashboardActivity.intent.getStringExtra("connected"))
                putExtra("lastDeviceAddress", this@DashboardActivity.intent.getStringExtra("lastDeviceAddress"))
                putExtra("testEnv", this@DashboardActivity.intent.getStringExtra("testEnv"))
            }
            startActivity(intent)
        }

        cardWholesaler.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()

        }
        cardScanQR.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()

        }
        cardPaymentHistory.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()
        }

        cardPaymentSummary.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()

        }

        cardTransferToBank.setOnClickListener {
            it.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }.start()

        }
    }

    private fun startClock() {
        val clockRunnable = object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("dd MMM YY  @  HH:mm")
                tiu_clock.text = sdf.format(Calendar.getInstance().time)
                clockHandler.postDelayed(this, 60000)
            }
        }
        // Set immediately then start repeating
        val sdf = SimpleDateFormat("dd MMM YY  @  HH:mm")
        tiu_clock.text = sdf.format(Calendar.getInstance().time)
        clockHandler.postDelayed(clockRunnable, 60000)
    }

    private fun getBalance() {
        Log.e("DashboardActivity", "getBalance called")
        lifecycleScope.launch(Dispatchers.IO) {
            supplierViewModel.getBalanceInfo().collect { result ->
                if (result.status == ApiStatus.SUCCESS) {
                    val finBalance: fin_balance = result.data as fin_balance
                    runOnUiThread {
                        tiu_title_balance.text = "Standard R " + finBalance.available_balance
                        Log.e("getBalance",finBalance.available_balance.toString())
                        tiu_title_balance_cash.text = " Bills R " + finBalance.balance_cash
                        sharedPreferences.accountNumber = finBalance.acn1
                        tiu_title_outlet.text = sharedPreferences.accountNumber
                    }
                }
            }
        }
    }

    private fun animateCards(vararg cards: CardView) {
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 60f
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay((index * 120 + 200).toLong())
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacksAndMessages(null)
    }
}