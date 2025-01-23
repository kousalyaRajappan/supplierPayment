package za.co.topitup.suppliers.utils

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import za.co.topitup.suppliers.BuildConfig
import za.co.topitup.suppliers.network.ApiResponse

fun logger(tag: String?, message: String, throwable: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (throwable != null) {
            Log.d(tag, message, throwable)
        } else {
            Log.d(tag, message)
        }
    }
}

fun checkApiResponse(response: Response<*>) : ApiResponse{

    return if (response.isSuccessful) {
        if (response.body() != null) {
            ApiResponse.Success(response.body())
        } else {
            ApiResponse.InvalidData
        }
    } else {
        when(response.code()) {
            403 -> ApiResponse.HttpErrors.ResourceForbidden(response.message())
            404 -> ApiResponse.HttpErrors.ResourceNotFound(response.message())
            500 -> ApiResponse.HttpErrors.InternalServerError(response.message())
            502 -> ApiResponse.HttpErrors.BadGateWay(response.message())
            301 -> ApiResponse.HttpErrors.ResourceRemoved(response.message())
            302 -> ApiResponse.HttpErrors.RemovedResourceFound(response.message())
            else -> ApiResponse.Error(response.message())
        }
    }
}


fun checkApiResponsePayment(response: Call<ResponseBody>) : ApiResponse{

    Log.e("response" ,"response in ckeck api"+response.toString())
    return if (!response.equals("")) {


        ApiResponse.Success(response)

//        }
    } else {


        ApiResponse.Error("error")


    }
}
fun checkApiResponsePeninsula(response: String) : ApiResponse{
    Log.e("response body",".....before..."+response)


    return if (!response.equals("")) {

      /*  if (response.contains("duplicate")) {
            ApiResponse.Error("Duplicate values")

        } else {*/

            ApiResponse.Success(response)

//        }
    } else {


        ApiResponse.Error("error")


    }
}
