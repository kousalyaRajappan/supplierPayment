package za.co.topitup.suppliers.ui.payment

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.ActivityCashManagmentBinding
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.utils.hideKeyboard
import za.co.topitup.suppliers.utils.logger
import za.co.topitup.suppliers.utils.toast
import java.util.*

const val ARG_SUPPLIER_ID_PEN = "supplier_id"
const val ARG_SUPPLIER_NAME_PEN = "supplier_name"
const val ARG_SUPPLIER_ACCOUNT_NUMBER_PEN = "supplier_account_number"


class PaymentFragmentPeninsula : Fragment(){
    private val paymentViewModel: PaymentViewModel by activityViewModels()


    private lateinit var _binding: ActivityCashManagmentBinding
    private val binding get() = _binding!!
    private var supplierId: Int = 0
    private lateinit var supplierName: String
    private lateinit var supplierAccountNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            supplierId = it.getInt(ARG_SUPPLIER_ID_PEN)
            supplierName = it.getString(ARG_SUPPLIER_NAME_PEN).toString()
            supplierAccountNumber = it.getString(ARG_SUPPLIER_ACCOUNT_NUMBER_PEN).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityCashManagmentBinding.inflate(
            inflater, container, false
        )


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val payment = PaymentRealm()

        binding.PaymentReferenceTextInputLayout.editText?.addTextChangedListener {
            payment.paymentReference = binding.PaymentReferenceTextInputLayout.editText?.text.toString()
        }
        binding.driverNumberTextInputLayout.editText?.addTextChangedListener {
            payment.driverNumber = binding.driverNumberTextInputLayout.editText?.text.toString()
        }
        binding.driverPhoneTextInputLayout.editText?.addTextChangedListener {
            payment.driverPhone = binding.driverPhoneTextInputLayout.editText?.text.toString()
        }

        binding.amountTextInputLayout.editText?.addTextChangedListener {
            val amount = binding.amountTextInputLayout.editText?.text.toString()
            if (amount.isEmpty()) {
                payment.amount = 0.0
            } else {
                if (amount != ".") {
                    payment.amount = amount.toDouble()
                }
            }
        }

        binding.payButton.setOnClickListener {
            payment.fullDate = Date()
            logger(TAG, payment.paymentReference)
            logger(TAG, payment.amount.toString())

            if (payment.paymentReference == " " || payment.paymentReference.isBlank()) {
                logger(TAG, payment.paymentReference )
                Toast.makeText(requireContext(),"Reference cannot be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
            if (payment.driverNumber == " " || payment.driverNumber!!.isBlank()) {
                Toast.makeText(requireContext(),"Driver Number cannot be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
            if (payment.driverPhone == " " || payment.driverPhone!!.isBlank()) {
            Toast.makeText(requireContext(),"Driver phone cannot be empty!", Toast.LENGTH_LONG).show()
            return@setOnClickListener

        }
            if (payment.amount == 0.0) {
                Toast.makeText(requireContext(),"Amount is invalid", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                binding.root.hideKeyboard()
                logger(TAG, payment.toString())
//                showReceiptDialog()
          paymentViewModel.sendPensuliaPaymentToApi(
                   payment,
                    supplierId,
                    supplierAccountNumber,
                    requireContext()
                )
                    .observe(viewLifecycleOwner) { paymentResult ->

                        if (paymentResult.status == ApiStatus.LOADING) {
                            binding.paymentProgressBar.visibility = View.VISIBLE
                            binding.cancelButton.isEnabled = false
                            binding.payButton.isEnabled = false

                        }
                        if (paymentResult.status == ApiStatus.SUCCESS) {
                            logger(TAG, "Payment Result in Fragment: ${paymentResult.message}")
                            binding.paymentProgressBar.visibility = View.INVISIBLE
//                            showReceiptDialog()
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    parentFragmentManager.popBackStack()
                                },
                                1000 // value in milliseconds
                            )



                            showReceiptDialog()

                        }
                        if (paymentResult.status == ApiStatus.ERROR) {
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            showCustomDialog()
                            paymentResult.message?.let { it1 -> requireContext().toast(it1) }
                        }

                        if (paymentResult.status == ApiStatus.EXCEPTION) {
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            logger(TAG, "Payment Exception: ${paymentResult.message}")
                            requireContext().toast(getString(R.string.networkError))
                        }
                    }
            }
        }
/*

        paymentViewModel.status.observe(viewLifecycleOwner) {
            if (it) {
                parentFragmentManager.popBackStack()
            }
        }

        paymentViewModel.paymentLiveData.observe(viewLifecycleOwner) {paymentResult ->
            lifecycleScope.launch {
                if (paymentResult != null) {
                    paymentViewModel.savePaymentToRealm(paymentResult, supplierId)
                }
            }
        }
*/

        binding.supplierNameTextView.text = supplierName
        binding.accountNumberTextView.text = supplierAccountNumber

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }



    companion object {
        @JvmStatic
        fun newInstance(supplierId: Int, supplierName: String?, supplierAccountNumber: String?) =
            PaymentFragmentPeninsula().apply {
                arguments = Bundle().apply {
                    if (supplierId != 0) {
                        putInt(ARG_SUPPLIER_ID_PEN, supplierId)
                    }
                    if (supplierName != null) {
                        putString(ARG_SUPPLIER_NAME_PEN, supplierName)
                    }
                    if (supplierAccountNumber != null) {
                        putString(ARG_SUPPLIER_ACCOUNT_NUMBER_PEN, supplierAccountNumber)
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        binding.PaymentReferenceTextInputLayout.editText?.setText("")
        binding.driverNumberTextInputLayout.editText?.setText("")
        binding.driverPhoneTextInputLayout.editText?.setText("")
        binding.amountTextInputLayout.editText?.setText("")
    }

     private fun showReceiptDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = PrintFragment()
        dialog.show(parentFragmentManager, "ReceiptDialogFragment")
    }


    private fun showCustomDialog() {
       val dialog :Dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_duplicate)
        dialog.setCancelable(false)

        //progressBar = ((ProgressBar) dialog.findViewById(R.id.progressBar));

        var bt_close = dialog.findViewById<View>(R.id.txt_ok) as Button
        bt_close.setOnClickListener(View.OnClickListener {

            dialog.dismiss()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    parentFragmentManager.popBackStack()
                },
                1000 // value in milliseconds
            )
        })
        dialog.show()
    }

}
