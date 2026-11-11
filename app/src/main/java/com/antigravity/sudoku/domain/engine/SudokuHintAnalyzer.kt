package com.antigravity.sudoku.domain.engine

import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.domain.model.HintStep

/**
 * 智能数独逻辑提示分析器
 * 优先按人类可读的解题技巧发现逻辑突破口：
 * 1. 唯一余数法 (Naked Single - 某个空格仅剩一个合法数字)
 * 2. 隐性唯余法 (Hidden Single - 在某一行、列或宫中，某个数字只能填在唯一的位置)
 * 3. 兜底回退：直接解析目标选中格的唯一解
 */
class SudokuHintAnalyzer {

    fun analyzeNextStep(board: List<Cell>, selectedIndex: Int?): HintStep? {
        val grid = Array(9) { r ->
            IntArray(9) { c ->
                val cell = board[r * 9 + c]
                if (cell.isFilled && !cell.isError) cell.value else 0
            }
        }

        // 1. 检查是否存在“唯一余数法 (Naked Single)”
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = board[r * 9 + c]
                if (cell.value == 0 || cell.isError) {
                    val candidates = getCandidates(grid, r, c)
                    if (candidates.size == 1) {
                        val digit = candidates.first()
                        val peers = getPeers(r, c)
                        return HintStep(
                            targetRow = r,
                            targetCol = c,
                            suggestedValue = digit,
                            title = "唯一余数法 (Naked Single)",
                            explanation = "第 ${r + 1} 行第 ${c + 1} 列所在区域已排除其他所有数字，此处只能填入 $digit。",
                            relatedCells = peers
                        )
                    }
                }
            }
        }

        // 2. 检查各行是否存在“隐性唯余法 (Hidden Single)”
        for (r in 0 until 9) {
            for (digit in 1..9) {
                val possibleCols = mutableListOf<Int>()
                for (c in 0 until 9) {
                    val cell = board[r * 9 + c]
                    if ((cell.value == 0 || cell.isError) && isDigitValidAt(grid, r, c, digit)) {
                        possibleCols.add(c)
                    }
                }
                if (possibleCols.size == 1) {
                    val targetC = possibleCols[0]
                    return HintStep(
                        targetRow = r,
                        targetCol = targetC,
                        suggestedValue = digit,
                        title = "行内摒除法 (Hidden Single in Row)",
                        explanation = "在第 ${r + 1} 行中，数字 $digit 只能填入第 ${targetC + 1} 列的位置。",
                        relatedCells = (0 until 9).map { r to it }
                    )
                }
            }
        }

        // 3. 检查各列是否存在“隐性唯余法”
        for (c in 0 until 9) {
            for (digit in 1..9) {
                val possibleRows = mutableListOf<Int>()
                for (r in 0 until 9) {
                    val cell = board[r * 9 + c]
                    if ((cell.value == 0 || cell.isError) && isDigitValidAt(grid, r, c, digit)) {
                        possibleRows.add(r)
                    }
                }
                if (possibleRows.size == 1) {
                    val targetR = possibleRows[0]
                    return HintStep(
                        targetRow = targetR,
                        targetCol = c,
                        suggestedValue = digit,
                        title = "列内摒除法 (Hidden Single in Column)",
                        explanation = "在第 ${c + 1} 列中，数字 $digit 只能填入第 ${targetR + 1} 行的位置。",
                        relatedCells = (0 until 9).map { it to c }
                    )
                }
            }
        }

        // 4. 检查各 3x3 宫是否存在“隐性唯余法”
        for (boxR in 0 until 3) {
            for (boxC in 0 until 3) {
                val boxCells = mutableListOf<Pair<Int, Int>>()
                for (dr in 0 until 3) {
                    for (dc in 0 until 3) {
                        boxCells.add((boxR * 3 + dr) to (boxC * 3 + dc))
                    }
                }
                for (digit in 1..9) {
                    val possible = boxCells.filter { (r, c) ->
                        val cell = board[r * 9 + c]
                        (cell.value == 0 || cell.isError) && isDigitValidAt(grid, r, c, digit)
                    }
                    if (possible.size == 1) {
                        val (tr, tc) = possible[0]
                        return HintStep(
                            targetRow = tr,
                            targetCol = tc,
                            suggestedValue = digit,
                            title = "宫内摒除法 (Hidden Single in Box)",
                            explanation = "在对应的 3×3 九宫格内，数字 $digit 只能填入第 ${tr + 1} 行第 ${tc + 1} 列。",
                            relatedCells = boxCells
                        )
                    }
                }
            }
        }

        // 5. 兜底策略：如果用户当前选中了某个空格，则提示当前选中格的解
        if (selectedIndex != null && selectedIndex in 0..80) {
            val cell = board[selectedIndex]
            if (cell.value == 0 || cell.isError) {
                return HintStep(
                    targetRow = cell.row,
                    targetCol = cell.col,
                    suggestedValue = cell.solution,
                    title = "逻辑突破提示",
                    explanation = "根据终盘逻辑推导，第 ${cell.row + 1} 行第 ${cell.col + 1} 列的正确数字应为 ${cell.solution}。",
                    relatedCells = getPeers(cell.row, cell.col)
                )
            }
        }

        // 6. 查找第一个空格作为提示
        val firstEmpty = board.firstOrNull { it.value == 0 || it.isError }
        return firstEmpty?.let {
            HintStep(
                targetRow = it.row,
                targetCol = it.col,
                suggestedValue = it.solution,
                title = "直接填入提示",
                explanation = "第 ${it.row + 1} 行第 ${it.col + 1} 列的正确数字应为 ${it.solution}。",
                relatedCells = getPeers(it.row, it.col)
            )
        }
    }

    /**
     * 计算某格所有合法候选数
     */
    fun getCandidates(grid: Array<IntArray>, r: Int, c: Int): Set<Int> {
        val used = mutableSetOf<Int>()
        for (i in 0 until 9) {
            if (grid[r][i] in 1..9) used.add(grid[r][i])
            if (grid[i][c] in 1..9) used.add(grid[i][c])
        }
        val startR = (r / 3) * 3
        val startC = (c / 3) * 3
        for (dr in 0 until 3) {
            for (dc in 0 until 3) {
                val v = grid[startR + dr][startC + dc]
                if (v in 1..9) used.add(v)
            }
        }
        return (1..9).filter { it !in used }.toSet()
    }

    private fun isDigitValidAt(grid: Array<IntArray>, r: Int, c: Int, digit: Int): Boolean {
        for (i in 0 until 9) {
            if (grid[r][i] == digit) return false
            if (grid[i][c] == digit) return false
        }
        val startR = (r / 3) * 3
        val startC = (c / 3) * 3
        for (dr in 0 until 3) {
            for (dc in 0 until 3) {
                if (grid[startR + dr][startC + dc] == digit) return false
            }
        }
        return true
    }

    private fun getPeers(r: Int, c: Int): List<Pair<Int, Int>> {
        val peers = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until 9) {
            if (i != c) peers.add(r to i)
            if (i != r) peers.add(i to c)
        }
        val startR = (r / 3) * 3
        val startC = (c / 3) * 3
        for (dr in 0 until 3) {
            for (dc in 0 until 3) {
                val tr = startR + dr
                val tc = startC + dc
                if (tr != r || tc != c) peers.add(tr to tc)
            }
        }
        return peers.toList()
    }
}
