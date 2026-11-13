package com.antigravity.sudoku.domain.engine

import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.domain.model.Difficulty
import kotlin.random.Random

data class GeneratedPuzzle(
    val initialBoard: List<Cell>,
    val solutionGrid: Array<IntArray>,
    val difficulty: Difficulty
)

/**
 * 具有严格唯一解保证的数独题目生成器
 */
class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver()
) {

    /**
     * 生成指定难度的数独题目
     * @param difficulty 难度
     * @param random 可注入伪随机数源（支持每日挑战确定性 seed）
     */
    fun generate(difficulty: Difficulty, random: Random = Random): GeneratedPuzzle {
        val targetClues = random.nextInt(difficulty.cluesRange.first, difficulty.cluesRange.last + 1)

        var bestFullGrid: Array<IntArray>? = null
        var bestPuzzleGrid: Array<IntArray>? = null
        var bestCluesCount = Int.MAX_VALUE

        val maxAttempts = when (difficulty) {
            Difficulty.MASTER -> 12
            Difficulty.EXPERT -> 8
            Difficulty.HARD -> 4
            else -> 2
        }

        for (attempt in 0 until maxAttempts) {
            val fullGrid = generateCompleteGrid(random)
            val puzzleGrid = Array(9) { r -> fullGrid[r].clone() }
            var remainingClues = 81

            val cellPositions = MutableList(81) { idx -> (idx / 9) to (idx % 9) }
            cellPositions.shuffle(random)

            for ((r, c) in cellPositions) {
                if (remainingClues <= targetClues) break
                if (puzzleGrid[r][c] == 0) continue

                val backup = puzzleGrid[r][c]
                puzzleGrid[r][c] = 0

                if (solver.countSolutions(puzzleGrid, maxSolutions = 2) == 1) {
                    remainingClues--
                } else {
                    puzzleGrid[r][c] = backup
                }
            }

            if (remainingClues < bestCluesCount) {
                bestCluesCount = remainingClues
                bestFullGrid = fullGrid
                bestPuzzleGrid = puzzleGrid
            }
            if (remainingClues in difficulty.cluesRange) {
                break
            }
        }

        val finalFullGrid = bestFullGrid!!
        val finalPuzzleGrid = bestPuzzleGrid!!

        // 组装领域单元格列表
        val cells = ArrayList<Cell>(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = finalPuzzleGrid[r][c]
                val sol = finalFullGrid[r][c]
                cells.add(
                    Cell(
                        row = r,
                        col = c,
                        value = value,
                        solution = sol,
                        isGiven = value != 0,
                        notes = 0,
                        isError = false
                    )
                )
            }
        }

        return GeneratedPuzzle(
            initialBoard = cells,
            solutionGrid = finalFullGrid,
            difficulty = difficulty
        )
    }

    /**
     * 生成合法完整的 9x9 终盘
     */
    private fun generateCompleteGrid(random: Random): Array<IntArray> {
        val grid = Array(9) { IntArray(9) }
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        val digits = (1..9).toList()

        // 先随机填充对角线上的 3 个独立 3x3 宫（它们互不影响，能极大加速终盘回溯）
        for (box in 0 until 9 step 3) {
            val shuffled = digits.shuffled(random)
            var idx = 0
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    val num = shuffled[idx++]
                    val gr = box + r
                    val gc = box + c
                    val b = (gr / 3) * 3 + (gc / 3)
                    val bit = 1 shl num
                    grid[gr][gc] = num
                    rowMask[gr] = rowMask[gr] or bit
                    colMask[gc] = colMask[gc] or bit
                    boxMask[b] = boxMask[b] or bit
                }
            }
        }

        fun fillRemaining(pos: Int): Boolean {
            var p = pos
            while (p < 81 && grid[p / 9][p % 9] != 0) {
                p++
            }
            if (p >= 81) return true

            val r = p / 9
            val c = p % 9
            val b = (r / 3) * 3 + (c / 3)
            val used = rowMask[r] or colMask[c] or boxMask[b]
            val available = 0x3FE and used.inv()
            if (available == 0) return false

            val candidates = IntArray(9)
            var count = 0
            var bits = available
            while (bits != 0) {
                val lsb = bits and -bits
                candidates[count++] = Integer.numberOfTrailingZeros(lsb)
                bits = bits xor lsb
            }
            // Fisher-Yates shuffle on candidates[0 until count]
            for (i in count - 1 downTo 1) {
                val j = random.nextInt(i + 1)
                val tmp = candidates[i]
                candidates[i] = candidates[j]
                candidates[j] = tmp
            }

            for (i in 0 until count) {
                val num = candidates[i]
                val bit = 1 shl num
                grid[r][c] = num
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit

                if (fillRemaining(p + 1)) return true

                grid[r][c] = 0
                rowMask[r] = rowMask[r] xor bit
                colMask[c] = colMask[c] xor bit
                boxMask[b] = boxMask[b] xor bit
            }

            return false
        }

        fillRemaining(3)
        return grid
    }
}
