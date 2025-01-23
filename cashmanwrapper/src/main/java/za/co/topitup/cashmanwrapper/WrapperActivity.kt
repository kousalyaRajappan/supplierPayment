package za.co.topitup.cashmanwrapper

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import za.co.topitup.suppliers.CashManActivity


class WrapperActivity : AppCompatActivity() {

    private val REQUEST_CODE: Int =100
    private val licence: String = "DEMO0630-4a3f-11e5-89af-001e6779cd30"
    private val posUser: String = "5128"
    private val deviceType: String = "android"
    private val retailerId: String = "1110"
    private val liveEnv: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrapper)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {


            /*    Intent intent = new Intent();
                            intent.setComponent(new ComponentName("za.co.topitup.suppliers", "za.co.topitup.suppliers.CashManActivity"));
                            intent.putExtra("licence", Topitup.TIU_LICENSE);
                            intent.putExtra("posUser", Topitup.POSUSER_ID);
                            intent.putExtra("deviceType", "android");
                            intent.putExtra("retailerId", Topitup.CUSTOMER_ID);
                            intent.putExtra("liveEnv", isLive);
                            startActivity(intent);*/
          /*  val intent = Intent()
            intent.component =
                ComponentName("za.co.topitup.suppliers", "za.co.topitup.suppliers.CashManActivity")
            intent.putExtra("licence", "0c65c164-d9ee-11ed-99c4-0cc47a4f05f0")
            intent.putExtra("posUser", "22476")
            intent.putExtra("deviceType", "android")
            intent.putExtra("retailerId", "51613")
            intent.putExtra("liveEnv", true)
            startActivity(intent)*/

         /* val intent = Intent(applicationContext, CashManActivity::class.java)
            intent.putExtra("licence", licence)
            intent.putExtra("posUser", posUser)
            intent.putExtra("deviceType", deviceType)
            intent.putExtra("retailerId", retailerId)
            intent.putExtra("liveEnv", liveEnv)
            startActivity(intent)*/
        }
    }



}