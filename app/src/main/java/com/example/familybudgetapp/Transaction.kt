package com.example.familybudgetapp

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val category: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    fun getFormattedDate(): String {
        return android.text.format.DateFormat.format("dd.MM.yyyy", Date(timestamp)).toString()
    }
}