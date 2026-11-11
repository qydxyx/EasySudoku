package com.antigravity.sudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.ui.components.ActionToolbar
import com.antigravity.sudoku.ui.components.GameStatusStrip
import com.antigravity.sudoku.ui.components.HintBanner
import com.antigravity.sudoku.ui.components.NumberPad
import com.antigravity.sudoku.ui.components.SudokuBoardCanvas
import com.antigravity.sudoku.ui.components.TopGameBar
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight

/**
 * 核心对局界面：
 * 采用均衡垂直分布比例，彻底消除通知栏重叠与中部大面积突兀留白
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight

    val filledCount = remember(state.cells) {
        state.cells.count { it.isFilled && !it.isError }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopGameBar(
                isDailyChallenge = state.isDailyChallenge,
                dailyDateString = state.dailyDateString,
                isPaused = state.isPaused,
                onBackClick = onNavigateBack,
                onRestartClick = {
                    if (state.isDailyChallenge) {
                        viewModel.startDailyChallenge(state.dailyDateString)
                    } else {
                        viewModel.startNewGame(state.difficulty)
                    }
                },
                onPauseToggleClick = { viewModel.togglePause() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = if (isDark) SudokuPrimaryDark else SudokuPrimary
                    )
                    Text(
                        text = "正在生成唯一解谜题...",
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 顶部适度弹性留白，让主视觉区自然居中偏上
                    Spacer(modifier = Modifier.weight(0.22f))

                    // 棋盘上方四栏状态信息条 (难度 / 错误 / 进度 / 用时)
                    GameStatusStrip(
                        difficulty = state.difficulty,
                        mistakesCount = state.mistakesCount,
                        mistakesLimitEnabled = state.settings.mistakesLimitEnabled,
                        filledCount = filledCount,
                        elapsedTimeSeconds = state.elapsedTimeSeconds
                    )

                    // 逻辑提示解析展开横幅（触发提示时显示）
                    state.currentHint?.let { hint ->
                        Spacer(modifier = Modifier.height(8.dp))
                        HintBanner(
                            title = hint.title,
                            explanation = hint.explanation,
                            onClose = { viewModel.dismissHint() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 9x9 核心交互棋盘与防作弊暂停遮罩
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SudokuBoardCanvas(
                                cells = state.cells,
                                selectedIndex = if (state.isPaused) null else state.selectedIndex,
                                highlightPeers = state.settings.highlightPeers,
                                highlightSameNumbers = state.settings.highlightSameNumbers,
                                waveCells = state.waveCells,
                                onCellClicked = { viewModel.selectCell(it) }
                            )

                            // 暂停防窥遮罩
                            if (state.isPaused) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background((if (isDark) Color(0xFF121418) else Color.White).copy(alpha = 0.96f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "游戏已暂停",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF111827)
                                        )
                                        Text(
                                            text = "点击右上角继续按钮恢复对局",
                                            fontSize = 13.sp,
                                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 棋盘与工具栏之间的适度弹性间距（不再使用单一 weight(1f) 造成巨大黑洞）
                    Spacer(modifier = Modifier.weight(0.42f))

                    // 五键圆形操作工具栏 (撤销、擦除、笔记、自动笔记、提示)
                    ActionToolbar(
                        canUndo = state.canUndo,
                        notesMode = state.notesMode,
                        hintsRemaining = state.hintsRemaining,
                        onUndoClick = { viewModel.undo() },
                        onEraseClick = { viewModel.eraseCurrentCell() },
                        onToggleNotesClick = { viewModel.toggleNotesMode() },
                        onAutoNotesClick = { viewModel.toggleAutoNotes() },
                        onHintClick = { viewModel.requestHint() }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1-9 底部高质感数字键盘
                    NumberPad(
                        remainingCounts = state.remainingCounts,
                        onNumberClick = { viewModel.handleNumberInput(it) }
                    )

                    // 底部安全留白
                    Spacer(modifier = Modifier.weight(0.36f))
                }
            }

            // 通关胜利弹窗
            if (state.isCompleted) {
                VictoryDialog(
                    difficulty = state.difficulty,
                    isDailyChallenge = state.isDailyChallenge,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    isNewRecord = state.isNewRecord,
                    onPlayAgainClick = {
                        if (state.isDailyChallenge) {
                            viewModel.startDailyChallenge(state.dailyDateString)
                        } else {
                            viewModel.startNewGame(state.difficulty)
                        }
                    },
                    onBackHomeClick = onNavigateBack
                )
            }

            // 3 次错误满失败弹窗
            if (state.isGameOver) {
                GameOverDialog(
                    mistakesCount = state.mistakesCount,
                    onReviveClick = { viewModel.reviveGame() },
                    onRestartClick = {
                        if (state.isDailyChallenge) {
                            viewModel.startDailyChallenge(state.dailyDateString)
                        } else {
                            viewModel.startNewGame(state.difficulty)
                        }
                    },
                    onExitClick = onNavigateBack
                )
            }
        }
    }
}
