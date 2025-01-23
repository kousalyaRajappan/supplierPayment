package za.co.topitup.suppliers.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Supplier(
    val id: Int,
    val name: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val logo: String = "",
    var distance: Int = 0,
    val phone: String? = "",
    val address: String? = "",
    val province: String? = "",
    val coverage: String? = "",
    var accountNumber: String? = "",
    var vendorActivated: Boolean = false,
    var requiresActivation: Boolean = false,
    var activationPending: Boolean = false,
    var lastPurchase: Date? = null,
    @SerializedName("link_id")
    var linkId: String? = null,
)
