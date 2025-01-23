package za.co.topitup.suppliers.ui.supplier.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.models.Supplier
import za.co.topitup.suppliers.network.ApiResponse
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository
import za.co.topitup.suppliers.utils.logger

private const val TAG = "SupplierInfoViewModel"

class SupplierInfoViewModel : ViewModel() {

    private val supplierRepository = SupplierRepository()
    private val database = SupplierDatabaseOperations()

    val getSupplier = fun (supplierId: Int) = flow {
        val result = database.getSupplier(supplierId)
            if (result != null) {
                emit(result)
            }
    }.asLiveData()

    val deactivateSupplier = fun (linkId: String) : Flow<ApiResponse> {
        return flow {
            emit(ApiResponse.Loading(_data = null, isLoading = true)) // 1. Loading State
            val response = supplierRepository.unlinkSupplierInAPI(linkId)

            if (response.status == ApiStatus.SUCCESS) {
                supplierRepository.deactivateSupplier(response.data as Supplier).toString()
                emit(response)
            }
            if (response.status == ApiStatus.EXCEPTION || response.status == ApiStatus.ERROR) {
                logger(TAG, response.message.toString())
                emit(response)
            }
        }
    }
}