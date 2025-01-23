package za.co.topitup.suppliers.ui.supplier.manage

import android.util.Log
import androidx.lifecycle.*
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.models.Supplier
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.network.ApiResponse
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository

class AddSupplierViewModel : ViewModel() {

    private val repository = SupplierRepository()

    /*fun getCategories(): LiveData<List<String>> {
        Log.d(TAG, "viewModel getCategories")
        val list = MutableLiveData<List<String>>().apply {
            viewModelScope.launch {
                value = SupplierDatabaseOperations().getSupplierCategories()
            }
        }
        return list
    }*/
   // fun initSupplierEntryList(): RealmResults<SupplierRealm> {
    fun initSupplierEntryList() : RealmResults<SupplierRealm> {

//        if(repository.getSuppliers("","").equals())

            return repository.getSuppliers("", "")

    }

    fun getCategories(): Flow<List<String>> = flow {
        val list = MutableLiveData<List<String>>().apply {
            value = repository.getSupplierCategories()
        }
        list.value?.let { emit(it)  }
    }.flowOn(Dispatchers.Main)

    fun activateSupplier(supplier: Supplier) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.activateSupplier(supplier)
        }
    }

    val sendSupplierLinkToApi = fun (supplierId: Int, retailerId: String, accountNumber: String): LiveData<ApiResponse> {

        //emit(ApiResult.Loading(isLoading = true)) // 1. Loading State
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = repository.linkSupplierInAPI(supplierId, retailerId, accountNumber)

            if (response.status == ApiStatus.SUCCESS) {
                //val result = response.data
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION) {
                //_status.value = false
                Log.d("Link", "ViewModel error: ${response.message}")
                emit(response)
            }
        }.asLiveData()

    }
}