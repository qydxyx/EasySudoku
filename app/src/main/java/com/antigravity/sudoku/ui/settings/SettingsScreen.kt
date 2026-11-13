package com.antigravity.sudoku.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.domain.model.UserSettings
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.LocalIsDarkTheme
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight

/**
 * 偏好设置面板：深色模式、锁定竖屏、音效、触觉振动、3 次错误失败限制、高亮与自动笔记
 */
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onUpdateSettings: (UserSettings) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

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
                    text = "偏好设置",
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "外观与显示",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Text(
                            text = "深色模式",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "切换浅色、深色或跟随系统外观",
                            fontSize = 12.sp,
                            color = textMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )
                        DarkModeSelector(
                            selectedDarkMode = settings.darkMode,
                            onSelect = { onUpdateSettings(settings.copy(darkMode = it)) },
                            primaryColor = primaryColor,
                            textColor = textPrimary,
                            mutedColor = textMuted,
                            isDark = isDark
                        )
                    }

                    SettingSwitchRow(
                        title = "锁定竖屏",
                        subtitle = "开启后固定竖屏显示，避免意外旋转屏幕",
                        checked = settings.portraitLocked,
                        onCheckedChange = { onUpdateSettings(settings.copy(portraitLocked = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "声音与反馈",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingSwitchRow(
                        title = "音效",
                        subtitle = "落子、擦除与通关提示音",
                        checked = settings.soundEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "触觉反馈 (振动)",
                        subtitle = "点击数字与完成动画微弱振动",
                        checked = settings.hapticEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(hapticEnabled = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "游戏规则与辅助",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingSwitchRow(
                        title = "错误次数上限 (3次)",
                        subtitle = "达到 3 次错误判定本局结束",
                        checked = settings.mistakesLimitEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(mistakesLimitEnabled = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "高亮相同数字",
                        subtitle = "选中时自动高亮棋盘所有相同数字",
                        checked = settings.highlightSameNumbers,
                        onCheckedChange = { onUpdateSettings(settings.copy(highlightSameNumbers = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "高亮同行/同列/同宫区域",
                        subtitle = "显示十字辅助高亮排查线",
                        checked = settings.highlightPeers,
                        onCheckedChange = { onUpdateSettings(settings.copy(highlightPeers = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "填入时自动移除笔记",
                        subtitle = "填入正确数字后自动清除关联候选数",
                        checked = settings.autoRemoveNotes,
                        onCheckedChange = { onUpdateSettings(settings.copy(autoRemoveNotes = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DarkModeSelector(
    selectedDarkMode: Boolean?,
    onSelect: (Boolean?) -> Unit,
    primaryColor: Color,
    textColor: Color,
    mutedColor: Color,
    isDark: Boolean
) {
    val options: List<Pair<Boolean?, String>> = listOf(
        null to "跟随系统",
        false to "浅色",
        true to "深色"
    )
    val unselectedBg = if (isDark) Color(0xFF252A36) else Color(0xFFF1F5F9)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (mode, label) ->
            val selected = (selectedDarkMode == mode)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (selected) primaryColor.copy(alpha = 0.16f) else unselectedBg,
                border = if (selected) BorderStroke(1.5.dp, primaryColor) else null,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(mode) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) primaryColor else if (isDark) textColor else mutedColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = mutedColor,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}
