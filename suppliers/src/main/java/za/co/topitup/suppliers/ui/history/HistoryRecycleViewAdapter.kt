package za.co.topitup.suppliers.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.topitup.suppliers.database.SupplierDatabaseOperations
import za.co.topitup.suppliers.databinding.HistoryItemsBinding
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.utils.toCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
