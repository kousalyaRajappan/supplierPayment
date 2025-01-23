package za.co.topitup.suppliers.ui.payment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import za.co.topitup.suppliers.CashManActivity
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.PaymentReceiptBinding
import za.co.topitup.suppliers.models.Payment
import za.co.topitup.suppliers.utils.logger
import za.co.topitup.suppliers.utils.toCurrency

const val TAG = "ReceiptDialog"

class ReceiptDialogFragment : DialogFragment() {

    var payment: Payment? = null

    //private lateinit var bitmap: Bitmap

    // Use this instance of the interface to deliver action events
    private lateinit var listener: ReceiptDialogListener
    private val paymentViewModel: PaymentViewModel by activityViewModels()

    private var _binding: PaymentReceiptBinding? = null
    private val binding get() = _binding!!

    interface ReceiptDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as ReceiptDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement ReceiptDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            // Inflate and set the layout for the dialog
            _binding = PaymentReceiptBinding.inflate(layoutInflater)

            val paymentStatus = paymentViewModel.paymentLiveData
            paymentStatus.observe(this) { paymentResult ->

                logger(TAG, "Payment Observed: ${paymentResult.toString()}")

                paymentResult?.let { thisPayment ->
                    binding.amountTextView.toCurrency(thisPayment.amount)
                    binding.receiptNumberTextView.text = thisPayment.id
                    binding.loadNumberTextView.text = thisPayment.paymentReference
                    binding.supplierNameTextView.text = thisPayment.supplierName
                    binding.accountTextView.text = thisPayment.account
                    binding.customerNumberTextView.text = thisPayment.customerNumber
                    binding.cashierTextView.text = thisPayment.cashier
                    binding.dateTextView.text = thisPayment.date
                    binding.timeTextView.text = thisPayment.time
                    binding.cashierTextView.text = thisPayment.cashier

                    // For receipt with logo
//                    if (thisPayment.id.isNotBlank()) {
//                        paymentViewModel.getSupplierLogo(thisPayment.supplierName)
//                            .observe(this) { logo ->
//                                if (logo.isNullOrEmpty().not()) {
//                                    loadSupplierImage(requireContext(), binding.imageView, logo)
//                                }
//                            }
//                    }
                    payment = paymentResult
                }
            }


            //builder.setView(inflater.inflate(R.layout.payment_receipt, null))
            builder.setView(binding.root)

                //.setMessage("Receipt")
                .setPositiveButton(
                    getString(R.string.receiptDialogPositive)
                ) { _, _ ->
                    listener.onDialogPositiveClick(this)
                    logger(TAG, "Positive")
                    payment?.let { it1 ->
                        //Uncomment to print this dialog as the receipt and send the bitmap in  PrintReceipt().
                        //bitmap = binding.root.drawToBitmap()

                        //Print receipt using default strings etc.
                        logger(TAG, "Printing Receipt $it1")

                        (activity as CashManActivity).printer.PrintReceipt(it1,null, false).start()
                    }
                    paymentStatus.removeObservers(this)
                }
                .setNegativeButton(
                    getString(R.string.receiptDialogNegative)
                ) { _, _ ->
                    listener.onDialogNegativeClick(this)
                    paymentStatus.removeObservers(this)
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