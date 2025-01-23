package za.co.topitup.suppliers.ui.history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.HistoryItemsBinding
import za.co.topitup.suppliers.models.MyItem
import za.co.topitup.suppliers.models.PaymentRealm
import za.co.topitup.suppliers.models.SupplierRealm
import za.co.topitup.suppliers.network.ApiResponse.InvalidData.data
import za.co.topitup.suppliers.ui.supplier.SupplierRecyclerViewAdapter
import za.co.topitup.suppliers.ui.supplier.manage.AddSupplierRecyclerViewAdapter

class HistoryListAdapter(
    private val onItemClickListener: OnClickListener,

    ):RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder>() {
    private var paymentList : ArrayList<MyItem>? = null


    fun setPaymentList(paymentList  : ArrayList<MyItem>){
        this.paymentList = paymentList

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {
        val layoutView = HistoryViewHolder(
            HistoryItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_view_item,parent,false)
        return  layoutView
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val myItem = paymentList?.get(position)
        if (myItem != null) {
            var newStrg= myItem.btn_date
            val mString = newStrg!!.split(",").toTypedArray()

            holder.name.text =myItem.supAmount
            holder.date.text =mString[0]
            holder.reference.text =myItem.supplierRef
            holder.amount.text =myItem.supCustomer
            holder.time.text = mString[1]

        }
        holder.itemView.setOnClickListener {
            myItem?.let { it1 -> onItemClickListener.onClick(it1) }
        }

    }

    override fun getItemCount(): Int {

        if(paymentList == null) return 0
        else return paymentList?.size!!
    }


    inner class HistoryViewHolder(binding: HistoryItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val name = binding.nameTextView
        val date = binding.dateTextView
        val amount = binding.amountTextView
        val reference = binding.referenceTextView
        val time = binding.timeTextView

    }

    class OnClickListener(val clickListener: (payment: MyItem?) -> Unit) {
        fun onClick(payment: MyItem) = clickListener(payment)

    }

}