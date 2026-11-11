package com.antigravity.sudoku.ui.game

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.ErrorText
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark

/**
 * 通关胜利弹窗
 */
@Composable
fun VictoryDialog(
    difficulty: Difficulty,
    isDailyChallenge: Boolean,
    elapsedTimeSeconds: Long,
    isNewRecord: Boolean,
    onPlayAgainClick: () -> Unit,
    onBackHomeClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val minutes = elapsedTimeSeconds / 60
    val seconds = elapsedTimeSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary

    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) CardBgDark else CardBgLight,
        icon = {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "奖杯",
                tint = Color(0xFFFFB300),
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        title = {
            Text(
                text = if (isDailyChallenge) "每日挑战完成！" else "恭喜通关！",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isDailyChallenge) "今日数独挑战已顺利通关" else "难度：${difficulty.displayName}",
                    fontSize = 14.sp,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
                Text(
                    text = "通关用时: $timeFormatted",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                if (isNewRecord && !isDailyChallenge) {
                    Text(
                        text = "🎉 创造了该难度的新最佳纪录！",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgainClick,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("再来一局", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onBackHomeClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("返回主页")
            }
        }
    )
}

/**
 * 3 次错误游戏结束弹窗
 */
@Composable
fun GameOverDialog(
    mistakesCount: Int,
    onReviveClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary

    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = if (isDark) CardBgDark else CardBgLight,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "警示",
                tint = ErrorText,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        title = {
            Text(
                text = "错误次数已达上限 (3/3)",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "本局已出现 3 次错误。您可以获得一次再来机会继续挑战，或者重新开始本局。",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
            )
        },
        confirmButton = {
            Button(
                onClick = onReviveClick,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("获得机会继续对局", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestartClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重玩本局")
                }
                OutlinedButton(
                    onClick = onExitClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("退出")
                }
            }
        }
    )
}
