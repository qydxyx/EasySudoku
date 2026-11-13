package com.antigravity.sudoku.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.R
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.LocalIsDarkTheme
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 优雅主页（Sudoku.com 风格）：
 * 顶部状态栏安全间距、每日挑战卡片、中部拟物化陶瓷键帽徽章视觉中心、底部继续对局与新游戏双主按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    savedGameDifficulty: Difficulty?,
    savedGameTime: Long,
    isTodayDailyCompleted: Boolean = false,
    onResumeGameClick: () -> Unit,
    onStartNewGameClick: (Difficulty) -> Unit,
    onDailyChallengeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    var showDifficultySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val todayFormatted = remember {
        SimpleDateFormat("M月d日 EEEE", Locale.CHINESE).format(Date())
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EasySudoku",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = 0.5.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF1E2430) else Color(0xFFEEF2F6),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onStatsClick) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "统计",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF1E2430) else Color(0xFFEEF2F6),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置",
                                tint = textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            val minScreenHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = minScreenHeight)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 1. 顶部每日挑战大卡片
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onDailyChallengeClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (isDark) Color(0xFF453211) else Color(0xFFFEF3C7),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "每日挑战",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "每日挑战",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Text(
                                    text = todayFormatted,
                                    fontSize = 13.sp,
                                    color = textMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isTodayDailyCompleted) {
                                Color(0xFF10B981).copy(alpha = 0.16f)
                            } else {
                                primaryColor.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = if (isTodayDailyCompleted) "今日已完成 ✓" else "去挑战",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTodayDailyCompleted) Color(0xFF10B981) else primaryColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 2. 中部拟物化陶瓷键帽图标视觉中心
                Spacer(modifier = Modifier.weight(0.45f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "EasySudoku Icon",
                        modifier = Modifier.size(124.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "经典数独 · 逻辑之美",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "纯粹无广告 · 严格唯一解 · 随时随地锻炼脑力",
                        fontSize = 13.sp,
                        color = textMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(0.55f))

                // 3. 底部主操作区：继续游戏 & 新游戏
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasSavedGame && savedGameDifficulty != null) {
                        val minutes = savedGameTime / 60
                        val seconds = savedGameTime % 60
                        val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

                        Button(
                            onClick = onResumeGameClick,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "继续游戏",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${savedGameDifficulty.displayName} · $timeFormatted",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showDifficultySheet = true },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, primaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text(
                                text = "新游戏",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    } else {
                        Button(
                            onClick = { showDifficultySheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                        ) {
                            Text(
                                text = "新游戏",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // 五档难度选择 BottomSheet
    if (showDifficultySheet) {
        ModalBottomSheet(
            onDismissRequest = { showDifficultySheet = false },
            sheetState = sheetState,
            containerColor = cardBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "选择新游戏难度",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Difficulty.entries.forEach { diff ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showDifficultySheet = false
                                onStartNewGameClick(diff)
                            }
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = diff.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "${diff.cluesRange.first}–${diff.cluesRange.last} 已知数",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }
                }
            }
        }
    }
}
