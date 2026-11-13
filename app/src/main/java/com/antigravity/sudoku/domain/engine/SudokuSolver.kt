package com.antigravity.sudoku.domain.engine

import kotlin.random.Random

/**
 * 高性能数独求解与唯一解验证器
 * 使用位运算记录每一行、每一列、每个 3x3 宫的已用数字
 */
class SudokuSolver {

    /**
     * 求解数独并计算解的数量（最多统计到 maxSolutions 即返回）
     */
    fun countSolutions(grid: Array<IntArray>, maxSolutions: Int = 2): Int {
        val workingGrid = Array(9) { r -> grid[r].clone() }
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val v = workingGrid[r][c]
                if (v in 1..9) {
                    val bit = 1 shl v
                    val b = (r / 3) * 3 + (c / 3)
                    if ((rowMask[r] and bit) != 0 || (colMask[c] and bit) != 0 || (boxMask[b] and bit) != 0) {
                        return 0 // 初始棋盘本身存在冲突，无解
                    }
                    rowMask[r] = rowMask[r] or bit
                    colMask[c] = colMask[c] or bit
                    boxMask[b] = boxMask[b] or bit
                }
            }
        }

        var count = 0

        fun solve() {
            if (count >= maxSolutions) return

            // 寻找拥有最少候选数的位置（MRV 启发式剪枝加速）
            var minOptions = 10
            var bestR = -1
            var bestC = -1
            var bestBits = 0

            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    if (workingGrid[r][c] == 0) {
                        val b = (r / 3) * 3 + (c / 3)
                        val used = rowMask[r] or colMask[c] or boxMask[b]
                        val available = 0x3FE and used.inv() // bits 1..9
                        val optionCount = Integer.bitCount(available)
                        if (optionCount == 0) return // 死胡同
                        if (optionCount < minOptions) {
                            minOptions = optionCount
                            bestR = r
                            bestC = c
                            bestBits = available
                            if (optionCount == 1) break
                        }
                    }
                }
                if (minOptions == 1) break
            }

            if (bestR == -1) {
                // 所有空格已填满，找到一个有效解
                count++
                return
            }

            val b = (bestR / 3) * 3 + (bestC / 3)
            var bits = bestBits
            while (bits != 0 && count < maxSolutions) {
                val lsb = bits and -bits
                val num = Integer.numberOfTrailingZeros(lsb)
                bits = bits xor lsb

                workingGrid[bestR][bestC] = num
                rowMask[bestR] = rowMask[bestR] or lsb
                colMask[bestC] = colMask[bestC] or lsb
                boxMask[b] = boxMask[b] or lsb

                solve()

                workingGrid[bestR][bestC] = 0
                rowMask[bestR] = rowMask[bestR] xor lsb
                colMask[bestC] = colMask[bestC] xor lsb
                boxMask[b] = boxMask[b] xor lsb
            }
        }

        solve()
        return count
    }

    /**
     * 获取完整解终盘（如果无解返回 null）
     */
    fun solve(grid: Array<IntArray>): Array<IntArray>? {
        val workingGrid = Array(9) { r -> grid[r].clone() }
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val v = workingGrid[r][c]
                if (v in 1..9) {
                    val bit = 1 shl v
                    val b = (r / 3) * 3 + (c / 3)
                    if ((rowMask[r] and bit) != 0 || (colMask[c] and bit) != 0 || (boxMask[b] and bit) != 0) {
                        return null // 初始棋盘存在冲突，无解
                    }
                    rowMask[r] = rowMask[r] or bit
                    colMask[c] = colMask[c] or bit
                    boxMask[b] = boxMask[b] or bit
                }
            }
        }

        fun backtrack(): Boolean {
            var minOptions = 10
            var bestR = -1
            var bestC = -1
            var bestBits = 0

            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    if (workingGrid[r][c] == 0) {
                        val b = (r / 3) * 3 + (c / 3)
                        val used = rowMask[r] or colMask[c] or boxMask[b]
                        val available = 0x3FE and used.inv()
                        val optionCount = Integer.bitCount(available)
                        if (optionCount == 0) return false
                        if (optionCount < minOptions) {
                            minOptions = optionCount
                            bestR = r
                            bestC = c
                            bestBits = available
                            if (optionCount == 1) break
                        }
                    }
                }
                if (minOptions == 1) break
            }

            if (bestR == -1) return true

            val b = (bestR / 3) * 3 + (bestC / 3)
            var bits = bestBits
            while (bits != 0) {
                val lsb = bits and -bits
                val num = Integer.numberOfTrailingZeros(lsb)
                bits = bits xor lsb

                workingGrid[bestR][bestC] = num
                rowMask[bestR] = rowMask[bestR] or lsb
                colMask[bestC] = colMask[bestC] or lsb
                boxMask[b] = boxMask[b] or lsb

                if (backtrack()) return true

                workingGrid[bestR][bestC] = 0
                rowMask[bestR] = rowMask[bestR] xor lsb
                colMask[bestC] = colMask[bestC] xor lsb
                boxMask[b] = boxMask[b] xor lsb
            }
            return false
        }

        return if (backtrack()) workingGrid else null
    }
}
