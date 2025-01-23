package za.co.topitup.suppliers.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import za.co.topitup.suppliers.utils.Constants.KEY_LOCATION_ENABLED

private const val FIRST_RUN = "first_run"
private const val VENDOR_LATITUDE = "vendor_latitude"
private const val VENDOR_LONGITUDE = "vendor_longitude"
private const val RETAILERID = "retailer_id"
const val KEY_ACCOUNT = "account"

class AppPreferences(context: Context?) {

    // Create Shared Preference File
    val preferences: SharedPreferences =
        context!!.getSharedPreferences("co.za.topitup.suppliers", Context.MODE_PRIVATE)

    var firstRun: Boolean
        get() = preferences.getBoolean(FIRST_RUN, true)
        set(value) = preferences.edit().putBoolean(FIRST_RUN, value).apply()

    var vendorLatitude: String?
        get() = preferences.getString(VENDOR_LATITUDE, "0.0")
    set(value) = preferences.edit().putString(VENDOR_LATITUDE, value).apply()

    var vendorLongitude: String?
        get() = preferences.getString(VENDOR_LONGITUDE, "0.0")
        set(value) = preferences.edit().putString(VENDOR_LONGITUDE, value).apply()

    var locationEnabled: Boolean
        get() = preferences.getBoolean(KEY_LOCATION_ENABLED, false)
        set(value) = preferences.edit().putBoolean(KEY_LOCATION_ENABLED, value).apply()

    var accountNumber: String?
        get() = preferences.getString(KEY_ACCOUNT, "")
        set(value) = preferences.edit().putString(KEY_ACCOUNT, value).apply()

    var RETAILERID: String?
        get() = preferences.getString(RETAILERID, "")
        set(value) = preferences.edit().putString(RETAILERID, value).apply()


    inline fun <reified T> observeKey(key: String, default: T): Flow<T>  {
        val flow = MutableStateFlow(getItem(key, default))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (key == k) {
                flow.value = getItem(key, default)!!
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)

        return flow
            .onCompletion { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    inline fun <reified T> getItem(key: String, default: T): T {
        @Suppress("UNCHECKED_CAST")
        return when (default){
            is String -> preferences.getString(key, default) as T
            is Int -> preferences.getInt(key, default) as T
            is Long -> preferences.getLong(key, default) as T
            is Boolean -> preferences.getBoolean(key, default) as T
            is Float -> preferences.getFloat(key, default) as T
            is Set<*> -> preferences.getStringSet(key, default as Set<String>) as T
            is MutableSet<*> -> preferences.getStringSet(key, default as MutableSet<String>) as T
            else -> throw IllegalArgumentException("generic type not handle ${T::class.java.name}")
        }
    }

}