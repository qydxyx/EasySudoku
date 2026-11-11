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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.sudoku.ui.components.ActionToolbar
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
 * 沉浸式顶部状态栏、9x9 高性能 Compose Canvas 棋盘、防窥暂停毛玻璃遮罩、逻辑提示条、五键工具栏与底部智能键盘
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight

    Scaffold(
        containerColor = bg,
        topBar = {
            TopGameBar(
                difficulty = state.difficulty,
                isDailyChallenge = state.isDailyChallenge,
                dailyDateString = state.dailyDateString,
                mistakesCount = state.mistakesCount,
                mistakesLimitEnabled = state.settings.mistakesLimitEnabled,
                elapsedTimeSeconds = state.elapsedTimeSeconds,
                isPaused = state.isPaused,
                onBackClick = onNavigateBack,
                onPauseToggleClick = { viewModel.togglePause() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 逻辑提示解析展开横幅
                    state.currentHint?.let { hint ->
                        HintBanner(
                            title = hint.title,
                            explanation = hint.explanation,
                            onClose = { viewModel.dismissHint() }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 9x9 核心交互棋盘与防作弊暂停遮罩
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SudokuBoardCanvas(
                            cells = state.cells,
                            selectedIndex = if (state.isPaused) null else state.selectedIndex,
                            highlightPeers = state.settings.highlightPeers,
                            highlightSameNumbers = state.settings.highlightSameNumbers,
                            waveCells = state.waveCells,
                            onCellClicked = { viewModel.selectCell(it) }
                        )

                        // 暂停防窥毛玻璃遮罩
                        if (state.isPaused) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background((if (isDark) Color.Black else Color.White).copy(alpha = 0.94f)),
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
                                        text = "点击右上角播放按钮继续对局",
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 底部五键操作工具栏 (撤销、擦除、笔记、自动笔记、提示)
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

                    // 1-9 底部智能数字键盘
                    NumberPad(
                        remainingCounts = state.remainingCounts,
                        onNumberClick = { viewModel.handleNumberInput(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
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
