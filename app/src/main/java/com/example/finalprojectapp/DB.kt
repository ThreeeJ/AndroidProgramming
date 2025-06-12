package com.example.finalprojectapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite 데이터베이스를 관리하는 헬퍼 클래스
 * 사용자, 카테고리, 거래 내역 테이블을 관리하며
 * 데이터의 추가/수정/삭제/조회 기능을 제공합니다.
 */
class DB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // 데이터베이스 기본 정보
        private const val DATABASE_NAME = "UserDB"
        private const val DATABASE_VERSION = 4

        // Users 테이블 관련 상수
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Categories 테이블 관련 상수
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_TYPE = "type" // 'income' or 'expense'

        // Transactions 테이블 관련 상수
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val COLUMN_TRANSACTION_ID = "id"
        private const val COLUMN_TRANSACTION_AMOUNT = "amount"
        private const val COLUMN_TRANSACTION_TYPE = "type" // 'income' or 'expense'
        private const val COLUMN_TRANSACTION_CATEGORY_ID = "category_id"
        private const val COLUMN_TRANSACTION_DATE = "date"
        private const val COLUMN_TRANSACTION_TIME = "time"

        private const val TAG = "DatabaseHelper"
    }

    /**
     * 데이터베이스가 처음 생성될 때 호출되는 메서드
     * 필요한 테이블들을 생성하고 기본 카테고리를 추가합니다.
     */
    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Users 테이블 생성
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_NAME TEXT,
                    $COLUMN_USERNAME TEXT UNIQUE,
                    $COLUMN_PASSWORD TEXT
                )
            """.trimIndent()
            db.execSQL(createUsersTable)

            // Categories 테이블 생성
            val createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                    $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CATEGORY_NAME TEXT,
                    $COLUMN_CATEGORY_TYPE TEXT
                )
            """.trimIndent()
            db.execSQL(createCategoriesTable)

            // Transactions 테이블 생성
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

    /**
     * 데이터베이스 버전이 업그레이드될 때 호출되는 메서드
     * 기존 테이블을 삭제하고 새로 생성합니다.
     */
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

    /**
     * 기본 카테고리를 데이터베이스에 추가하는 메서드
     * 수입 카테고리: 급여, 용돈, 기타수입
     * 지출 카테고리: 식비, 교통, 생활, 기타지출
     */
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

    /**
     * 지정된 타입의 모든 카테고리를 조회하는 메서드
     * @param type 카테고리 타입 ("income" 또는 "expense")
     * @return 카테고리 목록
     */
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

    /**
     * 카테고리 중복 여부를 확인하는 메서드
     * @param name 카테고리 이름
     * @param type 카테고리 타입
     * @return 중복된 카테고리가 있으면 true, 없으면 false
     */
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

    /**
     * 새로운 카테고리를 추가하는 메서드
     * @param name 카테고리 이름
     * @param type 카테고리 타입
     * @return 성공 시 새로운 카테고리의 ID, 실패 시 -1
     */
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

    /**
     * 카테고리 정보를 수정하는 메서드
     * @param id 수정할 카테고리의 ID
     * @param name 새로운 카테고리 이름
     * @return 수정된 행의 수
     */
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

    /**
     * 카테고리를 삭제하는 메서드
     * @param id 삭제할 카테고리의 ID
     * @return 삭제된 행의 수
     */
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

    /**
     * 새로운 거래 내역을 추가하는 메서드
     * @param amount 금액
     * @param type 거래 유형 ("income" 또는 "expense")
     * @param categoryId 카테고리 ID
     * @param date 거래 날짜 (yyyy-MM-dd 형식)
     * @param time 거래 시간 (HH:mm:ss 형식)
     * @return 성공 시 새로운 거래 내역의 ID, 실패 시 -1
     */
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

    /**
     * 특정 날짜의 모든 거래 내역을 조회하는 메서드
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 거래 내역 목록
     */
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

    /**
     * 거래 내역을 수정하는 메서드
     * @param id 수정할 거래 내역의 ID
     * @param amount 새로운 금액
     * @param categoryId 새로운 카테고리 ID
     * @return 수정된 행의 수
     */
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

    /**
     * 거래 내역을 삭제하는 메서드
     * @param id 삭제할 거래 내역의 ID
     * @return 삭제된 행의 수
     */
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
    /**
     * 사용자 아이디 중복 여부를 확인하는 메서드
     * @param username 확인할 사용자 아이디
     * @return 중복된 아이디가 있으면 true, 없으면 false 반환
     */
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

    /**
     * 새로운 사용자를 데이터베이스에 추가하는 메서드
     * @param name 사용자 이름
     * @param username 사용자 아이디
     * @param password 비밀번호
     * @return 추가된 행의 ID, 실패 시 -1 반환
     */
    fun addUser(name: String, username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    /**
     * 로그인 정보를 검증하는 메서드
     * @param username 사용자 아이디
     * @param password 비밀번호
     * @return 로그인 정보가 유효하면 true, 아니면 false 반환
     */
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

    /**
     * 연간 수입/지출 합계를 계산하는 메서드
     * @param year 조회할 연도 (yyyy 형식)
     * @return Pair(수입 합계, 지출 합계)
     */
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

    /**
     * 월간 수입/지출 합계를 계산하는 메서드
     * @param yearMonth 조회할 연월 (yyyy-MM 형식)
     * @return Pair(수입 합계, 지출 합계)
     */
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

    /**
     * 특정 날짜의 최근 거래 내역 3개를 조회하는 메서드
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 최근 거래 내역 목록 (최대 3개)
     */
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

    /**
     * 특정 날짜의 모든 거래 내역을 조회하는 메서드
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 모든 거래 내역 목록
     */
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

    /**
     * 일별 수입/지출 합계를 계산하는 메서드
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return Pair(수입 합계, 지출 합계)
     */
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

    /**
     * 연간 거래 내역을 조회하는 메서드
     * @param year 조회할 연도 (yyyy 형식)
     * @param type 거래 유형 ("income" 또는 "expense")
     * @return 연간 거래 내역 목록
     */
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

    /**
     * 월간 거래 내역을 조회하는 메서드
     * @param yearMonth 조회할 연월 (yyyy-MM 형식)
     * @param type 거래 유형 ("income" 또는 "expense")
     * @return 월간 거래 내역 목록
     */
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

    /**
     * 사용자의 이름을 조회하는 메서드
     * @param username 사용자 아이디
     * @return 사용자 이름
     */
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

    /**
     * 사용자의 이름을 수정하는 메서드
     * @param username 사용자 아이디
     * @param newName 새로운 이름
     * @return 수정된 행의 수
     */
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

    /**
     * 사용자 계정과 관련된 모든 데이터를 삭제하는 메서드
     * @param username 삭제할 사용자의 아이디
     * @return 삭제 성공 여부
     */
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

    /**
     * 사용자의 ID를 조회하는 메서드
     * @param username 사용자 아이디
     * @return 사용자 ID (없으면 -1)
     */
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

    /**
     * 일별 카테고리별 지출 데이터를 조회하는 메서드
     * @param date 조회할 날짜 (yyyy-MM-dd 형식)
     * @return 카테고리별 지출 금액 Map
     */
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

    /**
     * 월별 카테고리별 지출 데이터를 조회하는 메서드
     * @param yearMonth 조회할 연월 (yyyy-MM 형식)
     * @return 카테고리별 지출 금액 Map
     */
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

    /**
     * 연간 카테고리별 지출 데이터를 조회하는 메서드
     * @param year 조회할 연도 (yyyy 형식)
     * @return 카테고리별 지출 금액 Map
     */
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

    /**
     * 특정 기간의 총 지출을 조회하는 메서드
     * @param username 사용자 아이디
     * @param startDate 시작 날짜 (yyyy-MM-dd 형식)
     * @param endDate 종료 날짜 (yyyy-MM-dd 형식)
     * @return 총 지출 금액
     */
    fun getMonthlyExpense(username: String, startDate: String, endDate: String): Long {
        val db = this.readableDatabase
        var totalExpense = 0L

        val query = """
            SELECT COALESCE(SUM(amount), 0) 
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_TRANSACTION_TYPE = 'expense' 
            AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ?
        """.trimIndent()

        db.rawQuery(query, arrayOf(startDate, endDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                totalExpense = cursor.getLong(0)
            }
        }

        return totalExpense
    }

    /**
     * 특정 기간의 가장 많이 지출한 카테고리를 조회하는 메서드
     * @param username 사용자 아이디
     * @param startDate 시작 날짜 (yyyy-MM-dd 형식)
     * @param endDate 종료 날짜 (yyyy-MM-dd 형식)
     * @return 가장 많이 지출한 카테고리 이름
     */
    fun getTopCategory(username: String, startDate: String, endDate: String): String? {
        val db = this.readableDatabase
        var topCategory: String? = null

        val query = """
            SELECT c.$COLUMN_CATEGORY_NAME as category_name, SUM(t.$COLUMN_TRANSACTION_AMOUNT) as total
            FROM $TABLE_TRANSACTIONS t
            JOIN $TABLE_CATEGORIES c ON t.$COLUMN_TRANSACTION_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            WHERE t.$COLUMN_TRANSACTION_TYPE = 'expense'
            AND t.$COLUMN_TRANSACTION_DATE BETWEEN ? AND ?
            GROUP BY c.$COLUMN_CATEGORY_ID, c.$COLUMN_CATEGORY_NAME
            ORDER BY total DESC
            LIMIT 1
        """.trimIndent()

        db.rawQuery(query, arrayOf(startDate, endDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                topCategory = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
            }
        }

        return topCategory ?: "-"
    }

    /**
     * 특정 기간의 지출 거래 건수를 조회하는 메서드
     * @param username 사용자 아이디
     * @param startDate 시작 날짜 (yyyy-MM-dd 형식)
     * @param endDate 종료 날짜 (yyyy-MM-dd 형식)
     * @return 지출 거래 건수
     */
    fun getTransactionCount(username: String, startDate: String, endDate: String): Int {
        val db = this.readableDatabase
        var count = 0

        val query = """
            SELECT COUNT(*) 
            FROM $TABLE_TRANSACTIONS 
            WHERE $COLUMN_TRANSACTION_TYPE = 'expense'
            AND $COLUMN_TRANSACTION_DATE BETWEEN ? AND ?
        """.trimIndent()

        db.rawQuery(query, arrayOf(startDate, endDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        }

        return count
    }
} 