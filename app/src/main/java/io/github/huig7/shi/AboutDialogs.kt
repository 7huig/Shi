package io.github.huig7.shi

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
internal fun AboutDialog(version: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("关于時") },
        text = {
            Column {
                Text(
                    text = "時（Shi）· v$version",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "纯本地时间记录器：用时间块和多层环图记录、复盘一天。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "无账号 · 无联网权限 · 无广告。数据只保存在本机，卸载前请导出 JSON 备份。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        },
    )
}

@Composable
internal fun OnboardingDialog(onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("欢迎使用時") },
        text = {
            Column {
                Text(
                    text = "三件小事，就能开始。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OnboardingLine("1", "点右下角 + 记一笔；长按 + 是快速记录。")
                OnboardingLine("2", "点虚线占位，可以记录那段时间；不记也没关系。")
                OnboardingLine("3", "左右滑动，或点「時計」查看统计。")
            }
        },
        confirmButton = {
            TextButton(onClick = onDone) { Text("开始记录") }
        },
    )
}

@Composable
private fun OnboardingLine(number: String, text: String) {
    Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

internal fun appVersion(context: Context): String = runCatching {
    @Suppress("DEPRECATION")
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
}.getOrNull() ?: "0.3.0"
