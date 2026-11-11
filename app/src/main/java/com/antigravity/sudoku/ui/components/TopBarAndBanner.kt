package com.antigravity.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.ErrorText
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark

/**
 * 顶部导航栏：自动适配系统状态栏高度 (statusBarsPadding)，彻底解决与通知栏重合问题
 */
@Composable
fun TopGameBar(
    isDailyChallenge: Boolean,
    dailyDateString: String?,
    isPaused: Boolean,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onPauseToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val iconSurfaceBg = if (isDark) Color(0xFF1E2430) else Color(0xFFEEF2F6)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：返回按钮
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = textPrimary
            )
        }

        // 中间：标题
        Text(
            text = if (isDailyChallenge) "每日挑战 (${dailyDateString ?: ""})" else "数独 Sudoku",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )

        // 右侧：新开本局与暂停按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconSurfaceBg,
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(onClick = onRestartClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重新开始",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = iconSurfaceBg,
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(onClick = onPauseToggleClick) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "继续" else "暂停",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sudoku.com 标志性棋盘上方四栏状态信息条（难度 / 错误次数 / 填数进度 / 计时）
 */
@Composable
fun GameStatusStrip(
    difficulty: Difficulty,
    mistakesCount: Int,
    mistakesLimitEnabled: Boolean,
    filledCount: Int,
    elapsedTimeSeconds: Long,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val minutes = elapsedTimeSeconds / 60
    val seconds = elapsedTimeSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusColumnItem(
                label = "难度",
                value = difficulty.displayName,
                labelColor = textMuted,
                valueColor = textPrimary
            )

            if (mistakesLimitEnabled) {
                StatusColumnItem(
                    label = "错误",
                    value = "$mistakesCount / 3",
                    labelColor = textMuted,
                    valueColor = if (mistakesCount >= 2) ErrorText else textPrimary
                )
            }

            StatusColumnItem(
                label = "进度",
                value = "$filledCount / 81",
                labelColor = textMuted,
                valueColor = textPrimary
            )

            StatusColumnItem(
                label = "用时",
                value = timeFormatted,
                labelColor = textMuted,
                valueColor = textPrimary
            )
        }
    }
}

@Composable
private fun StatusColumnItem(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * 逻辑提示详情展开横幅
 */
@Composable
fun HintBanner(
    title: String,
    explanation: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bannerBg = if (isDark) Color(0xFF1E3A5F) else Color(0xFFE3F2FD)
    val titleColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
    val bodyColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF263238)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = explanation,
                    fontSize = 12.sp,
                    color = bodyColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭提示",
                tint = titleColor,
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable { onClose() }
            )
        }
    }
}
