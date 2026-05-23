package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: GameDao) {

    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val leaderboard: Flow<List<LeaderboardEntity>> = dao.getLeaderboard()
    val dailyChallenges: Flow<List<DailyChallengeEntity>> = dao.getDailyChallenges()

    suspend fun getProfile(): PlayerProfileEntity {
        val existing = dao.getPlayerProfileDirect()
        if (existing == null) {
            val defaultProfile = PlayerProfileEntity()
            dao.savePlayerProfile(defaultProfile)
            // Prepopulate daily challenges too
            initializeDailyChallenges()
            // Prepopulate mock leaderboards with typical snake.io highscores
            initializeLeaderboard()
            return defaultProfile
        } else if (existing.name == "SlitherConda" || existing.name == "AKHAN1834") {
            val updated = existing.copy(name = "M.AKhan1834")
            dao.savePlayerProfile(updated)
            return updated
        }
        return existing
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) {
        dao.savePlayerProfile(profile)
    }

    suspend fun addLeaderboard(name: String, score: Int, level: Int) {
        dao.addLeaderboardEntry(LeaderboardEntity(name = name, score = score, level = level))
    }

    suspend fun updateDailyChallengeProgress(type: String, increment: Int) {
        val challenges = dao.getDailyChallenges().firstOrNull() ?: return
        for (challenge in challenges) {
            if (challenge.type == type && !challenge.completed) {
                val newValue = (challenge.currentValue + increment).coerceAtMost(challenge.targetValue)
                val isCompleted = newValue >= challenge.targetValue
                val updatedChallenge = challenge.copy(
                    currentValue = newValue,
                    completed = isCompleted
                )
                dao.updateDailyChallenge(updatedChallenge)

                // If completed, reward the coins
                if (isCompleted) {
                    val profile = dao.getPlayerProfileDirect() ?: PlayerProfileEntity()
                    dao.savePlayerProfile(profile.copy(coins = profile.coins + challenge.rewardCoins))
                }
            }
        }
    }

    suspend fun initializeDailyChallenges() {
        val list = listOf(
            DailyChallengeEntity(1, "Eats 25 food pellets on the map", 25, 0, 50, false, "PELLET"),
            DailyChallengeEntity(2, "Eats/Eliminates 3 other competitor anacondas", 3, 0, 100, false, "ELIMINATE"),
            DailyChallengeEntity(3, "Use Speed-up button for a total of 15 seconds", 15, 0, 75, false, "BOOST")
        )
        dao.saveDailyChallenges(list)
    }

    private suspend fun initializeLeaderboard() {
        val initialEntries = listOf(
            LeaderboardEntity(name = "ApexPredator", score = 2500, level = 10),
            LeaderboardEntity(name = "SqueezeKing", score = 1850, level = 8),
            LeaderboardEntity(name = "CoilMaster", score = 1400, level = 6),
            LeaderboardEntity(name = "VenomousBite", score = 1100, level = 5),
            LeaderboardEntity(name = "SnakeyWaker", score = 750, level = 3),
            LeaderboardEntity(name = "GrassCrawl", score = 420, level = 1)
        )
        for (entry in initialEntries) {
            dao.addLeaderboardEntry(entry)
        }
    }

    suspend fun resetDailyChallenges() {
        initializeDailyChallenges()
    }
}
