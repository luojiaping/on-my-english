package com.luojiaping.onmyenglish.feature.wordbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luojiaping.onmyenglish.core.designsystem.DeckAccent
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.ui.AppPage

@Composable
fun DeckDetailScreen(
    state: DeckDetailUiState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deck = state.deck ?: return
    val learned = state.words.count(DeckWordItem::learned)

    AppPage(
        title = deck.name,
        modifier = modifier,
        actions = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CoverBanner(
                badge = deck.badge.ifBlank { deck.name.take(1) },
                accent = accentOf(deck.badge),
                learned = learned,
                total = state.words.size,
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = OmeSpacing.page,
                        vertical = OmeSpacing.small,
                    ),
                placeholder = { Text("搜索单词") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (state.filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "没有匹配的单词",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = state.filtered,
                        key = { word: DeckWordItem -> word.wordId },
                    ) { word ->
                        DeckWordRow(word)
                    }
                }
            }
        }
    }
}

private fun accentOf(badge: String): DeckAccent = when (badge) {
    "四" -> DeckAccent.PRIMARY
    "六" -> DeckAccent.SECONDARY
    "研" -> DeckAccent.TERTIARY
    else -> DeckAccent.NEUTRAL
}

private typealias DeckWordItem = com.luojiaping.onmyenglish.core.domain.DeckWord

@Composable
private fun CoverBanner(
    badge: String,
    accent: DeckAccent,
    learned: Int,
    total: Int,
) {
    val progress = if (total > 0) learned.toFloat() / total else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium)
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush = accent.brush()),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OmeSpacing.small),
            ) {
                Text(
                    text = "已掌握 $learned / $total",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(50),
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckWordRow(word: DeckWordItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = word.headword,
                style = MaterialTheme.typography.titleMedium,
                color = if (word.learned) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (word.phonetic.isNotBlank()) {
                Text(
                    text = word.phonetic,
                    modifier = Modifier.padding(start = OmeSpacing.small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = word.translation.ifBlank { word.definition },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
