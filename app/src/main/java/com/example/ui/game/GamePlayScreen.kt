package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.skins.SkinRegistry
import com.example.ui.skins.BattleGroundRegistry
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GamePlayScreen(viewModel: GameViewModel) {
    val playerSnake by viewModel.playerSnake.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val pellets by viewModel.pellets.collectAsState()
    val powerUps by viewModel.powerUps.collectAsState()
    val battleState by viewModel.battleState.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val aliveBotCount by viewModel.aliveBotCount.collectAsState()
    val gamesMode by viewModel.gameMode.collectAsState()

    val selectedBgId = profile?.selectedBackgroundId ?: 0
    val bgStyle = remember(selectedBgId) { BattleGroundRegistry.getStyleById(selectedBgId) }

    // Screen dimension measurement parameters
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Joystick position calculations
    var joystickOffset by remember { mutableStateOf(Offset.Zero) }
    val maxDragRange = 100f

    // Live list of all snakes in room sorted by score for live leaderboard HUD
    val liveLeaderboard = remember(playerSnake, bots) {
        val all = mutableListOf<Snake>()
        playerSnake?.let { all.add(it) }
        all.addAll(bots)
        all.sortByDescending { it.score }
        all.take(5)
    }

    if (battleState == BattleState.MATCHING) {
        val activeBotsNum by viewModel.robotQuantity.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F2015), Color(0xFF070F0A)))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NeonGreen,
                        strokeWidth = 6.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Matching animation",
                        tint = BrightGold,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Text(
                    text = "ESTABLISHING MULTIPLAYER MATCH...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "🛡️ ONLINE ARENA STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGold,
                            letterSpacing = 1.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Host Server", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("Asia-Southeast (GP-5)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opponents Connected", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("$activeBotsNum Live Snakes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Connection Ping", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("42 ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }

                        LinearProgressIndicator(
                            color = NeonGreen,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .padding(top = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Player ID: #ID-${kotlin.math.abs((profile?.name ?: "AKHAN1834").hashCode() % 90000) + 10000}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgStyle.backgroundColor) // Selected custom battleground background
    ) {
        val player = playerSnake
        if (player != null) {
            // Camera calculations centered on Player's head segment segments[0]
            val pHead = player.segments.getOrNull(0) ?: Offset(1100f, 1100f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("game_combat_canvas")
                    .pointerInput(canvasSize) {
                        // fallback touch control anywhere outside joystick to help player turn
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                viewModel.updateJoystick(dragAmount)
                            },
                            onDragEnd = { viewModel.updateJoystick(Offset.Zero) }
                        )
                    }
            ) {
                canvasSize = size
                val centerViewport = Offset(size.width / 2, size.height / 2)
                val cameraOffset = centerViewport - pHead

                // 1. Draw Ground Grid Pattern
                val gridSize = 120f
                val startX = ((-cameraOffset.x / gridSize).toInt() * gridSize) - gridSize
                val endX = startX + size.width + (gridSize * 2)
                val startY = ((-cameraOffset.y / gridSize).toInt() * gridSize) - gridSize
                val endY = startY + size.height + (gridSize * 2)

                for (x in startX.toInt()..endX.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = bgStyle.gridColor,
                        start = Offset(x + cameraOffset.x, 0f),
                        end = Offset(x + cameraOffset.x, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in startY.toInt()..endY.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = bgStyle.gridColor,
                        start = Offset(0f, y + cameraOffset.y),
                        end = Offset(size.width, y + cameraOffset.y),
                        strokeWidth = 1f
                    )
                }

                // 2. Draw outer arena boundaries limit
                drawRect(
                    color = Color(0xFF00FF88).copy(alpha = 0.5f),
                    topLeft = Offset.Zero + cameraOffset,
                    size = Size(viewModel.arenaSize.x, viewModel.arenaSize.y),
                    style = Stroke(width = 8f)
                )

                // 3. Draw power-ups spawning on map
                for (pwr in powerUps) {
                    val pwrPosOnCanvas = pwr.position + cameraOffset
                    if (pwrPosOnCanvas.x >= -30f && pwrPosOnCanvas.x <= size.width + 30f &&
                        pwrPosOnCanvas.y >= -30f && pwrPosOnCanvas.y <= size.height + 30f) {
                        val pColor = when (pwr.type) {
                            "SPEED" -> Color.Red
                            "MAGNET" -> Color.White
                            "SHIELD" -> Color(0xFF2979FF)
                            "MULTIPLIER" -> Color(0xFF00E676)
                            else -> BrightGold
                        }

                        // draw power ring aura
                        drawCircle(pColor.copy(alpha = 0.25f), radius = 24f, center = pwrPosOnCanvas)
                        drawCircle(pColor, radius = 10f, center = pwrPosOnCanvas)
                        // tiny visual identifier text
                        // drawn as small inner shapes
                        drawRect(Color.White, pwrPosOnCanvas - Offset(4f, 4f), Size(8f, 8f))
                    }
                }

                // 4. Draw Map pellets (food)
                for (pellet in pellets) {
                    val pPosOnCanvas = pellet.position + cameraOffset
                    // Frustum culling for extreme rendering optimization on massive lists!
                    if (pPosOnCanvas.x >= -20f && pPosOnCanvas.x <= size.width + 20f &&
                        pPosOnCanvas.y >= -20f && pPosOnCanvas.y <= size.height + 20f) {

                        val baseRad = if (pellet.isBonus) 8f else 5.5f
                        val pulseFactor = 1.0f + 0.15f * sin((System.currentTimeMillis() / 150f) + pellet.id)
                        val finalRadius = baseRad * pulseFactor

                        drawCircle(
                            Color(pellet.colorHex).copy(alpha = 0.85f),
                            radius = finalRadius,
                            center = pPosOnCanvas
                        )
                        drawCircle(
                            Color.White.copy(alpha = 0.9f),
                            radius = finalRadius * 0.4f,
                            center = pPosOnCanvas - Offset(1.5f, 1.5f)
                        )
                    }
                }

                // 5. Draw Bots (body segments + eyes!)
                for (bot in bots) {
                    if (bot.isDead) continue
                    val botSkin = SkinRegistry.getSkinById(bot.skinId)
                    val sRadius = 14f

                    // Draw body tail to neck
                    for (i in bot.segments.indices.reversed()) {
                        val seg = bot.segments[i]
                        val segOnCanvas = seg + cameraOffset
                        if (segOnCanvas.x >= -32f && segOnCanvas.x <= size.width + 32f &&
                            segOnCanvas.y >= -32f && segOnCanvas.y <= size.height + 32f) {
                            drawSnakePart(botSkin.primaryColor, botSkin.secondaryColor, botSkin.pattern, sRadius, segOnCanvas)
                        }
                    }

                    // Draw head segment eyes to indicate direction
                    val bHead = bot.segments.getOrNull(0)
                    if (bHead != null) {
                        val headOnCanvas = bHead + cameraOffset
                        val angle = bot.angle

                        // Draw big predator pupils facing direction vector
                        val eyeOffsetLeft = Offset(cos(angle - 0.5f) * 11f, sin(angle - 0.5f) * 11f)
                        val eyeOffsetRight = Offset(cos(angle + 0.5f) * 11f, sin(angle + 0.5f) * 11f)

                        drawCircle(Color.White, radius = 5.5f, center = headOnCanvas + eyeOffsetLeft)
                        drawCircle(Color.White, radius = 5.5f, center = headOnCanvas + eyeOffsetRight)
                        drawCircle(Color.Black, radius = 2.5f, center = headOnCanvas + eyeOffsetLeft + Offset(cos(angle)*1.5f, sin(angle)*1.5f))
                        drawCircle(Color.Black, radius = 2.5f, center = headOnCanvas + eyeOffsetRight + Offset(cos(angle)*1.5f, sin(angle)*1.5f))
                    }
                }

                // 6. Draw Player (tail to front segments)
                val pSkin = SkinRegistry.getSkinById(player.skinId)
                val pRadius = 15f

                // Draw aura background if some powerups are active
                for (seg in player.segments) {
                    val segOnCanvas = seg + cameraOffset
                    if (player.shieldDurationMs > 0) {
                        drawCircle(Color(0xFF2979FF).copy(alpha = 0.15f), radius = pRadius * 1.6f, center = segOnCanvas)
                    }
                    if (player.magnetDurationMs > 0) {
                        drawCircle(Color.White.copy(alpha = 0.12f), radius = pRadius * 1.5f, center = segOnCanvas)
                    }
                    if (player.multiplierDurationMs > 0) {
                        drawCircle(Color(0xFF00E676).copy(alpha = 0.15f), radius = pRadius * 1.5f, center = segOnCanvas)
                    }
                }

                // Draw segments
                for (i in player.segments.indices.reversed()) {
                    val segOnCanvas = player.segments[i] + cameraOffset
                    drawSnakePart(pSkin.primaryColor, pSkin.secondaryColor, pSkin.pattern, pRadius, segOnCanvas)
                }

                // Draw big predator pupils on head segment
                val pHeadOnCanvas = pHead + cameraOffset
                val angle = player.angle
                val eyeOffsetLeft = Offset(cos(angle - 0.5f) * 11f, sin(angle - 0.5f) * 11f)
                val eyeOffsetRight = Offset(cos(angle + 0.5f) * 11f, sin(angle + 0.5f) * 11f)

                drawCircle(Color.White, radius = 6.5f, center = pHeadOnCanvas + eyeOffsetLeft)
                drawCircle(Color.White, radius = 6.5f, center = pHeadOnCanvas + eyeOffsetRight)
                drawCircle(Color.Red, radius = 3.2f, center = pHeadOnCanvas + eyeOffsetLeft + Offset(cos(angle)*1.8f, sin(angle)*1.8f))
                drawCircle(Color.Red, radius = 3.2f, center = pHeadOnCanvas + eyeOffsetRight + Offset(cos(angle)*1.8f, sin(angle)*1.8f))
            }
        }

        // --- HUD OVERLAYS ---

        // HUD: Score / Live targets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 45.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player score badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("LENGTH SCORE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                    Text(
                        "${player?.score ?: 0}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen
                    )
                    Text(
                        "Lvl ${profile?.level ?: 1} Round",
                        fontSize = 11.sp,
                        color = BrightGold,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Connected mode indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (gamesMode == "online") NeonGreen else Color.LightGray)
                        )
                        Text(
                            text = if (gamesMode == "online") "ONLINE • 42ms" else "ROBO OFFLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // In-Game Live competitive leader scoreboard (Top right)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.width(170.dp).testTag("live_score_hud")
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "🏆 ARENA RANKS",
                        fontSize = 11.sp,
                        color = BrightGold,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    liveLeaderboard.forEachIndexed { rank, snake ->
                        val isUser = snake.id == "player"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${rank + 1}. ${snake.name}",
                                fontSize = 10.sp,
                                maxLines = 1,
                                color = if (isUser) NeonGreen else Color.White,
                                fontWeight = if (isUser) FontWeight.Black else FontWeight.Normal,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = "${snake.score}",
                                fontSize = 10.sp,
                                color = if (isUser) NeonGreen else Color.LightGray,
                                fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Competitors Alive: $aliveBotCount",
                        fontSize = 9.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // HUD: Active Power-Ups details (timer indicators)
        val shDuration = playerSnake?.shieldDurationMs ?: 0L
        val magDuration = playerSnake?.magnetDurationMs ?: 0L
        val multDuration = playerSnake?.multiplierDurationMs ?: 0L

        if (shDuration > 0 || magDuration > 0 || multDuration > 0) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 150.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (shDuration > 0) {
                    PowerUpIndicator(label = "SHIELD", durationMs = shDuration, totalMs = 15000L, color = Color(0xFF2979FF))
                }
                if (magDuration > 0) {
                    PowerUpIndicator(label = "MAGNET", durationMs = magDuration, totalMs = 12000L, color = Color.White)
                }
                if (multDuration > 0) {
                    PowerUpIndicator(label = "2X MULTI", durationMs = multDuration, totalMs = 12000L, color = Color(0xFF00E676))
                }
            }
        }

        // --- CONTROLS HUD (Joystick & Speed up) ---
        // Placing these within the safe reach boundary zones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter)
        ) {
            // 1. Virtual Joystick (Left)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .align(Alignment.BottomStart)
                    .testTag("joystick_touch_region")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Update position offset clamping within circle
                                val rawOffset = joystickOffset + dragAmount
                                val rawDist = sqrt(rawOffset.x * rawOffset.x + rawOffset.y * rawOffset.y)
                                joystickOffset = if (rawDist > maxDragRange) {
                                    Offset(
                                        rawOffset.x / rawDist * maxDragRange,
                                        rawOffset.y / rawDist * maxDragRange
                                    )
                                } else {
                                    rawOffset
                                }
                                viewModel.updateJoystick(joystickOffset)
                            },
                            onDragEnd = {
                                joystickOffset = Offset.Zero
                                viewModel.updateJoystick(Offset.Zero)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer guiding lines
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // Joystick dragging inner knob knob
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                joystickOffset.x.dp.value.toInt(),
                                joystickOffset.y.dp.value.toInt()
                            )
                        }
                        .size(54.dp)
                        .clip(CircleShape)
                        .shadow(4.dp, CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NeonGreen, Color(0xFF0F361C))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .testTag("joystick_thumb_knob")
                )
            }

            // 2. Neon Speedup Boost Button (Right)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(BorderStroke(2.dp, Color.Red.copy(alpha = 0.4f)), CircleShape)
                    .align(Alignment.BottomEnd)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.setSpeedUp(true)
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    viewModel.setSpeedUp(false)
                                }
                            }
                        )
                    }
                    .testTag("speed_up_boost_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow, // play represents lightning-like arrow
                        contentDescription = "Speed Up Boost",
                        tint = Color.Red,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        "SPEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Red
                    )
                }
            }
        }

        // --- WINNER / OVERLORD / LOSER COMBAT STATE MODALS ---
        AnimatedVisibility(
            visible = (battleState == BattleState.WON || battleState == BattleState.LOST),
            enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = shrinkOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGrey),
                border = BorderStroke(2.dp, if (battleState == BattleState.WON) NeonGreen else Color.Red),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .testTag("outcome_overlay_panel")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (battleState == BattleState.WON) {
                        Text(
                            text = "👑 VICTORY 👑",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "THE WORST ANACONDA SURVIVED!\nYou devoured everyone and became the worst of all in this arena!",
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        val nextLevelSkin = (profile?.level ?: 1) + 1
                        val correspondingSkin = SkinRegistry.getSkinById(nextLevelSkin.coerceAtMost(SkinRegistry.skins.size - 1))

                        // Congrats rewards
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Gold coins unlocked limit", tint = BrightGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "+${60 + ((profile?.level ?: 1) * 10)} Coins Awarded!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrightGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Next Level unlocked: Level ${(profile?.level ?: 1) + 1}",
                                fontSize = 13.sp,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Unlocked Skin: ${correspondingSkin.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = correspondingSkin.primaryColor
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo("menu")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("collect_prizes_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("COLLECT REWARDS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JungleDark)
                        }

                    } else {
                        // LOSER OVERLAY!
                        Text(
                            text = "💀 DEFEATED 💀",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Red,
                            letterSpacing = 2.sp
                        )

                        // Core rule statement reminder
                        Text(
                            text = "Losers will NOT move on to the next round!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Your Anaconda crashed in this round. You can restart to try again, or buy immediate entrance card bypass to next Level using coins/Shop!",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.startGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("try_again_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("RETRY ROUND", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.navigateTo("menu")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("MENU HOUSE", fontSize = 13.sp, color = Color.White)
                            }
                        }

                        // Premium Level Unlock Bypass Button with Coins/Credit
                        val coins = profile?.coins ?: 0
                        Button(
                            onClick = {
                                if (coins >= 50) {
                                    viewModel.skipCurrentLevelIAP()
                                    viewModel.navigateTo("menu")
                                } else {
                                    viewModel.navigateTo("shop")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("skip_round_purchased_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Pass", tint = JungleDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (coins >= 50) "SKIP LEVEL (Spent 50 Coins)" else "BUY SKIP LEVEL (COIN SHOP)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = JungleDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PowerUpIndicator(label: String, durationMs: Long, totalMs: Long, color: Color) {
    val progress = durationMs.toFloat() / totalMs.toFloat()
    Row(
        modifier = Modifier
            .width(160.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.width(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.15f)
        )
    }
}
