package com.example.familybudgetapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), TransactionDetailDialog.OnTransactionDeleteListener {

    private lateinit var db: AppDatabase
    private lateinit var incomeAmountEditText: EditText
    private lateinit var expenseAmountEditText: EditText
    private lateinit var incomeCategorySpinner: Spinner
    private lateinit var expenseCategorySpinner: Spinner
    private lateinit var addIncomeButton: Button
    private lateinit var addExpenseButton: Button
    private lateinit var balanceTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: TextView

    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(applicationContext)

        initViews()
        setupRecyclerView()
        setupListeners()
        observeTransactions()
    }

    private fun initViews() {
        incomeAmountEditText = findViewById(R.id.incomeAmountEditText)
        expenseAmountEditText = findViewById(R.id.expenseAmountEditText)
        incomeCategorySpinner = findViewById(R.id.incomeCategorySpinner)
        expenseCategorySpinner = findViewById(R.id.expenseCategorySpinner)
        addIncomeButton = findViewById(R.id.addIncomeButton)
        addExpenseButton = findViewById(R.id.addExpenseButton)
        balanceTextView = findViewById(R.id.balanceTextView)
        summaryTextView = findViewById(R.id.summaryTextView)
        recyclerView = findViewById(R.id.recyclerView)
        emptyState = findViewById(R.id.emptyState)

        val incomeCategories = listOf("Зарплата", "Подарки", "Инвестиции", "Сбережения")
        val expenseCategories = listOf("Еда", "Транспорт", "Подарки", "Коммуналка")

        val incomeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, incomeCategories)
        incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        incomeCategorySpinner.adapter = incomeAdapter

        val expenseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, expenseCategories)
        expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        expenseCategorySpinner.adapter = expenseAdapter
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(transactions) { transaction ->
            val dialog = TransactionDetailDialog.newInstance(transaction)
            dialog.show(supportFragmentManager, "TransactionDetailDialog")
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
    }

    private fun setupListeners() {
        addIncomeButton.setOnClickListener { addTransaction(isIncome = true) }
        addExpenseButton.setOnClickListener { addTransaction(isIncome = false) }
    }

    private fun observeTransactions() {
        db.transactionDao().getAllTransactions().observe(this) { transactions ->
            this.transactions.clear()
            this.transactions.addAll(transactions)
            adapter.updateTransactions(transactions)
            updateSummary(transactions)
            emptyState.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun addTransaction(isIncome: Boolean) {
        val amountText = if (isIncome) incomeAmountEditText.text else expenseAmountEditText.text
        val amount = amountText.toString().toDoubleOrNull()
        val category = if (isIncome) {
            incomeCategorySpinner.selectedItem.toString()
        } else {
            expenseCategorySpinner.selectedItem.toString()
        }

        if (amount != null && category.isNotBlank()) {
            val type = if (isIncome) "Доход" else "Расход"
            val transaction = Transaction(
                type = type,
                category = category,
                amount = amount
            )

            lifecycleScope.launch {
                db.transactionDao().insert(transaction)
            }

            if (isIncome) {
                incomeAmountEditText.text.clear()
            } else {
                expenseAmountEditText.text.clear()
            }

        } else {
            Toast.makeText(
                this,
                "Пожалуйста, введите корректные данные",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateSummary(transactions: List<Transaction>) {
        val income = transactions.filter { it.type == "Доход" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "Расход" }.sumOf { it.amount }
        val balance = income - expense

        balanceTextView.text = "Баланс: %.2f ₽".format(balance)
        summaryTextView.text = "Доходы: %.2f ₽ | Расходы: %.2f ₽".format(income, expense)
    }

    override fun onTransactionDelete(transaction: Transaction) {
        lifecycleScope.launch {
            db.transactionDao().delete(transaction)
        }
    }
}
