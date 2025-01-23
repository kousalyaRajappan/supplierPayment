package za.co.topitup.suppliers.network

enum class ApiStatus{
    SUCCESS,
    ERROR,
    LOADING,
    EXCEPTION,
    INVALID
}

sealed class ApiResponse(val status: ApiStatus, val data: Any?, val message:String?){

    data class Success<out R>(val _data : R?) : ApiResponse(
        status = ApiStatus.SUCCESS,
        data = _data,
        message = null
    )

    data class Loading<out R>(val _data : R? = null, val isLoading: Boolean) : ApiResponse(
        status = ApiStatus.LOADING,
        data = _data,
        message = null
    )

    object InvalidData : ApiResponse(
        status = ApiStatus.INVALID,
        data = null,
        message = null
    )

    data class Error(val _error : String?) : ApiResponse(
        status = ApiStatus.ERROR,
        data = null,
        message = _error
    )

    data class NetworkException(val _error : String?) : ApiResponse(
        status = ApiStatus.EXCEPTION,
        data = null,
        message = _error,
    )

    sealed class HttpErrors : ApiResponse(
        status = ApiStatus.EXCEPTION,
        data = null,
        message = null,
    ) {
        data class ResourceForbidden(val exception: String) : HttpErrors()
        data class ResourceNotFound(val exception: String) : HttpErrors()
        data class InternalServerError(val exception: String) : HttpErrors()
        data class BadGateWay(val exception: String) : HttpErrors()
        data class ResourceRemoved(val exception: String) : HttpErrors()
        data class RemovedResourceFound(val exception: String) : HttpErrors()
    }
}