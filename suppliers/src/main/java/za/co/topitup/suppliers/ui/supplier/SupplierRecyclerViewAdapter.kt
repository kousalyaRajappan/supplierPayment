package za.co.topitup.suppliers.ui.supplier


import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import za.co.topitup.suppliers.GlideApp
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.SupplierCardBinding
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.SupplierRealm

class SupplierRecyclerViewAdapter(
    private val onItemClickListener: OnClickListener,
    private val onInfoClickListener: OnClickListener,
    private var supplierList: List<SupplierRealm> = listOf()
) : RecyclerView.Adapter<SupplierRecyclerViewAdapter.SupplierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val layoutView = SupplierViewHolder(
            SupplierCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.itemView.setOnClickListener {
            val position = layoutView.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(supplierList[position])
            }
        }

        layoutView.infoButton.setOnClickListener {
            val position = layoutView.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onInfoClickListener.onInfoClick(supplierList[position])
            }
        }

        return layoutView
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val supplier = supplierList[position]
        holder.bind(supplier)
    }

    override fun getItemCount(): Int = supplierList.size

    fun updateData(newData: List<SupplierRealm>) {
        supplierList = newData
        notifyDataSetChanged()
    }

    class OnClickListener(val clickListener: (supplier: SupplierRealm) -> Unit) {
        fun onItemClick(supplier: SupplierRealm) = clickListener(supplier)
        fun onInfoClick(supplier: SupplierRealm) = clickListener(supplier)
    }

    inner class SupplierViewHolder(private val binding: SupplierCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val logo = binding.logoImageView
        val name = binding.nameTextView
        val distance = binding.distanceTextView
        val pending = binding.pendingActivationTextView
        val infoButton = binding.infoImageView

        fun bind(supplier: SupplierRealm) {
            name.text = supplier.name
            pending.visibility = if (supplier.activationPending) View.VISIBLE else View.INVISIBLE
            supplier.logo?.let { logoUrl ->
                val url = Retailer.logoBaseURL.plus("/").plus(logoUrl)
                GlideApp.with(itemView.context)
                    .load(url)
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
                    }).into(logo)
            }
            distance.text = supplier.distance?.let { "$it km" } ?: "N/A"
        }
    }

    fun applyFilter(coverage: String) {
        val results = SupplierDatabaseOperations().getActivatedSuppliers(coverage)
        updateData(results)
    }
}
