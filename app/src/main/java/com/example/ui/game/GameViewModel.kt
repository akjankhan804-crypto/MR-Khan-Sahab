package com.example.ui.game

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.skins.SkinRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GameDatabase.getDatabase(application)
    val repository = GameRepository(database.gameDao())

    // Database Flows
    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyChallenges: StateFlow<List<DailyChallengeEntity>> = repository.dailyChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Game Running States
    private val _battleState = MutableStateFlow(BattleState.IDLE)
    val battleState: StateFlow<BattleState> = _battleState.asStateFlow()

    private val _playerSnake = MutableStateFlow<Snake?>(null)
    val playerSnake: StateFlow<Snake?> = _playerSnake.asStateFlow()

    private val _bots = MutableStateFlow<List<Snake>>(emptyList())
    val bots: StateFlow<List<Snake>> = _bots.asStateFlow()

    private val _pellets = MutableStateFlow<List<GamePellet>>(emptyList())
    val pellets: StateFlow<List<GamePellet>> = _pellets.asStateFlow()

    private val _powerUps = MutableStateFlow<List<GamePowerUp>>(emptyList())
    val powerUps: StateFlow<List<GamePowerUp>> = _powerUps.asStateFlow()

    private val _aliveBotCount = MutableStateFlow(0)
    val aliveBotCount: StateFlow<Int> = _aliveBotCount.asStateFlow()

    // Mode choice state
    private val _gameMode = MutableStateFlow("offline") // "offline" -> ROBO MODE, "online" -> MULTIPLAYER
    val gameMode: StateFlow<String> = _gameMode.asStateFlow()

    // Robot quantity input
    private val _robotQuantity = MutableStateFlow(8) // chosen quantity of bots
    val robotQuantity: StateFlow<Int> = _robotQuantity.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Screen navigation
    private val _currentScreen = MutableStateFlow("menu") // menu, garage, play, challenges, leaderboard, shop, settings
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Joystick input
    private var joystickVector = Offset.Zero
    private var isSpeedingUp = false

    // Arena Constants (70% bigger than original 2200 x 2200)
    val arenaSize = Offset(3740f, 3740f)
    private val baseSpeed = 5.3f
    private val boostSpeed = baseSpeed * 3f
    private val segmentSpacingPoints = 4 // paths-points between segments

    // Path histories for smooth rendering of curves
    private var playerHistory = mutableListOf<Offset>()
    private val botHistories = mutableMapOf<String, MutableList<Offset>>()

    private var gameLoopJob: Job? = null
    private var scoreTracking = 0

    // Random Bot Names
    private val botNames = listOf(
        "SqueezeBoi", "SwampPython", "FierceViper", "GraveWinder", "KraitAttacker",
        "NeonSerpent", "SpitFire", "MudSlider", "ToxinScale", "JungleTerror",
        "SlitherMaster", "BoaBoss", "CoilCrusher", "GreenPhanton", "JungleScourge",
        "ApexTail", "PoisonFlesh", "ScaleStriker", "LurkCobra"
    )

    init {
        // Initialize Database Defaults
        viewModelScope.launch {
            repository.getProfile()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- GAME ACTIONS ---

    fun setGameMode(mode: String) {
        _gameMode.value = mode
    }

    fun setRobotQuantity(quantity: Int) {
        _robotQuantity.value = quantity.coerceIn(2, 25)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startGame() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: repository.getProfile()
            setupGame(profile.level, profile.selectedSkinId)
            
            if (_gameMode.value == "online") {
                _battleState.value = BattleState.MATCHING
                _currentScreen.value = "play"
                delay(2800) // Delay to simulate connecting with live multiplayer opponents
            }
            
            _battleState.value = BattleState.PLAYING
            _currentScreen.value = "play"
            startGameLoop()
        }
    }

    private fun setupGame(level: Int, selectedSkinId: Int) {
        playerHistory.clear()
        botHistories.clear()
        joystickVector = Offset.Zero
        isSpeedingUp = false

        val spawnX = arenaSize.x / 2
        val spawnY = arenaSize.y / 2
        val startHead = Offset(spawnX, spawnY)

        // Generate player snake with 8 starting segments
        val startingLength = 8
        val startingSegments = (0 until startingLength).map { Offset(spawnX, spawnY + it * 15f) }
        startingSegments.forEach { playerHistory.add(it) } // seed history

        _playerSnake.value = Snake(
            id = "player",
            name = playerProfile.value?.name ?: "AKHAN1834",
            isBot = false,
            segments = startingSegments,
            angle = -1.57f, // facing up
            skinId = selectedSkinId,
            score = startingSegments.size * 10
        )

        // Spawn Bots based on chosen user slider count or choice
        val botCount = _robotQuantity.value
        _aliveBotCount.value = botCount

        val createdBots = mutableListOf<Snake>()
        for (i in 0 until botCount) {
            val bId = "bot_$i"
            val bSpawn = getRandomSpawnOffset(startHead, 350f)
            val bAngle = Random.nextFloat() * 6.28f
            val bLength = 6 + Random.nextInt(4) + (level).coerceAtMost(15) // bots are bigger in higher levels!
            val bSegments = (0 until bLength).map { Offset(bSpawn.x, bSpawn.y + it * 15f) }

            val historyList = mutableListOf<Offset>()
            bSegments.forEach { historyList.add(it) }
            botHistories[bId] = historyList

            val botSkinId = Random.nextInt(SkinRegistry.skins.size)

            createdBots.add(
                Snake(
                    id = bId,
                    name = botNames.random(),
                    isBot = true,
                    segments = bSegments,
                    angle = bAngle,
                    skinId = botSkinId,
                    score = bSegments.size * 10
                )
            )
        }
        _bots.value = createdBots

        // Spawn initial map food items (scaled up to balance the 70% larger bounds)
        val listPellets = mutableListOf<GamePellet>()
        for (f in 0 until 450) {
            listPellets.add(spawnRandomPellet(f))
        }
        _pellets.value = listPellets

        // Spawn power-ups
        val powerTypes = listOf("SPEED", "MAGNET", "SHIELD", "MULTIPLIER")
        val listPowers = mutableListOf<GamePowerUp>()
        for (pw in 0 until 12) {
            listPowers.add(
                GamePowerUp(
                    id = pw,
                    position = Offset(Random.nextFloat() * arenaSize.x, Random.nextFloat() * arenaSize.y),
                    type = powerTypes.random()
                )
            )
        }
        _powerUps.value = listPowers
    }

    private fun getRandomSpawnOffset(avoidCenter: Offset, clearRadius: Float): Offset {
        while (true) {
            val ox = Random.nextFloat() * (arenaSize.x - 200f) + 100f
            val oy = Random.nextFloat() * (arenaSize.y - 200f) + 100f
            val dist = sqrt((ox - avoidCenter.x) * (ox - avoidCenter.x) + (oy - avoidCenter.y) * (oy - avoidCenter.y))
            if (dist > clearRadius) {
                return Offset(ox, oy)
            }
        }
    }

    private fun spawnRandomPellet(id: Int): GamePellet {
        val colors = listOf(0xFF00E676.toInt(), 0xFFFFD700.toInt(), 0xFF00B0FF.toInt(), 0xFFFF3D00.toInt(), 0xFFE040FB.toInt())
        return GamePellet(
            id = id,
            position = Offset(Random.nextFloat() * arenaSize.x, Random.nextFloat() * arenaSize.y),
            value = Random.nextInt(2) + 1,
            colorHex = colors.random()
        )
    }

    fun updateJoystick(vector: Offset) {
        joystickVector = vector
    }

    fun setSpeedUp(active: Boolean) {
        isSpeedingUp = active
    }

    // --- GAME LOOP ENGINE ---

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (_battleState.value == BattleState.PLAYING) {
                delay(30) // ~33 FPS
                tickGame()
            }
        }
    }

    private suspend fun tickGame() {
        val player = _playerSnake.value ?: return
        if (player.isDead) return

        // 1. Calculate speeds
        val pSpeed = if (isSpeedingUp || player.speedUpActive) boostSpeed else baseSpeed

        // Charge tail shrink on boost if standard input speedup is on
        var modifierPlayer = player
        if (isSpeedingUp && player.segments.size > 5) {
            modifierPlayer = modifierPlayer.copy(speedUpActive = true)
            // Trigger BOOST daily challenge increment
            repository.updateDailyChallengeProgress("BOOST", 1)
        } else {
            modifierPlayer = modifierPlayer.copy(speedUpActive = false)
        }

        // 2. Adjust player angle via joystick input smoothly
        var targetAngle = modifierPlayer.angle
        if (joystickVector != Offset.Zero) {
            targetAngle = atan2(joystickVector.y, joystickVector.x)
        }

        // smooth angle interpolation
        val angleDiff = diffAngle(targetAngle, modifierPlayer.angle)
        var finalAngle = modifierPlayer.angle + angleDiff * 0.15f

        // Move player head
        val pHead = modifierPlayer.segments[0]
        var pNewHead = Offset(
            (pHead.x + cos(finalAngle) * pSpeed).coerceIn(0f, arenaSize.x),
            (pHead.y + sin(finalAngle) * pSpeed).coerceIn(0f, arenaSize.y)
        )

        // Check boundary limits
        if (pNewHead.x <= 5f || pNewHead.x >= arenaSize.x - 5f || pNewHead.y <= 5f || pNewHead.y >= arenaSize.y - 5f) {
            if (isSpeedingUp || modifierPlayer.speedUpActive) {
                // Deflect safely off the border under speed boost!
                if (pNewHead.x <= 5f) {
                    pNewHead = Offset(25f, pNewHead.y)
                    finalAngle = if (cos(finalAngle) < 0f) 3.14159f - finalAngle else finalAngle
                } else if (pNewHead.x >= arenaSize.x - 5f) {
                    pNewHead = Offset(arenaSize.x - 25f, pNewHead.y)
                    finalAngle = if (cos(finalAngle) > 0f) 3.14159f - finalAngle else finalAngle
                }

                if (pNewHead.y <= 5f) {
                    pNewHead = Offset(pNewHead.x, 25f)
                    finalAngle = if (sin(finalAngle) < 0f) -finalAngle else finalAngle
                } else if (pNewHead.y >= arenaSize.y - 5f) {
                    pNewHead = Offset(pNewHead.x, arenaSize.y - 25f)
                    finalAngle = if (sin(finalAngle) > 0f) -finalAngle else finalAngle
                }
            } else {
                // Died by boundaries!
                triggerPlayerDefeat()
                return
            }
        }

        // Update player path history
        playerHistory.add(0, pNewHead)

        // Calculate segment locations along the custom history line
        val pTargetLen = modifierPlayer.segments.size
        val pRequiredHistoryPoints = pTargetLen * segmentSpacingPoints
        while (playerHistory.size > pRequiredHistoryPoints + 15) {
            playerHistory.removeAt(playerHistory.size - 1)
        }

        val updatedPlayerSegments = mutableListOf<Offset>()
        updatedPlayerSegments.add(pNewHead)
        for (i in 1 until pTargetLen) {
            val histIdx = i * segmentSpacingPoints
            if (histIdx < playerHistory.size) {
                updatedPlayerSegments.add(playerHistory[histIdx])
            } else {
                updatedPlayerSegments.add(playerHistory.lastOrNull() ?: pNewHead)
            }
        }

        // Store intermediate player update
        var updatedPlayer = modifierPlayer.copy(
            segments = updatedPlayerSegments,
            angle = finalAngle
        )

        // 3. Move bots with basic AI behavior
        val currentBots = _bots.value.toMutableList()
        val nextBots = mutableListOf<Snake>()

        for (bot in currentBots) {
            if (bot.isDead) continue

            val bId = bot.id
            val bHistory = botHistories.getOrPut(bId) { mutableListOf() }
            val bHead = bot.segments[0]

            // AI Steering Logic
            var bTargetAngle = bot.angle

            // Avoid boundaries steering
            if (bHead.x < 150f) bTargetAngle = 0f
            else if (bHead.x > arenaSize.x - 150f) bTargetAngle = 3.1415f
            if (bHead.y < 150f) bTargetAngle = 1.57f
            else if (bHead.y > arenaSize.y - 150f) bTargetAngle = -1.57f

            // Occasionally wander
            if (Random.nextFloat() < 0.04f) {
                bTargetAngle += (Random.nextFloat() * 2f - 1f) * 1.5f
            }

            // Steer to closest pellet or powerup
            if (Random.nextFloat() < 0.15f) {
                var closestPellet: GamePellet? = null
                var minDist = 400f
                for (p in _pellets.value) {
                    val dist = distance(bHead, p.position)
                    if (dist < minDist) {
                        minDist = dist
                        closestPellet = p
                    }
                }
                closestPellet?.let {
                    bTargetAngle = atan2(it.position.y - bHead.y, it.position.x - bHead.x)
                }
            }

            // Try to avoid player body or other bots bodies
            var isAvoidingCollision = false
            // Check player body
            for (seg in updatedPlayer.segments) {
                val dist = distance(bHead, seg)
                if (dist < 100f) {
                    // Turn opposite
                    val angleToSeg = atan2(seg.y - bHead.y, seg.x - bHead.x)
                    bTargetAngle = angleToSeg + 3.14f
                    if (Random.nextBoolean()) bTargetAngle += 0.5f else bTargetAngle -= 0.5f
                    isAvoidingCollision = true
                    break
                }
            }

            // Interpolate bot angle
            val bAngleDiff = diffAngle(bTargetAngle, bot.angle)
            val bFinalAngle = bot.angle + bAngleDiff * 0.12f

            // Move bot head
            val bSpeed = if (isAvoidingCollision) boostSpeed else baseSpeed
            val bNewHead = Offset(
                (bHead.x + cos(bFinalAngle) * bSpeed).coerceIn(0f, arenaSize.x),
                (bHead.y + sin(bFinalAngle) * bSpeed).coerceIn(0f, arenaSize.y)
            )

            // Die if hitting outer wall
            if (bNewHead.x <= 5f || bNewHead.x >= arenaSize.x - 5f || bNewHead.y <= 5f || bNewHead.y >= arenaSize.y - 5f) {
                disintegrateBot(bot)
                _aliveBotCount.value = (_aliveBotCount.value - 1).coerceAtLeast(0)
                continue
            }

            bHistory.add(0, bNewHead)
            val bTargetLen = bot.segments.size
            while (bHistory.size > bTargetLen * segmentSpacingPoints + 15) {
                bHistory.removeAt(bHistory.size - 1)
            }

            val bSegments = mutableListOf<Offset>()
            bSegments.add(bNewHead)
            for (i in 1 until bTargetLen) {
                val histIdx = i * segmentSpacingPoints
                if (histIdx < bHistory.size) {
                    bSegments.add(bHistory[histIdx])
                } else {
                    bSegments.add(bHistory.lastOrNull() ?: bNewHead)
                }
            }

            nextBots.add(
                bot.copy(
                    segments = bSegments,
                    angle = bFinalAngle
                )
            )
        }

        // 4. Power-Up active timer ticking
        if (updatedPlayer.magnetDurationMs > 0) {
            updatedPlayer = updatedPlayer.copy(magnetDurationMs = (updatedPlayer.magnetDurationMs - 30).coerceAtLeast(0))
        }
        if (updatedPlayer.shieldDurationMs > 0) {
            updatedPlayer = updatedPlayer.copy(shieldDurationMs = (updatedPlayer.shieldDurationMs - 30).coerceAtLeast(0))
        }
        if (updatedPlayer.multiplierDurationMs > 0) {
            updatedPlayer = updatedPlayer.copy(multiplierDurationMs = (updatedPlayer.multiplierDurationMs - 30).coerceAtLeast(0))
        }

        // 5. Handle collisions: Head-to-Body!
        var playerDiedThisFrame = false
        val liveBotsAfterCollision = mutableListOf<Snake>()

        // Check if player hits ANY bot's body
        val pHeadCurrent = updatedPlayer.segments[0]
        val isPlayerShielded = updatedPlayer.shieldDurationMs > 0

        for (bot in nextBots) {
            var botDied = false
            // Check if player head hit bot body segments
            for (idx in bot.segments.indices step 2) {
                val seg = bot.segments[idx]
                if (distance(pHeadCurrent, seg) < 24f) {
                    if (isPlayerShielded) {
                        // Shield bounce effect, deflect & remove shield
                        updatedPlayer = updatedPlayer.copy(shieldDurationMs = 0)
                        val reboundAngle = updatedPlayer.angle + 3.14f
                        playerHistory.clear()
                        playerHistory.add(pHeadCurrent - Offset(cos(reboundAngle) * 20f, sin(reboundAngle) * 20f))
                    } else {
                        playerDiedThisFrame = true
                        break
                    }
                }
            }
            if (playerDiedThisFrame) break

            // Check if this bot hits player's body
            val bHeadLoc = bot.segments[0]
            var botHitBody = false
            for (pIdx in updatedPlayer.segments.indices step 2) {
                if (distance(bHeadLoc, updatedPlayer.segments[pIdx]) < 24f) {
                    botHitBody = true
                    break
                }
            }

            // Check if this bot hits any OTHER bot's body
            if (!botHitBody) {
                for (otherBot in nextBots) {
                    if (otherBot.id == bot.id) continue
                    for (otherSegIdx in otherBot.segments.indices step 2) {
                        val otherSeg = otherBot.segments[otherSegIdx]
                        if (distance(bHeadLoc, otherSeg) < 24f) {
                            botHitBody = true
                            break
                        }
                    }
                    if (botHitBody) break
                }
            }

            if (botHitBody) {
                // Eliminate bot
                disintegrateBot(bot)
                _aliveBotCount.value = (_aliveBotCount.value - 1).coerceAtLeast(0)
                // Increment player daily challenges ELIMINATE progress
                repository.updateDailyChallengeProgress("ELIMINATE", 1)
            } else {
                liveBotsAfterCollision.add(bot)
            }
        }

        if (playerDiedThisFrame) {
            triggerPlayerDefeat()
            return
        }

        // 6. Food and Power-Up collection
        var foodMutable = _pellets.value.toMutableList()
        val nextFood = mutableListOf<GamePellet>()

        val isMagnetActive = updatedPlayer.magnetDurationMs > 0
        val isMultiplierActive = updatedPlayer.multiplierDurationMs > 0

        // Create a pre-indexed map of all snake heads (Index -1 for player, >=0 for live bots) to optimize distances checks
        val heads = mutableListOf<Pair<Int, Offset>>()
        heads.add(-1 to pHeadCurrent)
        for (bIdx in liveBotsAfterCollision.indices) {
            heads.add(bIdx to liveBotsAfterCollision[bIdx].segments[0])
        }

        for (pellet in foodMutable) {
            var eaten = false
            var pelletPos = pellet.position

            // Check magnet pulling effect for player only
            if (isMagnetActive) {
                val distToPlayer = distance(pHeadCurrent, pellet.position)
                if (distToPlayer < 190f) {
                    // Pull pellet closer towards head
                    val pullVector = pHeadCurrent - pellet.position
                    val mag = sqrt(pullVector.x * pullVector.x + pullVector.y * pullVector.y)
                    if (mag > 0.1f) {
                        pelletPos = pellet.position + Offset(
                            pullVector.x / mag * 8.5f,
                            pullVector.y / mag * 8.5f
                        )
                    }
                }
            }

            // Quick check against all heads using the prebuilt list
            for (hPair in heads) {
                if (distance(hPair.second, pelletPos) < 28f) {
                    eaten = true
                    if (hPair.first == -1) {
                        // Player ate pellet
                        val pelletVal = if (isMultiplierActive) pellet.value * 2 else pellet.value
                        val lengthGrowAmt = if (Random.nextFloat() < 0.35f) 1 else 0 // grow tail slowly to avoid infinite expansion

                        val extraSegments = mutableListOf<Offset>()
                        if (lengthGrowAmt > 0) {
                            val lastSeg = updatedPlayer.segments.lastOrNull() ?: pHeadCurrent
                            for (m in 0 until lengthGrowAmt) {
                                extraSegments.add(lastSeg)
                            }
                        }

                        updatedPlayer = updatedPlayer.copy(
                            segments = updatedPlayer.segments + extraSegments,
                            score = updatedPlayer.score + pelletVal * 15
                        )

                        // Accumulate daily challenge progress
                        repository.updateDailyChallengeProgress("PELLET", 1)
                    } else {
                        // A bot ate pellet
                        val bIdx = hPair.first
                        if (bIdx < liveBotsAfterCollision.size) {
                            val b = liveBotsAfterCollision[bIdx]
                            val lastSeg = b.segments.lastOrNull() ?: b.segments[0]
                            liveBotsAfterCollision[bIdx] = b.copy(
                                segments = b.segments + listOf(lastSeg),
                                score = b.score + pellet.value * 12
                            )
                        }
                    }
                    break
                }
            }

            if (!eaten) {
                nextFood.add(pellet.copy(position = pelletPos))
            } else {
                // Respawn typical map food immediately if it's general map food to keep density high
                if (!pellet.isSpecialRemains) {
                    nextFood.add(spawnRandomPellet(pellet.id))
                }
            }
        }
        _pellets.value = nextFood

        // Collect Power-ups
        val currentPowerUps = _powerUps.value.toMutableList()
        val nextPowerUps = mutableListOf<GamePowerUp>()

        for (pwr in currentPowerUps) {
            if (distance(pHeadCurrent, pwr.position) < 30f) {
                // Collect power
                when (pwr.type) {
                    "SPEED" -> updatedPlayer = updatedPlayer.copy(speedUpActive = true)
                    "MAGNET" -> updatedPlayer = updatedPlayer.copy(magnetDurationMs = 12000)
                    "SHIELD" -> updatedPlayer = updatedPlayer.copy(shieldDurationMs = 15000)
                    "MULTIPLIER" -> updatedPlayer = updatedPlayer.copy(multiplierDurationMs = 12000)
                }
                // Respawn powerup in a random spot
                nextPowerUps.add(
                    pwr.copy(
                        position = Offset(Random.nextFloat() * arenaSize.x, Random.nextFloat() * arenaSize.y),
                        type = listOf("SPEED", "MAGNET", "SHIELD", "MULTIPLIER").random()
                    )
                )
            } else {
                nextPowerUps.add(pwr)
            }
        }
        _powerUps.value = nextPowerUps

        // 7. Push states into Compose models
        _playerSnake.value = updatedPlayer
        _bots.value = liveBotsAfterCollision

        // Check level win Battle Royale criteria - "The player who eats everyone and becomes the worst of all will be the winner!"
        if (liveBotsAfterCollision.isEmpty() && _aliveBotCount.value == 0) {
            triggerPlayerVictory(updatedPlayer.score)
        }
    }

    private fun disintegrateBot(bot: Snake) {
        // Disintegrate snake segments into glowing Remains pellets on the map
        val remains = _pellets.value.toMutableList()
        var index = Random.nextInt(200000, 999990)
        for (seg in bot.segments) {
            remains.add(
                GamePellet(
                    id = index++,
                    position = seg,
                    value = Random.nextInt(3) + 3, // very high value!
                    colorHex = 0xFFFFEB3B.toInt(), // gold pellet remains
                    isBonus = true,
                    isSpecialRemains = true
                )
            )
        }
        _pellets.value = remains
    }

    private fun diffAngle(target: Float, current: Float): Float {
        var diff = target - current
        while (diff < -Math.PI) diff += (2 * Math.PI).toFloat()
        while (diff > Math.PI) diff -= (2 * Math.PI).toFloat()
        return diff
    }

    private fun distance(o1: Offset, o2: Offset): Float {
        val dx = o2.x - o1.x
        val dy = o2.y - o1.y
        return sqrt(dx * dx + dy * dy)
    }

    // --- BATTLE OUTCOMES ---

    private fun triggerPlayerDefeat() {
        _battleState.value = BattleState.LOST
        gameLoopJob?.cancel()

        viewModelScope.launch {
            val score = _playerSnake.value?.score ?: 100
            val pName = playerProfile.value?.name ?: "AKHAN1834"
            val pLevel = playerProfile.value?.level ?: 1
            // Save local High score log in leaderboard history
            repository.addLeaderboard(pName, score, pLevel)
        }
    }

    private fun triggerPlayerVictory(finalScore: Int) {
        _battleState.value = BattleState.WON
        gameLoopJob?.cancel()

        viewModelScope.launch {
            val profile = playerProfile.value ?: repository.getProfile()
            val oldLevel = profile.level
            val nextLevel = oldLevel + 1
            val coinBonus = 60 + (oldLevel * 10)

            // Unlock new eligible skins
            val activeSkins = profile.unlockedSkinsCsv.split(",").toMutableSet()
            for (skin in SkinRegistry.skins) {
                if (skin.unlockLevel <= nextLevel) {
                    activeSkins.add(skin.id.toString())
                }
            }

            var updatedProfile = profile.copy(
                level = nextLevel,
                coins = profile.coins + coinBonus,
                unlockedSkinsCsv = activeSkins.joinToString(",")
            )

            // Handle background unlock check as well
            val allSkinsUnlocked = SkinRegistry.skins.all { skin ->
                activeSkins.contains(skin.id.toString()) || skin.unlockLevel <= nextLevel
            }
            if (allSkinsUnlocked && updatedProfile.allSkinsUnlockedAtLevel == -1) {
                updatedProfile = updatedProfile.copy(allSkinsUnlockedAtLevel = nextLevel)
            }

            repository.updateProfile(updatedProfile)
            repository.addLeaderboard(profile.name, finalScore, oldLevel)
        }
    }

    // --- COIN SHOP & VISUAL VIRTUAL PURCHASES (IAP) ---

    fun buySkinsInstant(skinId: Int, price: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            if (profile.coins >= price) {
                val listSkins = profile.unlockedSkinsCsv.split(",").toMutableSet()
                listSkins.add(skinId.toString())
                var updated = profile.copy(
                    coins = profile.coins - price,
                    unlockedSkinsCsv = listSkins.joinToString(",")
                )

                // Track if all skins unlocked now
                val allSkinsUnlocked = SkinRegistry.skins.all { skin ->
                    listSkins.contains(skin.id.toString()) || skin.unlockLevel <= profile.level
                }
                if (allSkinsUnlocked && updated.allSkinsUnlockedAtLevel == -1) {
                    updated = updated.copy(allSkinsUnlockedAtLevel = profile.level)
                }

                repository.updateProfile(updated)
            }
        }
    }

    fun selectSkin(skinId: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = profile.copy(selectedSkinId = skinId)
            repository.updateProfile(updated)
        }
    }

    fun skipCurrentLevelIAP() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            // Price to skip level is 50 coins or free via mock transaction flow
            val coinsNeeded = 50
            if (profile.coins >= coinsNeeded) {
                val oldLevel = profile.level
                val nextLevel = oldLevel + 1

                val activeSkins = profile.unlockedSkinsCsv.split(",").toMutableSet()
                for (skin in SkinRegistry.skins) {
                    if (skin.unlockLevel <= nextLevel) {
                        activeSkins.add(skin.id.toString())
                    }
                }

                var updated = profile.copy(
                    level = nextLevel,
                    coins = profile.coins - coinsNeeded,
                    unlockedSkinsCsv = activeSkins.joinToString(",")
                )

                val allSkinsUnlocked = SkinRegistry.skins.all { skin ->
                    activeSkins.contains(skin.id.toString()) || skin.unlockLevel <= nextLevel
                }
                if (allSkinsUnlocked && updated.allSkinsUnlockedAtLevel == -1) {
                    updated = updated.copy(allSkinsUnlockedAtLevel = nextLevel)
                }

                repository.updateProfile(updated)
            }
        }
    }

    fun unlockAllCosmeticIAP() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val allIds = SkinRegistry.skins.map { it.id.toString() }
            var updated = profile.copy(
                unlockedSkinsCsv = allIds.joinToString(",")
            )
            if (updated.allSkinsUnlockedAtLevel == -1) {
                updated = updated.copy(allSkinsUnlockedAtLevel = profile.level)
            }
            repository.updateProfile(updated)
        }
    }

    fun updateVolumes(music: Float, sound: Float) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = profile.copy(
                musicVolume = music,
                soundVolume = sound
            )
            repository.updateProfile(updated)
        }
    }

    fun renamePlayer(newName: String) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val cleanName = if (newName.trim().isEmpty()) "CondaPlayer" else newName.trim()
            val updated = profile.copy(name = cleanName)
            repository.updateProfile(updated)
        }
    }

    fun addVirtualCoins(coins: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = profile.copy(coins = profile.coins + coins)
            repository.updateProfile(updated)
        }
    }

    fun selectBackground(bgId: Int) {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val updated = profile.copy(selectedBackgroundId = bgId)
            repository.updateProfile(updated)
        }
    }

    fun verifyBackgroundUnlockTrigger() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val unlockedSkinIds = profile.unlockedSkinsCsv.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            val allSkinsUnlocked = SkinRegistry.skins.all { skin ->
                unlockedSkinIds.contains(skin.id) || skin.unlockLevel <= profile.level
            }
            if (allSkinsUnlocked && profile.allSkinsUnlockedAtLevel == -1) {
                val updated = profile.copy(allSkinsUnlockedAtLevel = profile.level)
                repository.updateProfile(updated)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
