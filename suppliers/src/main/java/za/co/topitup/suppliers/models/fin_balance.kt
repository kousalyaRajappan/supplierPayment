package za.co.topitup.suppliers.models

import com.google.gson.annotations.SerializedName

class fin_balance (
    @SerializedName("balance")
    var balance: String?,

    @SerializedName("balance_cash")
    var balance_cash: String?,

    @SerializedName("acn1")
    var acn1: String?,



    )