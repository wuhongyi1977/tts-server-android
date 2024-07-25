package com.github.jing332.tts_server_android.compose.systts.replace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.compose.widgets.AppDialog
import com.github.jing332.tts_server_android.compose.widgets.TextCheckBox
import com.github.jing332.tts_server_android.constant.ReplaceExecution
import com.github.jing332.tts_server_android.data.entities.replace.ReplaceRuleGroup
import com.github.jing332.tts_server_android.utils.clickableRipple

@Composable
internal fun GroupEditDialog(
    onDismissRequest: () -> Unit,
    group: ReplaceRuleGroup,
    onGroupChange: (ReplaceRuleGroup) -> Unit,
    onConfirm: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        ReplaceExecution.BEFORE to "BEFORE",
        ReplaceExecution.AFTER to "AFTER",
        ReplaceExecution.TAG to "TAG"
    )

    AppDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(id = R.string.group)) },
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    label = { Text(stringResource(id = R.string.group_name)) },
                    value = group.name,
                    onValueChange = {
                        onGroupChange(group.copy(name = it))
                    }
                )

                Column {
                    TextButton(onClick = { expanded = true }) {
                        Text(options.find { it.first == group.onExecution }?.second ?: "TAG")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = {
                                onGroupChange(group.copy(onExecution = option.first))
                                expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }, buttons = {
            Row {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(id = R.string.cancel))
                }
                TextButton(onClick = onConfirm) {
                    Text(stringResource(id = R.string.confirm))
                }
            }
        }
    )
}