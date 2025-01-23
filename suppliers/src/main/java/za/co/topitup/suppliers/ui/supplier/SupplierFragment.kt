package za.co.topitup.suppliers.ui.supplier

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.topitup.suppliers.NavigationHost
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.FragmentSupplierBinding
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.RetailerApi
import za.co.topitup.suppliers.models.RetailerApiNew
import za.co.topitup.suppliers.models.RetailerNew
import za.co.topitup.suppliers.network.ApiStatus
import za.co.topitup.suppliers.repositories.SupplierRepository
import za.co.topitup.suppliers.ui.payment.PaymentFragment
import za.co.topitup.suppliers.ui.payment.PaymentFragment.Companion.newInstance
import za.co.topitup.suppliers.ui.payment.PaymentFragmentPeninsula
import za.co.topitup.suppliers.ui.supplier.info.SupplierInfoFragment
import za.co.topitup.suppliers.ui.supplier.manage.ActivateSupplierFragment
import za.co.topitup.suppliers.utils.AppPreferences
import za.co.topitup.suppliers.utils.Constants

const val TAG = "SupplierFragment"

class SupplierFragment : Fragment() {

    private var _binding: FragmentSupplierBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val supplierViewModel: SupplierViewModel by activityViewModels()

    private lateinit var sharedPreferences: AppPreferences
    private lateinit var vendorLatitude: String
    private lateinit var vendorLongitude: String
    private var firstRun: Boolean = true
    private val newDataLoaded =  MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = AppPreferences(requireContext())
        firstRun = sharedPreferences.firstRun
        vendorLatitude = sharedPreferences.vendorLatitude.toString()
        vendorLongitude = sharedPreferences.vendorLongitude.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSupplierBinding.inflate(inflater, container, false)
        firstRun = true
        //Get Supplier List if first time running

        vendorLatitude = sharedPreferences.vendorLatitude.toString()
        vendorLongitude = sharedPreferences.vendorLongitude.toString()

