package com.example.financemanager.feature.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financemanager.core.data.local.entities.TransactionEntity
import com.example.financemanager.feature.dashboard.presentation.DashboardUiState
import com.example.financemanager.feature.dashboard.presentation.DashboardViewModel
import com.example.financemanager.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Spending insight", style = Typography.titleLarge) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddTransaction(true) },
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            BudgetOverviewCard(
                state = uiState,
                onAdjustClick = { viewModel.showAdjustBudget(true) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Budget category",
                style = Typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            TransactionList(uiState.transactions)
        }

        if (uiState.isAddTransactionSheetVisible) {
            AddTransactionSheet(
                onDismiss = { viewModel.showAddTransaction(false) },
                onSave = { amount, category, note ->
                    viewModel.addTransaction(amount, category, note)
                }
            )
        }

        if (uiState.isAdjustBudgetDialogVisible) {
            AdjustBudgetDialog(
                currentBudget = uiState.monthlyBudget,
                onDismiss = { viewModel.showAdjustBudget(false) },
                onConfirm = { amount ->
                    viewModel.setBudget(amount)
                }
            )
        }
    }
}

@Composable
fun BudgetOverviewCard(
    state: DashboardUiState,
    onAdjustClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monthly budget", color = TextSecondary, style = Typography.labelSmall)
                    Text(
                        "$${state.monthlyBudget.toInt()}",
                        style = Typography.titleLarge.copy(fontSize = 28.sp)
                    )
                }
                TextButton(
                    onClick = onAdjustClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = PurplePrimary)
                ) {
                    Text("Adjust", fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Rounded.Adjust,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                RingChart(
                    spent = state.spentAmount,
                    total = state.monthlyBudget,
                    modifier = Modifier.size(200.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$${String.format("%.2f", state.spentAmount)}",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Spent", color = TextSecondary, style = Typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            val left = state.monthlyBudget - state.spentAmount
            Surface(
                color = PurplePrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Left to spend: $${String.format("%.2f", left)}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RingChart(
    spent: Double,
    total: Double,
    modifier: Modifier = Modifier
) {
    val sweepAngle = if (total > 0) ((spent / total) * 360f).toFloat().coerceIn(0f, 360f) else 0f
    
    Canvas(modifier = modifier) {
        drawArc(
            color = Color(0xFFF0F0F0),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = PurplePrimary,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TransactionList(transactions: List<TransactionEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity) {
    val categoryInfo = categories.find { it.name == transaction.category } ?: categories[0]
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = IndigoSecondary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    categoryInfo.icon,
                    contentDescription = null,
                    tint = IndigoSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.category, fontWeight = FontWeight.Bold)
            Text(transaction.note.ifEmpty { "Recent activity" }, color = TextSecondary, style = Typography.labelSmall)
        }
        Text(
            "$${transaction.amount.toInt()}",
            fontWeight = FontWeight.Bold,
            style = Typography.bodyLarge
        )
    }
}