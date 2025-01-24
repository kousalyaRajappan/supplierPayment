package za.co.topitup.suppliers.ui.history

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.network.RetroInsatnce
import za.co.topitup.suppliers.network.SupplierEndpointAPI
import java.util.*
import kotlin.collections.ArrayList

class HistoryViewModell : ViewModel() {

     lateinit var  livepaymentlist: MutableLiveData<ArrayList<MyItem>>
    init {

        livepaymentlist = MutableLiveData()
    }
    fun getLivePaymentObserver():MutableLiveData<ArrayList<MyItem>>{

        return  livepaymentlist
    }
    fun makeAPICall(startDate: String, endDate: String, txt: String) {

//        val supplierList = SupplierRepository().getPaymentSearchAPI("2023-06-01","2023-12-1","")

        var movies = ArrayList<MyItem>()

        val retroInsatnce =RetroInsatnce.getRetroInstance()
        val supplierEndpointAPI = retroInsatnce.create(SupplierEndpointAPI::class.java)

         var headerMap: HashMap<String, String> = HashMap()
        headerMap["device"] = Retailer.deviceType
        headerMap["license"] = Retailer.licence
        headerMap["posuser"] = Retailer.posUserId
        val supplierEndpointRequest: Call<ResponseBody> = supplierEndpointAPI.get_payment_search(headerMap,startDate,endDate,txt)
        supplierEndpointRequest.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                var res = ""
                res = try {
                    response.body()!!.string()
                } catch (ex: Exception) {
                    return
                }
                if (res.contains("<error><err>") || res.contains("ERR:") || res.contains("\"err\"")) {
//                    val matcher: String = StringUtils.substringBetween(res, "<err>", "</err>")

                    livepaymentlist.postValue(null);



                    //Toasty.info(mContext, matcher, 8000, true).show();
                    /* tv_response.setTextColor(Color.parseColor("#ff0000"))
                     tv_response.setText(matcher)
                     txtAccountNumber.setEnabled(true)
                     dialogEditText.setEnabled(true)
                     dialogEditText_cent.setEnabled(true)
                     dialogEditText.requestFocus()*/


                    //Toasty.info(mContext, matcher, 8000, true).show();
                } else {

                    Log.e("response", "..res........" + res)
//                var res = supplierList.value.toString()
                    val toSplit: Array<String> =
                        res.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (i in toSplit.indices) {
                        val s = toSplit[i]
                        //rowCount++;
                        val tokens = s.split("\\^".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
//                        Timber.i("tokens" + i + " value" + tokens[3])
                        val test = MyItem()
                        test.supplierRef = tokens[0]
                        test.sup_driver = tokens[4]
                        test.sup_driver_cell = tokens[5]


                        test.supShipment = tokens[6]
                        test.supCustomer = tokens[2]

                        test.supSupplierName = tokens[7]
                        try {
                            val value = tokens[3]
                            test.supAmount = " R " + value
                        } catch (ex: Exception) {
                            test.supAmount = tokens[3]
                        }
                        // Timber.i("REPRINT: " + value);
                        test.btn_date = tokens[1]
//                    test.btn_user = tokens[5]
                        movies.add(test)
                    }
                    livepaymentlist.postValue(movies)
                }


            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                livepaymentlist.postValue(null)


            }
        })


    }
}