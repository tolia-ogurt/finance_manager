package com.example.financemanager.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financemanager.core.ui.theme.PurplePrimary

data class CategoryInfo(val name: String, val icon: ImageVector)

val categories = listOf(
    CategoryInfo("General", Icons.Rounded.Category),
    CategoryInfo("Food", Icons.Rounded.Restaurant),
    CategoryInfo("Transport", Icons.Rounded.DirectionsBus),
    CategoryInfo("Education", Icons.Rounded.School),
    CategoryInfo("Charity", Icons.Rounded.Favorite)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        ) {
            Text("Add Transaction", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Category", style = MaterialTheme.typography.labelLarge)
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.name) },
                        leadingIcon = { Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary.copy(alpha = 0.2f),
                            selectedLabelColor = PurplePrimary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let {
                        onSave(it, selectedCategory.name, note)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Save Transaction")
            }
        }
    }
}