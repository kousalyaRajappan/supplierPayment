package za.co.topitup.suppliers.ui.payment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import za.co.topitup.suppliers.CashManActivity
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.DialogPrintScreenBinding
import za.co.topitup.suppliers.utils.logger
import java.io.BufferedReader
import java.io.StringReader
import java.util.*


class PrintFragment : DialogFragment(){

    private var _binding: DialogPrintScreenBinding? = null

    private val binding get() = _binding!!

    private lateinit var listener: ReceiptDialogFragment.ReceiptDialogListener

    private lateinit var supplierName: String




    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ReceiptDialogFragment.ReceiptDialogListener

        } catch (e: ClassCastException) {
            Log.e("error","................"+e)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            var html = ""
            var response = ""
            val sharedPreference =  requireContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

            val res  =sharedPreference.getString("response","")
       /*     val  res:String ="1\n" +
                    "                                                                                         1Peninsula Bevereges\n" +
                    "                                                                                         1\n" +
                    "                                                                                         1Demo Test Account\n" +
                    "                                                                                         1Date       Time     Cashier\n" +
                    "                                                                                         118/08/2023 08:39:04 Zamier\n" +
                    "                                                                                         1Top it Up Acc#: PTG999\n" +
                    "                                                                                         1\n" +
                    "                                                                                         1Ref     #: 390947\n" +
                    "                                                                                         1Customer#: 123456\n" +
                    "                                                                                         1Load    #: ATH123456\n" +
                    "                                                                                         1Driver  #: 8754\n" +
                    "                                                                                         1Drive Cell: 8297466736\n" +
                    "                                                                                         1Amount: R 1.00\n" +
                    "                                                                                         1\n" +
                    "                                                                                         1    Top it Up | 0860 111 723    \n" +
                    "                                                                                         1        www.topitup.co.za       \n" +
                    "                                                                                         1"*/
            // Inflate and set the layout for the dialog
            _binding = DialogPrintScreenBinding.inflate(layoutInflater)

//            val paymentStatus = paymentViewModel.paymentLiveData


            try {
                val bufReader = BufferedReader(StringReader(res))
                var line: String? = null
                while (bufReader.readLine().also { line = it } != null) {
                    var prnt_line = ""
                    var size = ""
                    if (line!!.length > 1) {
                        prnt_line = "" + line!!.substring(1)
                        size = "" + line!!.substring(0, 1)


                    }


                    Log.e("print line","print string...."+prnt_line)

                   var newString= prnt_line +"\n"
                    response +=newString
                    if (size == "1") {
                        html += "$prnt_line<br/>"
                    } else {
                        html += "<span style=\"font-size:1.4em\">"
                        html += "$prnt_line</span><br/>"
                    }

//                }
                }
            } catch (ex: Exception) {
                //
            }
            Log.e("response from api","print response....$response")
//        val wb_slip: WebView = this.findViewById<WebViewbView>(R.id.wb_slip)
            binding.wbSlip.settings.javaScriptEnabled = false
//            binding.wbSlip.text = response
            binding.wbSlip.loadDataWithBaseURL("", html!!, "text/html", "UTF-8", "")

            //builder.setView(inflater.inflate(R.layout.payment_receipt, null))
            builder.setView(binding.root)

                //.setMessage("Receipt")
                .setPositiveButton(
                    getString(R.string.receiptDialogPositive)
                ) { _, _ ->
                    listener.onDialogPositiveClick(this)
                    logger(TAG, "Positive")

                    (activity as CashManActivity).printer.PrintReceiptNew(response,null, false).start()

                    /*payment?.let { it1 ->
                        //Uncomment to print this dialog as the receipt and send the bitmap in  PrintReceipt().
                        //bitmap = binding.root.drawToBitmap()

                        //Print receipt using default strings etc.
                        logger(TAG, "Printing Receipt $it1")

                        (activity as CashManActivity).printer.PrintReceipt(it1,null, false).start()
                    }
                    paymentStatus.removeObservers(this)*/
                }
                .setNegativeButton(
                    getString(R.string.receiptDialogNegative)
                ) { _, _ ->
                    listener.onDialogNegativeClick(this)
//                    paymentStatus.removeObservers(this)
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}