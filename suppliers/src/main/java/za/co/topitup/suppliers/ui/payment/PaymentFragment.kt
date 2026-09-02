package za.co.topitup.suppliers.ui.payment

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.FragmentPaymentBinding
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.utils.hideKeyboard
import za.co.topitup.suppliers.utils.logger
import za.co.topitup.suppliers.utils.toast
import java.util.*

const val ARG_SUPPLIER_ID = "supplier_id"
const val ARG_SUPPLIER_NAME = "supplier_name"
const val ARG_SUPPLIER_ACCOUNT_NUMBER = "supplier_account_number"

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Pass in a Bundle with:
 * * [ARG_SUPPLIER_ID] of type [String]
 * * [ARG_SUPPLIER_NAME] of type [String]
 * * [ARG_SUPPLIER_ACCOUNT_NUMBER] of type [String]
 */
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null

    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by activityViewModels()

    private var supplierId: Int = 0
    private lateinit var supplierName: String
    private lateinit var supplierAccountNumber: String

    private var isPaymentInProgress = false

    companion object {
        private const val TAG = "PaymentFragment"

        @JvmStatic
        fun newInstance(supplierId: Int, supplierName: String?, supplierAccountNumber: String?) =
            PaymentFragment().apply {
                arguments = Bundle().apply {
                    if (supplierId != 0) {
                        putInt(ARG_SUPPLIER_ID, supplierId)
                    }
                    if (supplierName != null) {
                        putString(ARG_SUPPLIER_NAME, supplierName)
                    }
                    if (supplierAccountNumber != null) {
                        putString(ARG_SUPPLIER_ACCOUNT_NUMBER, supplierAccountNumber)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            supplierId = it.getInt(ARG_SUPPLIER_ID)
            supplierName = it.getString(ARG_SUPPLIER_NAME).toString()
            supplierAccountNumber = it.getString(ARG_SUPPLIER_ACCOUNT_NUMBER).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(
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
            if (isPaymentInProgress) {
                logger(TAG, "Payment already in progress, ignoring click")
                return@setOnClickListener
            }

            payment.fullDate = Date()
            logger(TAG, payment.paymentReference)
            logger(TAG, payment.amount.toString())

            if (payment.paymentReference == " " || payment.paymentReference.isBlank()) {
                logger(TAG, payment.paymentReference )
                Toast.makeText(requireContext(),"Reference cannot be empty!", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            }
            if (payment.amount == 0.0) {
                Toast.makeText(requireContext(),"Amount is invalid", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                binding.payButton.isEnabled = false
                showConfirmDialog(payment)


            }
        }

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

        binding.supplierNameTextView.text = supplierName
        binding.accountNumberTextView.text = supplierAccountNumber

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showReceiptDialog() {
        // Create an instance of the dialog fragment and show it
        val dialog = ReceiptDialogFragment()
        dialog.show(parentFragmentManager, "ReceiptDialogFragment")
    }

    private fun showConfirmDialog(payment: PaymentRealm) {
        val dialog: Dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirm)
        dialog.setCancelable(false)

        val txtCancel = dialog.findViewById<TextView>(R.id.txt_cancel)
        val btConfirm = dialog.findViewById<TextView>(R.id.txt_ok)
        var tvSuplierName = dialog.findViewById<TextView>(R.id.tv_supplier_name)

        tvSuplierName.text =supplierName

        txtCancel.setOnClickListener {
            binding.payButton.isEnabled = true
            dialog.dismiss()
        }

        btConfirm.setOnClickListener {
            // Don't re-enable the button here - keep it disabled during API call
            dialog.dismiss()

            // Set payment in progress flag
            isPaymentInProgress = true

            binding.root.hideKeyboard()
            logger(TAG, payment.toString())
            Log.e("payment$payment", ".......$supplierId...$supplierAccountNumber")

            paymentViewModel.sendPaymentToApi(payment, supplierId, supplierAccountNumber)
                .observe(viewLifecycleOwner) { paymentResult ->
                    when (paymentResult.status) {
                        ApiStatus.LOADING -> {
                            binding.paymentProgressBar.visibility = View.VISIBLE
                            binding.cancelButton.isEnabled = false
                            binding.payButton.isEnabled = false
                        }

                        ApiStatus.SUCCESS -> {
                            logger(TAG, "Payment Result in Fragment: $paymentResult")
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            isPaymentInProgress = false // Reset flag on success
                            showReceiptDialog()
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    parentFragmentManager.popBackStack()
                                },
                                1000
                            )
                        }

                        ApiStatus.ERROR -> {
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            isPaymentInProgress = false // Reset flag on error
                            logger(TAG, "Payment Error: ${paymentResult.message}")
                            paymentResult.message?.let { message ->
                                requireContext().toast(message)
                            }
                        }

                        ApiStatus.EXCEPTION -> {
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            isPaymentInProgress = false // Reset flag on exception
                            logger(TAG, "Payment Exception: ${paymentResult.message}")
                            requireContext().toast(getString(R.string.networkError))
                        }

                        ApiStatus.INVALID -> {
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            isPaymentInProgress = false // Reset flag on invalid status
                            logger(TAG, "Payment Invalid Status: ${paymentResult.message}")
                            requireContext().toast("Invalid payment status")
                        }

                        else -> {
                            // Handle any unexpected status
                            binding.paymentProgressBar.visibility = View.INVISIBLE
                            binding.cancelButton.isEnabled = true
                            binding.payButton.isEnabled = true
                            isPaymentInProgress = false // Reset flag for unknown status
                            logger(TAG, "Unknown payment status: ${paymentResult.status}")
                            requireContext().toast("Unexpected error occurred")
                        }
                    }
                }
        }

        dialog.show()
    }
}