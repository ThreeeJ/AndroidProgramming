package com.example.finalprojectapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageButton

class CategoryListFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var type: String
    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    companion object {
        private const val TAG = "CategoryListFragment"
        
        fun newInstance(type: String): CategoryListFragment {
            return CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type") ?: "income"
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            rvCategories = view.findViewById(R.id.rvCategories)
            rvCategories.layoutManager = LinearLayoutManager(context)
            categoryAdapter = CategoryAdapter(emptyList(),
                onEditClick = { category -> showEditDialog(category) },
                onDeleteClick = { category -> showDeleteDialog(category) }
            )
            rvCategories.adapter = categoryAdapter

            view.findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
                showAddDialog()
            }

            loadCategories()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "화면 초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        try {
            val categories = dbHelper.getCategories(type)
            if (categories.isEmpty() && type == "income") {
                // 수입 카테고리가 비어있으면 기본 카테고리 추가
                val defaultCategories = listOf("급여", "용돈", "기타수입")
                for (categoryName in defaultCategories) {
                    dbHelper.addCategory(categoryName, "income")
                }
            } else if (categories.isEmpty() && type == "expense") {
                // 지출 카테고리가 비어있으면 기본 카테고리 추가
                val defaultCategories = listOf("식비", "교통", "생활", "기타지출")
                for (categoryName in defaultCategories) {
                    dbHelper.addCategory(categoryName, "expense")
                }
            }
            
            // 카테고리 다시 로드
            val updatedCategories = dbHelper.getCategories(type)
            categoryAdapter.updateCategories(updatedCategories)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "카테고리 로딩 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddDialog() {
        try {
            val dialog = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null)
            val editText = dialogView.findViewById<EditText>(R.id.editText)

            dialog.setView(dialogView)
                .setTitle("카테고리 추가")
                .setPositiveButton("추가", null)
                .setNegativeButton("취소", null)

            val alertDialog = dialog.create()
            alertDialog.show()

            // Positive 버튼에 대한 클릭 리스너를 따로 설정
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val categoryName = editText.text.toString().trim()
                android.util.Log.d(TAG, "카테고리 추가 시도: 이름=$categoryName, 타입=$type")

                if (categoryName.isEmpty()) {
                    editText.error = "카테고리 이름을 입력해주세요"
                    android.util.Log.d(TAG, "카테고리 이름이 비어있음")
                    return@setOnClickListener
                }

                try {
                    if (dbHelper.isCategoryExists(categoryName, type)) {
                        editText.error = "이미 존재하는 카테고리입니다"
                        android.util.Log.d(TAG, "중복된 카테고리: $categoryName")
                        return@setOnClickListener
                    }

                    val result = dbHelper.addCategory(categoryName, type)
                    android.util.Log.d(TAG, "카테고리 추가 결과: $result")

                    if (result != -1L) {
                        loadCategories()
                        Toast.makeText(context, "카테고리가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    } else {
                        editText.error = "카테고리 추가에 실패했습니다. 다시 시도해주세요."
                        android.util.Log.e(TAG, "카테고리 추가 실패: 데이터베이스 오류")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e(TAG, "카테고리 추가 중 예외 발생: ${e.message}")
                    Toast.makeText(context, "카테고리 추가 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e(TAG, "다이얼로그 표시 중 예외 발생: ${e.message}")
            Toast.makeText(context, "다이얼로그 표시 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(category: Category) {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        editText.setText(category.name)

        dialog.setView(dialogView)
            .setTitle("카테고리 수정")
            .setPositiveButton("수정") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    try {
                        dbHelper.updateCategory(category.id, newName)
                        loadCategories()
                        Toast.makeText(context, "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "카테고리 수정 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("카테고리 삭제")
            .setMessage("이 카테고리를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                try {
                    dbHelper.deleteCategory(category.id)
                    loadCategories()
                    Toast.makeText(context, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "카테고리 삭제 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    inner class CategoryAdapter(
        private var categories: List<Category>,
        private val onEditClick: (Category) -> Unit,
        private val onDeleteClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.tvCategoryName.text = category.name
            holder.btnEdit.setOnClickListener { onEditClick(category) }
            holder.btnDelete.setOnClickListener { onDeleteClick(category) }
        }

        override fun getItemCount() = categories.size

        fun updateCategories(newCategories: List<Category>) {
            categories = newCategories
            notifyDataSetChanged()
        }
    }
} 