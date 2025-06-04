package com.example.finalprojectapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDB"
        private const val DATABASE_VERSION = 4

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Categories table
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_TYPE = "type" // 'income' or 'expense'

        // Transactions table
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TRANSACTION_AMOUNT = "amount"
        private const val COLUMN_TRANSACTION_TYPE = "type" // 'income' or 'expense'
        private const val COLUMN_TRANSACTION_CATEGORY_ID = "category_id"
        private const val COLUMN_TRANSACTION_DATE = "date"
        private const val COLUMN_TRANSACTION_TIME = "time"

        private const val TAG = "DatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Users table
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_NAME TEXT,
                    $COLUMN_USERNAME TEXT UNIQUE,
                    $COLUMN_PASSWORD TEXT
                )
            """.trimIndent()
            db.execSQL(createUsersTable)

            // Categories table - TEXT 길이 제한 제거
            val createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                    $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CATEGORY_NAME TEXT,
                    $COLUMN_CATEGORY_TYPE TEXT
                )
            """.trimIndent()
            db.execSQL(createCategoriesTable)

            // Transactions table
            val createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                    $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TRANSACTION_AMOUNT INTEGER,
                    $COLUMN_TRANSACTION_TYPE TEXT,
                    $COLUMN_TRANSACTION_CATEGORY_ID INTEGER,
                    $COLUMN_TRANSACTION_DATE TEXT,
                    $COLUMN_TRANSACTION_TIME TEXT,
                    FOREIGN KEY($COLUMN_TRANSACTION_CATEGORY_ID) 
                    REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID)
                )
            """.trimIndent()
            db.execSQL(createTransactionsTable)

            // 기본 카테고리 추가
            insertDefaultCategories(db)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        android.util.Log.d(TAG, "데이터베이스 업그레이드: $oldVersion -> $newVersion")
        try {
            // 기존 테이블 삭제
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            
            // 테이블 다시 생성
            onCreate(db)
            
            android.util.Log.d(TAG, "데이터베이스 업그레이드 완료")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "데이터베이스 업그레이드 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        // 수입 카테고리
        val incomeCategories = arrayOf("급여", "용돈", "기타수입")
        for (category in incomeCategories) {
            val values = ContentValues().apply {
                put(COLUMN_CATEGORY_NAME, category)
                put(COLUMN_CATEGORY_TYPE, "income")
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }

        // 지출 카테고리
        val expenseCategories = arrayOf("식비", "교통", "생활", "기타지출")
        for (category in expenseCategories) {
            val values = ContentValues().apply {
                put(COLUMN_CATEGORY_NAME, category)
                put(COLUMN_CATEGORY_TYPE, "expense")
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    // 카테고리 관련 메서드
    fun getCategories(type: String): List<Category> {
        val categories = mutableListOf<Category>()
        var cursor: Cursor? = null
        try {
            val db = this.readableDatabase
            cursor = db.query(
                TABLE_CATEGORIES,
                null,
                "$COLUMN_CATEGORY_TYPE = ?",
                arrayOf(type),
                null,
                null,
                "$COLUMN_CATEGORY_NAME ASC" // 이름순 정렬 추가
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME))
                    val categoryType = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY_TYPE))
                    categories.add(Category(id, name, categoryType))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return categories
    }

    // 카테고리 중복 체크 메서드 추가
    fun isCategoryExists(name: String, type: String): Boolean {
        var cursor: Cursor? = null
        try {
            val db = this.readableDatabase
            cursor = db.query(
                TABLE_CATEGORIES,
                arrayOf(COLUMN_CATEGORY_ID),
                "$COLUMN_CATEGORY_NAME = ? AND $COLUMN_CATEGORY_TYPE = ?",
                arrayOf(name, type),
                null, null, null
            )
            val exists = cursor?.count ?: 0 > 0
            android.util.Log.d(TAG, "카테고리 중복 체크: 이름=$name, 타입=$type, 존재여부=$exists")
            return exists
        } catch (e: Exception) {
            android.util.Log.e(TAG, "카테고리 중복 체크 중 오류 발생: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            cursor?.close()
        }
    }

    fun addCategory(name: String, type: String): Long {
        var result = -1L
        try {
            android.util.Log.d(TAG, "카테고리 추가 시도: 이름=$name, 타입=$type")
            
            // 중복 체크
            if (isCategoryExists(name, type)) {
                android.util.Log.d(TAG, "중복된 카테고리 발견: $name")
                return -1L
            }

            val db = this.writableDatabase
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(COLUMN_CATEGORY_NAME, name)
                    put(COLUMN_CATEGORY_TYPE, type)
                }
                
                result = db.insert(TABLE_CATEGORIES, null, values)
                android.util.Log.d(TAG, "데이터베이스 삽입 결과: $result")
                
                if (result != -1L) {
                    db.setTransactionSuccessful()
                    android.util.Log.d(TAG, "카테고리 추가 성공")
                } else {
                    android.util.Log.e(TAG, "카테고리 추가 실패: 데이터베이스 삽입 실패")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "카테고리 추가 중 오류 발생: ${e.message}")
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "데이터베이스 접근 중 오류 발생: ${e.message}")
            e.printStackTrace()
        }
        return result
    }

    fun updateCategory(id: Int, name: String): Int {
        var result = 0
        try {
            val db = this.writableDatabase
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(COLUMN_CATEGORY_NAME, name.trim())
                }
                result = db.update(
                    TABLE_CATEGORIES,
                    values,
                    "$COLUMN_CATEGORY_ID = ?",
                    arrayOf(id.toString())
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun deleteCategory(id: Int): Int {
        var result = 0
        try {
            val db = this.writableDatabase
            db.beginTransaction()
            try {
                result = db.delete(
                    TABLE_CATEGORIES,
                    "$COLUMN_CATEGORY_ID = ?",
                    arrayOf(id.toString())
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // 거래 내역 관련 메서드
    fun addTransaction(amount: Int, type: String, categoryId: Int, date: String, time: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRANSACTION_AMOUNT, amount)
            put(COLUMN_TRANSACTION_TYPE, type)
            put(COLUMN_TRANSACTION_CATEGORY_ID, categoryId)
            put(COLUMN_TRANSACTION_DATE, date)
            put(COLUMN_TRANSACTION_TIME, time)
        }
        val result = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return result
    }

    fun getTransactions(date: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val query = """
            SELECT t.*, c.name as category_name, c.type as category_type 
            FROM $TABLE_TRANSACTIONS t 
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID 
            WHERE t.$COLUMN_TRANSACTION_DATE = ? 
            ORDER BY t.$COLUMN_TRANSACTION_TIME DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(date))

        with(cursor) {
            while (moveToNext()) {
                transactions.add(
                    Transaction(
                        getInt(getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        getInt(getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        getString(getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                        getInt(getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)),
                        getString(getColumnIndexOrThrow("category_name")),
                        getString(getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)),
                        getString(getColumnIndexOrThrow(COLUMN_TRANSACTION_TIME))
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return transactions
    }

    fun updateTransaction(id: Int, amount: Int, categoryId: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TRANSACTION_AMOUNT, amount)
            put(COLUMN_TRANSACTION_CATEGORY_ID, categoryId)
        }
        val result = db.update(
            TABLE_TRANSACTIONS,
            values,
            "$COLUMN_TRANSACTION_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return result
    }

    fun deleteTransaction(id: Int): Int {
        var result = 0
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            result = db.delete(
                TABLE_TRANSACTIONS,
                "$COLUMN_TRANSACTION_ID = ?",
                arrayOf(id.toString())
            )
            if (result > 0) {
                db.setTransactionSuccessful()
                android.util.Log.d(TAG, "거래 내역 삭제 성공: ID=$id")
            } else {
                android.util.Log.e(TAG, "거래 내역 삭제 실패: ID=$id")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "거래 내역 삭제 중 오류 발생: ${e.message}")
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        return result
    }

    // 기존 User 관련 메서드들...
    fun addUser(name: String, username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)
        
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    fun isUserExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null)
        
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null)
        
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // 연간 합계 계산
    fun getYearlySummary(year: String): Pair<Int, Int> {
        var income = 0
        var expense = 0
        val db = this.readableDatabase
        
        val query = """
            SELECT t.type, SUM(t.amount) as total
            FROM $TABLE_TRANSACTIONS t
            WHERE t.$COLUMN_TRANSACTION_DATE LIKE '$year%'
            GROUP BY t.type
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf())
        cursor.use {
            while (it.moveToNext()) {
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val total = it.getInt(it.getColumnIndexOrThrow("total"))
                if (type == "income") income = total
                else if (type == "expense") expense = total
            }
        }
        
        return Pair(income, expense)
    }

    // 월간 합계 계산
    fun getMonthlySummary(yearMonth: String): Pair<Int, Int> {
        var income = 0
        var expense = 0
        val db = this.readableDatabase
        
        val query = """
            SELECT t.type, SUM(t.amount) as total
            FROM $TABLE_TRANSACTIONS t
            WHERE substr(t.$COLUMN_TRANSACTION_DATE, 1, 7) = ?
            GROUP BY t.type
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(yearMonth))
        cursor.use {
            while (it.moveToNext()) {
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val total = it.getInt(it.getColumnIndexOrThrow("total"))
                if (type == "income") income = total
                else if (type == "expense") expense = total
            }
        }
        
        return Pair(income, expense)
    }

    // 일별 거래 내역 조회 (최대 3개)
    fun getRecentTransactions(date: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val query = """
            SELECT t.*, c.name as category_name, c.type as category_type 
            FROM $TABLE_TRANSACTIONS t 
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID 
            WHERE t.$COLUMN_TRANSACTION_DATE = ? 
            ORDER BY t.$COLUMN_TRANSACTION_TIME DESC
            LIMIT 3
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(date))
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    Transaction(
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)),
                        it.getString(it.getColumnIndexOrThrow("category_name")),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TIME))
                    )
                )
            }
        }
        return transactions
    }

    // 전체 거래 내역 조회
    fun getAllTransactions(date: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val query = """
            SELECT t.*, c.name as category_name, c.type as category_type 
            FROM $TABLE_TRANSACTIONS t 
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID 
            WHERE t.$COLUMN_TRANSACTION_DATE = ? 
            ORDER BY t.$COLUMN_TRANSACTION_TIME DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(date))
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    Transaction(
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)),
                        it.getString(it.getColumnIndexOrThrow("category_name")),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TIME))
                    )
                )
            }
        }
        return transactions
    }

    // 일별 합계 계산
    fun getDailySummary(date: String): Pair<Int, Int> {
        var income = 0
        var expense = 0
        val db = this.readableDatabase
        
        try {
            val query = """
                SELECT t.type, SUM(t.amount) as total
                FROM $TABLE_TRANSACTIONS t
                WHERE t.$COLUMN_TRANSACTION_DATE = ?
                GROUP BY t.type
            """.trimIndent()
            
            val cursor = db.rawQuery(query, arrayOf(date))
            cursor.use {
                while (it.moveToNext()) {
                    val type = it.getString(it.getColumnIndexOrThrow("type"))
                    val total = it.getInt(it.getColumnIndexOrThrow("total"))
                    if (type == "income") income = total
                    else if (type == "expense") expense = total
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        
        return Pair(income, expense)
    }

    // 연간 상세 내역 조회
    fun getYearlyTransactions(year: String, type: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val query = """
            SELECT t.*, c.name as category_name, c.type as category_type 
            FROM $TABLE_TRANSACTIONS t 
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID 
            WHERE t.$COLUMN_TRANSACTION_DATE LIKE '$year%'
            AND t.$COLUMN_TRANSACTION_TYPE = ?
            ORDER BY t.$COLUMN_TRANSACTION_DATE DESC, t.$COLUMN_TRANSACTION_TIME DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(type))
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    Transaction(
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)),
                        it.getString(it.getColumnIndexOrThrow("category_name")),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TIME))
                    )
                )
            }
        }
        return transactions
    }

    // 월간 상세 내역 조회
    fun getMonthlyTransactions(yearMonth: String, type: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val query = """
            SELECT t.*, c.name as category_name, c.type as category_type 
            FROM $TABLE_TRANSACTIONS t 
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID 
            WHERE substr(t.$COLUMN_TRANSACTION_DATE, 1, 7) = ? 
            AND t.$COLUMN_TRANSACTION_TYPE = ?
            ORDER BY t.$COLUMN_TRANSACTION_DATE DESC, t.$COLUMN_TRANSACTION_TIME DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(yearMonth, type))
        cursor.use {
            while (it.moveToNext()) {
                transactions.add(
                    Transaction(
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                        it.getInt(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY_ID)),
                        it.getString(it.getColumnIndexOrThrow("category_name")),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE)),
                        it.getString(it.getColumnIndexOrThrow(COLUMN_TRANSACTION_TIME))
                    )
                )
            }
        }
        return transactions
    }

    // 사용자 이름 가져오기
    fun getUserName(username: String): String {
        val db = this.readableDatabase
        var name = ""
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_NAME),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
        }
        cursor.close()
        return name
    }

    // 사용자 이름 업데이트
    fun updateUserName(username: String, newName: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, newName)
        }
        
        return db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USERNAME = ?",
            arrayOf(username)
        )
    }

    // 회원 탈퇴 (사용자와 관련된 모든 데이터 삭제)
    fun deleteUser(username: String): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // 1. 사용자의 거래 내역 삭제
            val userId = getUserId(username)
            if (userId != -1) {
                db.delete(TABLE_TRANSACTIONS, null, null)
            }

            // 2. 사용자 삭제
            val result = db.delete(
                TABLE_USERS,
                "$COLUMN_USERNAME = ?",
                arrayOf(username)
            )

            if (result > 0) {
                db.setTransactionSuccessful()
                return true
            }
            return false
        } finally {
            db.endTransaction()
        }
    }

    // 사용자 ID 가져오기
    private fun getUserId(username: String): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            cursor.close()
            id
        } else {
            cursor.close()
            -1
        }
    }

    // 일별 카테고리별 지출 데이터 조회
    fun getDailyExpenseByCategory(date: String): Map<String, Float> {
        val categoryExpenses = mutableMapOf<String, Float>()
        val db = this.readableDatabase
        
        val query = """
            SELECT c.name as category_name, SUM(t.amount) as total_amount
            FROM $TABLE_TRANSACTIONS t
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            WHERE t.$COLUMN_TRANSACTION_DATE = ?
            AND t.$COLUMN_TRANSACTION_TYPE = 'expense'
            GROUP BY c.$COLUMN_CATEGORY_ID, c.name
            ORDER BY total_amount DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(date))
        cursor.use {
            while (it.moveToNext()) {
                val categoryName = it.getString(it.getColumnIndexOrThrow("category_name"))
                val amount = it.getFloat(it.getColumnIndexOrThrow("total_amount"))
                categoryExpenses[categoryName] = amount
            }
        }
        
        return categoryExpenses
    }

    // 월별 카테고리별 지출 데이터 조회
    fun getMonthlyExpenseByCategory(yearMonth: String): Map<String, Float> {
        val categoryExpenses = mutableMapOf<String, Float>()
        val db = this.readableDatabase
        
        val query = """
            SELECT c.name as category_name, SUM(t.amount) as total_amount
            FROM $TABLE_TRANSACTIONS t
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            WHERE substr(t.$COLUMN_TRANSACTION_DATE, 1, 7) = ?
            AND t.$COLUMN_TRANSACTION_TYPE = 'expense'
            GROUP BY c.$COLUMN_CATEGORY_ID, c.name
            ORDER BY total_amount DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(yearMonth))
        cursor.use {
            while (it.moveToNext()) {
                val categoryName = it.getString(it.getColumnIndexOrThrow("category_name"))
                val amount = it.getFloat(it.getColumnIndexOrThrow("total_amount"))
                categoryExpenses[categoryName] = amount
            }
        }
        
        return categoryExpenses
    }

    // 연간 카테고리별 지출 데이터 조회
    fun getYearlyExpenseByCategory(year: String): Map<String, Float> {
        val categoryExpenses = mutableMapOf<String, Float>()
        val db = this.readableDatabase
        
        val query = """
            SELECT c.name as category_name, SUM(t.amount) as total_amount
            FROM $TABLE_TRANSACTIONS t
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            WHERE t.$COLUMN_TRANSACTION_DATE LIKE '$year%'
            AND t.$COLUMN_TRANSACTION_TYPE = 'expense'
            GROUP BY c.$COLUMN_CATEGORY_ID, c.name
            ORDER BY total_amount DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf())
        cursor.use {
            while (it.moveToNext()) {
                val categoryName = it.getString(it.getColumnIndexOrThrow("category_name"))
                val amount = it.getFloat(it.getColumnIndexOrThrow("total_amount"))
                categoryExpenses[categoryName] = amount
            }
        }
        
        return categoryExpenses
    }
} 