package io.github.huig7.shi.ui.record

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.huig7.shi.domain.model.CascadeLevel
import io.github.huig7.shi.domain.model.CategoryCascade
import io.github.huig7.shi.domain.model.CategoryNode
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.cascadeFor
import io.github.huig7.shi.ui.home.CategoryDot

private val LevelColumnShape = RoundedCornerShape(12.dp)
private val OptionShape = RoundedCornerShape(9.dp)
private val LevelTitles = listOf("一级", "二级", "三级")

/**
 * 三级分类依次选择：三列并排（一级 / 二级 / 三级），点哪一级就把记录记到哪一级。
 *
 * 真实数据有 39 个分类、最深 3 层，一次性铺开会很长也很难看出层级；改成三栏后
 * 每栏只列当前分支的选项，固定 160dp 高度各自滚动，既能快速切换也不会撑高表单。
 */
@Composable
fun CategoryCascadePicker(
    tree: CategoryTree,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cascade = tree.cascadeFor(selectedId)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CascadePath(cascade = cascade)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cascade.levels.forEachIndexed { index, level ->
                CascadeColumn(
                    title = LevelTitles.getOrElse(index) { "" },
                    level = level,
                    emptyLabel = emptyLabelFor(cascade, index),
                    emptyDescription = emptyDescriptionFor(cascade, index),
                    onSelect = onSelect,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** 顶部：左「分类」，右当前路径；路径太长只留一行，超出省略。 */
@Composable
private fun CascadePath(cascade: CategoryCascade) {
    val pathText = if (cascade.path.isEmpty()) {
        "未选择"
    } else {
        cascade.path.joinToString(" / ") { it.name }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "分类",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = pathText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * 一栏：栏头 + 固定 208dp 的选项列表：真实数据里常见 5 个根分类，整栏刚好容纳 5 行；
 * 更长的列表在栏内滚动，栏头右侧的数量提示还有多少项。
 * 栏本身不可点击：只有选项能点，避免误触后出现「点了但没选中」的困惑。
 */
@Composable
private fun CascadeColumn(
    title: String,
    level: CascadeLevel,
    emptyLabel: String,
    emptyDescription: String,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(LevelColumnShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = LevelColumnShape,
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 22.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            // 数量让用户知道这一栏还有多少项藏在滚动区里（真实数据可能有 6 个子分类）。
            if (level.options.isNotEmpty()) {
                Text(
                    text = "${level.options.size} 项",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(208.dp)) {
            if (level.options.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = emptyDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = emptyLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            } else {
                val listState = rememberLazyListState()
                // 旋转 / 编辑已有记录时也要对准选中项；切换上一级导致选项整体换掉时回到顶部。
                LaunchedEffect(level.selectedId, level.options) {
                    val index = level.options.indexOfFirst { it.category.id == level.selectedId }
                    listState.scrollToItem(index.coerceAtLeast(0))
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(items = level.options, key = { it.category.id }) { option ->
                        CascadeOptionRow(
                            option = option,
                            selected = option.category.id == level.selectedId,
                            onClick = { onSelect(option.category.id) },
                        )
                    }
                }
            }
        }
    }
}

/** 单个选项：分类色圆点 + 名称，选中时底色与描边都用该分类色（深色模式也成立）。 */
@Composable
private fun CascadeOptionRow(
    option: CategoryNode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(option.category.colorHex)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
        shape = OptionShape,
        color = if (selected) color.copy(alpha = 0.16f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, color.copy(alpha = 0.55f)) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryDot(colorHex = option.category.colorHex, size = 6.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = option.category.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 空栏目文案：默认「—」，再按常识提示下一步（选完二级 / 无二级 / 无三级）。 */
private fun emptyLabelFor(cascade: CategoryCascade, index: Int): String = when (index) {
    // 一级已选：有子级就列出来（只是还没选），没有才说「无二级」。
    1 -> if (cascade.levels[0].selectedId != null) "无二级" else "—"
    2 -> when {
        cascade.levels[1].options.isEmpty() -> "—"
        cascade.levels[1].selectedId == null -> "选完二级"
        else -> "无三级"
    }
    else -> "—"
}

/** 空栏目读屏说明：视觉上的「—」读不出含义，这里补一句完整的话。 */
private fun emptyDescriptionFor(cascade: CategoryCascade, index: Int): String = when (index) {
    1 -> if (cascade.levels[0].selectedId != null) "该一级分类没有二级分类" else "还没有选择一级分类"
    2 -> when {
        cascade.levels[1].options.isEmpty() -> "该一级分类没有二级分类"
        cascade.levels[1].selectedId == null -> "请先选择二级分类"
        else -> "该二级分类没有三级分类"
    }
    else -> "没有可选的一级分类"
}
