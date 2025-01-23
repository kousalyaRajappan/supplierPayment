package za.co.topitup.suppliers.database

import io.realm.RealmConfiguration
import io.realm.RealmMigration
import java.util.*

private const val realmVersion = 14L

val migration = RealmMigration { realm, oldVersion, _ ->
    var currentVersion = oldVersion

    if (currentVersion == 1L) {
        realm.schema.get("PaymentRealm")
            ?.addField("name", String::class.java)
        currentVersion++
    }
    if (currentVersion == 2L) {
        realm.schema.get("SupplierRealm")
            ?.addField("distance", Int::class.java)
            ?.addField("vendorActivated", Boolean::class.java)
            ?.removeField("latitude")
            ?.addField("latitude", Double::class.java)

        realm.schema.get("SupplierRealm")
            ?.transform {
                it.setDouble("latitude", 0.0)
            }

        currentVersion++
    }
    if (currentVersion == 3L) {
        realm.schema.get("SupplierRealm")
            ?.removeField("activated")
            ?.addField("requiresActivation", Boolean::class.java)
            ?.removeField("longitude")
            ?.addField("longitude", Double::class.java)

        currentVersion++
    }
    if (currentVersion == 4L) {
        realm.schema.get("PaymentRealm")
            ?.addField("date2", Date::class.java)
            ?.transform { record ->
                val date = Date(record.getLong("date"))
                record.set("date2", date)
            }
            ?.removeField("date")
            ?.renameField("date2", "date")

        currentVersion++
    }
    if (currentVersion == 5L) {
        realm.schema.get("SupplierRealm")
            ?.addField("category", String::class.java)
            ?.setRequired("category", true)

        realm.schema.get("SupplierRealm")
            ?.transform {
                it.setString("category", "All")
            }
        currentVersion++
    }
    if (currentVersion == 6L) {
        realm.schema.get("SupplierRealm")
            ?.addField("lastPurchase", Date::class.java)
            ?.addField("activationPending", Boolean::class.java)

        currentVersion++
    }
    if (currentVersion == 7L) {
        realm.schema.get("SupplierRealm")
            ?.addField("activationPending", Boolean::class.java)

        currentVersion++
    }
    if (currentVersion == 8L) {
        realm.schema.get("SupplierRealm")
            ?.addField("accountNumber", String::class.java)
            ?.addField("phoneNumber", String::class.java)
            ?.addField("address", String::class.java)

        currentVersion++
    }
    if (currentVersion == 9L) {
        realm.schema.get("SupplierRealm")
            ?.renameField("phoneNumber", "phone")
        currentVersion++
    }
    if (currentVersion == 10L) {
        realm.schema.get("PaymentRealm")
            ?.renameField("reference", "paymentReference")
            ?.renameField("date", "fullDate")
            ?.addField("date", String::class.java)
            ?.addField("time", String::class.java)
            ?.addField("cashier", String::class.java)
            ?.addField("account", String::class.java)
            ?.addField("customerNumber", String::class.java)
            ?.addField("driverNumber", String::class.java)
            ?.addField("driverPhone", String::class.java)

        currentVersion++
    }

    if (currentVersion == 11L) {
        realm.schema.get("SupplierRealm")
            ?.removeField("id")
            ?.addField("id", Int::class.java)

        realm.schema.get("SupplierRealm")
            ?.transform {
                it.setDouble("latitude", 0.0)
            }
        currentVersion++
    }
    if (currentVersion == 12L) {
        realm.schema.get("SupplierRealm")
            ?.addField("province", String::class.java)
            ?.addField("coverage", String::class.java)
        currentVersion++
    }
    if (currentVersion == 13L) {
    realm.schema.get("SupplierRealm")
        ?.addField("linkId", String::class.java)

    // Todo - Uncomment when adding another migration
    //currentVersion++
}
}

fun providesRealmConfig(): RealmConfiguration = RealmConfiguration
    .Builder()
    .name("suppliers.realm")
    .schemaVersion(realmVersion)
    .migration(migration)
    .build()

/*val config = RealmConfiguration.Builder(schema = setOf())
    .build()*/
//val realm: Realm = Realm.open(config)
