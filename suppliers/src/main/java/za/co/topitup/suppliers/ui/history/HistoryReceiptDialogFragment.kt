package za.co.topitup.suppliers.ui.history

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.core.view.drawToBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import za.co.topitup.suppliers.CashManActivity
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.PaymentReceiptBinding
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.utils.toCurrency

class HistoryReceiptDialogFragment : DialogFragment() {

    private lateinit var dateHis: String

    private lateinit var userHis: String
    private lateinit var descHis: String
    private lateinit var amountHis: String
    private lateinit var paymentId: String

    private lateinit var refHis: String
    private lateinit var driverHis: String
    private lateinit var drivercellHis: String
    private lateinit var custHis: String
    private lateinit var shipmentHis: String
    private lateinit var supplierNameHis: String




    private lateinit var bitmap: Bitmap

    private val historyViewModel: HistoryViewModel by activityViewModels()

    private var _binding: PaymentReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        /*paymentId = arguments?.getString(ID) ?: throw IllegalStateException("No arguments provided.")
        var payment : Payment? = null*/

         amountHis = arguments?.getString(AMOUNT)!!
         dateHis = arguments?.getString(DATE)!!
         refHis = arguments?.getString(REF)!!
         custHis = arguments?.getString(CUST)!!
        driverHis = arguments?.getString(DRIVER)!!
        drivercellHis = arguments?.getString(DRIVER_CELL)!!

        supplierNameHis = arguments?.getString(SUPPLIER_NAME)!!
        shipmentHis = arguments?.getString(SHIPMENT)!!
        var newStrg= dateHis
        val mString = newStrg!!.split(",").toTypedArray()

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            _binding = PaymentReceiptBinding.inflate(layoutInflater)


            binding.amountTextView.text= amountHis

            binding.driverNumberTextView.text = driverHis
            binding.drivercellNumberTextView.text = drivercellHis
            binding.shipNumberTextView.text = shipmentHis
            binding.loadNumberTextView.text = refHis
            binding.supplierNameTextView.text = supplierNameHis
//            binding.accountTextView.text = thisPayment.account
            binding.customerNumberTextView.text = custHis
//            binding.cashierTextView.text = thisPayment.cashier
            binding.dateTextView.text = mString[0]
            binding.timeTextView.text = mString[1]

           var myPaymentItem: MyItem = MyItem()
            myPaymentItem.supAmount = amountHis
            myPaymentItem.supplierRef = refHis
            myPaymentItem.supCustomer = custHis
            myPaymentItem.sup_driver = driverHis
            myPaymentItem.sup_driver_cell = drivercellHis
            myPaymentItem.btn_date = dateHis
            myPaymentItem.supShipment = shipmentHis
//            binding.cashierTextView.text = thisPayment.cashier

//            val paymentLiveData = historyViewModel.getPayment(paymentId)
/*
            paymentLiveData.observe(this) { response ->

                response?.let { thisPayment ->

                    binding.amountTextView.toCurrency(thisPayment.amount)
                    binding.receiptNumberTextView.text = thisPayment.id
                    binding.loadNumberTextView.text = thisPayment.paymentReference
                    binding.supplierNameTextView.text = thisPayment.supplierName
                    binding.accountTextView.text = thisPayment.account
                    binding.customerNumberTextView.text = thisPayment.customerNumber
                    binding.cashierTextView.text = thisPayment.cashier
                    binding.dateTextView.text = thisPayment.date
                    binding.timeTextView.text = thisPayment.time?.dropLast(3)
                    binding.cashierTextView.text = thisPayment.cashier

                    // For receipt with logo
//                    historyViewModel.getSupplierLogoByName(thisPayment.supplierName)
//                        .observe(this) { logo ->
//                            if (logo.isNullOrEmpty().not()) {
//                                loadSupplierImage(requireContext(), binding.imageView, logo)
//                            }
//                        }
                    payment = thisPayment
                }
            }
*/

            builder.setView(binding.root)
                .setMessage("Reprint Receipt")
                .setPositiveButton(
                    getString(R.string.receiptDialogPositive)
                ) { _, _ ->
                    myPaymentItem?.let { it1 -> (activity as CashManActivity).printer.PrintSupplierReceipt(it1,null, true).start() }
                }
                .setNegativeButton(
                    getString(R.string.receiptDialogNegative)
                ) { _, _ ->
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

//        private const val ID = "paymentId"

        private const val DATE = "date"
        private const val REF = "ref"
        private const val CUST = "cust"
        private const val DRIVER = "driver"
        private const val DRIVER_CELL = "driver_cell"

        private const val AMOUNT = "amount"
        private const val SHIPMENT = "shipment"
        private const val SUPPLIER_NAME = "supplier_name"



        fun newInstance(
            date: String,
            ref: String,
            cust: String ,
            driver: String,
            driverCell: String ,
            amount: String,
            shipment : String,
            supplierName:String
        ): HistoryReceiptDialogFragment = HistoryReceiptDialogFragment().apply {
            arguments = Bundle().apply {
                putString(DATE, date)
                putString(REF, ref)
                putString(CUST, cust)
                putString(DRIVER, driver)
                putString(DRIVER_CELL, driverCell)
                putString(AMOUNT, amount)
                putString(SHIPMENT ,shipment)
                putString(SUPPLIER_NAME,supplierName)

            }
        }
    }
}