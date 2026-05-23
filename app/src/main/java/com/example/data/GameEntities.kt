package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: String = "singleton",
    val name: String = "M.AKhan1834",
    val level: Int = 1,
    val coins: Int = 100,
    val selectedSkinId: Int = 0,
    val soundVolume: Float = 0.8f,
    val musicVolume: Float = 0.5f,
    val unlockedSkinsCsv: String = "0,1,2", // 0, 1, 2 are free
    val selectedBackgroundId: Int = 0,
    val allSkinsUnlockedAtLevel: Int = -1
)

@Entity(tableName = "leaderboards")
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val score: Int,
    val level: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val id: Int,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val rewardCoins: Int,
    val completed: Boolean = false,
    val type: String // "PELLET", "ELIMINATE", "LEVEL_UP", "BOOST"
)

@Dao
interface GameDao {
    @Query("SELECT * FROM player_profile WHERE id = 'singleton' LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 'singleton' LIMIT 1")
    suspend fun getPlayerProfileDirect(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM leaderboards ORDER BY score DESC LIMIT 15")
    fun getLeaderboard(): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLeaderboardEntry(entry: LeaderboardEntity)

    @Query("SELECT * FROM daily_challenges")
    fun getDailyChallenges(): Flow<List<DailyChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyChallenges(challenges: List<DailyChallengeEntity>)

    @Update
    suspend fun updateDailyChallenge(challenge: DailyChallengeEntity)
}
