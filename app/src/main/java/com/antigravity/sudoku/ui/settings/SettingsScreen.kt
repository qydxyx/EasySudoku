package com.antigravity.sudoku.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.data.feedback.HapticAndSoundManager
import com.antigravity.sudoku.data.local.GameStorage
import com.antigravity.sudoku.domain.model.UserSettings
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight

/**
 * 偏好设置面板：音效、触觉振动、3 次错误失败限制、相同数字高亮、关联区域高亮、自动清除笔记
 */
@Composable
fun SettingsScreen(
    storage: GameStorage,
    feedbackManager: HapticAndSoundManager,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    var settings by remember { mutableStateOf(storage.getSettings()) }

    fun updateSettings(newSettings: UserSettings) {
        settings = newSettings
        storage.saveSettings(newSettings)
        feedbackManager.soundEnabled = newSettings.soundEnabled
        feedbackManager.hapticEnabled = newSettings.hapticEnabled
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "声音与反馈",
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
                    SettingSwitchRow(
                        title = "音效",
                        subtitle = "落子、擦除与通关提示音",
                        checked = settings.soundEnabled,
                        onCheckedChange = { updateSettings(settings.copy(soundEnabled = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "触觉反馈 (振动)",
                        subtitle = "点击数字与完成动画微弱振动",
                        checked = settings.hapticEnabled,
                        onCheckedChange = { updateSettings(settings.copy(hapticEnabled = it)) },
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
                        onCheckedChange = { updateSettings(settings.copy(mistakesLimitEnabled = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "高亮相同数字",
                        subtitle = "选中时自动高亮棋盘所有相同数字",
                        checked = settings.highlightSameNumbers,
                        onCheckedChange = { updateSettings(settings.copy(highlightSameNumbers = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "高亮同行/同列/同宫区域",
                        subtitle = "显示十字辅助高亮排查线",
                        checked = settings.highlightPeers,
                        onCheckedChange = { updateSettings(settings.copy(highlightPeers = it)) },
                        textColor = textPrimary,
                        mutedColor = textMuted,
                        primaryColor = primaryColor
                    )
                    SettingSwitchRow(
                        title = "填入时自动移除笔记",
                        subtitle = "填入正确数字后自动清除关联候选数",
                        checked = settings.autoRemoveNotes,
                        onCheckedChange = { updateSettings(settings.copy(autoRemoveNotes = it)) },
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
