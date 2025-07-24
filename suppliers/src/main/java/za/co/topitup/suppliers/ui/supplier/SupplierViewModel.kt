package za.co.topitup.suppliers.ui.supplier

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.network.ApiResponse
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository

class SupplierViewModel : ViewModel() {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text*/

    private val supplierRepository = SupplierRepository()

    private val _localSupplierCount = MutableLiveData<Int>()
    val localSupplierCount: LiveData<Int> = _localSupplierCount

    private val _nationalSupplierCount = MutableLiveData<Int>()
    val nationalSupplierCount: LiveData<Int> = _nationalSupplierCount

    fun initSupplierEntryList(): RealmResults<SupplierRealm> {
        return supplierRepository.getActivatedSuppliersFromDB()
    }

    fun getSuppliersCountByType(retailerProvince: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _localSupplierCount.postValue(
                SupplierDatabaseOperations().getLocalOrNationalSuppliersCount(retailerProvince)
            )

            _nationalSupplierCount.postValue(
                SupplierDatabaseOperations().getLocalOrNationalSuppliersCount("National")
            )
        }
    }

    val getAdvertUrls = fun(): Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.getAdvertURLsFromAPI()

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                emit(response)
            }
        }
    }

    val getRetailerInfo = fun(): Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.getRetailerInfo()

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                emit(response)
            }

        }
    }
    val getRetailerInfoNew = fun(): Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.getRetailerInfoNew()

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                emit(response)
            }

        }
    }
    val getBalanceInfo = fun(): Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.getBalanceInfo()

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                Log.e("exception", "ex00" + response.message)
                emit(response)
            }

        }
    }
    val updateVer: (String, String) -> Flow<ApiResponse> = { version, licence ->
        flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // Loading state

            val response = supplierRepository.getUpdateInfo(version, licence)

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            } else if (response.status == ApiStatus.EXCEPTION) {
                Log.e("exception", "ex00" + response.message)
                emit(response)
            }
        }
    }
    /*val updateVer:(version: String,licence:String) = fun(): Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.getUpdateInfo()

            if (response.status == ApiStatus.SUCCESS) {
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                Log.e("exception", "ex00" + response.message)
                emit(response)
            }

        }
    }*/


}

