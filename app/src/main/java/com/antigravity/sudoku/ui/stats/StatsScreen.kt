package com.antigravity.sudoku.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.data.local.GameStorage
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight

/**
 * 历史统计面板：按 5 档难度分段展示总场次、胜率、最快用时、平均用时、连胜数据
 */
@Composable
fun StatsScreen(
    storage: GameStorage,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    val stats = remember(selectedDifficulty) { storage.getStats(selectedDifficulty) }

    fun formatSeconds(sec: Long): String {
        if (sec <= 0) return "--:--"
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = textPrimary
                    )
                }
                Text(
                    text = "统计数据",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // 5 档难度切换 Tab
            ScrollableTabRow(
                selectedTabIndex = Difficulty.entries.indexOf(selectedDifficulty),
                containerColor = bg,
                contentColor = primaryColor,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val index = Difficulty.entries.indexOf(selectedDifficulty)
                    if (index in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = primaryColor
                        )
                    }
                }
            ) {
                Difficulty.entries.forEach { diff ->
                    Tab(
                        selected = diff == selectedDifficulty,
                        onClick = { selectedDifficulty = diff },
                        text = {
                            Text(
                                text = diff.displayName,
                                fontWeight = if (diff == selectedDifficulty) FontWeight.Bold else FontWeight.Normal,
                                color = if (diff == selectedDifficulty) primaryColor else textMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 统计卡片列表
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsRowCard(cardBg, textPrimary, textMuted, "已玩局数", stats.gamesPlayed.toString())
                StatsRowCard(cardBg, textPrimary, textMuted, "获胜场次", stats.gamesWon.toString())
                StatsRowCard(cardBg, textPrimary, textMuted, "胜率", "${stats.winRate}%")
                StatsRowCard(cardBg, textPrimary, textMuted, "最快用时", formatSeconds(stats.bestTimeSeconds))
                StatsRowCard(cardBg, textPrimary, textMuted, "平均用时", formatSeconds(stats.averageTimeSeconds))
                StatsRowCard(cardBg, textPrimary, textMuted, "当前连胜", stats.currentStreak.toString())
                StatsRowCard(cardBg, textPrimary, textMuted, "最高连胜纪录", stats.maxStreak.toString())
            }
        }
    }
}

@Composable
private fun StatsRowCard(
    containerColor: Color,
    textColor: Color,
    mutedColor: Color,
    title: String,
    value: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = mutedColor
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
