package com.sravila.apexmoney.core.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, symbol: String = "$"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formattedNumber = formatter.format(amount)
        return "$symbol$formattedNumber"
    }

    fun formatCompact(amount: Double, symbol: String = "$"): String {
        return when {
            amount >= 1_000_000 -> "$symbol${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
            amount >= 10_000 -> "$symbol${String.format(Locale.US, "%.1fk", amount / 1_000)}"
            else -> format(amount, symbol)
        }
    }
}
