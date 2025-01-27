package za.co.topitup.suppliers.models

import za.co.topitup.suppliers.utils.Constants.DEMO_ENDPOINT_BASE_URL
import za.co.topitup.suppliers.utils.Constants.DEMO_LOGO_BASE_URL
import za.co.topitup.suppliers.utils.Constants.PROD_ENDPOINT_BASE_URL
import za.co.topitup.suppliers.utils.Constants.PROD_ENDPOINT_BASE_URL_TX1
import za.co.topitup.suppliers.utils.Constants.PROD_ENDPOINT_BASE_URL_TX2
import za.co.topitup.suppliers.utils.Constants.PROD_LOGO_BASE_URL

object Retailer {
    lateinit var id: String

    lateinit var licence: String
    private set
    lateinit var posUserId: String
    private set

    lateinit var deviceType: String
    private set

    var accountNumber: String = ""

    var province: String = ""

    var provinceId: Int = 0

    private var liveEnvironment: String =""

    lateinit var endpointBaseURL: String
    private set

    lateinit var logoBaseURL: String
    private set

    lateinit var deviceTypeTo: String

        private set

    lateinit var connected: String
        private set


    var dcCode: String? = ""


    fun create(id: String, licence: String, posUserId: String, deviceType: String,
               liveEnvironment: String,connected:String) {
        this.id = id
        this.licence = licence
        this.posUserId = posUserId
        this.deviceType = deviceType
        this.liveEnvironment = liveEnvironment
        var baseUrl: String =""
        var logoUrl: String =""
        var connecectedType =connected;


        if (liveEnvironment.contains("LIVE", ignoreCase = true)){



            if (liveEnvironment.equals("SERVER3LIVE", ignoreCase = true)) {
                baseUrl= PROD_ENDPOINT_BASE_URL_TX2
                logoUrl = PROD_LOGO_BASE_URL
            } else if (liveEnvironment.equals("SERVER2LIVE", ignoreCase = true)) {
                baseUrl= PROD_ENDPOINT_BASE_URL_TX1
                logoUrl = PROD_LOGO_BASE_URL
            } else {
                baseUrl= PROD_ENDPOINT_BASE_URL
                logoUrl = PROD_LOGO_BASE_URL
            }
        }else{
            baseUrl=  DEMO_ENDPOINT_BASE_URL
            logoUrl = DEMO_LOGO_BASE_URL




        }


        this.endpointBaseURL = baseUrl
        this.logoBaseURL = logoUrl

        this.connected = connecectedType


        var device :String = ""
        if(android.os.Build.MANUFACTURER == "CHUWI"){

            if(android.os.Build.MODEL == "Hi10 XPro"){
                device = "tab"

            }else {
                device = "Mobile"
            }
        }else{
             device = "wpos"
        }
        this.deviceTypeTo = device
    }

}
