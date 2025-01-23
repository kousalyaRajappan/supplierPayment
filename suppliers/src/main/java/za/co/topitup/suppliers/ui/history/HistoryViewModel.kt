package za.co.topitup.suppliers.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.realm.RealmResults
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.repositories.SupplierRepository
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel : ViewModel() {

    private val repository = SupplierRepository()
    private val database = SupplierDatabaseOperations()

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    //TODO Move to repository
    suspend fun getSupplierNames(): LiveData<List<String>> {
        val list = MutableLiveData<List<String>>().apply {
            value = database.getSupplierNames()
        }
        return list
    }

    //TODO Move to repository
    fun getPayments(name:String, date: String): RealmResults<PaymentRealm> {
        //TODO Refactor - Duplicated with RecyclerView
        val searchDate = if (date == "") {
            "2022-05-01"
        } else {
            date
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateFrom = formatter.parse(searchDate)

        val formatter2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTo = if (searchDate == "2022-05-01") {
            formatter2.parse("2099-01-01 23:59:59")
        } else {
            formatter2.parse(searchDate.plus(" 23:59:59"))
        }
        return database.getPayments(name, dateFrom as Date, dateTo as Date)
    }


    fun getPayment(paymentId: String) = liveData {
            repository.getPaymentFromDB(paymentId)?.let { emit(it) }
        }


    fun getSupplierLogo(supplierId: String ) = liveData {
        emit( repository.getSupplierLogoFromRealmByName(supplierId) )
    }

    fun getSupplierLogoByName(supplierName: String ) = liveData {
        emit(repository.getSupplierLogoFromRealmByName(supplierName))
    }



}