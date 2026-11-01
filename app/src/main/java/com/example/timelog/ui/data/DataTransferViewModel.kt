package com.example.timelog.ui.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timelog.data.repository.TimeLogRepository
import com.example.timelog.data.transfer.TransferJson
import com.example.timelog.domain.model.ImportMode
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 导入 / 导出的状态：busy 期间按钮禁用，message 直接显示在卡片里。 */
data class DataTransferState(
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

/**
 * SAF（Storage Access Framework）文件读写 + 仓库快照 / 恢复。
 *
 * 导出：repository.snapshot() -> TransferJson.encode -> contentResolver.openOutputStream
 * 导入：contentResolver.openInputStream -> TransferJson.decode -> repository.restore
 * 文件读写都放在 Dispatchers.IO，UI 线程只更新状态。
 */
class DataTransferViewModel(private val repository: TimeLogRepository) : ViewModel() {

    private val _state = MutableStateFlow(DataTransferState())
    val state: StateFlow<DataTransferState> = _state.asStateFlow()

    fun export(context: Context, uri: Uri) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null, isError = false) }
        val appContext = context.applicationContext ?: context
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val json = TransferJson.encode(repository.snapshot())
                    val output = appContext.contentResolver.openOutputStream(uri)
                        ?: throw IOException("无法打开目标文件")
                    output.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                }.exceptionOrNull()
            }
            if (error == null) {
                _state.update { it.copy(busy = false, message = "已导出", isError = false) }
            } else {
                _state.update { it.copy(busy = false, message = exportMessage(error), isError = true) }
            }
        }
    }

    fun import(context: Context, uri: Uri, mode: ImportMode) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true, message = null, isError = false) }
        val appContext = context.applicationContext ?: context
        viewModelScope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val input = appContext.contentResolver.openInputStream(uri)
                        ?: throw IOException("无法打开所选文件")
                    val text = input.use { stream -> stream.readBytes().toString(Charsets.UTF_8) }
                    val snapshot = TransferJson.decode(text)
                    repository.restore(snapshot, mode)
                }.exceptionOrNull()
            }
            if (error == null) {
                _state.update { it.copy(busy = false, message = "已导入", isError = false) }
            } else {
                _state.update { it.copy(busy = false, message = importMessage(error), isError = true) }
            }
        }
    }

    private fun exportMessage(error: Throwable): String =
        if (error is SecurityException) "没有访问该文件的权限" else "导出失败，请重试"

    private fun importMessage(error: Throwable): String {
        if (error is SecurityException) return "没有访问该文件的权限"
        val message = (error as? IllegalArgumentException)?.message
        if (!message.isNullOrBlank()) return message
        return "导入失败，请重试"
    }

    companion object {
        fun factory(repository: TimeLogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DataTransferViewModel(repository) as T
            }
    }
}
