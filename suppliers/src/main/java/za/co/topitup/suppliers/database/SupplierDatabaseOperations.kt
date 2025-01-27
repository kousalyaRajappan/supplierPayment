package za.co.topitup.suppliers.database

import io.realm.*
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import za.co.topitup.suppliers.models.Payment
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.models.Supplier
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.utils.logger
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val config: RealmConfiguration = providesRealmConfig()

const val TAG = "Supplier DB"

class SupplierDatabaseOperations {

    /* suspend fun insertSupplier(
         name: String, category: String, logo: String, vendorActivated: Boolean, requiresActivation: Boolean,
         latitude: Double, longitude: Double, distance: Int,
     ) {
         val realm = Realm.getInstance(config)
         realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
             val supplier = SupplierRealm(
                 name = name,
                 category = category,
                 logo = logo,
                 vendorActivated = vendorActivated,
                 requiresActivation = requiresActivation,
                 latitude = latitude,
                 longitude = longitude,
                 distance = distance,
                 linkId = linkId,
             )
             //realmTransaction.insert(supplier)
             realmTransaction.insertOrUpdate(supplier)
         }
         realm.close()
     }
 */

    suspend fun insertSuppliers(suppliers: List<Supplier>) {
        val realm = Realm.getInstance(config)

        val realmList: RealmList<SupplierRealm> = RealmList()
        suppliers.forEach {

            val supplierRealm = SupplierRealm()
            supplierRealm.id = it.id
            supplierRealm.name = it.name
            supplierRealm.category = it.category
            supplierRealm.vendorActivated = it.vendorActivated
            supplierRealm.requiresActivation = it.requiresActivation
            supplierRealm.activationPending = it.activationPending
            supplierRealm.latitude = it.latitude
            supplierRealm.longitude = it.longitude
            supplierRealm.logo = it.logo
            supplierRealm.distance = it.distance
            supplierRealm.lastPurchase = it.lastPurchase
            supplierRealm.phone = it.phone
            supplierRealm.address = it.address
            supplierRealm.accountNumber = it.accountNumber
            supplierRealm.province = it.province
            supplierRealm.coverage = it.coverage
            supplierRealm.linkId = it.linkId

            realmList.add(supplierRealm)
        }

        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            realmTransaction.copyToRealmOrUpdate(realmList)
        }
        realm.close()
    }

    private fun mapSupplier(supplierRealm: SupplierRealm): Supplier {
        return Supplier(
            id = supplierRealm.id,
            name = supplierRealm.name,
            latitude = supplierRealm.latitude,
            longitude = supplierRealm.longitude,
            logo = supplierRealm.logo,
            vendorActivated = supplierRealm.vendorActivated,
            requiresActivation = supplierRealm.requiresActivation,
            activationPending = supplierRealm.activationPending,
            distance = supplierRealm.distance,
            category = supplierRealm.category,
            lastPurchase = supplierRealm.lastPurchase,
            address = supplierRealm.address,
            accountNumber = supplierRealm.accountNumber,
            phone = supplierRealm.phone,
            province = supplierRealm.province,
            coverage = supplierRealm.coverage,
            linkId = supplierRealm.linkId
        )
    }

    fun getSuppliers(name: String, type: String): RealmResults<SupplierRealm> {
        val realm = Realm.getInstance(config)

        return realm
            .where(SupplierRealm::class.java)
            .and().equalTo("activationPending", false)
            .and().contains("category", type)
            .and().contains("name", name, Case.INSENSITIVE)
            .sort("name", Sort.ASCENDING, "distance", Sort.ASCENDING)
            .findAllAsync()
    }

    suspend fun getSupplier(supplierId: Int): Supplier? {
        val realm = Realm.getInstance(config)

        var supplier: Supplier? = null
        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            val supplierRealm = realmTransaction
                .where(SupplierRealm::class.java)
                .and().equalTo("id", supplierId)
                .findFirst()

            supplier = supplierRealm?.let { mapSupplier(it) }
        }
        realm.close()
        return supplier
    }

    fun getSupplierLogoByName(supplierName: String): String {
        val realm = Realm.getInstance(config)
        val supplier = realm
            .where(SupplierRealm::class.java)
            .and().equalTo("name", supplierName)
            .findFirst()

        realm.close()
        return supplier?.logo ?: ""
    }

    fun getActivatedSuppliers(retailerProvince: String = ""): RealmResults<SupplierRealm> {
        val realm = Realm.getInstance(config)

        return realm
            .where(SupplierRealm::class.java)
            .and().contains("coverage", retailerProvince)
            .and().equalTo("vendorActivated", true)
            .or().equalTo("activationPending", true)
            .sort("name", Sort.ASCENDING)
            .findAll()
    }

    suspend fun activateSupplier(supplier: Supplier) {
        val realm = Realm.getInstance(config)

        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            val thisSupplier = realmTransaction
                .where(SupplierRealm::class.java)
                .and().equalTo("id", supplier.id)
                .findFirst()

            thisSupplier?.accountNumber = supplier.accountNumber
            thisSupplier?.vendorActivated = supplier.vendorActivated
            thisSupplier?.activationPending = supplier.activationPending
            thisSupplier?.linkId = supplier.linkId

            if (thisSupplier != null) {
                realmTransaction.insertOrUpdate(thisSupplier)
            }
        }
        realm.close()
    }

    suspend fun deactivateSupplier(unlinkSupplier: Supplier) {
        val realm = Realm.getInstance(config)

        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            val supplier = realmTransaction
                .where(SupplierRealm::class.java)
                .and().equalTo("linkId", unlinkSupplier.linkId)
                .findFirst()

            supplier?.vendorActivated = unlinkSupplier.vendorActivated
            supplier?.activationPending = false

            if (supplier != null) {
                realmTransaction.insertOrUpdate(supplier)
            }
        }
        realm.close()
    }

    suspend fun getSupplierNames(): List<String> {
        val realm = Realm.getInstance(config)
        val suppliersList = mutableListOf<String>()

        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            suppliersList.addAll(realmTransaction
                .where(SupplierRealm::class.java)
                .and().equalTo("vendorActivated", true)
                .sort("name", Sort.ASCENDING)
                .findAll()
                .map {
                    it.name
                }
            )
        }
        realm.close()

        return suppliersList
    }

    suspend fun getSupplierCategories(): List<String> {
        val realm = Realm.getInstance(config)
        val categoryList = mutableListOf<String>()

        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
            //return realm
            categoryList.addAll(realmTransaction
                .where(SupplierRealm::class.java)
                .distinct("category")
                .sort("category", Sort.ASCENDING)
                .findAll()
                .map {
                    it.category
                }
            )
        }
        realm.close()
        return categoryList
    }


    suspend fun insertPayment(
        supplierId: Int, payment: Payment,
    ) {
        val realm = Realm.getInstance(config)
        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->

            val supplier = realmTransaction
                .where(SupplierRealm::class.java)
                .equalTo("id", supplierId)
                .findFirst()!!

            val paymentRealm = realmTransaction.createObject(
                //PaymentRealm::class.java, ObjectId().toHexString()
                PaymentRealm::class.java, payment.id
            )


            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss",  Locale.getDefault())
            val fullDate: Date = formatter.parse("${payment.date} ${payment.time}") as Date

            logger(TAG, "${payment.date} ${payment.time}")
            logger(TAG, DateFormat.getDateTimeInstance().format(fullDate))

            paymentRealm.paymentReference = payment.paymentReference.toString()
            paymentRealm.amount = payment.amount
            paymentRealm.name = payment.supplierName
            paymentRealm.date = payment.date
            paymentRealm.time = payment.time
            paymentRealm.cashier = payment.cashier
            paymentRealm.account = payment.account
            paymentRealm.customerNumber = payment.customerNumber
            paymentRealm.driverNumber = payment.driverNumber
            paymentRealm.driverPhone = payment.driverPhone

            fullDate.let { date ->
                paymentRealm.fullDate = date
                supplier.lastPurchase = date
            }

            supplier.payments.add(paymentRealm)
            realmTransaction.insert(paymentRealm)

            logger("Payment DB", supplier.toString())
            logger("Payment DB", paymentRealm.toString())
        }
        realm.close()
    }


    private fun mapPayment(payment: PaymentRealm): Payment {
        return Payment(

            id = payment.id,
            paymentReference = payment.paymentReference,
            fullDate = payment.fullDate,
            amount = payment.amount,
            supplierName = payment.name,
            date = payment.date,
            time = payment.time,
            cashier = payment.cashier,
            account = payment.account,
            customerNumber = payment.customerNumber,
            driverNumber = payment.driverNumber,
            driverPhone = payment.driverPhone,
        )
    }

    fun getPayments(name: String, dateFrom: Date, dateTo: Date): RealmResults<PaymentRealm> {
        val realm = Realm.getInstance(config)

        return realm
            .where(PaymentRealm::class.java)
            .and().contains("name", name)
            .between("fullDate", dateFrom, dateTo)
            .sort("fullDate", Sort.DESCENDING)
            .findAllAsync()
    }

    fun getPayment(paymentId: String): Payment? {
        val realm = Realm.getInstance(config)
        val payment = realm
            .where(PaymentRealm::class.java)
            .and().equalTo("id", paymentId)
            .findFirst()

        realm.close()

        return if (payment != null) {
            mapPayment(payment)
        } else null

    }

    fun getLocalOrNationalSuppliersCount(retailerProvince: String): Int {
        return getActivatedSuppliers(retailerProvince).count()
    }

    //Uncomment if using SettingsFragment
//    suspend fun updateSupplierDistance(vendorLocation: Location) {
//        val realm = Realm.getInstance(config)
//
//        realm.executeTransactionAwait(Dispatchers.IO) { realmTransaction ->
//
//            val suppliers = realmTransaction
//                .where(SupplierRealm::class.java)
//                .findAll()
//
//            suppliers.map { supplier ->
//                val location = Location(supplier.name)
//                location.latitude = supplier.latitude
//                location.longitude = supplier.longitude
//
//                val distance = location.distanceTo(vendorLocation)
//                supplier.distance = distance.toInt()
//            }
//
//            realmTransaction.copyToRealmOrUpdate(suppliers)
//        }
//        realm.close()
//    }
}