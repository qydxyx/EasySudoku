package com.antigravity.sudoku.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.ui.theme.CellBackgroundDark
import com.antigravity.sudoku.ui.theme.CellBackgroundLight
import com.antigravity.sudoku.ui.theme.ErrorHighlightDark
import com.antigravity.sudoku.ui.theme.ErrorHighlightLight
import com.antigravity.sudoku.ui.theme.ErrorText
import com.antigravity.sudoku.ui.theme.GivenTextDark
import com.antigravity.sudoku.ui.theme.GivenTextLight
import com.antigravity.sudoku.ui.theme.GridBorderThickDark
import com.antigravity.sudoku.ui.theme.GridBorderThickLight
import com.antigravity.sudoku.ui.theme.GridBorderThinDark
import com.antigravity.sudoku.ui.theme.GridBorderThinLight
import com.antigravity.sudoku.ui.theme.NoteTextDark
import com.antigravity.sudoku.ui.theme.NoteTextLight
import com.antigravity.sudoku.ui.theme.PeerHighlightDark
import com.antigravity.sudoku.ui.theme.PeerHighlightLight
import com.antigravity.sudoku.ui.theme.SameNumberHighlightDark
import com.antigravity.sudoku.ui.theme.SameNumberHighlightLight
import com.antigravity.sudoku.ui.theme.SelectedCellDark
import com.antigravity.sudoku.ui.theme.SelectedCellLight
import com.antigravity.sudoku.ui.theme.UserFilledTextDark
import com.antigravity.sudoku.ui.theme.UserFilledTextLight

/**
 * 9x9 高性能 Compose Canvas 棋盘
 * 负责渲染四级高亮、网格线、初始给定数、玩家填入数、3x3 候选数笔记以及波浪完成动效
 */
