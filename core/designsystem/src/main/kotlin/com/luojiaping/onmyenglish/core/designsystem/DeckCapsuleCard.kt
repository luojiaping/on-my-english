package com.luojiaping.onmyenglish.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DeckAccent(val start: Color, val end: Color) {
    PRIMARY(Color(0xFF176B51), Color(0xFF4FAE8A)),
    SECONDARY(Color(0xFF685D32), Color(0xFFB3A05B)),
    TERTIARY(Color(0xFF8D3B4B), Color(0xFFC97A87)),
    NEUTRAL(Color(0xFF45504A), Color(0xFF7C8A82)),
    ;

    @Composable
    fun brush() = Brush.linearGradient(listOf(start, end))
}

/** Capsule-shaped deck card: 56dp cover slot + title/progress body. */
@Composable
fun DeckCapsuleCard(
    title: String,
    subtitle: String,
    badge: String,
    accent: DeckAccent,
    wordCount: Int,
    learnedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverImage: @Composable (() -> Unit)? = null,
    trailingChip: String? = null,
) {
    val progress = if (wordCount > 0) learnedCount.toFloat() / wordCount else 0f
    val capsule = RoundedCornerShape(46.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(capsule)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = capsule,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (coverImage != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                ) {
                    coverImage()
                }
            } else {
                DeckCoverPlaceholder(
                    badge = badge,
                    accent = accent,
                    modifier = Modifier.size(56.dp),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (trailingChip != null) {
                        DeckChip(text = trailingChip)
                    }
                }

                if (wordCount > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$subtitle · $wordCount 词",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                } else {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun DeckChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun DeckCoverPlaceholder(
    badge: String,
    accent: DeckAccent,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Box(
        modifier = modifier.clip(CircleShape).background(brush = accent.brush()),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
            )
        } else {
            Text(
                text = badge,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
