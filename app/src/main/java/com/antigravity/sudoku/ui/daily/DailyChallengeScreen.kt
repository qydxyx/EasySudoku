package com.antigravity.sudoku.ui.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.ui.theme.CardBgDark
import com.antigravity.sudoku.ui.theme.CardBgLight
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 每日挑战日历视图与奖杯进度
 */
@Composable
fun DailyChallengeScreen(
    completedDates: Set<String>,
    onSelectDate: (dateString: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight
    val cardBg = if (isDark) CardBgDark else CardBgLight
    val primaryColor = if (isDark) SudokuPrimaryDark else SudokuPrimary
    val textPrimary = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val todayCalendar = remember { Calendar.getInstance() }

    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH) // 0..11

    val monthName = remember(year, month) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        SimpleDateFormat("yyyy年 M月", Locale.CHINESE).format(cal.time)
    }

    // 计算当月天数与第一天星期几
    val daysInMonth = remember(year, month) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val firstDayOfWeek = remember(year, month) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 转为周一为 0
    }

    // 当月已完成打卡统计
    val monthPrefix = String.format("%04d-%02d", year, month + 1)
    val monthCompletedCount = remember(completedDates, monthPrefix) {
        completedDates.count { it.startsWith(monthPrefix) }
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
                    text = "每日挑战",
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
        ) {
            // 当月奖杯进度卡片
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "月度奖杯",
                        tint = if (monthCompletedCount >= daysInMonth) Color(0xFFFFB300) else Color(0xFFD1D5DB),
                        modifier = Modifier.size(42.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = "${month + 1} 月度挑战奖杯",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "已完成 $monthCompletedCount / $daysInMonth 天",
                            fontSize = 12.sp,
                            color = textMuted,
                            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                        )
                        LinearProgressIndicator(
                            progress = { monthCompletedCount.toFloat() / daysInMonth.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 月份切换栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = currentCalendar.clone() as Calendar
                    cal.add(Calendar.MONTH, -1)
                    currentCalendar = cal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上个月", tint = textPrimary)
                }

                Text(
                    text = monthName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                IconButton(onClick = {
                    val cal = currentCalendar.clone() as Calendar
                    cal.add(Calendar.MONTH, 1)
                    currentCalendar = cal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下个月", tint = textPrimary)
                }
            }

            // 星期标头
            val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { w ->
                    Text(
                        text = w,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 日历网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 填充第一天之前的空格
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                // 渲染各日期单元格
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                    val isDone = completedDates.contains(dateStr)

                    val isToday = (year == todayCalendar.get(Calendar.YEAR) &&
                            month == todayCalendar.get(Calendar.MONTH) &&
                            day == todayCalendar.get(Calendar.DAY_OF_MONTH))

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> Color(0xFFFEF3C7)
                                    isToday -> primaryColor.copy(alpha = 0.15f)
                                    else -> cardBg
                                }
                            )
                            .clickable {
                                onSelectDate(dateStr)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "已完成",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) primaryColor else textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
