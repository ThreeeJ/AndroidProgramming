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

/**
 * 카테고리 목록을 관리하는 프래그먼트
 * 수입/지출 카테고리의 추가/수정/삭제 기능을 제공합니다.
 */
class CategoryListFragment : Fragment() {
    // 데이터베이스 헬퍼 클래스
    private lateinit var dbHelper: DB
    // 카테고리 유형 ("income" 또는 "expense")
    private lateinit var type: String
    // 카테고리 목록을 표시하는 RecyclerView
    private lateinit var rvCategories: RecyclerView
    // 카테고리 어댑터
    private lateinit var categoryAdapter: CategoryAdapter

    companion object {
        private const val TAG = "CategoryListFragment"
        
        /**
         * CategoryListFragment의 새로운 인스턴스를 생성하는 팩토리 메서드
         * @param type 카테고리 유형 ("income" 또는 "expense")
         */
        fun newInstance(type: String): CategoryListFragment {
            return CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
        }
    }

    /**
     * 프래그먼트가 생성될 때 호출되는 메서드
     * 카테고리 유형과 데이터베이스 헬퍼를 초기화합니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("type") ?: "income"
        dbHelper = DB(requireContext())
    }

    /**
     * 프래그먼트의 뷰가 생성될 때 호출되는 메서드
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_category_list, container, false)
    }

    /**
     * 프래그먼트의 뷰가 생성된 후 호출되는 메서드
     * RecyclerView와 어댑터를 초기화하고 카테고리를 로드합니다.
     */
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

    /**
     * 카테고리 목록을 데이터베이스에서 로드하는 메서드
     * 카테고리가 없는 경우 기본 카테고리를 추가합니다.
     */
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

    /**
     * 카테고리 추가 다이얼로그를 표시하는 메서드
     * 사용자가 입력한 카테고리 이름의 유효성을 검사하고 데이터베이스에 추가합니다.
     */
    private fun showAddDialog() {
        try {
            val dialog = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
            val editText = dialogView.findViewById<EditText>(R.id.etCategoryName)
            val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            val alertDialog = dialog.create()
            alertDialog.setView(dialogView)
            
            // 다이얼로그 크기 설정
            alertDialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            
            alertDialog.show()

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            btnAdd.setOnClickListener {
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

    /**
     * 카테고리 수정 다이얼로그를 표시하는 메서드
     * @param category 수정할 카테고리
     */
    private fun showEditDialog(category: Category) {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_category, null)
        val editText = dialogView.findViewById<EditText>(R.id.etCategoryName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        
        editText.setText(category.name)

        val alertDialog = dialog.create()
        alertDialog.setView(dialogView)
        
        // 다이얼로그 크기 설정
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        alertDialog.show()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newName = editText.text.toString().trim()
            if (newName.isEmpty()) {
                editText.error = "카테고리 이름을 입력해주세요"
                return@setOnClickListener
            }

            try {
                if (dbHelper.isCategoryExists(newName, type) && newName != category.name) {
                    editText.error = "이미 존재하는 카테고리입니다"
                    return@setOnClickListener
                }

                dbHelper.updateCategory(category.id, newName)
                loadCategories()
                Toast.makeText(context, "카테고리가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "카테고리 수정 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 카테고리 삭제 확인 다이얼로그를 표시하는 메서드
     * @param category 삭제할 카테고리
     */
    private fun showDeleteDialog(category: Category) {
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        val alertDialog = dialog.create()
        alertDialog.setView(dialogView)
        
        // 다이얼로그 크기 설정
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        alertDialog.show()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            try {
                dbHelper.deleteCategory(category.id)
                loadCategories()
                Toast.makeText(context, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "카테고리 삭제 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 카테고리 목록을 표시하는 RecyclerView 어댑터
     * 각 카테고리 항목에 대한 수정/삭제 기능을 제공합니다.
     *
     * @property categories 표시할 카테고리 목록
     * @property onEditClick 수정 버튼 클릭 시 호출될 콜백 함수
     * @property onDeleteClick 삭제 버튼 클릭 시 호출될 콜백 함수
     */
    inner class CategoryAdapter(
        private var categories: List<Category>,
        private val onEditClick: (Category) -> Unit,
        private val onDeleteClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        /**
         * 카테고리 항목의 뷰를 담당하는 ViewHolder 클래스
         */
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }

        /**
         * ViewHolder를 생성하는 메서드
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }

        /**
         * ViewHolder에 데이터를 바인딩하는 메서드
         * 카테고리 이름을 표시하고 수정/삭제 버튼의 클릭 리스너를 설정합니다.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = categories[position]
            holder.tvCategoryName.text = category.name
            holder.btnEdit.setOnClickListener { onEditClick(category) }
            holder.btnDelete.setOnClickListener { onDeleteClick(category) }
        }

        /**
         * 카테고리 목록의 크기를 반환하는 메서드
         */
        override fun getItemCount() = categories.size

        /**
         * 카테고리 목록을 업데이트하는 메서드
         * @param newCategories 새로운 카테고리 목록
         */
        fun updateCategories(newCategories: List<Category>) {
            categories = newCategories
            notifyDataSetChanged()
        }
    }
} 