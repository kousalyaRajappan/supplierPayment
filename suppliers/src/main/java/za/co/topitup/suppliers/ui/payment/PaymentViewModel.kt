package za.co.topitup.suppliers.ui.payment

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Printer
import android.view.View
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.models.Payment
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.network.ApiResponse
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository
import za.co.topitup.suppliers.utils.logger
import java.io.IOException


class PaymentViewModel : ViewModel() {

    // TODO Do something cool with LiveData
    /* private val _text = MutableLiveData<String>().apply {
         value = "This is dashboard Fragment"
     }
     val text: LiveData<String> = _text*/

    private val repository = SupplierRepository()
    private val database = SupplierDatabaseOperations()

    //private val _paymentLiveData = MutableLiveData<Response<Payment>>()
    //val paymentLiveData: LiveData<Response<Payment>> = _paymentLiveData
    private val _paymentLiveData = MutableLiveData<Payment?>()
    val paymentLiveData: LiveData<Payment?> = _paymentLiveData

    //TODO - Remove
    private val _status = MutableLiveData<Boolean>().apply { value = false }
    val status: LiveData<Boolean> = _status


    fun getSupplierLogo(supplierName: String): LiveData<String> {
        val logo = MutableLiveData<String>()

        viewModelScope.launch(Dispatchers.IO) {
            logo.postValue(repository.getSupplierLogoFromRealmByName(supplierName))
        }

        return logo
    }



    val sendPaymentToApi = fun (payment: PaymentRealm,
                                supplierId: Int,
                                supplierAccountNumber: String): LiveData<ApiResponse> {

        //emit(ApiResult.Loading(isLoading = true)) // 1. Loading State
        return flow {
            logger("Payment Fragment", payment.toString())
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = repository.sendPaymentToAPI(payment, supplierId, supplierAccountNumber)
            logger("Payment", "ViewModel response: $response")
            logger("Payment", "ViewModel response: ${response.status}")
            logger("Payment", "ViewModel response: ${response.data}")
            logger("Payment", "ViewModel response: ${response.message}")

            if (response.status == ApiStatus.SUCCESS) {
                val paymentResult = response.data as Payment?
                if (paymentResult?.id.isNullOrEmpty().not()) {
                    _paymentLiveData.value = paymentResult
                    logger("Payment", "Payment Response Success: $paymentResult")
                    emit(response)
                } else {
                    logger("Payment", "Payment Response is null: $paymentResult")
                    emit(ApiResponse.Error("Payment Response is Empty"))
                }
            }
            if (response.status == ApiStatus.EXCEPTION) {
                _status.value = false
                logger("Payment", "ViewModel error: ${response.message}")
                emit(response)
            }
        }.asLiveData()

    }


    val sendPensuliaPaymentToApi = @SuppressLint("SuspiciousIndentation")
    fun (payment: PaymentRealm,
         supplierId: Int,
         supplierAccountNumber: String, mcon: Context): LiveData<ApiResponse> {
        return flow {
            logger("Payment Fragment", payment.toString())
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = repository.sendPaymentToAPIPensulia(payment, supplierId, supplierAccountNumber,mcon)

//            if (ApiStatus.SUCCESS.equals("SUCCESS")) {
//                val paymentResult = response.data as ResponseBody?



                emit(response)

           /* }else{
                emit(ApiResponse.Error("Duplicate Payment"))

            }*/
/*

            if (response.status == ApiStatus.SUCCESS) {
                val paymentResult = response.data as ResponseBody?

//                Log.e("response","api call........"+jsonObject.toString())

                if (response.toString().contains("err")) {
                    //  Toasty.error(mContext, "Insufficient funds!!!, please contact Top it Up.", 8000, true).show();
                    emit(ApiResponse.Error("Duplicate Payment"))

                } else {
                    emit(response)
                }
               } else {
                    logger("Payment", "Payment Response is null: $paymentResult")
                    emit(ApiResponse.Error("Payment Response is Empty"))
                }
            }
            if (response.status == ApiStatus.EXCEPTION) {
                _status.value = false
                logger("Payment", "ViewModel error: ${response.message}")
                emit(response)
            }*/
        }.asLiveData()

    }

    suspend fun savePaymentToRealm(payment: Payment, supplierId: Int) {
        try {
            if (payment.id.isNotBlank()) {
                //TODO - Add to repository
                database.insertPayment(payment = payment, supplierId = supplierId)
            }
        } catch (e: Exception) {
            logger("Payment", "An Error occurred saving the Payment", e)
        }
        _paymentLiveData.value = null
    }
}