package za.co.topitup.suppliers.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class SupplierRealm(
    @PrimaryKey
    var id: Int = 0,
    @Required
    var name: String = "",
    var category: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var logo: String = "",
    var distance: Int = 0,
    var phone: String? = "",
    var address: String? = "",
    var province: String? = "",
    var coverage: String? = "",
    var accountNumber: String? = "",
    var vendorActivated: Boolean = false,
    var requiresActivation: Boolean = false,
    var activationPending: Boolean = false,
    var payments: RealmList<PaymentRealm> = RealmList(),
    var lastPurchase: Date? = null,
    var linkId: String? = null,
    )  : RealmObject()