package za.co.topitup.suppliers.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import za.co.topitup.suppliers.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

fun TextView.toDistanceString(distance: Int) {

    if (distance < 1000) {
        this.text = distance.toString().plus(" m")
    } else {
        this.text = (distance / 1000).toString().plus(" km")
    }
}

fun TextView.toDateString(date: Date, pattern: String? = "yyyy-MM-dd @ HH:mm") {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    this.text = simpleDateFormat.format(date)

}
fun Any.toCurrency(value: Double): String {
    return String.format("R %.2f", value)
}

fun TextView.toCurrency(value: String) {
    //this.text = String.format("%.2f", value)
    //this.text = value.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toPlainString()
    //val currency = context.resources.getString(R.string.default_currency)
    val localeLanguage = context.getString(R.string.locale_language)
    val localeCode = context.getString(R.string.locale_code)
    val locale = Locale(localeLanguage, localeCode)
    /* The Simple way
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance(currency)
    */

    val format: DecimalFormat = NumberFormat.getCurrencyInstance() as DecimalFormat
    val symbol = Currency.getInstance(locale).getSymbol(locale)
    format.maximumFractionDigits = 2
    format.minimumFractionDigits = 2
    format.isGroupingUsed = true
    format.groupingSize = 3
    format.positivePrefix = symbol.plus(" ")

    this.text = format.format(value)
}


fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE)
                as InputMethodManager
    //val imm = ContextCompat.getSystemService(context, InputMethodManager::class.java) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.toast(msg: String, duration: Int = Toast.LENGTH_LONG){
    Toast.makeText(this, msg, duration).show()
}

fun Context.toast(@StringRes resId: Int,  duration: Int = Toast.LENGTH_LONG){
    toast(getString(resId), duration)
}

