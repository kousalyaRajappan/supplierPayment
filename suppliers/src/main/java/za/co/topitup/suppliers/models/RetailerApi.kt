package za.co.topitup.suppliers.models

import com.google.gson.annotations.SerializedName

data class RetailerApi(
    @SerializedName("customer_id")
    var id: String?,

    @SerializedName("cashms_acc_number")
    var accountNumber: String?,

    @SerializedName("province_id")
    var provinceId: Int?,

    @SerializedName("dc_code")
    var dcCode: String?,
    )


