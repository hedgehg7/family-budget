package com.example.familybudgetapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.text.NumberFormat
import java.util.*

class TransactionDetailDialog : DialogFragment() {

    companion object {
        private const val ARG_TRANSACTION = "transaction"

        fun newInstance(transaction: Transaction): TransactionDetailDialog {
            return TransactionDetailDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRANSACTION, transaction)
                }
            }
        }
    }

    interface OnTransactionDeleteListener {
        fun onTransactionDelete(transaction: Transaction)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val transaction = requireArguments().getParcelable<Transaction>(ARG_TRANSACTION)!!

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_transaction_detail, null as ViewGroup?)

        val currencyFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
        }

        view.findViewById<TextView>(R.id.typeValue).text = transaction.type
        view.findViewById<TextView>(R.id.categoryValue).text = transaction.category
        view.findViewById<TextView>(R.id.amountValue).text =
            "${currencyFormat.format(transaction.amount)} ₽"
        view.findViewById<TextView>(R.id.dateValue).text = transaction.getFormattedDate()

        // Обработка кнопки удаления
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            val listener = parentFragment as? OnTransactionDeleteListener
                ?: activity as? OnTransactionDeleteListener
            listener?.onTransactionDelete(transaction)
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Детали транзакции")
            .setView(view)
            .setPositiveButton("Закрыть") { dialog, _ -> dialog.dismiss() }
            .create()
    }
}
