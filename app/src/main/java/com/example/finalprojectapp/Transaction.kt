package com.example.finalprojectapp

data class Transaction(
    val id: Int,
    val amount: Int,
    val type: String, // "income" or "expense"
    val categoryId: Int,
    val categoryName: String,
    val date: String,
    val time: String
) 