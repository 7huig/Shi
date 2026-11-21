package io.github.huig7.shi.ui.annual

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.annual.AnnualReport
import io.github.huig7.shi.util.DateTimeFormat

/**
 * 年度报告整屏浮层。
 *
 * 十个章节横向滑动：开场、总时长、年度称号、时间去向、月度节奏、记录习惯、
 * 年度之最、四季、同比与分享。每页只讲一件事，数据页可纵向滚动。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AnnualReportScreen(
    state: AnnualReportUiState,
    onClose: () -> Unit,
    onOlderYear: () -> Unit,
    onNewerYear: () -> Unit,
) {
    val context = LocalContext.current
    val report = state.report
    val persona = remember(report) { report?.let(::annualPersona) }
    val pagerState = rememberPagerState(pageCount = { AnnualReportPage.entries.size })
    var showSharePreview by remember(state.year) { mutableStateOf(false) }

    // 换年份回到第一页，避免停留在上一年的「结尾」。
    LaunchedEffect(state.year) {
        pagerState.scrollToPage(0)
        showSharePreview = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {
        AnnualReportHeader(
            year = state.year,
            canGoOlder = state.canGoOlder,
            canGoNewer = state.canGoNewer,
            onClose = onClose,
            onOlderYear = onOlderYear,
            onNewerYear = onNewerYear,
        )
        when {
            state.isLoading -> Unit
            report == null -> Unit
            !report.hasData -> EmptyAnnualReport(year = state.year)
            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    key = { AnnualReportPage.entries[it].name },
                ) { index ->
                    when (AnnualReportPage.entries[index]) {
                        AnnualReportPage.COVER -> AnnualCoverPage(report)
                        AnnualReportPage.TOTAL -> AnnualTotalPage(report)
                        AnnualReportPage.PERSONA -> AnnualPersonaPage(report, persona!!)
                        AnnualReportPage.CATEGORIES -> AnnualCategoriesPage(report)
                        AnnualReportPage.MONTHS -> AnnualMonthsPage(report)
                        AnnualReportPage.HABITS -> AnnualHabitsPage(report)
                        AnnualReportPage.SUPERLATIVES -> AnnualSuperlativesPage(report)
                        AnnualReportPage.SEASONS -> AnnualSeasonsPage(report)
                        AnnualReportPage.COMPARISON -> AnnualComparisonPage(report)
                        AnnualReportPage.SHARE -> AnnualSharePage(
                            report = report,
                            persona = persona!!,
                            onShareText = { shareAnnualReportText(context, report) },
                            onShareImage = { showSharePreview = true },
                        )
                    }
                }
                PagerDots(
                    current = pagerState.currentPage,
                    count = AnnualReportPage.entries.size,
                )
            }
        }
    }

    if (showSharePreview && report != null && report.hasData && persona != null) {
        AnnualSharePreviewDialog(
            report = report,
            persona = persona,
            onDismiss = { showSharePreview = false },
        )
    }
}

@Composable
private fun AnnualReportHeader(
    year: Int,
    canGoOlder: Boolean,
    canGoNewer: Boolean,
    onClose: () -> Unit,
    onOlderYear: () -> Unit,
    onNewerYear: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "关闭年度报告",
                tint = muted,
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOlderYear, enabled = canGoOlder) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "上一年",
                tint = if (canGoOlder) MaterialTheme.colorScheme.onSurface else muted.copy(alpha = 0.3f),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$year",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "年度报告",
                fontSize = 11.sp,
                color = muted,
            )
        }
        IconButton(onClick = onNewerYear, enabled = canGoNewer) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "下一年",
                tint = if (canGoNewer) MaterialTheme.colorScheme.onSurface else muted.copy(alpha = 0.3f),
            )
        }
        Spacer(Modifier.weight(1f))
        // 与左侧关闭按钮等宽，保证年份标题居中。
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun PagerDots(current: Int, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == current) 7.dp else 4.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun EmptyAnnualReport(year: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "${year} 年还没有记录",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
