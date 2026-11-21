package io.github.huig7.shi.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.huig7.shi.domain.model.CategoryTree
import io.github.huig7.shi.domain.model.TimelineEvent
import io.github.huig7.shi.ui.home.CategoryDot
import io.github.huig7.shi.util.DateTimeFormat

/**
 * 备注搜索页：整屏列表（不用 Scaffold），顶部是返回箭头 + 自动聚焦的搜索框，
 * 下面是分类筛选与结果列表；命中记录按「备注 / 分类路径 / 日期 + 时间段」展示，点击回调事件 id。
 * 支持只按分类筛选（不输关键词），所选分类包含其整棵子树。
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenEvent: (Long) -> Unit,
    viewModel: SearchViewModel,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val tree by viewModel.categoryTree.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showCategoryFilter by rememberSaveable { mutableStateOf(false) }

    // 分类在别处被删除后，筛选条件自动回到「全部分类」，避免停留在永远为空的结果。
    LaunchedEffect(tree, selectedCategoryId) {
        val selected = selectedCategoryId
        if (selected != null && tree.find(selected) == null) {
            viewModel.onCategorySelected(null)
        }
    }

    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onBack = onBack,
            focusRequester = focusRequester,
        )
        SearchFilterBar(
            selectedPath = selectedCategoryId?.let { tree.pathOf(it).ifBlank { null } },
            onOpen = {
                // 收起键盘，把弹窗空间留给分类树。
                focusManager.clearFocus()
                showCategoryFilter = true
            },
            onClear = { viewModel.onCategorySelected(null) },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        when {
            query.isBlank() && selectedCategoryId == null -> SearchHint()
            results.isEmpty() -> NoSearchResult(
                query = query,
                hasCategoryFilter = selectedCategoryId != null,
            )
            else -> SearchResultList(
                results = results,
                query = query,
                onOpenEvent = onOpenEvent,
            )
        }
    }

    if (showCategoryFilter) {
        CategoryFilterDialog(
            tree = tree,
            selectedCategoryId = selectedCategoryId,
            onSelect = { categoryId ->
                showCategoryFilter = false
                viewModel.onCategorySelected(categoryId)
            },
            onDismiss = { showCategoryFilter = false },
        )
    }
}

/** 搜索框下方的分类筛选：未选时显示「全部分类」，选中后显示完整路径并可一键清除。 */
@Composable
private fun SearchFilterBar(
    selectedPath: String?,
    onOpen: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = selectedPath != null,
            onClick = onOpen,
            modifier = Modifier.weight(1f, fill = false),
            label = {
                Text(
                    text = selectedPath ?: "全部分类",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
        if (selectedPath != null) {
            TextButton(
                onClick = onClear,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(
                    text = "清除",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/** 分类筛选弹窗：单列缩进列出整棵树，选任意层级都包含其子树。 */
@Composable
private fun CategoryFilterDialog(
    tree: CategoryTree,
    selectedCategoryId: Long?,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val rows = remember(tree) { tree.flatten() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("按分类筛选") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                item(key = "all") {
                    CategoryFilterRow(
                        title = "全部分类",
                        depth = 0,
                        colorHex = null,
                        selected = selectedCategoryId == null,
                        onClick = { onSelect(null) },
                    )
                }
                items(items = rows, key = { it.first.category.id }) { (node, depth) ->
                    CategoryFilterRow(
                        title = node.category.name,
                        depth = depth,
                        colorHex = node.category.colorHex,
                        selected = selectedCategoryId == node.category.id,
                        onClick = { onSelect(node.category.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

/** 筛选弹窗里的单行：缩进表示层级，右侧勾选当前筛选。 */
@Composable
private fun CategoryFilterRow(
    title: String,
    depth: Int,
    colorHex: Long?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (depth * 16).dp, top = 11.dp, bottom = 11.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (colorHex != null) {
            CategoryDot(colorHex = colorHex, size = 8.dp)
        } else {
            Box(modifier = Modifier.size(8.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "当前筛选",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester,
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "返回",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "搜索备注",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "清空",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(14.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@Composable
private fun SearchResultList(
    results: List<TimelineEvent>,
    query: String,
    onOpenEvent: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
    ) {
        item(key = "count") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "找到 " + results.size + " 条记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "共 " + DateTimeFormat.durationText(
                        results.sumOf { it.event.durationMillis },
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        items(items = results, key = { it.event.id }) { item ->
            SearchResultRow(
                item = item,
                query = query,
                onClick = { onOpenEvent(item.event.id) },
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    item: TimelineEvent,
    query: String,
    onClick: () -> Unit,
) {
    val event = item.event
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.padding(top = 7.dp)) {
                CategoryDot(colorHex = item.categoryColor, size = 8.dp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.note?.let { highlightMatches(it, query) } ?: AnnotatedString("无备注"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (event.note != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.categoryPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateTimeFormat.datePart(event.startAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = DateTimeFormat.rangeText(event.startAt, event.endAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

/** 把命中的关键词标出来；长备注里一眼能看到匹配位置。 */
@Composable
private fun highlightMatches(text: String, query: String): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty()) return AnnotatedString(text)
    val background = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val color = MaterialTheme.colorScheme.onSurface
    val lowerText = text.lowercase()
    val lowerNeedle = needle.lowercase()
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val index = lowerText.indexOf(lowerNeedle, cursor)
            if (index < 0) {
                append(text.substring(cursor))
                break
            }
            append(text.substring(cursor, index))
            withStyle(
                SpanStyle(
                    color = color,
                    background = background,
                    fontWeight = FontWeight.Medium,
                ),
            ) {
                append(text.substring(index, index + needle.length))
            }
            cursor = index + needle.length
        }
    }
}

@Composable
private fun SearchHint() {    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "搜索备注",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun NoSearchResult(
    query: String,
    hasCategoryFilter: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "没有找到",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (query.isBlank()) {
                if (hasCategoryFilter) {
                    "这个分类下还没有记录。"
                } else {
                    "还没有可以筛选的记录。"
                }
            } else {
                "没有备注包含「" + query.trim() + "」。" +
                    if (hasCategoryFilter) "也可以清除分类筛选后再试。" else ""
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
