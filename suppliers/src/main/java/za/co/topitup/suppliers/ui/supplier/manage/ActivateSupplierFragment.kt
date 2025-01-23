package za.co.topitup.suppliers.ui.supplier.manage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.FragmentActivateSupplierBinding
import za.co.topitup.suppliers.models.Supplier
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.utils.*

private const val ARG_SUPPLIER_ID = "supplier_id"
private const val ARG_SUPPLIER_NAME = "supplier_name"
private const val ARG_SUPPLIER_ACTIVATION_PENDING = "supplier_pending"
private const val ARG_SUPPLIER_ACCOUNT_NUMBER = "supplier_account_number"

private const val TAG = "Activate Supplier"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivateSupplierFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivateSupplierFragment : Fragment() {

    private var _binding: FragmentActivateSupplierBinding? = null

    private val binding get() = _binding!!

    private var supplierId: Int? = null
    private var supplierName: String? = null
    private var accountNumber: String? = null
    private var activationPending: Boolean = false
    private var retailerId: String = ""

    private val addSupplierViewModel: AddSupplierViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            supplierId = it.getInt(ARG_SUPPLIER_ID)
            supplierName = it.getString(ARG_SUPPLIER_NAME)
            accountNumber = it.getString(ARG_SUPPLIER_ACCOUNT_NUMBER)
            activationPending = it.getBoolean(ARG_SUPPLIER_ACTIVATION_PENDING)
        }

        retailerId = Constants.RETAILER_ID
        Log.e("retailer id","........retailer......"+retailerId);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivateSupplierBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nameTextView.text = supplierName

        if (activationPending) {
            //TODO Request update from Server
            binding.messageTextView.text = getString(R.string.activation_in_progress)
            binding.activateButton.visibility = View.GONE
            binding.cancelButton.text = getString(R.string.go_back)
            binding.accountNumberInputEditText.isEnabled = false
            binding.accountNumberInputEditText.setText(accountNumber)
        }
        else {
            binding.messageTextView.text = getString(R.string.activate_supplier_message, supplierName)
            binding.activateButton.isEnabled = false
        }

        binding.accountNumberInputEditText.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                binding.activateButton.isEnabled = true
                accountNumber = it.toString()
            }
            else {
                binding.activateButton.isEnabled = false
            }
            accountNumber = it.toString()
        }

        binding.activateButton.setOnClickListener {
            binding.root.hideKeyboard()
            binding.progressBar.visibility = View.VISIBLE
            binding.responseTextView.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
//                supplierId?.let { id ->
//                    accountNumber?.let { accountNumber1 ->
//                        addSupplierViewModel.activateSupplier(id, accountNumber1)
//                    }
//                }
            }
            // TODO Remove this and send an actual request
            supplierId?.let { supplierId ->
                accountNumber?.let { accountNumber ->
                    addSupplierViewModel.sendSupplierLinkToApi(supplierId, retailerId, accountNumber)
                        .observe(viewLifecycleOwner) { linkResult ->
                            binding.cancelButton.isEnabled = false

                            if (linkResult.status == ApiStatus.LOADING) {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.cancelButton.isEnabled = false
                                binding.activateButton.isEnabled = false
                            }
                            if (linkResult.status == ApiStatus.SUCCESS) {
                                val supplier: Supplier = linkResult.data as Supplier
                                logger(TAG, supplier.toString())
                                logger(TAG, supplier.id.toString())
                                binding.progressBar.visibility = View.INVISIBLE
                                addSupplierViewModel.activateSupplier(supplier)
                                parentFragmentManager.popBackStack()
                            }
                            if (linkResult.status == ApiStatus.ERROR) {
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.cancelButton.isEnabled = true
                                binding.activateButton.isEnabled = true
                                logger(TAG, "Link Error: ${linkResult.message}")
                                logger(TAG, "Link Error: ${linkResult.data}")
                                linkResult.message?.let { it1 -> requireContext().toast(it1) }
                            }
                            Log.e("api .....","api status.........."+linkResult.status)
                            if (linkResult.status == ApiStatus.EXCEPTION) {
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.cancelButton.isEnabled = true
                                binding.activateButton.isEnabled = true

                                var s: String? = linkResult.message
                                val isExist = s?.contains("Duplicate")
                                logger(TAG, "${ isExist }Link ${s}Exception: ${linkResult.message}")

                                if(isExist!!){
                                    requireContext().toast(getString(R.string.duplicate))

                                }else {
                                    requireContext().toast(getString(R.string.networkError))
                                }
                            }
                        }
                }
            }

//            Looper.myLooper()?.let { looper ->
//                binding.cancelButton.isEnabled = false
//                Handler(looper).postDelayed({
//                    parentFragmentManager.popBackStack()
//                }, 2000)
//            }
            //parentFragmentManager.popBackStack()
        }

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param supplierId Parameter 1.
         * @param supplierName Parameter 2.
         * @param activationPending Parameter 3
         * @return A new instance of fragment ActivateSupplierFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(supplierId: Int, supplierName: String?, accountNumber: String?,activationPending: Boolean = false) =
            ActivateSupplierFragment().apply {
                arguments = Bundle().apply {
                    if (supplierId != 0) {
                        putInt(ARG_SUPPLIER_ID, supplierId)
                    }
                    if (supplierName != null) {
                        putString(ARG_SUPPLIER_NAME, supplierName)
                    }
                    if (accountNumber != null) {
                        putString(ARG_SUPPLIER_ACCOUNT_NUMBER, accountNumber)
                    }
                    putBoolean(ARG_SUPPLIER_ACTIVATION_PENDING, activationPending)
                }
            }
    }
}