@Composable
fun SudokuBoardCanvas(
    cells: List<Cell>,
    selectedIndex: Int?,
    highlightPeers: Boolean,
    highlightSameNumbers: Boolean,
    waveCells: Set<Int>, // 触发波浪完成动效的单元格索引集
    onCellClicked: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // 波浪动效插值器 (0f -> 1f -> 0f)
    val waveAnim = remember { Animatable(0f) }
    LaunchedEffect(waveCells) {
        if (waveCells.isNotEmpty()) {
            waveAnim.snapTo(0f)
            waveAnim.animateTo(1f, animationSpec = tween(durationMillis = 220))
            waveAnim.animateTo(0f, animationSpec = tween(durationMillis = 300))
        }
    }

    // 预备文本画笔 (原生 Android Paint 确保极致高刷与文字垂直居中精度)
    val textBounds = remember { Rect() }
    val givenPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            color = if (isDark) GivenTextDark.toArgb() else GivenTextLight.toArgb()
        }
    }
    val userPaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            color = if (isDark) UserFilledTextDark.toArgb() else UserFilledTextLight.toArgb()
        }
    }
    val errorPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            color = ErrorText.toArgb()
        }
    }
    val notePaint = remember(isDark) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = if (isDark) NoteTextDark.toArgb() else NoteTextLight.toArgb()
        }
    }

    // 主题色彩准备
    val bgCellColor = if (isDark) CellBackgroundDark else CellBackgroundLight
    val selectedColor = if (isDark) SelectedCellDark else SelectedCellLight
    val peerColor = if (isDark) PeerHighlightDark else PeerHighlightLight
    val sameNumColor = if (isDark) SameNumberHighlightDark else SameNumberHighlightLight
    val errorBgColor = if (isDark) ErrorHighlightDark else ErrorHighlightLight
    val thickBorderColor = if (isDark) GridBorderThickDark else GridBorderThickLight
    val thinBorderColor = if (isDark) GridBorderThinDark else GridBorderThinLight

    val selectedCell = selectedIndex?.let { cells.getOrNull(it) }
    val selectedValue = selectedCell?.value?.takeIf { it in 1..9 }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 9f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 8)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 8)
                    onCellClicked(row * 9 + col)
                }
            }
    ) {
        val boardWidth = size.width
        val cellSize = boardWidth / 9f

        // 更新画笔字号
        givenPaint.textSize = cellSize * 0.62f
        userPaint.textSize = cellSize * 0.62f
        errorPaint.textSize = cellSize * 0.62f
        notePaint.textSize = cellSize * 0.24f

        // 1. 绘制单元格背景 (四级高亮体系)
        for (index in 0 until 81) {
            val cell = cells[index]
            val r = cell.row
            val c = cell.col
            val x = c * cellSize
            val y = r * cellSize

            val isSelected = (index == selectedIndex)
            val isPeer = selectedCell != null &&
                    (r == selectedCell.row || c == selectedCell.col || cell.blockIndex == selectedCell.blockIndex)
            val isSameValue = selectedValue != null && cell.value == selectedValue

            val cellColor = when {
                cell.isError -> errorBgColor
                isSelected -> selectedColor
                isSameValue && highlightSameNumbers -> sameNumColor
                isPeer && highlightPeers -> peerColor
                else -> bgCellColor
            }

            drawRect(
                color = cellColor,
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize)
            )

            // 波浪完成动效高光
            if (index in waveCells && waveAnim.value > 0.01f) {
                drawRect(
                    color = Color.White.copy(alpha = waveAnim.value * 0.45f),
                    topLeft = Offset(x, y),
                    size = Size(cellSize, cellSize)
                )
            }
        }

        // 2. 绘制网格线条
        // 细线 (各单元格分割)
        val thinStroke = 1.5f
        for (i in 1 until 9) {
            if (i % 3 != 0) {
                val pos = i * cellSize
                // 垂直细线
                drawLine(thinBorderColor, Offset(pos, 0f), Offset(pos, boardWidth), strokeWidth = thinStroke)
                // 水平细线
                drawLine(thinBorderColor, Offset(0f, pos), Offset(boardWidth, pos), strokeWidth = thinStroke)
            }
        }

        // 粗线 (3x3 宫与四周外框)
        val thickStroke = 4.5f
        for (i in 0..3) {
            val pos = i * 3 * cellSize
            // 垂直粗线
            drawLine(thickBorderColor, Offset(pos, 0f), Offset(pos, boardWidth), strokeWidth = thickStroke)
            // 水平粗线
            drawLine(thickBorderColor, Offset(0f, pos), Offset(boardWidth, pos), strokeWidth = thickStroke)
        }

        // 3. 绘制文字 (数字与笔记)
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            for (index in 0 until 81) {
                val cell = cells[index]
                val r = cell.row
                val c = cell.col
                val centerX = c * cellSize + cellSize / 2f
                val centerY = r * cellSize + cellSize / 2f

                if (cell.isFilled) {
                    val paint = when {
                        cell.isError -> errorPaint
                        cell.isGiven -> givenPaint
                        else -> userPaint
                    }
                    val text = cell.value.toString()
                    paint.getTextBounds(text, 0, text.length, textBounds)
                    val baselineY = centerY - textBounds.exactCenterY()
                    native.drawText(text, centerX, baselineY, paint)
                } else if (cell.notes != 0) {
                    // 绘制 3x3 微型候选笔记
                    val miniCellSize = cellSize / 3f
                    for (digit in 1..9) {
                        if (cell.hasNote(digit)) {
                            val dr = (digit - 1) / 3
                            val dc = (digit - 1) % 3
                            val noteX = c * cellSize + dc * miniCellSize + miniCellSize / 2f
                            val noteY = r * cellSize + dr * miniCellSize + miniCellSize / 2f

                            val noteStr = digit.toString()
                            notePaint.getTextBounds(noteStr, 0, 1, textBounds)
                            val noteBaseline = noteY - textBounds.exactCenterY()

                            // 如果当前选中的数字刚好是这个候选数，加粗突出高亮
                            val oldFakeBold = notePaint.isFakeBoldText
                            if (selectedValue != null && digit == selectedValue) {
                                notePaint.isFakeBoldText = true
                                notePaint.color = if (isDark) UserFilledTextDark.toArgb() else UserFilledTextLight.toArgb()
                            } else {
                                notePaint.isFakeBoldText = false
                                notePaint.color = if (isDark) NoteTextDark.toArgb() else NoteTextLight.toArgb()
                            }

                            native.drawText(noteStr, noteX, noteBaseline, notePaint)
                            notePaint.isFakeBoldText = oldFakeBold
                        }
                    }
                }
            }
        }
    }
}
