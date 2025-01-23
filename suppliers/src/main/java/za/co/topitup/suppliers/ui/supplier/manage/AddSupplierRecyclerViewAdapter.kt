package za.co.topitup.suppliers.ui.supplier.manage

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import za.co.topitup.suppliers.GlideApp
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.AddSupplierCardBinding
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.utils.toDistanceString

class AddSupplierRecyclerViewAdapter(
    private val onAddClickListener: OnClickListener,
    private val supplierList: List<SupplierRealm>
) : RecyclerView.Adapter<AddSupplierRecyclerViewAdapter.AddSupplierViewHolder>(), Filterable {

    private var filteredList: List<SupplierRealm> = supplierList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSupplierViewHolder {
        val binding = AddSupplierCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddSupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddSupplierViewHolder, position: Int) {
        val supplier = filteredList[position]

        holder.name.text = supplier.name

        // Load supplier logo using Glide
        val logoUrl = Retailer.logoBaseURL.plus("/").plus(supplier.logo)
        GlideApp.with(holder.itemView.context)
            .load(logoUrl)
            .error(R.drawable.ic_error)
            .into(holder.logo)

        // Set activation required status
        holder.activationRequired.text = if (supplier.requiresActivation == true) {
            holder.itemView.context.getString(R.string.activation_required)
        } else {
            holder.itemView.context.getString(R.string.no_activation_required)
        }

        // Populate other fields
        holder.category.text = supplier.category
        holder.phone.text = supplier.phone
        holder.address.text = supplier.address
        supplier.distance?.let { holder.distance.toDistanceString(it) }

        // Set click listeners
        holder.addButton.setOnClickListener { onAddClickListener.onAddClick(supplier) }
        holder.infoButton.setOnClickListener {
            holder.infoLayout.visibility =
                if (holder.infoLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun updateData(newList: List<SupplierRealm>) {
        this.filteredList = newList
        notifyDataSetChanged()
    }

    /* ViewHolder */
    inner class AddSupplierViewHolder(binding: AddSupplierCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val logo: ImageView = binding.logoImageView
        val name: TextView = binding.nameTextView
        val distance = binding.distanceTextView
        val addButton = binding.addButton
        val infoButton = binding.infoButton
        val category = binding.categoryTextView
        val phone = binding.phoneTextView
        val address = binding.addressTextView
        val activationRequired = binding.activationRequiredTextView
        val infoLayout = binding.infoLinearLayout
    }

    /* OnClickListener */
    class OnClickListener(val clickListener: (supplier: SupplierRealm) -> Unit) {
        fun onAddClick(supplier: SupplierRealm) = clickListener(supplier)
    }

    /* Filtering */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (!constraint.isNullOrEmpty()) {
                    val parts = constraint.split(":")
                    val name = parts.getOrNull(1)?.trim() ?: ""
                    val category = parts.getOrNull(3)?.trim() ?: ""

                    val filtered = supplierList.filter { supplier ->
                        (name.isEmpty() || supplier.name.contains(name, ignoreCase = true)) &&
                                (category.isEmpty() || supplier.category.equals(category, ignoreCase = true))
                    }
                    filterResults.values = filtered
                    filterResults.count = filtered.size
                } else {
                    filterResults.values = supplierList
                    filterResults.count = supplierList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredList = results?.values as? List<SupplierRealm> ?: supplierList
                notifyDataSetChanged()
            }
        }
    }
}


/*class AddSupplierRecyclerViewAdapter(
    private val onAddClickListener: OnClickListener,
    private var supplierList: List<SupplierRealm> = listOf()
) : RecyclerView.Adapter<AddSupplierRecyclerViewAdapter.AddSupplierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSupplierViewHolder {
        val layoutView = AddSupplierViewHolder(
            AddSupplierCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.addButton.setOnClickListener {
            val position = layoutView.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onAddClickListener.onAddClick(supplierList[position])
            }
        }

        layoutView.infoButton.setOnClickListener {
            if (layoutView.infoLayout.visibility == View.GONE) {
                layoutView.infoLayout.visibility = View.VISIBLE
            } else {
                layoutView.infoLayout.visibility = View.GONE
            }
        }

        return layoutView
    }

    override fun onBindViewHolder(holder: AddSupplierViewHolder, position: Int) {
        val supplier = supplierList[position]
        holder.bind(supplier)
    }

    override fun getItemCount(): Int = supplierList.size

    fun updateData(newData: List<SupplierRealm>) {
        supplierList = newData
        notifyDataSetChanged()
    }

    class OnClickListener(val clickListener: (supplier: SupplierRealm) -> Unit) {
        fun onAddClick(supplier: SupplierRealm) = clickListener(supplier)
    }

    inner class AddSupplierViewHolder(private val binding: AddSupplierCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val logo: ImageView = binding.logoImageView
        val name: TextView = binding.nameTextView
        val distance = binding.distanceTextView
        val addButton = binding.addButton
        val infoButton = binding.infoButton
        val category = binding.categoryTextView
        val phone = binding.phoneTextView
        val address = binding.addressTextView
        val activationRequired = binding.activationRequiredTextView
        val infoLayout = binding.infoLinearLayout

        fun bind(supplier: SupplierRealm) {
            name.text = supplier.name
            val logoUrl = Retailer.logoBaseURL.plus("/").plus(supplier.logo)

            GlideApp.with(itemView.context)
                .load(logoUrl)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .error(za.co.topitup.suppliers.R.drawable.ic_error)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(logo)

            activationRequired.text = if (supplier.requiresActivation) {
                itemView.context.getString(R.string.activation_required)
            } else {
                itemView.context.getString(R.string.no_activation_required)
            }

            distance.text = supplier.distance?.let { "$it km" } ?: "N/A"
            category.text = supplier.category
            phone.text = supplier.phone
            address.text = supplier.address
        }
    }

    public fun applyFilter(name: String, category: String) {
        val results = SupplierDatabaseOperations().getSuppliers(name, category)
        updateData(results)
    }

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val strings = constraint?.split(":") ?: return
                val name = strings.getOrNull(1).orEmpty()
                val category = if (strings.getOrNull(3) == "All") "" else strings[3]
                applyFilter(name, category)
            }
        }
    }
}*/

/*
class AddSupplierRecyclerViewAdapter(
    private val onAddClickListener: OnClickListener,
    supplierList: RealmResults<SupplierRealm>
) : RealmRecyclerViewAdapter<SupplierRealm?,
        AddSupplierRecyclerViewAdapter.AddSupplierViewHolder?>(
    supplierList, true, false
), Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSupplierViewHolder {
        val layoutView = AddSupplierViewHolder(
            AddSupplierCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.addButton.setOnClickListener {
            val position = layoutView.absoluteAdapterPosition
            onAddClickListener.onAddClick(data?.get(position))
        }

        layoutView.infoButton.setOnClickListener {
            if (layoutView.infoLayout.visibility == View.GONE) {
                layoutView.infoLayout.visibility = View.VISIBLE
            } else {
                layoutView.infoLayout.visibility = View.GONE
            }
        }
        return layoutView
    }

    override fun onBindViewHolder(holder: AddSupplierViewHolder, position: Int) {
        if (position < data?.size!!) {
            val supplier = data?.get(position)//  supplierList.[position]
            holder.name.text = supplier?.name
                val url = Retailer.logoBaseURL.plus("/").plus(supplier?.logo)

            GlideApp.with(holder.itemView.context).load(url)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .error(za.co.topitup.suppliers.R.drawable.ic_error)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        @Nullable e: GlideException?,
                        model: Any,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any,
                        target: Target<Drawable?>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).into(holder.logo)
           */
/* supplier?.logo?.let { logo ->
                loadSupplierImage(
                    context = holder.itemView.context,
                    imageView = holder.logo,
                    imageFileName = logo,
                    errorImageDrawable = R.drawable.ic_suppliers,
                )
            }*//*

            if (supplier?.requiresActivation?.not() == true) {
                holder.activationRequired.text = holder.itemView.context.getString(R.string.no_activation_required)
            } else {
                holder.activationRequired.text = holder.itemView.context.getString(R.string.activation_required)
            }

            supplier?.distance?.let { holder.distance.toDistanceString(it) }

            holder.category.text = supplier?.category
            holder.phone.text = supplier?.phone
            holder.address.text = supplier?.address

        }

    }

    override fun getItemCount(): Int {
        return data?.size!!
    }


    class OnClickListener(val clickListener: (supplier: SupplierRealm?) -> Unit) {
        fun onAddClick(supplier: SupplierRealm?) = clickListener(supplier)
    }

    */
/* ----------------------- ViewHolder --------------------------- *//*


    inner class AddSupplierViewHolder(binding: AddSupplierCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val logo: ImageView = binding.logoImageView
        val name: TextView = binding.nameTextView
        val distance = binding.distanceTextView
        val vendorActivated = binding.activatedImageView
        val addButton = binding.addButton
        val infoButton = binding.infoButton
        val category = binding.categoryTextView
        val phone = binding.phoneTextView
        val address = binding.addressTextView
        val activationRequired = binding.activationRequiredTextView

        val infoLayout = binding.infoLinearLayout

    }

    */
/* ----------------------- Filter --------------------------- *//*

    private fun filterResults(name: String, type: String) {
        val results = SupplierDatabaseOperations().getSuppliers(name, type)
        updateData(results)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val strings = constraint?.split(":")
                if (strings != null) {
                    val name = strings[1]
                    val category = if (strings[3] == "All") {
                        ""
                    } else {
                        strings[3]
                    }
                    filterResults(name, category)
                }
            }

        }
    }
}*/
