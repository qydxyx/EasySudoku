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
        // 1. 生成一个完整随机终盘
        val fullGrid = generateCompleteGrid(random)

        // 2. 确定目标线索数量
        val targetClues = random.nextInt(difficulty.cluesRange.first, difficulty.cluesRange.last + 1)
        var remainingClues = 81

        val puzzleGrid = Array(9) { r -> fullGrid[r].clone() }

        // 3. 构建挖空候选顺序列表（带对称策略）
        val cellPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                cellPositions.add(r to c)
            }
        }
        cellPositions.shuffle(random)

        // 4. 挖空并严格验证唯一解
        for ((r, c) in cellPositions) {
            if (remainingClues <= targetClues) break
            if (puzzleGrid[r][c] == 0) continue

            val backup = puzzleGrid[r][c]
            puzzleGrid[r][c] = 0

            // 验证解的数量是否严格为 1
            if (solver.countSolutions(puzzleGrid, maxSolutions = 2) == 1) {
                remainingClues--
            } else {
                // 如果出现多解或无解，撤销本次挖空
                puzzleGrid[r][c] = backup
            }
        }

        // 5. 组装领域单元格列表
        val cells = ArrayList<Cell>(81)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = puzzleGrid[r][c]
                val sol = fullGrid[r][c]
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
            solutionGrid = fullGrid,
            difficulty = difficulty
        )
    }

    /**
     * 生成合法完整的 9x9 终盘
     */
    private fun generateCompleteGrid(random: Random): Array<IntArray> {
        val grid = Array(9) { IntArray(9) }
        val digits = (1..9).toList()

        // 先随机填充对角线上的 3 个独立 3x3 宫（它们互不影响，能极大加速终盘回溯）
        for (box in 0 until 9 step 3) {
            val shuffled = digits.shuffled(random)
            var idx = 0
            for (r in 0 until 3) {
                for (c in 0 until 3) {
                    grid[box + r][box + c] = shuffled[idx++]
                }
            }
        }

        // 填充剩余空格
        fun fillRemaining(r: Int, c: Int): Boolean {
            var nextR = r
            var nextC = c + 1
            if (nextC >= 9) {
                nextR = r + 1
                nextC = 0
            }
            if (nextR >= 9) return true

            if (grid[nextR][nextC] != 0) {
                return fillRemaining(nextR, nextC)
            }

            val boxIndex = (nextR / 3) * 3 + (nextC / 3)
            val usedInRow = (0 until 9).map { grid[nextR][it] }.toSet()
            val usedInCol = (0 until 9).map { grid[it][nextC] }.toSet()
            val boxStartR = (nextR / 3) * 3
            val boxStartC = (nextC / 3) * 3
            val usedInBox = mutableSetOf<Int>()
            for (br in 0 until 3) {
                for (bc in 0 until 3) {
                    usedInBox.add(grid[boxStartR + br][boxStartC + bc])
                }
            }

            val candidates = digits.filter {
                it !in usedInRow && it !in usedInCol && it !in usedInBox
            }.shuffled(random)

            for (num in candidates) {
                grid[nextR][nextC] = num
                if (fillRemaining(nextR, nextC)) return true
                grid[nextR][nextC] = 0
            }

            return false
        }

        fillRemaining(0, 2) // 从第一个非对角线空位开始
        return grid
    }
}
