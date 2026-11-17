package io.github.huig7.shi.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.huig7.shi.domain.model.SolarTermSeasons

/** 24 节气色板：4 行（春夏秋冬）× 6 列，颜色下方标注节气名。 */
@Composable
internal fun SolarTermPalette(selectedHex: Long, onSelect: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SolarTermSeasons.forEach { (season, terms) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = season,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(20.dp)
                        .padding(top = 7.dp),
                )
                terms.forEach { term ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ColorSwatch(
                            colorHex = term.colorHex,
                            selected = term.colorHex == selectedHex,
                            onClick = { onSelect(term.colorHex) },
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = term.name,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            maxLines = 1,
                            color = if (term.colorHex == selectedHex) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(colorHex: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(colorHex), CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}
