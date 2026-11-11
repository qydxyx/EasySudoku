package com.antigravity.sudoku.domain.model

enum class Difficulty(val displayName: String, val cluesRange: IntRange) {
    EASY("简单", 38..42),
    MEDIUM("中等", 32..36),
    HARD("困难", 28..31),
    EXPERT("专家", 25..27),
    MASTER("大师", 22..24);

    companion object {
        fun fromName(name: String): Difficulty =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
    }
}

/**
 * 单元格数据模型
 * @param row 行索引 (0..8)
 * @param col 列索引 (0..8)
 * @param value 当前填入的数字 (0 表示未填，1..9 表示已填)
 * @param solution 唯一解确定的正确数字 (1..9)
 * @param isGiven 是否是初始给定的固定线索数
 * @param notes 候选数笔记（通过 9 位 bitmask 存储，第 k 位为 1 表示包含数字 k，1..9）
 * @param isError 当前填入的值是否错误
 */
data class Cell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val solution: Int = 0,
    val isGiven: Boolean = false,
    val notes: Int = 0,
    val isError: Boolean = false
) {
    val blockIndex: Int get() = (row / 3) * 3 + (col / 3)
    val isFilled: Boolean get() = value != 0
    val isCorrect: Boolean get() = value == solution

    fun hasNote(digit: Int): Boolean = (notes and (1 shl digit)) != 0

    fun toggleNote(digit: Int): Cell {
        val newNotes = notes xor (1 shl digit)
        return copy(notes = newNotes)
    }

    fun removeNote(digit: Int): Cell {
        val newNotes = notes and (1 shl digit).inv()
        return copy(notes = newNotes)
    }

    fun withNotes(newNotes: Int): Cell = copy(notes = newNotes)
}

/**
 * 撤销历史步记录
 */
data class MoveRecord(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val newValue: Int,
    val previousNotes: Int,
    val newNotes: Int,
    // 自动清除关联笔记时的受波及格子快照 (row, col, oldNotes, newNotes)
    val affectedNotes: List<AffectedNote> = emptyList(),
    val wasError: Boolean = false
)

data class AffectedNote(
    val row: Int,
    val col: Int,
    val oldNotes: Int,
    val newNotes: Int
)

/**
 * 逻辑提示分析结果
 */
data class HintStep(
    val targetRow: Int,
    val targetCol: Int,
    val suggestedValue: Int,
    val title: String,
    val explanation: String,
    val relatedCells: List<Pair<Int, Int>> = emptyList()
)

/**
 * 统计数据模型
 */
data class DifficultyStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val bestTimeSeconds: Long = 0L,
    val totalTimeSeconds: Long = 0L,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0
) {
    val winRate: Int get() = if (gamesPlayed > 0) (gamesWon * 100) / gamesPlayed else 0
    val averageTimeSeconds: Long get() = if (gamesWon > 0) totalTimeSeconds / gamesWon else 0L
}

/**
 * 用户全局偏好配置
 */
data class UserSettings(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val mistakesLimitEnabled: Boolean = true,
    val highlightSameNumbers: Boolean = true,
    val highlightPeers: Boolean = true,
    val autoRemoveNotes: Boolean = true,
    val darkMode: Boolean? = null // null: 跟随系统, true: 深色, false: 浅色
)
