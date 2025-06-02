package com.example.finalprojectapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val isYearlyView: Boolean = false
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        
        // 연간 보기일 때는 날짜를, 그 외에는 시간을 표시
        if (isYearlyView) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            holder.tvTime.text = transaction.date
        } else {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            holder.tvTime.text = transaction.time.substring(0, 5)  // HH:mm 형식으로 표시
        }
        
        holder.tvCategory.text = transaction.categoryName
        
        val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
        val formattedAmount = numberFormat.format(transaction.amount)
        holder.tvAmount.text = "₩ $formattedAmount"
        
        // 수입은 연한 초록색, 지출은 연한 빨간색으로 표시
        val textColor = if (transaction.type == "income") {
            Color.parseColor("#4CAF50") // 연한 초록색
        } else {
            Color.parseColor("#F44336") // 연한 빨간색
        }
        holder.tvAmount.setTextColor(textColor)

        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} 