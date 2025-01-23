package za.co.topitup.suppliers.ui.supplier

import android.R
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import za.co.topitup.suppliers.GlideApp
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.SupplierCardBinding
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.utils.toDistanceString
import javax.annotation.Nullable


class SupplierRecyclerViewAdapter(
    private val onItemClickListener: OnClickListener,
    private val onInfoClickListener: OnClickListener,
    supplierList: RealmResults<SupplierRealm>
    ) : RealmRecyclerViewAdapter<SupplierRealm?,
        SupplierRecyclerViewAdapter.SupplierViewHolder?>(
            supplierList, false, false
        ), Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val layoutView = SupplierViewHolder(
            SupplierCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.itemView.setOnClickListener {
            val position = layoutView.absoluteAdapterPosition
            data?.get(position)
        }
      /*  layoutView.infoButton.setOnClickListener {
            val position = layoutView.absoluteAdapterPosition
            supplierList[position]
        }*/
        return layoutView
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        if (position < data?.size!!) {

            val supplier = data?.get(position)
            holder.name.text = supplier?.name

            holder.pending.visibility = View.INVISIBLE
            if (supplier?.activationPending == true) {
                holder.pending.visibility = View.VISIBLE
            }

            supplier?.logo?.let {logo ->
                val url = Retailer.logoBaseURL.plus("/").plus(logo)

                Log.e("url........","...........111"+supplier?.logo)



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

                /*loadSupplierImage(
                    context = holder.itemView.context,
                    imageView = holder.logo,
                    imageFileName = logo,
                    errorImageDrawable = R.drawable.ic_suppliers,
                )*/
            }
            supplier?.distance?.let { holder.distance.toDistanceString(it) }

           /* val category = "Category: ".plus(supplier.category)
            holder.category.text = category
            holder.lastPurchase.text = supplier.lastPurchase.toString()*/

            holder.itemView.setOnClickListener {
                onItemClickListener.onItemClick(data?.get(position))
            }
            holder.infoButton.setOnClickListener {
                onInfoClickListener.onInfoClick(data?.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return data?.size!!
    }

    //TODO Supplier -> SupplierEntry
    class OnClickListener(val clickListener: (supplier: SupplierRealm?) -> Unit) {
        fun onItemClick(supplier: SupplierRealm?) = clickListener(supplier)
        fun onInfoClick(supplier: SupplierRealm?) = clickListener(supplier)

    }

    inner class SupplierViewHolder(binding: SupplierCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val logo = binding.logoImageView
        val name = binding.nameTextView
        val distance = binding.distanceTextView
        val pending = binding.pendingActivationTextView
        //val category = binding.categoryTextView
        //val lastPurchase = binding.purchaseTextView
        val infoButton = binding.infoImageView

    }

    /*-----------------------------------------------------------------------*/

    private fun filterResults(coverage: String) {
        val results = SupplierDatabaseOperations().getActivatedSuppliers(coverage)
        updateData(null)
        updateData(results)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                var coverage = ""

                if (constraint == "LOCAL") {
                     coverage = Retailer.province
                }
                else if (constraint == "NATIONAL") {
                     coverage = "National"
                }

                filterResults(coverage)

            }
        }
    }
}
