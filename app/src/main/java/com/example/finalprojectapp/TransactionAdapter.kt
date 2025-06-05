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

/**
 * 거래 내역 목록을 표시하는 RecyclerView 어댑터
 *
 * @property transactions 표시할 거래 내역 목록
 * @property onItemClick 거래 내역 항목 클릭 시 호출될 콜백 함수
 * @property isYearlyView 연간 보기 여부 (true일 경우 날짜를, false일 경우 시간을 표시)
 */
class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit,
    private val isYearlyView: Boolean = false
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    /**
     * 거래 내역 항목의 뷰를 담당하는 ViewHolder 클래스
     *
     * @property tvTime 시간/날짜를 표시하는 TextView
     * @property tvCategory 카테고리 이름을 표시하는 TextView
     * @property tvAmount 거래 금액을 표시하는 TextView
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    /**
     * ViewHolder를 생성하는 메서드
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    /**
     * ViewHolder에 데이터를 바인딩하는 메서드
     * - 시간/날짜 표시 (연간 보기 여부에 따라 다름)
     * - 카테고리 이름 표시
     * - 금액 표시 (수입은 초록색, 지출은 빨간색)
     */
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

    /**
     * 거래 내역 목록의 크기를 반환하는 메서드
     */
    override fun getItemCount() = transactions.size

    /**
     * 거래 내역 목록을 업데이트하는 메서드
     * @param newTransactions 새로운 거래 내역 목록
     */
    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} 