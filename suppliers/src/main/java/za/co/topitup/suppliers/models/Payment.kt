package za.co.topitup.suppliers.models

import com.google.gson.annotations.SerializedName
import io.realm.annotations.PrimaryKey
import java.util.*

data class Payment(
    @PrimaryKey
    @SerializedName("reference")
    val id: String,

    @SerializedName("supplier")
    var supplierName: String = "",

    @SerializedName("load_no")
    val paymentReference: String? = "",

    val fullDate: Date? = null,
    val amount: Double = 0.0,
    val date: String? = "",
    val time: String? = "",
    val cashier: String? = "",

    @SerializedName("accountNo")
    val account: String? = "",

    val customerNumber: String? = "",
    val driverNumber: String? = "",

    @SerializedName("driver_cell")
    val driverPhone: String? = "",
)
