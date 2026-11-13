package com.antigravity.sudoku

import com.antigravity.sudoku.domain.engine.SudokuGenerator
import com.antigravity.sudoku.domain.engine.SudokuHintAnalyzer
import com.antigravity.sudoku.domain.engine.SudokuSolver
import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuEngineTest {

    private val solver = SudokuSolver()
    private val generator = SudokuGenerator(solver)
    private val hintAnalyzer = SudokuHintAnalyzer()

    @Test
    fun testGeneratedPuzzlesAllDifficultiesHaveUniqueSolution() {
        for ((idx, diff) in Difficulty.entries.withIndex()) {
            val puzzle = generator.generate(diff, Random(42L + idx))
            val grid = Array(9) { r ->
                IntArray(9) { c ->
                    puzzle.initialBoard[r * 9 + c].value
                }
            }

            val cluesCount = puzzle.initialBoard.count { it.isGiven }
            assertTrue(
                "Expected clues $cluesCount to be <= ${diff.cluesRange.last + 2} for $diff",
                cluesCount in diff.cluesRange
            )

            // 验证有且仅有一个有效解
            val solutionCount = solver.countSolutions(grid, maxSolutions = 2)
            assertEquals("Unique solution expected for $diff", 1, solutionCount)

            // 验证求解结果与预设终盘完全一致
            val solved = solver.solve(grid)
            assertNotNull(solved)
            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    assertEquals(puzzle.solutionGrid[r][c], solved!![r][c])
                }
            }
        }
    }

    @Test
    fun testSolverRejectsConflictingBoard() {
        val grid = Array(9) { IntArray(9) }
        grid[0][0] = 5
        grid[0][1] = 5 // 同行冲突
        assertEquals(0, solver.countSolutions(grid))
        assertNull(solver.solve(grid))
    }

    @Test
    fun testCellBitmaskNotes() {
        var cell = Cell(row = 0, col = 0)
        cell = cell.toggleNote(1).toggleNote(9)
        assertTrue(cell.hasNote(1))
        assertTrue(cell.hasNote(9))
        assertFalse(cell.hasNote(5))

        cell = cell.removeNote(1)
        assertFalse(cell.hasNote(1))
        assertTrue(cell.hasNote(9))
    }

    @Test
    fun testDailyChallengeDeterminism() {
        val dateStr = "2026-11-11"
        val seed = dateStr.hashCode().toLong()

        val puzzle1 = generator.generate(Difficulty.MEDIUM, Random(seed))
        val puzzle2 = generator.generate(Difficulty.MEDIUM, Random(seed))

        for (i in 0 until 81) {
            assertEquals(puzzle1.initialBoard[i].value, puzzle2.initialBoard[i].value)
            assertEquals(puzzle1.initialBoard[i].solution, puzzle2.initialBoard[i].solution)
        }
    }

    @Test
    fun testHintAnalyzerFindsLogicalStep() {
        val puzzle = generator.generate(Difficulty.EASY, Random(100))
        val hint = hintAnalyzer.analyzeNextStep(puzzle.initialBoard, selectedIndex = null)

        assertNotNull(hint)
        val targetIdx = hint!!.targetRow * 9 + hint.targetCol
        assertEquals(puzzle.initialBoard[targetIdx].solution, hint.suggestedValue)
        assertTrue(hint.title.isNotEmpty())
        assertTrue(hint.explanation.isNotEmpty())
    }
}
