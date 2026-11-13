package com.antigravity.sudoku.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import com.antigravity.sudoku.ui.components.ActionToolbar
import com.antigravity.sudoku.ui.components.GameStatusStrip
import com.antigravity.sudoku.ui.components.HintBanner
import com.antigravity.sudoku.ui.components.NumberPad
import com.antigravity.sudoku.ui.components.SudokuBoardCanvas
import com.antigravity.sudoku.ui.components.TopGameBar
import com.antigravity.sudoku.ui.theme.LocalIsDarkTheme
import com.antigravity.sudoku.ui.theme.SudokuPrimary
import com.antigravity.sudoku.ui.theme.SudokuPrimaryDark
import com.antigravity.sudoku.ui.theme.SurfaceBgDark
import com.antigravity.sudoku.ui.theme.SurfaceBgLight

/**
 * 核心对局界面：
 * 采用均衡垂直分布比例，自适应横竖屏与小屏滚动，消除通知栏重叠与无效留白
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalIsDarkTheme.current
    val bg = if (isDark) SurfaceBgDark else SurfaceBgLight

    var showRestartConfirm by remember { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        viewModel.onGameScreenEntered()
        onDispose {
            viewModel.onGameScreenExited()
        }
    }

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
                onRestartClick = { showRestartConfirm = true },
                onPauseToggleClick = { viewModel.togglePause() }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            val isLandscape = maxWidth > maxHeight * 1.1f && maxWidth > 500.dp
            val containerHeight = maxHeight

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
            } else if (isLandscape) {
                // 横屏双栏布局：左侧正方形棋盘，右侧状态栏 + 工具栏 + 数字键盘
                val boardSide = minOf(maxHeight - 16.dp, maxWidth * 0.48f).coerceAtLeast(200.dp)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardSurfaceWithPauseOverlay(
                        state = state,
                        isDark = isDark,
                        onCellClicked = { viewModel.selectCell(it) },
                        modifier = Modifier.size(boardSide)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GameStatusStrip(
                            difficulty = state.difficulty,
                            mistakesCount = state.mistakesCount,
                            mistakesLimitEnabled = state.settings.mistakesLimitEnabled,
                            filledCount = filledCount,
                            elapsedTimeSeconds = state.elapsedTimeSeconds
                        )

                        state.currentHint?.let { hint ->
                            Spacer(modifier = Modifier.height(6.dp))
                            HintBanner(
                                title = hint.title,
                                explanation = hint.explanation,
                                onClose = { viewModel.dismissHint() }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

                        NumberPad(
                            remainingCounts = state.remainingCounts,
                            onNumberClick = { viewModel.handleNumberInput(it) }
                        )
                    }
                }
            } else {
                // 竖屏均衡布局（同时支持小屏/分屏垂直滚动，确保底部键盘不溢出）
                val minPortraitHeight = if (state.currentHint != null) 670.dp else 590.dp
                val targetHeight = if (containerHeight > minPortraitHeight) containerHeight else minPortraitHeight

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(targetHeight)
                            .padding(horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

                        BoardSurfaceWithPauseOverlay(
                            state = state,
                            isDark = isDark,
                            onCellClicked = { viewModel.selectCell(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.weight(0.42f))
                        Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1-9 底部高质感数字键盘
                        NumberPad(
                            remainingCounts = state.remainingCounts,
                            onNumberClick = { viewModel.handleNumberInput(it) }
                        )

                        Spacer(modifier = Modifier.weight(0.36f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 顶部重开确认弹窗
            if (showRestartConfirm) {
                RestartConfirmDialog(
                    isDailyChallenge = state.isDailyChallenge,
                    onConfirm = {
                        showRestartConfirm = false
                        viewModel.restartCurrentBoardOrNewGame()
                    },
                    onDismiss = { showRestartConfirm = false }
                )
            }

            // 通关胜利弹窗
            if (state.isCompleted) {
                VictoryDialog(
                    difficulty = state.difficulty,
                    isDailyChallenge = state.isDailyChallenge,
                    elapsedTimeSeconds = state.elapsedTimeSeconds,
                    isNewRecord = state.isNewRecord,
                    onPlayAgainClick = { viewModel.restartCurrentBoardOrNewGame() },
                    onBackHomeClick = onNavigateBack
                )
            }

            // 3 次错误满失败弹窗
            if (state.isGameOver) {
                GameOverDialog(
                    mistakesCount = state.mistakesCount,
                    onReviveClick = { viewModel.reviveGame() },
                    onRestartClick = { viewModel.restartCurrentBoardOrNewGame() },
                    onExitClick = {
                        viewModel.abandonOnGameOverExit()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun BoardSurfaceWithPauseOverlay(
    state: GameUiState,
    isDark: Boolean,
    onCellClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            SudokuBoardCanvas(
                cells = state.cells,
                selectedIndex = if (state.isPaused) null else state.selectedIndex,
                highlightPeers = state.settings.highlightPeers,
                highlightSameNumbers = state.settings.highlightSameNumbers,
                waveCells = state.waveCells,
                waveTriggerId = state.waveTriggerId,
                onCellClicked = onCellClicked
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
}
