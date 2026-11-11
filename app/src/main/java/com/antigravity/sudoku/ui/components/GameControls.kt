package com.antigravity.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * Sudoku.com 风格顶部五键工具栏
 * 包含：撤销、擦除、笔记 (带 ON/OFF 胶囊指示)、一键自动填满笔记、智能提示 (带剩余次数角标)
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
    val inactiveColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val disabledColor = if (isDark) Color(0xFF4B5563) else Color(0xFFD1D5DB)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 撤销 (Undo)
        ToolbarItem(
            icon = Icons.Default.Undo,
            label = "撤销",
            enabled = canUndo,
            tint = if (canUndo) activeColor else disabledColor,
            onClick = onUndoClick
        )

        // 2. 擦除 (Erase)
        ToolbarItem(
            icon = Icons.Default.Delete,
            label = "擦除",
            enabled = true,
            tint = inactiveColor,
            onClick = onEraseClick
        )

        // 3. 笔记 (Notes) - 带 ON / OFF 药丸胶囊
        Box(contentAlignment = Alignment.TopEnd) {
            ToolbarItem(
                icon = Icons.Default.Create,
                label = "笔记",
                enabled = true,
                tint = if (notesMode) activeColor else inactiveColor,
                onClick = onToggleNotesClick
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (notesMode) activeColor else (if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)),
                modifier = Modifier.padding(end = 4.dp, top = 2.dp)
            ) {
                Text(
                    text = if (notesMode) "ON" else "OFF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (notesMode) Color.White else inactiveColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // 4. 一键候选数 (Auto Notes)
        ToolbarItem(
            icon = Icons.Default.AutoFixHigh,
            label = "自动笔记",
            enabled = true,
            tint = inactiveColor,
            onClick = onAutoNotesClick
        )

        // 5. 智能提示 (Hint) - 带剩余次数徽标
        Box(contentAlignment = Alignment.TopEnd) {
            ToolbarItem(
                icon = Icons.Default.Lightbulb,
                label = "提示",
                enabled = hintsRemaining > 0,
                tint = if (hintsRemaining > 0) activeColor else disabledColor,
                onClick = onHintClick
            )
            if (hintsRemaining > 0) {
                Surface(
                    shape = CircleShape,
                    color = activeColor,
                    modifier = Modifier.padding(end = 6.dp, top = 2.dp)
                ) {
                    Text(
                        text = hintsRemaining.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled,
                indication = ripple(bounded = true, radius = 28.dp),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * 1-9 底部智能数字键盘
 * 实时显示各数字的剩余待填数，集满 9 个自动置灰勾选
 */
@Composable
fun NumberPad(
    remainingCounts: Map<Int, Int>, // 各数字剩余待填数量 (1..9 -> count)
    onNumberClick: (number: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) CardBgDark else CardBgLight
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textSub = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val completedBg = if (isDark) Color(0xFF262C38) else Color(0xFFF1F5F9)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (num in 1..9) {
            val remaining = remainingCounts[num] ?: 9
            val isCompleted = (remaining <= 0)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isCompleted) completedBg else baseBg)
                    .clickable(
                        enabled = !isCompleted,
                        indication = ripple(bounded = true),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onNumberClick(num) },
                horizontalAlignment = Alignment.CenterVertically,
                verticalArrangement = Arrangement.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已完成",
                        tint = textSub.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = num.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = remaining.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSub
                    )
                }
            }
        }
    }
}
