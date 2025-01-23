package za.co.topitup.suppliers.ui.supplier.manage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.NavigationHost
import za.co.topitup.suppliers.databinding.FragmentAddSupplierBinding
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.utils.AppPreferences

class AddSupplierFragment : Fragment() {

    private var _binding: FragmentAddSupplierBinding? = null
    private val binding get() = _binding!!

    private var name: String = ""
    private var category: String = ""

    private val addSupplierViewModel: AddSupplierViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddSupplierBinding.inflate(inflater,
        container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        recyclerView.adapter = null

        val supplierListLiveData = addSupplierViewModel.initSupplierEntryList()

        val spinner = binding.categorySpinner
        val categories = ArrayList<String>()
        //Add "All" as the first entry in the Category list
        categories.add("All")
        val supplierCategoriesFlow = addSupplierViewModel.getCategories()
        Log.e("supplier category size"+supplierListLiveData.size,"...............size"+addSupplierViewModel.getCategories())

        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                categories)
        // Set layout to use when the list of choices appear
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        val recyclerViewAdapter = AddSupplierRecyclerViewAdapter(
                // Add Button onClickListener
                AddSupplierRecyclerViewAdapter.OnClickListener { supplier ->
                    if (supplier != null) {
                        addSupplier(supplier)
                    }
                },
                // Populate the RecycleView
                supplierListLiveData
            )

        recyclerView.adapter = recyclerViewAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category = categories[position]
                recyclerViewAdapter.applyFilter(
                    "name:$name:type:$category",
                    category = TODO()
                )
                return
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //TODO("Not yet implemented")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    name = newText
                }
                recyclerViewAdapter.applyFilter(
                    "name:$name:type:$category",
                    category = TODO()
                )
                return true
            }
        })

        val spinnerAdapterLiveData = MutableLiveData<Boolean>()
        spinnerAdapterLiveData.observe(viewLifecycleOwner) {
            if (it) {
                spinner.adapter = spinnerAdapter
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            supplierCategoriesFlow.collect { category ->
                categories.addAll(category)
                // Set Adapter to Spinner
                spinnerAdapterLiveData.postValue(true)

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun addSupplier(item: SupplierRealm) {


        val activateSupplierFragment =
            ActivateSupplierFragment.newInstance(item.id, item.name, item.accountNumber)
        (activity as NavigationHost).navigateTo(
            activateSupplierFragment, true
        )

    }
}