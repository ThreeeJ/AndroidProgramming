package com.example.finalprojectapp

/**
 * 수입/지출 카테고리를 나타내는 데이터 클래스
 *
 * @property id 카테고리의 고유 식별자
 * @property name 카테고리 이름 (예: "급여", "식비" 등)
 * @property type 카테고리 유형 ("income" 또는 "expense")
 */
data class Category(
    val id: Int,
    val name: String,
    val type: String // "income" or "expense"
) 