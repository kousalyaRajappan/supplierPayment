package za.co.topitup.suppliers.models

import com.google.gson.annotations.SerializedName

data class RetailerApiNew (


    @SerializedName("cashms_acc_number")
    var accountNumber: String?,

    @SerializedName("dc_code")
    var dcCode: String?,
)


