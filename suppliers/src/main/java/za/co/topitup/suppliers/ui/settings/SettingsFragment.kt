package za.co.topitup.suppliers.ui.settings

//import android.Manifest
//import android.content.pm.PackageManager
//import android.location.Address
//import android.location.Geocoder
//import android.location.Location
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.launch
//import za.co.topitup.suppliers.database.SupplierDatabaseOperations
//import za.co.topitup.suppliers.databinding.FragmentSettingsBinding
//import za.co.topitup.suppliers.location.GetLocation
//import za.co.topitup.suppliers.utils.AppPreferences
//import java.util.*
//
///*
//class SettingsFragment : PreferenceFragmentCompat() {
//
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        setPreferencesFromResource(R.xml.root_preferences, rootKey)
//    }
//}*/
//
//class SettingsFragment : Fragment() {
//    private var _binding: FragmentSettingsBinding? = null
//
//    // This property is only valid between onCreateView and
//    // onDestroyView.
//    private val binding get() = _binding!!
//
//    private lateinit var sharedPreferences: AppPreferences
//    //private var vendorActivated: Boolean? = null
//    private lateinit var vendorLatitude: String
//    private lateinit var vendorLongitude: String
//
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        sharedPreferences = AppPreferences(requireContext())
//
//        vendorLatitude = sharedPreferences.vendorLatitude.toString()
//        vendorLongitude = sharedPreferences.vendorLongitude.toString()
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        updateAddressTextView()
//
//
//        binding.resetLocationButton.setOnClickListener {
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.d("Location", "NO LOCATION PERMISSION!!!")
//                return@setOnClickListener
//            }
//            binding.addressProgressBar.visibility = View.VISIBLE
//            lifecycleScope.launch(Dispatchers.Default) {
//                    GetLocation(fusedLocationProviderClient, requireContext())
//                        .fetchUpdates()
//                        .collect { currentLocation ->
//                            sharedPreferences.vendorLatitude = currentLocation.latitude.toString()
//                            sharedPreferences.vendorLongitude = currentLocation.longitude.toString()
//                            updateSupplierDistance(currentLocation)
//                            updateAddressTextView(currentLocation)
//                            this.cancel("No Longer required.")
//
//                        }
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private suspend fun updateSupplierDistance(currentLocation: Location) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            SupplierDatabaseOperations().updateSupplierDistance(currentLocation)
//        }
//    }
//
//    private fun updateAddressTextView(currentLocation: Location? = null) {
//        lifecycleScope.launch(Dispatchers.Default) {
//            val location = Location("")
//            if (currentLocation == null) {
//                location.latitude = vendorLatitude.toDouble()
//                location.longitude = vendorLongitude.toDouble()
//            } else {
//                location.longitude = currentLocation.longitude
//                location.latitude = currentLocation.latitude
//            }
//
//            if (vendorLatitude.toDouble() == 0.0 || vendorLongitude.toDouble() == 0.0  ) return@launch
//
//            lifecycleScope.launch(Dispatchers.Main) {
//                try {
//                    val newAddress = getGeoLocation(location.latitude, location.longitude)
//
//                    newAddress.collect() {
//                        binding.oldAddressTextView.text = binding.addressTextView.text
//                        binding.addressTextView.text = it
//
//                    }
//                    /*binding.oldAddressTextView.text = binding.addressTextView.text
//                    binding.addressTextView.text = getGeoLocation(
//                        location.latitude,
//                        location.longitude
//                    )*/
//
//                } catch (e: Exception) {
//                    //TODO String Resource
//                    binding.addressTextView.text =
//                        "An Error occurred getting the Address"
//                    Log.d("Location", "Address Error", e)
//                } finally {
//                    binding.addressProgressBar.visibility = View.INVISIBLE
//                }
//            }
//        }
//    }
//
//    private fun getGeoLocation(latitude: Double, longitude: Double): Flow<String> {
//            val geocoder = Geocoder(requireContext(), Locale.getDefault())
//            val addresses: List<Address> = geocoder.getFromLocation(
//                latitude,
//                longitude,
//                1
//            ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//            //val address: String = addresses[0].getAddressLine(0)
//
//            // If any additional address line present then only
//            // check with max available address lines by getMaxAddressLineIndex()
//
//            /*val city: String = addresses[0].locality
//            val state: String = addresses[0].adminArea
//            val country: String = addresses[0].countryName
//            val postalCode: String = addresses[0].postalCode
//            val thoroughfare: String = addresses[0].thoroughfare
//            val subThoroughfare: String = addresses[0].subThoroughfare
//
//            val addressText = thoroughfare.plus(", ").subThoroughfare.plus(", ").plus(city).plus(", ").plus(state)
//            .plus(", ").plus(postalCode).plus(", ").plus(country)
//            */
//
//            //return address
//        return flow {
//            emit(addresses[0].getAddressLine(0))
//        }
//
//    }
//
//}