package com.antigravity.sudoku.data.local

import android.content.Context
import android.content.SharedPreferences
import com.antigravity.sudoku.domain.model.Cell
import com.antigravity.sudoku.domain.model.Difficulty
import com.antigravity.sudoku.domain.model.DifficultyStats
import com.antigravity.sudoku.domain.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject

/**
 * 本地持久化数据仓库 (基于 JSON + SharedPreferences)
 * 具备极佳的兼容性与轻量高效特性，支持随时恢复中断对局、统计记录、每日挑战与偏好配置
 */
class GameStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sudoku_storage", Context.MODE_PRIVATE)

    // ==========================================
    // 1. 中断对局 (Saved Game)
    // ==========================================

    data class SavedGameData(
        val difficulty: Difficulty,
        val isDailyChallenge: Boolean,
        val dailyDateString: String?,
        val elapsedTimeSeconds: Long,
        val mistakesCount: Int,
        val hintsUsed: Int,
        val cells: List<Cell>
    )

    fun saveActiveGame(data: SavedGameData) {
        val json = JSONObject()
        json.put("difficulty", data.difficulty.name)
        json.put("isDailyChallenge", data.isDailyChallenge)
        json.put("dailyDateString", data.dailyDateString ?: "")
        json.put("elapsedTimeSeconds", data.elapsedTimeSeconds)
        json.put("mistakesCount", data.mistakesCount)
        json.put("hintsUsed", data.hintsUsed)

        val cellsArray = JSONArray()
        data.cells.forEach { cell ->
            val cellObj = JSONObject()
            cellObj.put("r", cell.row)
            cellObj.put("c", cell.col)
            cellObj.put("v", cell.value)
            cellObj.put("s", cell.solution)
            cellObj.put("g", cell.isGiven)
            cellObj.put("n", cell.notes)
            cellObj.put("e", cell.isError)
            cellsArray.put(cellObj)
        }
        json.put("cells", cellsArray)

        prefs.edit().putString(KEY_ACTIVE_GAME, json.toString()).apply()
    }

    fun loadActiveGame(): SavedGameData? {
        val str = prefs.getString(KEY_ACTIVE_GAME, null) ?: return null
        return try {
            val json = JSONObject(str)
            val difficulty = Difficulty.fromName(json.optString("difficulty", "MEDIUM"))
            val isDaily = json.optBoolean("isDailyChallenge", false)
            val dailyDate = json.optString("dailyDateString", null).takeIf { !it.isNullOrEmpty() }
            val time = json.optLong("elapsedTimeSeconds", 0L)
            val mistakes = json.optInt("mistakesCount", 0)
            val hints = json.optInt("hintsUsed", 0)

            val cellsArray = json.getJSONArray("cells")
            if (cellsArray.length() != 81) {
                clearActiveGame()
                return null
            }
            val cells = mutableListOf<Cell>()
            for (i in 0 until cellsArray.length()) {
                val o = cellsArray.getJSONObject(i)
                cells.add(
                    Cell(
                        row = o.getInt("r"),
                        col = o.getInt("c"),
                        value = o.getInt("v"),
                        solution = o.getInt("s"),
                        isGiven = o.getBoolean("g"),
                        notes = o.getInt("n"),
                        isError = o.optBoolean("e", false)
                    )
                )
            }
            SavedGameData(
                difficulty = difficulty,
                isDailyChallenge = isDaily,
                dailyDateString = dailyDate,
                elapsedTimeSeconds = time,
                mistakesCount = mistakes,
                hintsUsed = hints,
                cells = cells
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearActiveGame() {
        prefs.edit().remove(KEY_ACTIVE_GAME).apply()
    }

    // ==========================================
    // 2. 统计数据 (Statistics per Difficulty)
    // ==========================================

    fun getStats(difficulty: Difficulty): DifficultyStats {
        val key = "$KEY_STATS_PREFIX${difficulty.name}"
        val str = prefs.getString(key, null) ?: return DifficultyStats()
        return try {
            val json = JSONObject(str)
            DifficultyStats(
                gamesPlayed = json.optInt("played", 0),
                gamesWon = json.optInt("won", 0),
                bestTimeSeconds = json.optLong("bestTime", 0L),
                totalTimeSeconds = json.optLong("totalTime", 0L),
                currentStreak = json.optInt("currentStreak", 0),
                maxStreak = json.optInt("maxStreak", 0)
            )
        } catch (e: Exception) {
            DifficultyStats()
        }
    }

    fun recordGameFinished(difficulty: Difficulty, won: Boolean, timeSeconds: Long) {
        val old = getStats(difficulty)
        val newPlayed = old.gamesPlayed + 1
        val newWon = if (won) old.gamesWon + 1 else old.gamesWon
        val newTotalTime = if (won) old.totalTimeSeconds + timeSeconds else old.totalTimeSeconds
        val newBestTime = if (won) {
            if (old.bestTimeSeconds == 0L || timeSeconds < old.bestTimeSeconds) timeSeconds else old.bestTimeSeconds
        } else {
            old.bestTimeSeconds
        }
        val newStreak = if (won) old.currentStreak + 1 else 0
        val newMaxStreak = maxOf(old.maxStreak, newStreak)

        val updated = DifficultyStats(
            gamesPlayed = newPlayed,
            gamesWon = newWon,
            bestTimeSeconds = newBestTime,
            totalTimeSeconds = newTotalTime,
            currentStreak = newStreak,
            maxStreak = newMaxStreak
        )

        val json = JSONObject().apply {
            put("played", updated.gamesPlayed)
            put("won", updated.gamesWon)
            put("bestTime", updated.bestTimeSeconds)
            put("totalTime", updated.totalTimeSeconds)
            put("currentStreak", updated.currentStreak)
            put("maxStreak", updated.maxStreak)
        }
        prefs.edit().putString("$KEY_STATS_PREFIX${difficulty.name}", json.toString()).apply()
    }

    // ==========================================
    // 3. 每日挑战日历记录 (Daily Challenge Calendar)
    // ==========================================

    fun getCompletedDailyDates(): Set<String> {
        return prefs.getStringSet(KEY_COMPLETED_DAILIES, emptySet())?.toSet() ?: emptySet()
    }

    fun recordDailyCompleted(dateString: String) {
        val current = getCompletedDailyDates().toMutableSet()
        current.add(dateString)
        prefs.edit().putStringSet(KEY_COMPLETED_DAILIES, current).apply()
    }

    // ==========================================
    // 4. 用户设置偏好 (Settings)
    // ==========================================

    fun getSettings(): UserSettings {
        return UserSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            mistakesLimitEnabled = prefs.getBoolean(KEY_MISTAKES_LIMIT, true),
            highlightSameNumbers = prefs.getBoolean(KEY_HIGHLIGHT_SAME, true),
            highlightPeers = prefs.getBoolean(KEY_HIGHLIGHT_PEERS, true),
            autoRemoveNotes = prefs.getBoolean(KEY_AUTO_REMOVE_NOTES, true),
            darkMode = if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null,
            portraitLocked = prefs.getBoolean(KEY_PORTRAIT_LOCKED, false)
        )
    }

    fun saveSettings(settings: UserSettings) {
        val editor = prefs.edit()
            .putBoolean(KEY_SOUND, settings.soundEnabled)
            .putBoolean(KEY_HAPTIC, settings.hapticEnabled)
            .putBoolean(KEY_MISTAKES_LIMIT, settings.mistakesLimitEnabled)
            .putBoolean(KEY_HIGHLIGHT_SAME, settings.highlightSameNumbers)
            .putBoolean(KEY_HIGHLIGHT_PEERS, settings.highlightPeers)
            .putBoolean(KEY_AUTO_REMOVE_NOTES, settings.autoRemoveNotes)
            .putBoolean(KEY_PORTRAIT_LOCKED, settings.portraitLocked)

        if (settings.darkMode != null) {
            editor.putBoolean(KEY_DARK_MODE, settings.darkMode)
        } else {
            editor.remove(KEY_DARK_MODE)
        }
        editor.apply()
    }

    companion object {
        private const val KEY_ACTIVE_GAME = "active_game_state"
        private const val KEY_STATS_PREFIX = "stats_difficulty_"
        private const val KEY_COMPLETED_DAILIES = "completed_daily_dates"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_MISTAKES_LIMIT = "pref_mistakes_limit"
        private const val KEY_HIGHLIGHT_SAME = "pref_highlight_same"
        private const val KEY_HIGHLIGHT_PEERS = "pref_highlight_peers"
        private const val KEY_AUTO_REMOVE_NOTES = "pref_auto_remove_notes"
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_PORTRAIT_LOCKED = "pref_portrait_locked"
    }
}
