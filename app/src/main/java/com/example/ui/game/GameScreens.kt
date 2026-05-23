package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.skins.AnacondaSkin
import com.example.ui.skins.SkinPattern
import com.example.ui.skins.SkinRegistry
import com.example.ui.skins.BattleGroundRegistry
import kotlin.math.*

// Custom Color Constants matching the Anaconda Jungle Neon vibe
val JungleDark = Color(0xFF0F2015)
val JungleGreen = Color(0xFF1B5E20)
val NeonGreen = Color(0xFF00E676)
val BrightGold = Color(0xFFFFD700)
val DarkGrey = Color(0xFF1E1E1E)
val LightGrey = Color(0xFFF5F5F5)

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val robotQuantity by viewModel.robotQuantity.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }

    // Synchronize local input state with profile name
    LaunchedEffect(profile) {
        profile?.let { nameInput = it.name }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JungleDark, Color(0xFF070F0A))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background particles representing glowing pellets
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF00E676).copy(alpha = 0.15f), radius = 120f, center = Offset(150f, 250f))
            drawCircle(Color(0xFFFFD700).copy(alpha = 0.12f), radius = 80f, center = Offset(size.width - 200f, 400f))
            drawCircle(Color(0xFFE040FB).copy(alpha = 0.1f), radius = 160f, center = Offset(250f, size.height - 350f))
            drawCircle(Color(0xFF00B0FF).copy(alpha = 0.15f), radius = 90f, center = Offset(size.width - 150f, size.height - 200f))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Title and Badge Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ID ANACONDA",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGreen,
                    letterSpacing = 3.sp,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = ".IO ARENA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightGold,
                    letterSpacing = 6.sp,
                    modifier = Modifier.offset(y = (-8).dp)
                )

                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "LAST SNAKE SURVIVING WINS!",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Player Profile Block with Player ID
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Unique Deterministic Player ID Display
                    val computedId = remember(profile?.name) {
                        val numeric = kotlin.math.abs((profile?.name ?: "AKHAN1834").hashCode() % 90000) + 10000
                        "#CONDA-$numeric"
                    }
                    
                    Surface(
                        color = BrightGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BrightGold))
                            Text(
                                text = "PLAYER ID: $computedId",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightGold
                            )
                        }
                    }

                    // Profile Name editing row
                    if (isEditingName) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Enter Your Name", color = NeonGreen) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.renamePlayer(nameInput)
                                    isEditingName = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save Name", tint = NeonGreen)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("name_input_field")
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = profile?.name ?: "M.AKhan1834",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = { isEditingName = true },
                                modifier = Modifier.size(36.dp).testTag("edit_name_button")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Level & Coin stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LEVEL", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            Text(
                                "Lvl ${profile?.level ?: 1}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }

                        VerticalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.height(30.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GOLD COINS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Coins", tint = BrightGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${profile?.coins ?: 0}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrightGold
                                )
                            }
                        }
                    }

                    // Current Selected Skin Preview
                    val selectedSkinId = profile?.selectedSkinId ?: 0
                    val currentSkin = SkinRegistry.getSkinById(selectedSkinId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Skin Selected: ",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = currentSkin.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentSkin.primaryColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(currentSkin.primaryColor)
                        )
                    }
                }
            }

            // ------------------ GAME MODE OPTION ------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🏆 SELECT GAME MODE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // OFFLINE ROBO MODE BUTTON
                        val isOffline = gameMode == "offline"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOffline) NeonGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = if (isOffline) 2.dp else 1.dp,
                                    color = if (isOffline) NeonGreen else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setGameMode("offline") }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ROBO MODE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isOffline) NeonGreen else Color.White)
                                Text("OFFLINE SINGLEPLAY", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // ONLINE MULTIPLAYER BUTTON
                        val isOnline = gameMode == "online"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOnline) BrightGold.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = if (isOnline) 2.dp else 1.dp,
                                    color = if (isOnline) BrightGold else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setGameMode("online") }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MULTIPLAYER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isOnline) BrightGold else Color.White)
                                Text("ONLINE SIMULATOR", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // ------------------ ROBOTS QUANTITY CHOICE ------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🤖 ARENA BOT QUANTITY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Text(
                            text = "$robotQuantity BOTS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = BrightGold
                        )
                    }

                    Text(
                        text = "Configure how many artificial intelligence bots challenge your territory:",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Minus Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (robotQuantity > 2) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .clickable(enabled = robotQuantity > 2) { viewModel.setRobotQuantity(robotQuantity - 1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        // Slider
                        Slider(
                            value = robotQuantity.toFloat(),
                            onValueChange = { viewModel.setRobotQuantity(it.roundToInt()) },
                            valueRange = 2f..25f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = BrightGold,
                                activeTrackColor = NeonGreen,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Right Plus Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (robotQuantity < 25) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .clickable(enabled = robotQuantity < 25) { viewModel.setRobotQuantity(robotQuantity + 1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ------------------ SEARCH OTHER PLAYERS ------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🔍 SEARCH COMPETITOR PROFILES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search player name (e.g. Viper, Squeeze)...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filtered list of mock players
                    val matchedPlayers = remember(searchQuery) {
                        listOf(
                            Triple("CONDA-8821", "NeonSerpent", 8),
                            Triple("CONDA-4412", "FierceViper", 12),
                            Triple("CONDA-9901", "SwampPython", 5),
                            Triple("CONDA-1044", "CoilCrusher", 15),
                            Triple("CONDA-3211", "BoaBoss", 9),
                            Triple("CONDA-5678", "SqueezeBoi", 3),
                            Triple("CONDA-7231", "GraveWinder", 11),
                            Triple("CONDA-8119", "KraitAttacker", 14)
                        ).filter { searchQuery.isEmpty() || it.second.contains(searchQuery, ignoreCase = true) }
                    }

                    if (matchedPlayers.isEmpty()) {
                        Text(
                            text = "No compatible players found.",
                            fontSize = 11.sp,
                            color = Color.LightGray.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            matchedPlayers.take(3).forEach { (id, name, level) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonGreen)
                                            )
                                        }
                                        Text("#$id • Level $level", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                    }

                                    Button(
                                        onClick = { 
                                            viewModel.updateSearchQuery(name)
                                            viewModel.startGame()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("CHALLENGE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = JungleDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Controls Information Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎮 INSTRUCTIONS & CONTROLS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightGold
                    )
                    Text(
                        text = "• JOYSTICK on the left guides your slithering anaconda in 360° directions.\n" +
                               "• SPEED BOOST on the right triples your speed. Drains speed-ups!\n" +
                               "• ELIMINATE smaller/larger competitors by steering them to crash head-to-body into you.\n" +
                               "• DEVOUR their remains payload. The LAST survivor becomes the winner. Losers do NOT move to next Level!",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }

            // Main Menu Buttons
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_game_button"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = JungleDark, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START BATTLE", fontSize = 18.sp, fontWeight = FontWeight.Black, color = JungleDark)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo("challenges") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("challenges_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Daily Challenges", tint = BrightGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DAILY", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.navigateTo("leaderboard") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("leaderboards_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Rankings", tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RANKS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo("settings") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("settings_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.navigateTo("shop") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("shop_tab_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shop Store", tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COIN SHOP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ------------------ OFFICIAL ARENA RULES ------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📋 OFFICIAL ARENA RULES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen,
                        letterSpacing = 0.5.sp
                    )
                    
                    Text(
                        text = "Follow all structural combat protocols inside the battlefield grid:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    val rules = listOf(
                        "RULE 1: NAME REGISTER" to "Every pilot enters with a verified handle. The default handle is registered as M.AKhan1834.",
                        "RULE 2: LEVEL UP REWARDS" to "Compete & survive. Every 3 levels won unlocks a brand-new, completely unique exotic skin.",
                        "RULE 3: SKIN ARSENAL" to "Amass up to 50 active Anaconda skins. Each skin features a highly distinctive custom aesthetic; no two skins are ever aligned or identical.",
                        "RULE 4: BATTLE FIELDS" to "Once all 50 skins are unlocked, the Battle Ground simulator unlocks. Gain access to 25 magnificent background styles, unlocked step-by-step with every 3 levels won.",
                        "RULE 5: COMBAT CONDUCT" to "Crash head-to-body into any competitor to vaporize them into high-value energy pellets. Avoid colliding into other snakes or boundary shield walls yourself."
                    )

                    rules.forEach { (title, desc) ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BrightGold
                            )
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    var soundVol by remember { mutableStateOf(0.8f) }
    var musicVol by remember { mutableStateOf(0.5f) }

    LaunchedEffect(profile) {
        profile?.let {
            soundVol = it.soundVolume
            musicVol = it.musicVolume
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JungleDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_panel")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    IconButton(onClick = { viewModel.navigateTo("menu") }) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Sound Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Sound Effects", tint = NeonGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SOUND EFFECTS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("${(soundVol * 100).toInt()}%", fontSize = 14.sp, color = NeonGreen)
                    }
                    Slider(
                        value = soundVol,
                        onValueChange = {
                            soundVol = it
                            viewModel.updateVolumes(musicVol, soundVol)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }

                // Music Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Music", tint = BrightGold, modifier = Modifier.size(19.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BACKGROUND MUSIC", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("${(musicVol * 100).toInt()}%", fontSize = 14.sp, color = BrightGold)
                    }
                    Slider(
                        value = musicVol,
                        onValueChange = {
                            musicVol = it
                            viewModel.updateVolumes(musicVol, soundVol)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = BrightGold,
                            activeTrackColor = BrightGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Under Settings, there is a GARAGE button
                Button(
                    onClick = { viewModel.navigateTo("garage") },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("garage_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Garage Change Skin", tint = JungleDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OPEN GARAGE", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = JungleDark)
                }
            }
        }
    }
}

@Composable
fun GarageScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val unlockedSkinIds = remember(profile?.unlockedSkinsCsv) {
        profile?.unlockedSkinsCsv?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(0, 1, 2)
    }
    val currentSelectedSkinId = profile?.selectedSkinId ?: 0
    val playerCoins = profile?.coins ?: 0
    val playerLevel = profile?.level ?: 1

    var activeTab by remember { mutableStateOf("skins") } // "skins" or "backgrounds"

    // Automatically check / sync background unlock criteria state
    LaunchedEffect(unlockedSkinIds) {
        viewModel.verifyBackgroundUnlockTrigger()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JungleDark)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ANACONDA GARAGE", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Cash", tint = BrightGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$playerCoins Coins",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGold
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Lvl $playerLevel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // Modern segment tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                // Skins Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == "skins") NeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (activeTab == "skins") 1.dp else 0.dp,
                            color = if (activeTab == "skins") NeonGreen else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeTab = "skins" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Skins (50)",
                        color = if (activeTab == "skins") NeonGreen else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Battle Ground Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == "backgrounds") BrightGold.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = if (activeTab == "backgrounds") 1.dp else 0.dp,
                            color = if (activeTab == "backgrounds") BrightGold else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { activeTab = "backgrounds" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Battle Ground (25)",
                        color = if (activeTab == "backgrounds") BrightGold else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (activeTab == "skins") {
                Text(
                    "Customize skins or bypass locked levels using coins instantly!",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                // Grid displaying all 50 Anaconda skins
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("skins_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(SkinRegistry.skins) { skin ->
                        val isUnlocked = unlockedSkinIds.contains(skin.id) || skin.unlockLevel <= playerLevel
                        val isSelected = currentSelectedSkinId == skin.id

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF133620) else Color.White.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                2.dp,
                                if (isSelected) NeonGreen else if (isUnlocked) Color.White.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isUnlocked) {
                                        viewModel.selectSkin(skin.id)
                                    }
                                }
                                .testTag("skin_item_${skin.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = skin.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                // Skin Visual representation demo
                                Canvas(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.2f))
                                ) {
                                    val radius = size.minDimension / 4
                                    val centerPt = center
                                    // draw a wavy segment line of the snake
                                    val pathPoints = listOf(
                                        Offset(centerPt.x - 22f, centerPt.y + 12f),
                                        Offset(centerPt.x - 10f, centerPt.y - 10f),
                                        Offset(centerPt.x + 10f, centerPt.y + 10f),
                                        Offset(centerPt.x + 22f, centerPt.y - 12f)
                                    )
                                    for (p in pathPoints) {
                                        when (skin.pattern) {
                                            SkinPattern.SOLID -> {
                                                drawCircle(skin.primaryColor, radius = radius, center = p)
                                            }
                                            SkinPattern.STRIPED -> {
                                                drawCircle(skin.primaryColor, radius = radius, center = p)
                                                drawCircle(skin.secondaryColor, radius = radius / 2, center = p)
                                            }
                                            SkinPattern.SPOTTED -> {
                                                drawCircle(skin.primaryColor, radius = radius, center = p)
                                                drawCircle(skin.secondaryColor, radius = radius / 3, center = p - Offset(4f, 4f))
                                                drawCircle(skin.secondaryColor, radius = radius / 3, center = p + Offset(4f, 4f))
                                            }
                                            SkinPattern.CHECKERED -> {
                                                drawCircle(skin.primaryColor, radius = radius, center = p)
                                                drawRect(skin.secondaryColor, Offset(p.x - 4f, p.y - 4f), Size(8f, 8f))
                                            }
                                            SkinPattern.GRADIENT, SkinPattern.NEON_GLOW -> {
                                                drawCircle(
                                                    Brush.radialGradient(listOf(skin.primaryColor, skin.secondaryColor)),
                                                    radius = radius,
                                                    center = p
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isUnlocked) {
                                    if (isSelected) {
                                        Text("SELECTED", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Black)
                                    } else {
                                        Text("TAP TO EQUIP", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                } else {
                                    // Locked skin buying button
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Unlocks Lvl ${skin.unlockLevel}",
                                            fontSize = 11.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                if (playerCoins >= skin.coinPrice) {
                                                    viewModel.buySkinsInstant(skin.id, skin.coinPrice)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("buy_skin_${skin.id}")
                                        ) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Buy", tint = JungleDark, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${skin.coinPrice} Coins", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = JungleDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Battlegrounds list screen
                val totalSkinsCount = SkinRegistry.skins.size
                val unlockedSkinsCount = remember(unlockedSkinIds, playerLevel) {
                    SkinRegistry.skins.count { skin -> unlockedSkinIds.contains(skin.id) || skin.unlockLevel <= playerLevel }
                }
                val allSkinsUnlocked = unlockedSkinsCount >= totalSkinsCount

                if (!allSkinsUnlocked) {
                    // Show block banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "🔒 BATTLEFIELD CUSTOMIZATION LOCKED",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Red
                            )
                            Text(
                                "You must completely unlock all 50 unique skins first to activate the Battle Ground simulator option!",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Skin weapon progress
                            Text(
                                text = "Skins Unlocked: $unlockedSkinsCount / $totalSkinsCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightGold
                            )

                            LinearProgressIndicator(
                                progress = { unlockedSkinsCount.toFloat() / totalSkinsCount.toFloat() },
                                color = BrightGold,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                        }
                    }
                } else {
                    // Battleground customizing grid list
                    val baseUnlockLevel = if ((profile?.allSkinsUnlockedAtLevel ?: -1) > 0) profile!!.allSkinsUnlockedAtLevel else 142
                    
                    Text(
                        text = "Customize the Battle Ground arena style! Access 25 distinct themes, unlocked one by one for every 3 levels won after harnessing all 50 skins.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("backgrounds_grid"),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(BattleGroundRegistry.styles) { bg ->
                            val reqLevel = baseUnlockLevel + (bg.id + 1) * 3
                            val isBgUnlocked = playerLevel >= reqLevel
                            val isBgSelected = (profile?.selectedBackgroundId ?: 0) == bg.id

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isBgSelected) Color(0xFF1B3024) else Color.White.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(
                                    width = if (isBgSelected) 2.dp else 1.dp,
                                    color = if (isBgSelected) NeonGreen else if (isBgUnlocked) Color.White.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isBgUnlocked) {
                                        viewModel.selectBackground(bg.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Mini-palette canvas preview
                                    Canvas(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bg.backgroundColor)
                                            .border(1.dp, bg.gridColor, RoundedCornerShape(10.dp))
                                    ) {
                                        // Draw miniature coordinates gridlines
                                        drawLine(bg.gridColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), 2f)
                                        drawLine(bg.gridColor, Offset(size.width * 2 / 3f, 0f), Offset(size.width * 2 / 3f, size.height), 2f)
                                        drawLine(bg.gridColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), 2f)
                                        drawLine(bg.gridColor, Offset(0f, size.height * 2 / 3f), Offset(size.width, size.height * 2 / 3f), 2f)
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${bg.id + 1}. ${bg.name}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isBgUnlocked) Color.White else Color.White.copy(alpha = 0.4f)
                                        )

                                        if (isBgUnlocked) {
                                            Text(
                                                text = if (isBgSelected) "ACTIVE IN ARENA" else "UNLOCKED • TAP TO EQUIP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isBgSelected) NeonGreen else Color.LightGray
                                            )
                                        } else {
                                            val remaining = reqLevel - playerLevel
                                            Text(
                                                text = "LOCKED • Unlocks Lvl $reqLevel (win $remaining more level${if (remaining > 1) "s" else ""})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    if (isBgUnlocked) {
                                        RadioButton(
                                            selected = isBgSelected,
                                            onClick = { viewModel.selectBackground(bg.id) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = NeonGreen,
                                                unselectedColor = Color.LightGray
                                            )
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked style",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val leaderboard by viewModel.leaderboard.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JungleDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxSize().testTag("leaderboards_panel")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GLOBAL LEADERS", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    IconButton(onClick = { viewModel.navigateTo("menu") }) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                if (leaderboard.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No Rank Records found yet!", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        leaderboard.forEachIndexed { idx, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (idx == 0) Color(0xFF26502E) else Color.White.copy(alpha = 0.03f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "#${idx + 1}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx == 0) BrightGold else Color.White,
                                        modifier = Modifier.width(36.dp)
                                    )
                                    Text(
                                        entry.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    "${entry.score} pts (Lvl ${entry.level})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengesScreen(viewModel: GameViewModel) {
    val dailyChallenges by viewModel.dailyChallenges.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JungleDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().testTag("challenges_panel")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DAILY CHALLENGES", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    IconButton(onClick = { viewModel.navigateTo("menu") }) {
                        Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                dailyChallenges.forEach { challenge ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (challenge.completed) Color(0xFF0F2B18) else Color.White.copy(alpha = 0.03f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (challenge.completed) NeonGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    challenge.description,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(0.8f)
                                )
                                if (challenge.completed) {
                                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = NeonGreen)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = "Prize", tint = BrightGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("+${challenge.rewardCoins}", fontSize = 12.sp, color = BrightGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom progress meter
                            val progress = if (challenge.completed) 1f else challenge.currentValue.toFloat() / challenge.targetValue.toFloat()
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = NeonGreen,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "${challenge.currentValue} / ${challenge.targetValue}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopScreen(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val playerLevel = profile?.level ?: 1
    val playerCoins = profile?.coins ?: 0

    // For confirming visual mock purchase success dialog
    var modalTrigger by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JungleDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("COIN & CORRUPT SHOP", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Cash", tint = BrightGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$playerCoins Gold Coins",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrightGold
                        )
                    }
                }
                IconButton(onClick = { viewModel.navigateTo("menu") }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Text(
                "Need more cosmetics or want to skip tough levels? Unlock elite status instantly with visual premium mock IAP packages!",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // IAP Package 1: Coins Bag
                ShopItemRow(
                    title = "Pile of 100 Gold Coins",
                    desc = "Standard stack of golden gems.",
                    priceTag = "$0.99 USD",
                    badgeText = "POPULAR",
                    badgeColor = BrightGold,
                    onClick = {
                        viewModel.addVirtualCoins(100)
                        modalTrigger = "100 Gold Coins successfully credited!"
                    }
                )

                // IAP Package 2: Golden Trunk Coins
                ShopItemRow(
                    title = "Treasure Chest of 700 Coins",
                    desc = "Massive payload for immediate Garage Shopping.",
                    priceTag = "$3.99 USD",
                    badgeText = "BEST BUY",
                    badgeColor = NeonGreen,
                    onClick = {
                        viewModel.addVirtualCoins(700)
                        modalTrigger = "700 Gold Coins successfully credited to inventory!"
                    }
                )

                // IAP Package 3: Skip Current Level Bypass
                ShopItemRow(
                    title = "Skip Level Instant Card",
                    desc = "Instantly skips to the next Level, unlocks the corresponding level skin, and gives level bonus!",
                    priceTag = "50 Coins OR $0.49 USD",
                    badgeText = "FAST TRACK",
                    badgeColor = Color.Red,
                    onClick = {
                        if (playerCoins >= 50) {
                            viewModel.skipCurrentLevelIAP()
                            modalTrigger = "Level Skip successfully activated! You moved up to level ${playerLevel + 1}"
                        } else {
                            modalTrigger = "Not enough coins! Buy additional coins or unlock visually."
                        }
                    }
                )

                // IAP Package 4: Master Unlock All skins
                ShopItemRow(
                    title = "VIP All-Skins Unlock Token",
                    desc = "Bypass all level lockers. Get the entire garage of 37 Anaconda Skins active immediately!",
                    priceTag = "$4.99 USD",
                    badgeText = "ULTIMATE DELUXE",
                    badgeColor = Color(0xFFD500F9),
                    onClick = {
                        viewModel.unlockAllCosmeticIAP()
                        modalTrigger = "VIP Premium Unlock SUCCESS! All 37 Skins are now unlocked in your garage!"
                    }
                )
            }
        }

        // Show mock confirmation dialog
        modalTrigger?.let { msg ->
            AlertDialog(
                onDismissRequest = { modalTrigger = null },
                confirmButton = {
                    TextButton(onClick = { modalTrigger = null }) {
                        Text("AWESOME", color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("MOCK IN-APP PURCHASE", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text(msg, color = Color.White.copy(alpha = 0.9f)) },
                containerColor = Color(0xFF1E2F23),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ShopItemRow(
    title: String,
    desc: String,
    priceTag: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = JungleDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(priceTag, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = JungleDark)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = desc,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )
        }
    }
}

// Draw custom beautiful snake skin patterns on draw scopes
fun DrawScope.drawSnakePart(color1: Color, color2: Color, pattern: SkinPattern, sizeRadius: Float, point: Offset) {
    when (pattern) {
        SkinPattern.SOLID -> {
            drawCircle(color1, radius = sizeRadius, center = point)
        }
        SkinPattern.STRIPED -> {
            drawCircle(color1, radius = sizeRadius, center = point)
            drawCircle(color2, radius = sizeRadius * 0.5f, center = point)
        }
        SkinPattern.SPOTTED -> {
            drawCircle(color1, radius = sizeRadius, center = point)
            // draw tiny leopard spot/dots
            drawCircle(color2, radius = sizeRadius * 0.25f, center = point - Offset(sizeRadius * 0.35f, sizeRadius * 0.35f))
            drawCircle(color2, radius = sizeRadius * 0.25f, center = point + Offset(sizeRadius * 0.35f, sizeRadius * 0.35f))
        }
        SkinPattern.CHECKERED -> {
            drawCircle(color1, radius = sizeRadius, center = point)
            drawRect(color2, point - Offset(sizeRadius * 0.4f, sizeRadius * 0.4f), Size(sizeRadius * 0.8f, sizeRadius * 0.8f))
        }
        SkinPattern.GRADIENT, SkinPattern.NEON_GLOW -> {
            drawCircle(
                Brush.radialGradient(listOf(color1, color2)),
                radius = sizeRadius,
                center = point
            )
            if (pattern == SkinPattern.NEON_GLOW) {
                drawCircle(
                    color1,
                    radius = sizeRadius * 1.25f,
                    center = point,
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}