       /* val supplierList =
            SupplierRepository().getSuppliersFromAPI(vendorLatitude, vendorLongitude)
*/
        if (firstRun) {
            vendorLatitude = sharedPreferences.vendorLatitude.toString()
            vendorLongitude = sharedPreferences.vendorLongitude.toString()

            val supplierList = SupplierRepository().getSuppliersFromAPI(vendorLatitude, vendorLongitude)

            val progressBar = activity?.findViewById<ProgressBar>(R.id.AppProgressBar)
            progressBar?.visibility = View.VISIBLE
            supplierList.observe(viewLifecycleOwner) { suppliersList ->
                lifecycleScope.launch {
                    if (suppliersList[0].id != 0) {
                        SupplierDatabaseOperations().insertSuppliers(suppliersList)
                        firstRun = false
                        progressBar?.visibility = View.INVISIBLE
                        sharedPreferences.firstRun = firstRun
//                        newDataLoaded.value = true
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.network_error_basic),
                            Toast.LENGTH_LONG
                        ).show()
                        progressBar?.visibility = View.INVISIBLE
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* ---------------------------------------------------------------------------------*/
        // Local and National Suppliers counts
        val localSupplierCount = supplierViewModel.localSupplierCount
        val nationalSupplierCount = supplierViewModel.nationalSupplierCount

        binding.localButton.text = getString(R.string.local_suppliers_button_text, 0)
        binding.nationalButton.text = getString(R.string.national_suppliers_button_text, 0)

        supplierViewModel.getSuppliersCountByType(Retailer.province)

        localSupplierCount.observe(viewLifecycleOwner) { count ->
            binding.localButton.text = getString(R.string.local_suppliers_button_text, count)
        }
        nationalSupplierCount.observe(viewLifecycleOwner) {count ->
            binding.nationalButton.text = getString(R.string.national_suppliers_button_text, count)
        }

        /* ---------------------------------------------------------------------------------*/
        // RecyclerView
        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(
            context,
            2,
            RecyclerView.VERTICAL,
            false
        )
        recyclerView.adapter = null

        val supplierList = supplierViewModel.initSupplierEntryList()

        val recyclerViewAdapter = SupplierRecyclerViewAdapter(
            SupplierRecyclerViewAdapter.OnClickListener { item ->

                if (item != null) {
                    if (item.vendorActivated && item.activationPending.not()) {
                        if(item.name.contains("Peninsula")){

                            Log.e("if.....","name")
                            val paymentFragmentPeninsula = PaymentFragmentPeninsula.newInstance(item.id,item.name,item.accountNumber)
//                            val paymentFragmentPeni = paymentFragmentPeninsula.newInstance(item.id, item.name, item.accountNumber)

                            (activity as NavigationHost).navigateTo(
                                paymentFragmentPeninsula, true
                            )
                        }else{
                            val paymentFragment = PaymentFragment.newInstance(item.id, item.name, item.accountNumber)

                            (activity as NavigationHost).navigateTo(
                                paymentFragment, true
                            )
                        }

                    } else {
                        val activateSupplierFragment = ActivateSupplierFragment.newInstance(
                            item.id, item.name, item.accountNumber, item.activationPending
                        )
                        (activity as NavigationHost).navigateTo(
                            activateSupplierFragment, true
                        )
                    }
                }

            },
            //Info button listener
            SupplierRecyclerViewAdapter.OnClickListener { supplier ->
                val supplierInfoFragment = supplier?.let { thisSupplier ->
                    thisSupplier.linkId?.let { linkId ->
                        SupplierInfoFragment.newInstance(thisSupplier.id, linkId)
                    }
                }
                if (supplierInfoFragment != null) {
                    (activity as NavigationHost).navigateTo(supplierInfoFragment, true)
                }
            },
            // Populate the RecycleView
            supplierList
        )

        if (recyclerViewAdapter.itemCount != 0) {
            binding.emptyListTextView.visibility = View.INVISIBLE
        }

        recyclerView.adapter = recyclerViewAdapter

        newDataLoaded.observe(viewLifecycleOwner) {status ->
            if (status) {
               // recyclerViewAdapter.notifyDataSetChanged()
                if (recyclerViewAdapter.itemCount != 0) {
                    recyclerViewAdapter.notifyItemRangeInserted(0, recyclerViewAdapter.itemCount)
                    binding.emptyListTextView.visibility = View.INVISIBLE
                }
            }
        }

        var localFiltered = false
        var nationalFiltered = false

        binding.localButton.setOnClickListener {
            if (!localFiltered) {
                recyclerViewAdapter.applyFilter("LOCAL")
                localFiltered = true
                nationalFiltered = false
            }
            else {
                recyclerViewAdapter.applyFilter("")
                localFiltered = false
            }
        }

        binding.nationalButton.setOnClickListener {
            if (!nationalFiltered) {
                recyclerViewAdapter.applyFilter("NATIONAL")
                localFiltered = false
                nationalFiltered = true
            }
            else {
                recyclerViewAdapter.applyFilter("f")
                nationalFiltered = false
            }
        }



        lifecycleScope.launch(Dispatchers.IO) {

            supplierViewModel.getRetailerInfo().collect { result ->
                if (result.status == ApiStatus.SUCCESS) {
                    val retailer: RetailerApi = result.data as RetailerApi
                    Log.e("retailer .","retailer data...."+retailer.accountNumber)
                    retailer.id?.let { Retailer.id = it }
                    retailer.accountNumber?.let { Retailer.accountNumber = it }
                    retailer.provinceId?.let {
                        Retailer.province = Constants.Provinces[it] as String
                        Retailer.provinceId = it
                    }
                    retailer.dcCode?.let { Retailer.dcCode = it }

                    supplierViewModel.getSuppliersCountByType(Retailer.province)
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {

            supplierViewModel.getRetailerInfoNew().collect { result ->
                if (result.status == ApiStatus.SUCCESS) {
                    val retailer: RetailerApiNew = result.data as RetailerApiNew
                    retailer.accountNumber?.let { RetailerNew.accountNumber = it }

                    retailer.dcCode?.let { RetailerNew.dcCode = it }

                }
            }
        }

        /* --------------------------------------------------------------------------------- */
        // Advertisement ViewPager
        val advertViewPager = binding.advertViewPager
        advertViewPager.adapter = null
        val advertImagesArray: MutableList<String> = mutableListOf()
        val advertViewPagerAdapter = AdvertViewPagerAdapter(advertImagesArray)
        advertViewPager.adapter = advertViewPagerAdapter
        advertViewPager.autoScroll(viewLifecycleOwner.lifecycleScope, 5000L)
        advertViewPager.isUserInputEnabled = false
//        advertViewPager.clipToPadding = false
//        advertViewPager.clipChildren = false
        advertViewPager.offscreenPageLimit = 2
//        advertViewPager.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER


        val advertImageArrayIndexLiveData = MutableLiveData<Int>()

        lifecycleScope.launch(Dispatchers.Main) {
            advertImageArrayIndexLiveData.observe(viewLifecycleOwner) {index ->
                advertViewPagerAdapter.notifyItemInserted(index)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {

            supplierViewModel.getAdvertUrls().collect { advertListResponse ->
                if (advertListResponse.status == ApiStatus.SUCCESS) {
                    val imagesArray = (advertListResponse.data as? Array<*>)?.filterIsInstance<String>()
                    imagesArray?.forEachIndexed { index, url ->
                        advertImagesArray.add(url)
                        advertImageArrayIndexLiveData.postValue(index)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Thanks https://lucianoluzzi.medium.com/android-infinite-carousel-with-coroutines-and-viewpager2-54c3e3a9039b
    private fun ViewPager2.autoScroll(lifecycleScope: LifecycleCoroutineScope, interval: Long) {
        lifecycleScope.launchWhenResumed {
            scrollIndefinitely(interval)
        }
    }

    private suspend fun ViewPager2.scrollIndefinitely(interval: Long) {
        delay(interval)
        val numberOfItems = adapter?.itemCount ?: 0
        val lastIndex = if (numberOfItems > 0) numberOfItems - 1 else 0
        val nextItem = if (currentItem == lastIndex) 0 else currentItem + 1

        setCurrentItem(nextItem, true)
        scrollIndefinitely(interval)
    }

}