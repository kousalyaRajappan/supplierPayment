package za.co.topitup.suppliers.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.HistoryItemsBinding
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.utils.logger
import za.co.topitup.suppliers.utils.toCurrency
import java.text.SimpleDateFormat
import java.util.*

// Custom adapter
class HistoryRecycleViewAdapter(
    private var paymentList: List<PaymentRealm>,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<HistoryRecycleViewAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutView = HistoryViewHolder(
            HistoryItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.itemView.setOnClickListener {
            val position = layoutView.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onClickListener.onClick(paymentList[position])
            }
        }

        return layoutView
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val payment = paymentList[position]
        holder.bind(payment)
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }

    class OnClickListener(val clickListener: (payment: PaymentRealm) -> Unit) {
        fun onClick(payment: PaymentRealm) = clickListener(payment)
    }

    inner class HistoryViewHolder(private val binding: HistoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentRealm) {
            binding.nameTextView.text = payment.name
            binding.dateTextView.text = payment.date
            binding.timeTextView.text = payment.time
            binding.amountTextView.text = payment.amount.toCurrency(payment.amount)
            binding.referenceTextView.text = payment.paymentReference
        }
    }

    fun updateData(newData: List<PaymentRealm>) {
        paymentList = newData
        notifyDataSetChanged()
    }

    private fun filterResults(name: String, dateFrom: Date, dateTo: Date) {
        // Replace this with the appropriate query logic for the latest Realm API
        val results = SupplierDatabaseOperations().getPayments(name, dateFrom, dateTo)
        updateData(results)
    }

    fun applyFilter(constraint: CharSequence?) {
        val strings = constraint?.split(":")
        if (strings != null) {
            val name = if (strings[1] == "All") {
                ""
            } else {
                strings[1]
            }
            val date = if (strings[3] == "") {
                "2022-05-01"
            } else {
                strings[3]
            }

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateFrom = formatter.parse(date)

            val formatter2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateTo = if (date == "2022-05-01") {
                formatter2.parse("2099-01-01 23:59:59")
            } else {
                formatter2.parse("$date 23:59:59")
            }

            if (dateFrom != null && dateTo != null) {
                filterResults(name, dateFrom, dateTo)
            }
        }
    }
}

/*
class HistoryRecycleViewAdapter(
    paymentList: RealmResults<PaymentRealm>,
    private val onClickListener: OnClickListener
) : RealmRecyclerViewAdapter<PaymentRealm,
        HistoryRecycleViewAdapter.HistoryViewHolder>(
    paymentList, true, true
),
    Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutView = HistoryViewHolder(
            HistoryItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        layoutView.itemView.setOnClickListener {
            val position = layoutView.absoluteAdapterPosition
            data?.get(position) //paymentList[position]
        }

        return layoutView
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (position < data?.size!!) {
            val payment = data?.get(position)
            if (payment != null) {
                holder.name.text = payment.name

                //val dateString = payment.date?.let { SimpleDateFormat.getDateInstance().format(it) }
                // TODO Decide which to use
                //  val timeString = SimpleDateFormat("HH:mm").format(payment.date)
                //val time = payment.date?.let { SimpleDateFormat.getTimeInstance().format(it) }

                holder.date.text = payment.date
                holder.time.text = payment.time

                holder.amount.toCurrency(payment.amount)

                //TODO Change to string resource
                holder.reference.text = payment.paymentReference

            }


            holder.itemView.setOnClickListener {
                onClickListener.onClick(data!![position])
            }
        }
    }

    override fun getItemCount(): Int {
        return data?.size!! //paymentList.size
    }

    class OnClickListener(val clickListener: (payment: PaymentRealm) -> Unit) {
        fun onClick(payment: PaymentRealm) = clickListener(payment)
    }

    inner class HistoryViewHolder(binding: HistoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val name = binding.nameTextView
        val date = binding.dateTextView
        val amount = binding.amountTextView
        val reference = binding.referenceTextView
        val time = binding.timeTextView

    }

    private fun filterResults(name: String, dateFrom: Date, dateTo: Date) {

        val results = SupplierDatabaseOperations().getPayments(name, dateFrom, dateTo)
        updateData(results)

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                logger("Filter", "Filter: $constraint")
                val strings = constraint?.split(":")
                if (strings != null) {
                    val name = if (strings[1] == "All") {
                        ""
                    } else {
                        strings[1]
                    }
                    val date = if (strings[3] == "") {
                        "2022-05-01"
                    } else {
                        strings[3]
                    }

                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateFrom = formatter.parse(date)

                    val formatter2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dateTo = if (date == "2022-05-01") {
                        formatter2.parse("2099-01-01 23:59:59")
                    } else {
                        formatter2.parse(date.plus(" 23:59:59"))
                    }
                    if (dateFrom != null) {
                        if (dateTo != null) {
                            filterResults(name, dateFrom, dateTo)
                            logger("History Filter","$name: $dateFrom -> $dateTo" )
                        }
                    }


                }
            }


        }
    }


}*/
