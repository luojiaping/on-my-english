package com.luojiaping.onmyenglish.feature.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.luojiaping.onmyenglish.core.ui.AppPage
import com.luojiaping.onmyenglish.core.ui.EmptyState

@Composable
fun StudyScreen(modifier: Modifier = Modifier) {
    AppPage(title = "学习", modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "暂无待复习单词",
                icon = Icons.Outlined.School,
            )
        }
    }
}
