package com.luojiaping.onmyenglish.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.ui.AppPage
import com.luojiaping.onmyenglish.core.ui.EmptyState

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    AppPage(title = "统计", modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OmeSpacing.page),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Metric(label = "连续学习", value = "0 天")
                Metric(label = "已掌握", value = "0")
                Metric(label = "本周复习", value = "0")
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            EmptyState(
                title = "暂无学习记录",
                icon = Icons.Outlined.Star,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(OmeSpacing.small),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
