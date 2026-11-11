package com.antigravity.sudoku.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.sudoku.data.feedback.HapticAndSoundManager
import com.antigravity.sudoku.data.local.GameStorage
import com.antigravity.sudoku.domain.engine.SudokuGenerator
import com.antigravity.sudoku.domain.engine.SudokuHintAnalyzer
import com.antigravity.sudoku.domain.model.AffectedNote
import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.domain.model.HintStep
import com.antigravity.sudoku.domain.model.MoveRecord
import com.antigravity.sudoku.domain.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class GameUiState(
    val isLoading: Boolean = false,
    val cells: List<Cell> = emptyList(),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val isDailyChallenge: Boolean = false,
    val dailyDateString: String? = null,
    val selectedIndex: Int? = null,
    val notesMode: Boolean = false,
    val elapsedTimeSeconds: Long = 0L,
    val mistakesCount: Int = 0,
    val hintsRemaining: Int = 3,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val isNewRecord: Boolean = false,
    val currentHint: HintStep? = null,
    val waveCells: Set<Int> = emptySet(),
    val remainingCounts: Map<Int, Int> = (1..9).associateWith { 9 },
    val canUndo: Boolean = false,
    val settings: UserSettings = UserSettings()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = GameStorage(application)
    val feedbackManager = HapticAndSoundManager(application)
    private val generator = SudokuGenerator()
    private val hintAnalyzer = SudokuHintAnalyzer()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<MoveRecord>()
    private var timerJob: Job? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val s = storage.getSettings()
        feedbackManager.soundEnabled = s.soundEnabled
        feedbackManager.hapticEnabled = s.hapticEnabled
        _uiState.update { it.copy(settings = s) }
    }

    fun startNewGame(difficulty: Difficulty) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isPaused = false, isCompleted = false, isGameOver = false) }
            val puzzle = withContext(Dispatchers.Default) {
                generator.generate(difficulty)
            }
            undoStack.clear()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    cells = puzzle.initialBoard,
                    difficulty = difficulty,
                    isDailyChallenge = false,
                    dailyDateString = null,
                    selectedIndex = null,
                    notesMode = false,
                    elapsedTimeSeconds = 0L,
                    mistakesCount = 0,
                    hintsRemaining = 3,
                    isCompleted = false,
                    isGameOver = false,
                    currentHint = null,
                    waveCells = emptySet(),
                    remainingCounts = calculateRemaining(puzzle.initialBoard),
                    canUndo = false
                )
            }
            startTimer()
            saveCurrentGame()
        }
    }

    fun startDailyChallenge(dateString: String? = null) {
        val targetDate = dateString ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isPaused = false, isCompleted = false, isGameOver = false) }

            val puzzle = withContext(Dispatchers.Default) {
                // 基于日期哈希作为确定性随机种子，确保所有人、任何时候玩同一天题目完全一致
                val seed = targetDate.hashCode().toLong()
                generator.generate(Difficulty.MEDIUM, Random(seed))
            }
            undoStack.clear()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    cells = puzzle.initialBoard,
                    difficulty = Difficulty.MEDIUM,
                    isDailyChallenge = true,
                    dailyDateString = targetDate,
                    selectedIndex = null,
                    notesMode = false,
                    elapsedTimeSeconds = 0L,
                    mistakesCount = 0,
                    hintsRemaining = 3,
                    isCompleted = false,
                    isGameOver = false,
                    currentHint = null,
                    waveCells = emptySet(),
                    remainingCounts = calculateRemaining(puzzle.initialBoard),
                    canUndo = false
                )
            }
            startTimer()
            saveCurrentGame()
        }
    }

    fun resumeActiveGame(): Boolean {
        val saved = storage.loadActiveGame() ?: return false
        undoStack.clear()
        _uiState.update {
            it.copy(
                cells = saved.cells,
                difficulty = saved.difficulty,
                isDailyChallenge = saved.isDailyChallenge,
                dailyDateString = saved.dailyDateString,
                selectedIndex = null,
                notesMode = false,
                elapsedTimeSeconds = saved.elapsedTimeSeconds,
                mistakesCount = saved.mistakesCount,
                hintsRemaining = (3 - saved.hintsUsed).coerceAtLeast(0),
                isPaused = false,
                isCompleted = false,
                isGameOver = false,
                currentHint = null,
                waveCells = emptySet(),
                remainingCounts = calculateRemaining(saved.cells),
                canUndo = false
            )
        }
        startTimer()
        return true
    }

    fun selectCell(index: Int) {
        if (_uiState.value.isPaused || _uiState.value.isCompleted) return
        feedbackManager.vibrateClick()
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun toggleNotesMode() {
        feedbackManager.vibrateClick()
        _uiState.update { it.copy(notesMode = !it.notesMode) }
    }

    fun togglePause() {
        val currentlyPaused = _uiState.value.isPaused
        if (currentlyPaused) {
            _uiState.update { it.copy(isPaused = false) }
            startTimer()
        } else {
            _uiState.update { it.copy(isPaused = true) }
            stopTimer()
        }
    }

    fun handleNumberInput(number: Int) {
        val state = _uiState.value
        val index = state.selectedIndex ?: return
        if (state.isPaused || state.isCompleted || state.isGameOver) return
        val currentCell = state.cells[index]
        if (currentCell.isGiven) return

        if (state.notesMode) {
            // 笔记模式：切换候选数位掩码
            val newCell = currentCell.toggleNote(number)
            val updated = state.cells.toMutableList()
            updated[index] = newCell

            undoStack.add(
                MoveRecord(
                    row = currentCell.row,
                    col = currentCell.col,
                    previousValue = currentCell.value,
                    newValue = currentCell.value,
                    previousNotes = currentCell.notes,
                    newNotes = newCell.notes
                )
            )

            feedbackManager.playNoteSound()
            feedbackManager.vibrateClick()

            _uiState.update {
                it.copy(cells = updated, canUndo = true)
            }
            saveCurrentGame()
        } else {
            // 正常填数模式
            if (currentCell.value == number) return

            val isCorrect = (number == currentCell.solution)
            val wasError = !isCorrect
            var newMistakes = state.mistakesCount
            if (wasError) {
                newMistakes++
                feedbackManager.playErrorSound()
                feedbackManager.vibrateError()
            } else {
                feedbackManager.playNumberSound()
                feedbackManager.vibrateClick()
            }

            val newCell = currentCell.copy(
                value = number,
                isError = wasError,
                notes = 0
            )

            val updated = state.cells.toMutableList()
            updated[index] = newCell

            // 智能联动清除：如果填入正确，自动移除同行、同列、同宫其他格的对应候选数
            val affectedList = mutableListOf<AffectedNote>()
            if (isCorrect && state.settings.autoRemoveNotes) {
                for (i in 0 until 81) {
                    val c = updated[i]
                    if (!c.isFilled && c.hasNote(number)) {
                        val isPeer = c.row == currentCell.row || c.col == currentCell.col || c.blockIndex == currentCell.blockIndex
                        if (isPeer) {
                            val cleared = c.removeNote(number)
                            updated[i] = cleared
                            affectedList.add(AffectedNote(c.row, c.col, c.notes, cleared.notes))
                        }
                    }
                }
            }

            undoStack.add(
                MoveRecord(
                    row = currentCell.row,
                    col = currentCell.col,
                    previousValue = currentCell.value,
                    newValue = number,
                    previousNotes = currentCell.notes,
                    newNotes = 0,
                    affectedNotes = affectedList,
                    wasError = wasError
                )
            )

            // 检查是否有行、列、宫在此步刚被填满且全部正确（触发波浪动效）
            val completedWave = checkWaveTrigger(updated, currentCell.row, currentCell.col)

            val remaining = calculateRemaining(updated)
            val isWin = checkWinCondition(updated)
            val isOver = state.settings.mistakesLimitEnabled && newMistakes >= 3

            _uiState.update {
                it.copy(
                    cells = updated,
                    mistakesCount = newMistakes,
                    isGameOver = isOver,
                    waveCells = completedWave,
                    remainingCounts = remaining,
                    canUndo = true
                )
            }

            if (isWin) {
                handleVictory()
            } else if (isOver) {
                stopTimer()
            }
            saveCurrentGame()
        }
    }

    fun eraseCurrentCell() {
        val state = _uiState.value
        val index = state.selectedIndex ?: return
        if (state.isPaused || state.isCompleted || state.isGameOver) return
        val currentCell = state.cells[index]
        if (currentCell.isGiven || (currentCell.value == 0 && currentCell.notes == 0)) return

        val newCell = currentCell.copy(value = 0, notes = 0, isError = false)
        val updated = state.cells.toMutableList()
        updated[index] = newCell

        undoStack.add(
            MoveRecord(
                row = currentCell.row,
                col = currentCell.col,
                previousValue = currentCell.value,
                newValue = 0,
                previousNotes = currentCell.notes,
                newNotes = 0
            )
        )

        feedbackManager.playEraseSound()
        feedbackManager.vibrateClick()

        _uiState.update {
            it.copy(
                cells = updated,
                remainingCounts = calculateRemaining(updated),
                canUndo = true
            )
        }
        saveCurrentGame()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val lastMove = undoStack.removeAt(undoStack.lastIndex)
        val state = _uiState.value
        val updated = state.cells.toMutableList()

        val targetIdx = lastMove.row * 9 + lastMove.col
        val curr = updated[targetIdx]
        updated[targetIdx] = curr.copy(
            value = lastMove.previousValue,
            notes = lastMove.previousNotes,
            isError = false
        )

        // 还原受波及自动清除的笔记
        for (aff in lastMove.affectedNotes) {
            val affIdx = aff.row * 9 + aff.col
            val c = updated[affIdx]
            updated[affIdx] = c.copy(notes = aff.oldNotes)
        }

        var mistakes = state.mistakesCount
        if (lastMove.wasError && mistakes > 0) {
            mistakes--
        }

        feedbackManager.vibrateClick()

        _uiState.update {
            it.copy(
                cells = updated,
                mistakesCount = mistakes,
                isGameOver = false,
                remainingCounts = calculateRemaining(updated),
                canUndo = undoStack.isNotEmpty()
            )
        }
        saveCurrentGame()
    }

    /**
     * 一键自动填满（或在已填满时清除）全盘所有空格的合法候选数
     */
    fun toggleAutoNotes() {
        val state = _uiState.value
        if (state.isPaused || state.isCompleted) return

        val grid = Array(9) { r ->
            IntArray(9) { c ->
                val cell = state.cells[r * 9 + c]
                if (cell.isFilled && !cell.isError) cell.value else 0
            }
        }

        // 检查当前是否已经填有了笔记
        val hasAnyNotes = state.cells.any { !it.isFilled && it.notes != 0 }

        val updated = state.cells.map { cell ->
            if (cell.isFilled) {
                cell
            } else {
                if (hasAnyNotes) {
                    cell.copy(notes = 0)
                } else {
                    val candidates = hintAnalyzer.getCandidates(grid, cell.row, cell.col)
                    var mask = 0
                    candidates.forEach { mask = mask or (1 shl it) }
                    cell.copy(notes = mask)
                }
            }
        }

        feedbackManager.vibrateClick()
        _uiState.update { it.copy(cells = updated) }
        saveCurrentGame()
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.hintsRemaining <= 0 || state.isPaused || state.isCompleted) return

        val hint = hintAnalyzer.analyzeNextStep(state.cells, state.selectedIndex) ?: return
        val targetIdx = hint.targetRow * 9 + hint.targetCol

        // 自动填入提示答案
        val currentCell = state.cells[targetIdx]
        val newCell = currentCell.copy(
            value = hint.suggestedValue,
            solution = hint.suggestedValue,
            isError = false,
            notes = 0
        )
        val updated = state.cells.toMutableList()
        updated[targetIdx] = newCell

        // 自动清除关联笔记
        if (state.settings.autoRemoveNotes) {
            for (i in 0 until 81) {
                val c = updated[i]
                if (!c.isFilled && c.hasNote(hint.suggestedValue)) {
                    val isPeer = c.row == hint.targetRow || c.col == hint.targetCol || c.blockIndex == newCell.blockIndex
                    if (isPeer) {
                        updated[i] = c.removeNote(hint.suggestedValue)
                    }
                }
            }
        }

        feedbackManager.playVictoryChord()
        feedbackManager.vibrateSuccess()

        val newHints = state.hintsRemaining - 1
        val completedWave = checkWaveTrigger(updated, hint.targetRow, hint.targetCol)
        val remaining = calculateRemaining(updated)
        val isWin = checkWinCondition(updated)

        _uiState.update {
            it.copy(
                cells = updated,
                selectedIndex = targetIdx,
                hintsRemaining = newHints,
                currentHint = hint,
                waveCells = completedWave,
                remainingCounts = remaining
            )
        }

        if (isWin) {
            handleVictory()
        }
        saveCurrentGame()
    }

    fun dismissHint() {
        _uiState.update { it.copy(currentHint = null) }
    }

    fun reviveGame() {
        _uiState.update {
            it.copy(
                mistakesCount = 2,
                isGameOver = false,
                isPaused = false
            )
        }
        startTimer()
    }

    private fun handleVictory() {
        stopTimer()
        feedbackManager.playVictoryChord()
        feedbackManager.vibrateSuccess()

        val state = _uiState.value
        storage.clearActiveGame()

        if (state.isDailyChallenge && state.dailyDateString != null) {
            storage.recordDailyCompleted(state.dailyDateString)
        }

        val oldStats = storage.getStats(state.difficulty)
        val isNewRecord = oldStats.bestTimeSeconds == 0L || state.elapsedTimeSeconds < oldStats.bestTimeSeconds
        storage.recordGameFinished(state.difficulty, won = true, timeSeconds = state.elapsedTimeSeconds)

        _uiState.update {
            it.copy(isCompleted = true, isNewRecord = isNewRecord)
        }
    }

    private fun checkWaveTrigger(board: List<Cell>, r: Int, c: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        // 检查行
        val rowComplete = (0 until 9).all { col ->
            val cell = board[r * 9 + col]
            cell.isFilled && cell.isCorrect
        }
        if (rowComplete) {
            (0 until 9).forEach { result.add(r * 9 + it) }
        }

        // 检查列
        val colComplete = (0 until 9).all { row ->
            val cell = board[row * 9 + c]
            cell.isFilled && cell.isCorrect
        }
        if (colComplete) {
            (0 until 9).forEach { result.add(it * 9 + c) }
        }

        // 检查 3x3 宫
        val boxStartR = (r / 3) * 3
        val boxStartC = (c / 3) * 3
        var boxComplete = true
        val boxIndices = mutableListOf<Int>()
        for (dr in 0 until 3) {
            for (dc in 0 until 3) {
                val idx = (boxStartR + dr) * 9 + (boxStartC + dc)
                boxIndices.add(idx)
                val cell = board[idx]
                if (!cell.isFilled || !cell.isCorrect) {
                    boxComplete = false
                }
            }
        }
        if (boxComplete) {
            result.addAll(boxIndices)
        }

        return result
    }

    private fun checkWinCondition(board: List<Cell>): Boolean {
        return board.all { it.isFilled && it.isCorrect }
    }

    private fun calculateRemaining(board: List<Cell>): Map<Int, Int> {
        val counts = IntArray(10)
        for (cell in board) {
            if (cell.isFilled && cell.isCorrect) {
                counts[cell.value]++
            }
        }
        return (1..9).associateWith { (9 - counts[it]).coerceAtLeast(0) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isCompleted && !_uiState.value.isGameOver) {
                    _uiState.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveCurrentGame() {
        val state = _uiState.value
        if (state.cells.isEmpty() || state.isCompleted) return
        storage.saveActiveGame(
            GameStorage.SavedGameData(
                difficulty = state.difficulty,
                isDailyChallenge = state.isDailyChallenge,
                dailyDateString = state.dailyDateString,
                elapsedTimeSeconds = state.elapsedTimeSeconds,
                mistakesCount = state.mistakesCount,
                hintsUsed = 3 - state.hintsRemaining,
                cells = state.cells
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        saveCurrentGame()
    }
}
