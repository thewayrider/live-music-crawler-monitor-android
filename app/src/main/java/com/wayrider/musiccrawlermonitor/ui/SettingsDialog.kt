package com.wayrider.musiccrawlermonitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wayrider.musiccrawlermonitor.ui.theme.TextMuted

@Composable
fun SettingsDialog(
    initialGistId: String,
    initialToken: String,
    initialSampleMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (gistId: String, token: String, sampleMode: Boolean) -> Unit
) {
    var gistId by remember { mutableStateOf(initialGistId) }
    var token by remember { mutableStateOf(initialToken) }
    var sampleMode by remember { mutableStateOf(initialSampleMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud Sync Settings") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Connect to your GitHub Gist to receive live telemetry from your Mini PC crawlers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use Sample Data", modifier = Modifier.weight(1f))
                    Switch(
                        checked = sampleMode,
                        onCheckedChange = { sampleMode = it }
                    )
                }

                if (!sampleMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = gistId,
                        onValueChange = { gistId = it },
                        label = { Text("GitHub Gist ID") },
                        placeholder = { Text("e.g. 8f4a2b9...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("GitHub Token (Optional for public Gists)") },
                        placeholder = { Text("ghp_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(gistId, token, sampleMode) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
