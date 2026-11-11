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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.antigravity.sudoku.ui.theme.ErrorText
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark

/**
 * 游戏顶栏：返回按钮、难度标签、错误指示器 (0/3)、计时器与暂停
 */
@Composable
fun TopGameBar(
    difficulty: Difficulty,
    isDailyChallenge: Boolean,
    dailyDateString: String?,
    mistakesCount: Int,
    mistakesLimitEnabled: Boolean,
    elapsedTimeSeconds: Long,
    isPaused: Boolean,
    onBackClick: () -> Unit,
    onPauseToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val minutes = elapsedTimeSeconds / 60
    val seconds = elapsedTimeSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = textPrimary
            )
        }

        // 中间：难度或每日挑战标签
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isDailyChallenge) "每日挑战" else difficulty.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            if (isDailyChallenge && dailyDateString != null) {
                Text(
                    text = dailyDateString,
                    fontSize = 11.sp,
                    color = textMuted
                )
            } else if (mistakesLimitEnabled) {
                Text(
                    text = "错误: $mistakesCount/3",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (mistakesCount >= 2) ErrorText else textMuted
                )
            }
        }

        // 右侧：时间与暂停按钮
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = timeFormatted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
            IconButton(onClick = onPauseToggleClick) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "继续" else "暂停",
                    tint = if (isDark) SudokuPrimaryDark else SudokuPrimary
                )
            }
        }
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
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
