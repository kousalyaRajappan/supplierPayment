package za.co.topitup.suppliers.ui.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.databinding.FragmentHistoryBinding
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.utils.logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val TAG = "History"

class HistoryFragment : Fragment() {

    
    private lateinit var recyclerView: RecyclerView
    lateinit var historyListAdapter: HistoryListAdapter
    private var _binding: FragmentHistoryBinding? = null
    private val historyViewModel: HistoryViewModel by activityViewModels()
    var mContext: Context? = null

    private var name = ""
    private var date = ""
    var movies = ArrayList<MyItem>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         recyclerView = binding.recyclerView

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        mContext = context


        val spinner = binding.supplierSpinner
        val dateLabel = binding.dateTextView
        dateLabel.text = "All Dates"


        historyListAdapter = HistoryListAdapter(
            HistoryListAdapter.OnClickListener{payment: MyItem? ->


                val dialog = HistoryReceiptDialogFragment.newInstance(
                    payment!!.btn_date,
                    payment!!.supplierRef,
                    payment!!.supCustomer,
                    payment!!.sup_driver,
                    payment!!.sup_driver_cell,
                    payment!!.supAmount,
                    payment!!.supShipment,
                    payment!!.supSupplierName
                )
                dialog.show(childFragmentManager, "HistoryReceiptDialogFragment")

        })
        recyclerView.adapter=historyListAdapter

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val current = LocalDateTime.now().format(formatter)
        val viewModel :HistoryViewModell = ViewModelProvider(this).get(HistoryViewModell::class.java)



        viewModel.makeAPICall("",current,"")

        viewModel.getLivePaymentObserver().observe(viewLifecycleOwner,{
            Log.e("payment response","....sssss......pay res ksjdfh")

            if(it != null){

                Log.e("payment response","..........pay res ksjdfh")
                historyListAdapter.setPaymentList(it)
                historyListAdapter.notifyDataSetChanged()
            }else{

            }
        })


        lifecycleScope.launch {
            // RecyclerView gowthami comment
           /* val adapter = HistoryRecycleViewAdapter(historyViewModel.getPayments(name, date),
                    HistoryRecycleViewAdapter.OnClickListener { item ->
                        //binding.recyclerView
                        // todo - expand to show details
                        // todo - move to repository

                        val dialog = HistoryReceiptDialogFragment.newInstance(item.id)
                        dialog.show(childFragmentManager, "HistoryReceiptDialogFragment")

                        logger(TAG, "${item.id} clicked")
                    }
                )

            recyclerView.adapter = adapter*/


            // Spinner
            val supplierNames = ArrayList<String>()
            supplierNames.add("All")
            val supplierNamesLiveData = historyViewModel.getSupplierNames()

            supplierNamesLiveData.observe(viewLifecycleOwner) {
                supplierNames.addAll(it)
            }

            val spinnerAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, supplierNames)
            // Set layout to use when the list of choices appear
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Set Adapter to Spinner gowthami comment
            spinner.adapter = spinnerAdapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    name = supplierNames[position]
                    logger("Filter", "NAME SELECTED -> Name: $name & Date: $date")


                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    return
                }
            }

            binding.dateButton.setOnClickListener {

                val datePickerFragment = DatePickerFragment()
                val supportFragmentManager = requireActivity().supportFragmentManager

                supportFragmentManager.setFragmentResultListener(
                    "REQUEST_KEY",
                    viewLifecycleOwner
                ) { resultKey, bundle ->
                    if (resultKey == "REQUEST_KEY") {
                        val selectedDate = bundle.getString("SELECTED_DATE")

                        date = selectedDate.toString()
                        dateLabel.text = date
                        viewModel.makeAPICall(date,current,"")
                        historyListAdapter.notifyDataSetChanged()

//                        adapter.filter.filter("name:$name:date:$date")
                        //spinner.setSelection(0)
                    }
                }
                // show the DatePicker
                datePickerFragment.show(supportFragmentManager, "DatePickerFragment")

            }

            binding.resetButton.setOnClickListener {
                name = ""
                date = ""
                dateLabel.text = "All Dates"
                viewModel.makeAPICall("",current,"")
                historyListAdapter.notifyDataSetChanged()
//                adapter.filter.filter("name:$name:date:$date")
                spinner.setSelection(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
