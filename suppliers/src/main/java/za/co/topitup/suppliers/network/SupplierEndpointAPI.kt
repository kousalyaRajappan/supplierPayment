package za.co.topitup.suppliers.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import za.co.topitup.suppliers.models.*

//TODO Add common headers device, posuser, licence etc with Interceptor instead
interface SupplierEndpointAPI {

    //Get List of Suppliers
    @GET("supplier/cashmx/get-all-supliers")
    fun getSuppliers(
        @HeaderMap headers: Map<String, String>,
    ): Call<List<Supplier>>

    @GET("supplier/cashmx/account-payment-supplier")
    suspend fun sendPayment(
        @HeaderMap headers: Map<String, String>,
        @Query("supplier_id") supplierId: Int,
        @Query("supplier_account_no") supplierAccountNumber: String,
        @Query("loadno") loadNo: String?,
        @Query("driverno") driverNo: String?,
        @Query("drivercell") driverCell: String?,
        @Query("amt") paymentAmount: String?,
    ): Response<Payment>



    @GET("supplier/cashmx/account-payment-supplier")
    suspend fun sendPaymentPensulia(
        @HeaderMap headers: Map<String, String>,
        @Query("supplier_id") supplierId: Int,
        @Query("supplier_account_no") supplierAccountNumber: String,
        @Query("loadno") loadNo: String?,
        @Query("driverno") driverNo: String?,
        @Query("drivercell") driverCell: String?,
        @Query("amt") paymentAmount: String?,
    ): Response<Payment>


    @GET("/penbev/cashmx/account-payment")
    suspend fun cash_acct_payment(
        @HeaderMap headers: Map<String, String>,
        @Query("ptype") ptype: String?,
        @Query("storeno") storeno: String?,
        @Query("loadno") loadno: String?,
        @Query("driverno") driverno: String?,
        @Query("drivercell") drivercell: String?,
        @Query("amt") amt: String?,
        @Query("shipmentno") shipmentno: String?
    ): Response<ResponseBody>


    @GET("supplier/cashmx/link-supplier-retailer")
    suspend fun linkSupplier(
        @HeaderMap headers: Map<String, String>,
        @Query("supplier_id") supplierId: Int,
        @Query("customer_id") retailerId : String,
        @Query("supplier_account_no") accountNumber: String,
    ): Response<Supplier>

    @GET("supplier/cashmx/get-advert-url")
    suspend fun getAdvertURLs(
        @HeaderMap headers: Map<String, String>,
    ): Response<Array<String>>


    @GET("supplier/cashmx/get-customer-info")
    suspend fun getRetailerInfo(
        @HeaderMap headers: Map<String, String>,
        @Header("ignore") ignore: Int = 1,
    ): Response<RetailerApi>

    @GET("terminal/cashmx/get-customer-info")
    suspend fun getRetailerInfoNew(
        @HeaderMap headers: Map<String, String>,
    ): Response<RetailerApiNew>

    @GET("supplier/cashmx/disable-supplier-link")
    suspend fun unlinkSupplier(
        @HeaderMap headers: Map<String, String>,
        @Query("link_id") linkId: String,
    ): Response<Supplier>


    @GET("voucherapi/financial/get-balance")
    suspend fun getbalancenew(
        @HeaderMap headers: Map<String, String>,
        @Query("lvl") lvl: String?
    ): Response<fin_balance>


    @GET("salaah/connect/upsert-supplier-app")
    suspend fun upsertSupplierApp(
        @HeaderMap headers: Map<String, String>,
        @Query("asset_id") assetId: String,
        @Query("supplier_app_ver") supplierAppVer: String
    ): Response<UpsertResponse>


    @GET("terminal/customer/get-posuser-list")
     fun get_posuser_list(
        @HeaderMap headers: Map<String, String>,

        @Query("json") json: Int
    ): Call<List<pos_users>>
    @GET("supplier/cashmx/get-payment-search")
    fun get_payment_search(
        @HeaderMap headers: Map<String, String>,
        @Query("sdate") sdate: String?,
        @Query("edate") edate: String?,
        @Query("txt") param: String?
    ): Call<ResponseBody>


}