package com.antigravity.sudoku.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark

/**
 * Sudoku.com 风格五键操作工具栏
 * 每个按钮采用独立的圆形触控底盘，角标位于圆形右上角外侧，绝不遮挡中心图标
 */
@Composable
fun ActionToolbar(
    canUndo: Boolean,
    notesMode: Boolean,
    hintsRemaining: Int,
    onUndoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onToggleNotesClick: () -> Unit,
    onAutoNotesClick: () -> Unit,
    onHintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val activeColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val iconColor = if (isDark) Color(0xFFE5E7EB) else Color(0xFF374151)
    val labelColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
    val disabledColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)
    val circleBg = if (isDark) Color(0xFF1E2430) else Color(0xFFEEF2F6)
    val activeCircleBg = activeColor.copy(alpha = if (isDark) 0.25f else 0.15f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 撤销 (Undo)
        ToolbarItem(
            icon = Icons.Default.Undo,
            label = "撤销",
            enabled = canUndo,
            iconTint = if (canUndo) iconColor else disabledColor,
            labelTint = if (canUndo) labelColor else disabledColor,
            circleBackground = circleBg,
            badgeText = null,
            badgeActive = false,
            activeColor = activeColor,
            isDark = isDark,
            onClick = onUndoClick
        )

        // 2. 擦除 (Erase)
        ToolbarItem(
            icon = Icons.Default.Delete,
            label = "擦除",
            enabled = true,
            iconTint = iconColor,
            labelTint = labelColor,
            circleBackground = circleBg,
            badgeText = null,
            badgeActive = false,
            activeColor = activeColor,
            isDark = isDark,
            onClick = onEraseClick
        )

        // 3. 笔记 (Notes) - 右上角外挂 ON/OFF 胶囊角标，不遮挡铅笔图标
        ToolbarItem(
            icon = Icons.Default.Create,
            label = "笔记",
            enabled = true,
            iconTint = if (notesMode) activeColor else iconColor,
            labelTint = if (notesMode) activeColor else labelColor,
            circleBackground = if (notesMode) activeCircleBg else circleBg,
            badgeText = if (notesMode) "ON" else "OFF",
            badgeActive = notesMode,
            activeColor = activeColor,
            isDark = isDark,
            onClick = onToggleNotesClick
        )

        // 4. 一键候选数 (Auto Notes)
        ToolbarItem(
            icon = Icons.Default.AutoFixHigh,
            label = "自动笔记",
            enabled = true,
            iconTint = iconColor,
            labelTint = labelColor,
            circleBackground = circleBg,
            badgeText = null,
            badgeActive = false,
            activeColor = activeColor,
            isDark = isDark,
            onClick = onAutoNotesClick
        )

        // 5. 智能提示 (Hint) - 右上角外挂剩余次数徽标，不遮挡灯泡图标
        ToolbarItem(
            icon = Icons.Default.Lightbulb,
            label = "提示",
            enabled = hintsRemaining > 0,
            iconTint = if (hintsRemaining > 0) activeColor else disabledColor,
            labelTint = if (hintsRemaining > 0) activeColor else disabledColor,
            circleBackground = if (hintsRemaining > 0) activeCircleBg else circleBg,
            badgeText = if (hintsRemaining > 0) hintsRemaining.toString() else "0",
            badgeActive = hintsRemaining > 0,
            activeColor = activeColor,
            isDark = isDark,
            onClick = onHintClick
        )
    }
}

@Composable
private fun ToolbarItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    iconTint: Color,
    labelTint: Color,
    circleBackground: Color,
    badgeText: String?,
    badgeActive: Boolean,
    activeColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            // 圆形按钮背景
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(circleBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 右上角外置角标（完全避开中心图标）
            if (badgeText != null) {
                val badgeBg = if (badgeActive) {
                    activeColor
                } else {
                    if (isDark) Color(0xFF374151) else Color(0xFFD1D5DB)
                }
                val badgeTextColor = if (badgeActive) {
                    Color.White
                } else {
                    if (isDark) Color(0xFFD1D5DB) else Color(0xFF374151)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = labelTint
        )
    }
}

/**
 * 1-9 底部高质感数字键盘
 * 实时显示各数字的剩余待填数，集满 9 个自动置灰完成勾选
 */
@Composable
fun NumberPad(
    remainingCounts: Map<Int, Int>,
    onNumberClick: (number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) CardBgDark else CardBgLight
    val borderColor = if (isDark) Color(0xFF2D3545) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFF64B5F6) else Color(0xFF1E5BB9)
    val textSub = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val completedBg = if (isDark) Color(0xFF181B22) else Color(0xFFF1F5F9)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (num in 1..9) {
            val remaining = remainingCounts[num] ?: 9
            val isCompleted = (remaining <= 0)

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isCompleted) completedBg else baseBg,
                border = if (isCompleted) null else BorderStroke(1.dp, borderColor),
                shadowElevation = if (isCompleted) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .clickable(
                            enabled = !isCompleted,
                            onClick = { onNumberClick(num) }
                        )
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已完成",
                            tint = textSub.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = num.toString(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = remaining.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textSub
                        )
                    }
                }
            }
        }
    }
}
