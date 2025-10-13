package com.example.androidgamekt.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class GameSettings(
    val gameSpeed: Int,
    val bugSpeed: Int,
    val maxBugs: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int,
    val difficulty: String
)

class GameSettingsViewModel : ViewModel() {

    private val _settings = MutableLiveData(
        GameSettings(
            gameSpeed = DEFAULT_GAME_SPEED,
            bugSpeed = DEFAULT_BUG_SPEED,
            maxBugs = DEFAULT_MAX_BUGS,
            bonusIntervalSeconds = DEFAULT_BONUS_INTERVAL_SECONDS,
            roundDurationSeconds = DEFAULT_ROUND_DURATION_SECONDS,
            difficulty = DEFAULT_DIFFICULTY
        )
    )
    val settings: LiveData<GameSettings> = _settings

    fun setGameSpeed(speed: Int) {
        updateSettings { current ->
            current.copy(gameSpeed = speed.coerceAtLeast(MIN_GAME_SPEED))
        }
    }

    fun setBugSpeed(speed: Int) {
        updateSettings { current ->
            current.copy(bugSpeed = speed.coerceAtLeast(MIN_BUG_SPEED))
        }
    }

    fun setMaxBugs(maxBugs: Int) {
        updateSettings { current ->
            current.copy(maxBugs = maxBugs.coerceAtLeast(MIN_MAX_BUGS))
        }
    }

    fun setBonusInterval(seconds: Int) {
        updateSettings { current ->
            current.copy(bonusIntervalSeconds = seconds.coerceAtLeast(0))
        }
    }

    fun setRoundDuration(seconds: Int) {
        updateSettings { current ->
            current.copy(roundDurationSeconds = seconds.coerceAtLeast(MIN_ROUND_DURATION_SECONDS))
        }
    }

    fun setDifficulty(difficulty: String) {
        updateSettings { current ->
            val newSettings = when (difficulty) {
                "Легкий" -> GameSettings(
                    gameSpeed = 3,
                    bugSpeed = 3,
                    maxBugs = 10,
                    bonusIntervalSeconds = 15,
                    roundDurationSeconds = 90,
                    difficulty = difficulty
                )
                "Средний" -> GameSettings(
                    gameSpeed = 5,
                    bugSpeed = 5,
                    maxBugs = 15,
                    bonusIntervalSeconds = 10,
                    roundDurationSeconds = 60,
                    difficulty = difficulty
                )
                "Сложный" -> GameSettings(
                    gameSpeed = 8,
                    bugSpeed = 8,
                    maxBugs = 20,
                    bonusIntervalSeconds = 5,
                    roundDurationSeconds = 45,
                    difficulty = difficulty
                )
                else -> current.copy(difficulty = difficulty)
            }
            newSettings
        }
    }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val current = _settings.value ?: DEFAULT_SETTINGS
        _settings.value = transform(current)
    }

    companion object {
        private const val MIN_GAME_SPEED = 1
        private const val MIN_BUG_SPEED = 1
        private const val MIN_MAX_BUGS = 1
        private const val MIN_ROUND_DURATION_SECONDS = 5
        private const val DEFAULT_GAME_SPEED = 5
        private const val DEFAULT_BUG_SPEED = 5
        private const val DEFAULT_MAX_BUGS = 15
        private const val DEFAULT_BONUS_INTERVAL_SECONDS = 10
        private const val DEFAULT_ROUND_DURATION_SECONDS = 60

        private const val DEFAULT_DIFFICULTY = "Средний"

        private val DEFAULT_SETTINGS = GameSettings(
            gameSpeed = DEFAULT_GAME_SPEED,
            bugSpeed = DEFAULT_BUG_SPEED,
            maxBugs = DEFAULT_MAX_BUGS,
            bonusIntervalSeconds = DEFAULT_BONUS_INTERVAL_SECONDS,
            roundDurationSeconds = DEFAULT_ROUND_DURATION_SECONDS,
            difficulty = DEFAULT_DIFFICULTY
        )
    }
}
