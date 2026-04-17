package com.example.apltracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apltracker.data.ApexPlatform

/**
 * 玩家输入弹窗。需要玩家名的功能入口若当前没有保存过用户，会先弹出；
 * 顶部的 “编辑当前用户” 按钮也复用此弹窗。
 */
@Composable
fun PlayerInputDialog(
    initialName: String,
    initialPlatform: ApexPlatform,
    onConfirm: (String, ApexPlatform) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var platform by remember { mutableStateOf(initialPlatform) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置当前玩家") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("玩家名 (Origin/Steam)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text("平台", style = MaterialTheme.typography.labelLarge)
                ApexPlatform.entries.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = platform == p, onClick = { platform = p })
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = platform == p, onClick = { platform = p })
                        Spacer(Modifier.height(0.dp))
                        Text(p.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), platform) },
            ) { Text("确认") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
