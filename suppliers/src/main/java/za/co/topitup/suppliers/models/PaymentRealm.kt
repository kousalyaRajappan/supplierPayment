package za.co.topitup.suppliers.models

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import java.util.*

open class PaymentRealm(
    @PrimaryKey
    var id: String = ObjectId().toHexString(),
    var paymentReference: String = "",
    var fullDate: Date? = null,
    var amount: Double = 0.0,
    var name: String = "",
    @LinkingObjects("payments")
    val supplier: RealmResults<SupplierRealm>? = null,
    var date: String? = "",
    var time: String? = "",
    var cashier: String? = "",
    var account: String? = "",
    var customerNumber: String? = "",
    var driverNumber: String? = "",
    var driverPhone: String? = "",

    ) : RealmObject()