package za.co.topitup.suppliers.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import io.realm.RealmResults
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
//import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.models.*
import za.co.topitup.suppliers.network.ApiResponse
import za.co.topitup.suppliers.network.SupplierEndpointAPI
import za.co.topitup.suppliers.network.pos_users
import za.co.topitup.suppliers.utils.checkApiResponse
import za.co.topitup.suppliers.utils.checkApiResponsePayment
import za.co.topitup.suppliers.utils.checkApiResponsePeninsula
import za.co.topitup.suppliers.utils.logger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


private const val TAG = "SupplierRepository"

class SupplierRepository {

    private val supplierEndpointAPI: SupplierEndpointAPI
    private val database = SupplierDatabaseOperations()

    private var headerMap: HashMap<String, String> = HashMap()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging)
            .build()
        val gson = GsonBuilder()
            .setLenient()

            .create()



        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Retailer.endpointBaseURL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)

            .build()
        supplierEndpointAPI = retrofit.create(SupplierEndpointAPI::class.java)

        headerMap["device"] = Retailer.deviceType
        headerMap["license"] = Retailer.licence
        headerMap["posuser"] = Retailer.posUserId

    }

    fun getActivatedSuppliersFromDB(): RealmResults<SupplierRealm> {
        return database.getActivatedSuppliers()
    }

    fun getSuppliersFromAPI(latitude: String, longitude: String): MutableLiveData<List<Supplier>> {
        val responseLiveData: MutableLiveData<List<Supplier>> = MutableLiveData()
        val supplierEndpointRequest: Call<List<Supplier>> = supplierEndpointAPI.getSuppliers(headerMap)

        //TODO Get Location on Vendor Activate and save in Shared Preferences
        val vendorLocation = Location("vendorLocation")
        if (latitude != "null") {
            vendorLocation.latitude = latitude.toDouble()
        }

        if (longitude != "null") {
            vendorLocation.longitude = longitude.toDouble()
        }
        supplierEndpointRequest.enqueue(object: Callback<List<Supplier>> {
            override fun onFailure(call: Call<List<Supplier>>, t: Throwable) {
                logger(TAG, "Get Suppliers: Failed to fetch Suppliers", t)
                //Todo Find a better way with callbacks or interfaces etc.
                responseLiveData.value = listOf(Supplier(0))

            }

            override fun onResponse(call: Call<List<Supplier>>, response: Response<List<Supplier>>) {
                val supplierEntry: List<Supplier>? = response.body()

                Log.e("suppliers list","suppliers................"+supplierEntry?.size)
                supplierEntry?.forEach {

                    val location = Location(it.name)
                    location.latitude = it.latitude
                    location.longitude = it.longitude

                    val distance = location.distanceTo(vendorLocation)
                    it.distance = distance.toInt()

                }

                if (response.isSuccessful) {
                    responseLiveData.value = supplierEntry
                }
            }
        } )
    return responseLiveData
    }

    suspend fun sendPaymentToAPI(payment: PaymentRealm,
                                 supplierId: Int,
                                 supplierAccountNumber: String): ApiResponse {
        val amountInCents = (payment.amount * 100).toLong()
        logger(supplierAccountNumber+"supplierId payment......"+supplierId,payment.paymentReference+"......."+ amountInCents.toString())

        return try {
            val response = supplierEndpointAPI.sendPayment(
                headerMap,
                supplierId = supplierId,
                supplierAccountNumber = supplierAccountNumber,
                loadNo = payment.paymentReference,
                paymentAmount = amountInCents.toString(),
                driverCell = "", //"0822345678",
                driverNo = "", //"4565"

            )

            checkApiResponse(response)
        } catch (error: IOException) {
            logger(TAG, "Send Payment Error: ${error.message}")
            ApiResponse.NetworkException(error.message)
        }
    }


    suspend fun sendPaymentToAPIPensulia(payment: PaymentRealm, supplierId: Int, supplierAccountNumber: String,mcon:Context): ApiResponse {
        val amountInCents = (payment.amount * 100).toLong()
        val driverNumber = payment.driverNumber
        val driverPhone = payment.driverPhone
        logger(supplierAccountNumber+"supplierId payment......"+supplierId,payment.paymentReference+"......."+ amountInCents.toString())
         var dataResponse :String = ""


        var loadNoRetailer = RetailerNew.dcCode+RetailerNew.accountNumber

        Log.e("retailer id",RetailerNew.accountNumber+"...........retailer......"+loadNoRetailer)

         return try {
             val response = supplierEndpointAPI.cash_acct_payment(
                 headerMap,
                 ptype = "penbev",
                 storeno = RetailerNew.accountNumber,
                 loadno = loadNoRetailer,
                 driverno = driverNumber,
                 drivercell = driverPhone,
                 amt = amountInCents.toString(),
                 shipmentno = payment.paymentReference
             )

             val res :String = response.body()?.string().toString()
             val sharedPreference =  mcon.getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
             var editor = sharedPreference.edit()
             editor.putString("response",res)
             editor.commit()

             checkApiResponsePeninsula(res)
         } catch (error: IOException) {
             logger(TAG, "Send Payment Error: ${error.message}")
             ApiResponse.NetworkException(error.message)
         }

    }

    fun getSupplierLogoFromRealmByName(supplierName: String): String {
        return SupplierDatabaseOperations().getSupplierLogoByName(supplierName)
    }

    fun getPaymentFromDB(paymentId: String) : Payment? {
        return SupplierDatabaseOperations().getPayment(paymentId)
    }

    suspend fun getAdvertURLsFromAPI(): ApiResponse {
        return try {
            val response = supplierEndpointAPI.getAdvertURLs(headerMap)

            checkApiResponse(response)

        } catch (error: IOException) {
            ApiResponse.NetworkException(error.message)
        } catch (error: IllegalStateException) {
            ApiResponse.NetworkException(error.message)
        }
    }

    suspend fun getRetailerInfo(): ApiResponse {
        return try {
            val response = supplierEndpointAPI.getRetailerInfo(headerMap)

            checkApiResponse(response)

        } catch (error: IOException) {
            ApiResponse.NetworkException(error.message)
        } catch (error: IllegalStateException) {
            ApiResponse.NetworkException(error.message)
        }

    }
    suspend fun getRetailerInfoNew(): ApiResponse {
        return try {
            val response = supplierEndpointAPI.getRetailerInfoNew(headerMap)

            checkApiResponse(response)

        } catch (error: IOException) {
            ApiResponse.NetworkException(error.message)
        } catch (error: IllegalStateException) {
            ApiResponse.NetworkException(error.message)
        }

    }

    suspend fun activateSupplier(supplier: Supplier) {
        database.activateSupplier(supplier)
    }

    suspend fun deactivateSupplier(supplier: Supplier) {
        database.deactivateSupplier(supplier)
    }

    fun getSuppliers(name: String, type: String): RealmResults<SupplierRealm> {
        return database.getSuppliers(name, type)
    }

    suspend fun getSupplierCategories(): List<String> {
        return database.getSupplierCategories()
    }

    suspend fun linkSupplierInAPI(supplierId: Int, retailerId: String, accountNumber: String): ApiResponse {
        return try {
            val response = supplierEndpointAPI.linkSupplier(
                headerMap,
                supplierId = supplierId,
                accountNumber = accountNumber,
                retailerId = retailerId
            )

            checkApiResponse(response)

        } catch (error: IOException) {
            logger("Link", "Error ${error.message}")
            ApiResponse.NetworkException(error.message)
        }
    }

    suspend fun unlinkSupplierInAPI(linkId: String): ApiResponse {
        return try {
            val response = supplierEndpointAPI.unlinkSupplier(
                headerMap,
                linkId = linkId,
            )
            checkApiResponse(response)

        } catch (error: IOException) {
            logger("Link", "Error ${error.message}")
            ApiResponse.NetworkException(error.message)
        }
    }
    suspend fun getBalanceInfo(): ApiResponse {
        return try {
            val response = supplierEndpointAPI.getbalancenew(headerMap,"1")

            checkApiResponse(response)

        } catch (error: IOException) {
            ApiResponse.NetworkException(error.message)
        } catch (error: IllegalStateException) {
            ApiResponse.NetworkException(error.message)
        }
    }

    suspend fun getUpdateInfo(version: String, licence: String): ApiResponse {
        return try {

            val response = supplierEndpointAPI.upsertSupplierApp(headerMap,licence,version)

            checkApiResponse(response)

        } catch (error: IOException) {
            ApiResponse.NetworkException(error.message)
        } catch (error: IllegalStateException) {
            ApiResponse.NetworkException(error.message)
        }
    }



    fun getPosuserListAPI(): MutableLiveData<List<pos_users>> {
        val responseLiveData: MutableLiveData<List<pos_users>> = MutableLiveData()
        val supplierEndpointRequest: Call<List<pos_users>> = supplierEndpointAPI.get_posuser_list(headerMap,1)

        //TODO Get Location on Vendor Activate and save in Shared Preferences

        supplierEndpointRequest.enqueue(object: Callback<List<pos_users>> {
            override fun onFailure(call: Call<List<pos_users>>, t: Throwable) {
                logger(TAG, "Get Suppliers: Failed to fetch Suppliers", t)
                //Todo Find a better way with callbacks or interfaces etc.
                responseLiveData.value = listOf(pos_users(0))

            }

            override fun onResponse(call: Call<List<pos_users>>, response: Response<List<pos_users>>) {
                val supplierEntry: List<pos_users>? = response.body()

                Log.e("suppliers list","suppliers................"+supplierEntry?.size)
                /*supplierEntry?.forEach {

                    val location = Location(it.name)
                    location.latitude = it.latitude
                    location.longitude = it.longitude

                    val distance = location.distanceTo(vendorLocation)
                    it.distance = distance.toInt()

                }*/

                if (response.isSuccessful) {
                    responseLiveData.value = supplierEntry
                }
            }
        } )
        return responseLiveData
    }

}