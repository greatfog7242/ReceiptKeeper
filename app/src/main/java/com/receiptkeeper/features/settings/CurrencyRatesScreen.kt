package com.receiptkeeper.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.core.preferences.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyRatesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CurrencyRatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var cnyRateText by remember(uiState.cnyToUsdRate) {
        mutableStateOf(uiState.cnyToUsdRate.toString())
    }
    var cnyRateError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) viewModel.clearSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Exchange Rates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "All amounts are stored and calculated in USD. Set the exchange rate for each supported currency below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // USD row (always 1.0, read-only)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "USD — US Dollar",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Base currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "1.0000",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // CNY row (editable)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CNY — Chinese Yuan (Renminbi)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = cnyRateText,
                        onValueChange = {
                            cnyRateText = it
                            cnyRateError = it.toDoubleOrNull()?.let { v -> v <= 0 } ?: true
                        },
                        label = { Text("1 CNY = ? USD") },
                        placeholder = { Text(PreferencesManager.DEFAULT_CNY_TO_USD_RATE.toString()) },
                        isError = cnyRateError,
                        supportingText = if (cnyRateError) {
                            { Text("Enter a positive number, e.g. 0.1374") }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Example: if 1 CNY = 0.1374 USD, enter 0.1374",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    val rate = cnyRateText.toDoubleOrNull()
                    if (rate != null && rate > 0) {
                        viewModel.saveRates(rate)
                    } else {
                        cnyRateError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !cnyRateError
            ) {
                Text("Save Rates")
            }

            if (uiState.isSaved) {
                Text(
                    text = "Rates saved successfully.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
