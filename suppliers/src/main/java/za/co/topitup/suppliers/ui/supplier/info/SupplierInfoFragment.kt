package za.co.topitup.suppliers.ui.supplier.info

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.FragmentSupplierInfoBinding
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.network.loadSupplierImage
import za.co.topitup.suppliers.utils.logger
import za.co.topitup.suppliers.utils.toDateString
import za.co.topitup.suppliers.utils.toDistanceString
import za.co.topitup.suppliers.utils.toast

private const val ARG_SUPPLIER_ID = "supplier_id"
private const val ARG_LINK_ID = "link_id"

private const val TAG = "SupplierInfo"

class SupplierInfoFragment : Fragment() {

    companion object {
        fun newInstance(supplierId: Int, linkId: String) =
            SupplierInfoFragment().apply {
                arguments = Bundle().apply {
                    if (supplierId != 0) {
                        putInt(ARG_SUPPLIER_ID, supplierId)
                    }
                    if (linkId.isNotBlank()) {
                        putString(ARG_LINK_ID, linkId)
                    }
                }
            }
    }

    private var _binding: FragmentSupplierInfoBinding? = null

    private val binding get() = _binding!!

    private val supplierInfoViewModel: SupplierInfoViewModel by activityViewModels()

    private var supplierId: Int = 0
    private lateinit var linkId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupplierInfoBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            supplierId = it.getInt(ARG_SUPPLIER_ID)
            linkId = it.getString(ARG_LINK_ID).toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getSupplier = supplierInfoViewModel.getSupplier(supplierId)
        getSupplier.observe(viewLifecycleOwner) { supplier ->
            binding.nameTextView.text = supplier.name
            loadSupplierImage(requireContext(), binding.logoImageView, supplier.logo)
            binding.categoryTextView.text = supplier.category
            binding.distanceTextView.toDistanceString(supplier.distance)
            supplier.lastPurchase?.let {
                binding.lastPaymentTextView.toDateString(it)
            } ?: kotlin.run {
                binding.lastPaymentTextView.text = getString(R.string.no_payments_text)
            }

            //TODO Add fields to Realm
            binding.phoneTextView.text = supplier.phone
            binding.accountNumberTextView.text = supplier.accountNumber
            binding.addressTextView.text = supplier.address
        }

        binding.cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.deleteButton.setOnClickListener {

            lifecycleScope.launch {
                showDialog(linkId)

                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog(linkId: String) {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Remove Supplier?")
            alertDialog.setMessage("Do you want to remove this Supplier?")
            alertDialog.setPositiveButton(getString(R.string.dialogPositiveText)) { dialog, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    deactivateSupplier(linkId)
                }
                dialog.cancel()
            }
            alertDialog.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            val alert = alertDialog.create()
            alert.show()
    }

    private suspend fun deactivateSupplier(linkId: String) {

            val result = supplierInfoViewModel.deactivateSupplier(linkId)
            result.collect { response ->
                if (response.status == ApiStatus.SUCCESS){
                    parentFragmentManager.popBackStack()
                }
                if (response.status == ApiStatus.ERROR){
                    logger(TAG, "Deactivation Error: ${response.message}")
                    requireContext().toast("An error occurred", Toast.LENGTH_LONG)
                }
                if (response.status == ApiStatus.EXCEPTION) {
                    requireContext().toast(R.string.networkError, Toast.LENGTH_LONG)
                    logger(TAG, "Deactivation Exception: ${response.message}")
                }

                //parentFragmentManager.popBackStack()

            }


    }
}