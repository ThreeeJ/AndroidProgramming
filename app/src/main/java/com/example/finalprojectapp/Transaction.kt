package com.example.finalprojectapp

/**
 * 거래 내역을 나타내는 데이터 클래스
 *
 * @property id 거래 내역의 고유 식별자
 * @property amount 거래 금액
 * @property type 거래 유형 ("income" 또는 "expense")
 * @property categoryId 카테고리의 고유 식별자
 * @property categoryName 카테고리 이름
 * @property date 거래 날짜 (yyyy-MM-dd 형식)
 * @property time 거래 시간 (HH:mm:ss 형식)
 */
data class Transaction(
    val id: Int,
    val amount: Int,
    val type: String, // "income" or "expense"
    val categoryId: Int,
    val categoryName: String,
    val date: String,
    val time: String
) 