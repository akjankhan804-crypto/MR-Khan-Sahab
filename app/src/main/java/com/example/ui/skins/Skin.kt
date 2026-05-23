package com.example.ui.skins

import androidx.compose.ui.graphics.Color

enum class SkinPattern {
    SOLID,
    STRIPED,
    SPOTTED,
    CHECKERED,
    GRADIENT,
    NEON_GLOW
}

data class AnacondaSkin(
    val id: Int,
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val pattern: SkinPattern,
    val unlockLevel: Int, // 1 means free or unlocked by default
    val coinPrice: Int = 80 // price to unlock instantly with coins
)

object SkinRegistry {
    val skins: List<AnacondaSkin> = listOf(
        // 3 Free skins
        AnacondaSkin(0, "Forest Python", Color(0xFF2E7D32), Color(0xFF1B5E20), SkinPattern.SOLID, 1),
        AnacondaSkin(1, "Sunset Adder", Color(0xFFE65100), Color(0xFFFFB74D), SkinPattern.STRIPED, 1),
        AnacondaSkin(2, "Aqua Viper", Color(0xFF006064), Color(0xFF00E5FF), SkinPattern.SPOTTED, 1),

        // 47 Locked skins (from level 4 to 142, unlocked one by one at +3 levels each)
        AnacondaSkin(3, "Golden Boa", Color(0xFFFFD700), Color(0xFFFF8C00), SkinPattern.NEON_GLOW, 4, 60),
        AnacondaSkin(4, "Amethyst Krait", Color(0xFF8E24AA), Color(0xFFBA68C8), SkinPattern.GRADIENT, 7, 65),
        AnacondaSkin(5, "Chameleon Coil", Color(0xFF4CAF50), Color(0xFFFFEB3B), SkinPattern.STRIPED, 10, 70),
        AnacondaSkin(6, "Carbon Python", Color(0xFF212121), Color(0xFF757575), SkinPattern.CHECKERED, 13, 75),
        AnacondaSkin(7, "Blaze Mamba", Color(0xFFD84315), Color(0xFFFF3D00), SkinPattern.NEON_GLOW, 16, 80),
        AnacondaSkin(8, "Deep Ocean", Color(0xFF0D47A1), Color(0xFF1976D2), SkinPattern.GRADIENT, 19, 85),
        AnacondaSkin(9, "Tiger Snake", Color(0xFFFF8F00), Color(0xFF37474F), SkinPattern.STRIPED, 22, 90),
        AnacondaSkin(10, "Crimson Scale", Color(0xFFC62828), Color(0xFFEF5350), SkinPattern.SPOTTED, 25, 95),
        AnacondaSkin(11, "Toxic Waste", Color(0xFF76FF03), Color(0xFF33691E), SkinPattern.NEON_GLOW, 28, 100),
        AnacondaSkin(12, "Cosmic Void", Color(0xFF1A237E), Color(0xFFE040FB), SkinPattern.GRADIENT, 31, 105),
        AnacondaSkin(13, "Bubblegum Ribbon", Color(0xFFEC407A), Color(0xFFF8BBD0), SkinPattern.STRIPED, 34, 110),
        AnacondaSkin(14, "Zebra Stripe", Color(0xFFFFFFFF), Color(0xFF000000), SkinPattern.CHECKERED, 37, 115),
        AnacondaSkin(15, "Jade Emperor", Color(0xFF00E676), Color(0xFF1B5E20), SkinPattern.GRADIENT, 40, 120),
        AnacondaSkin(16, "Radiant Sun", Color(0xFFFFEB3B), Color(0xFFFF9100), SkinPattern.NEON_GLOW, 43, 125),
        AnacondaSkin(17, "Ghost Snake", Color(0xFFECEFF1), Color(0xFFCFD8DC), SkinPattern.SPOTTED, 46, 130),
        AnacondaSkin(18, "Sand Weaver", Color(0xFFD7CCC8), Color(0xFF8D6E63), SkinPattern.STRIPED, 49, 135),
        AnacondaSkin(19, "Cherry Blossom", Color(0xFFF06292), Color(0xFFFCE4EC), SkinPattern.GRADIENT, 52, 140),
        AnacondaSkin(20, "Copperhead", Color(0xFFA1887F), Color(0xFF5D4037), SkinPattern.SOLID, 55, 145),
        AnacondaSkin(21, "Electric Storm", Color(0xFF00E5FF), Color(0xFF2979FF), SkinPattern.NEON_GLOW, 58, 150),
        AnacondaSkin(22, "Mint Coral", Color(0xFF69F0AE), Color(0xFF00B0FF), SkinPattern.SPOTTED, 61, 155),
        AnacondaSkin(23, "Royal Purple", Color(0xFF4A148C), Color(0xFFEA80FC), SkinPattern.GRADIENT, 64, 160),
        AnacondaSkin(24, "Matrix Grid", Color(0xFF00FF00), Color(0xFF000000), SkinPattern.CHECKERED, 67, 165),
        AnacondaSkin(25, "Infernal Serpent", Color(0xFFDD2C00), Color(0xFFFFAB00), SkinPattern.NEON_GLOW, 70, 170),
        AnacondaSkin(26, "Emerald Glitter", Color(0xFF00C853), Color(0xFFB9F6CA), SkinPattern.SPOTTED, 73, 175),
        AnacondaSkin(27, "Indigo Twilight", Color(0xFF3F51B5), Color(0xFFE91E63), SkinPattern.GRADIENT, 76, 180),
        AnacondaSkin(28, "Boreal Aurora", Color(0xFF00E676), Color(0xFFE040FB), SkinPattern.NEON_GLOW, 79, 185),
        AnacondaSkin(29, "Redline racer", Color(0xFFD50000), Color(0xFF212121), SkinPattern.STRIPED, 82, 190),
        AnacondaSkin(30, "Steel scale", Color(0xFF455A64), Color(0xFF90A4AE), SkinPattern.CHECKERED, 85, 195),
        AnacondaSkin(31, "Pink Panther", Color(0xFFFF4081), Color(0xFFF50057), SkinPattern.STRIPED, 88, 200),
        AnacondaSkin(32, "Ocean Tide", Color(0xFF03A9F4), Color(0xFF002244), SkinPattern.GRADIENT, 91, 205),
        AnacondaSkin(33, "Magma Core", Color(0xFFFF3D00), Color(0xFF3E2723), SkinPattern.CHECKERED, 94, 210),
        AnacondaSkin(34, "Hyper Neon", Color(0xFFF50057), Color(0xFF00E5FF), SkinPattern.NEON_GLOW, 97, 215),
        AnacondaSkin(35, "Platinum Slink", Color(0xFF9E9E9E), Color(0xFFF5F5F5), SkinPattern.GRADIENT, 100, 220),
        AnacondaSkin(36, "Supreme Overlord", Color(0xFFFFD700), Color(0xFFD50000), SkinPattern.NEON_GLOW, 103, 250),
        
        // 13 New unique exotic skins making 50 skins in total (each unlocks 3 levels after previous)
        AnacondaSkin(37, "Nebula Gazer", Color(0xFF4A148C), Color(0xFF00B0FF), SkinPattern.GRADIENT, 106, 255),
        AnacondaSkin(38, "Desert Cobra", Color(0xFFFFC107), Color(0xFF795548), SkinPattern.SPOTTED, 109, 260),
        AnacondaSkin(39, "Frost Wyrm", Color(0xFF80D8FF), Color(0xFFFFFFFF), SkinPattern.NEON_GLOW, 112, 265),
        AnacondaSkin(40, "Cyberpunk Glitch", Color(0xFFFF007F), Color(0xFF37474F), SkinPattern.CHECKERED, 115, 270),
        AnacondaSkin(41, "Pumpkin Feast", Color(0xFFFF6D00), Color(0xFF1B5E20), SkinPattern.STRIPED, 118, 275),
        AnacondaSkin(42, "Acidic Ooze", Color(0xFFAEEA00), Color(0xFF263238), SkinPattern.SOLID, 121, 280),
        AnacondaSkin(43, "Shadow Assassin", Color(0xFF000000), Color(0xFFD50000), SkinPattern.SOLID, 124, 285),
        AnacondaSkin(44, "Electric Lemon", Color(0xFFFFEA00), Color(0xFF00E5FF), SkinPattern.NEON_GLOW, 127, 290),
        AnacondaSkin(45, "Candy Cane", Color(0xFFFF1744), Color(0xFFFFFFFF), SkinPattern.STRIPED, 130, 295),
        AnacondaSkin(46, "Lavender Lustre", Color(0xFFD1C4E9), Color(0xFFF8BBD0), SkinPattern.GRADIENT, 133, 300),
        AnacondaSkin(47, "Bronze Sentinel", Color(0xFFCD7F32), Color(0xFF3E2723), SkinPattern.CHECKERED, 136, 305),
        AnacondaSkin(48, "Abyss Lurker", Color(0xFF0D47A1), Color(0xFF12005E), SkinPattern.SPOTTED, 139, 310),
        AnacondaSkin(49, "Phoenix Fire", Color(0xFFD50000), Color(0xFFFFAB00), SkinPattern.NEON_GLOW, 142, 320)
    )

    fun getSkinById(id: Int): AnacondaSkin {
        return skins.firstOrNull { it.id == id } ?: skins[0]
    }
}

data class BattleGroundStyle(
    val id: Int,
    val name: String,
    val backgroundColor: Color,
    val gridColor: Color
)

object BattleGroundRegistry {
    val styles: List<BattleGroundStyle> = listOf(
        BattleGroundStyle(0, "Emerald Jungle", Color(0xFF07140B), Color(0xFF11301B)),
        BattleGroundStyle(1, "Volcanic Caldera", Color(0xFF1B0707), Color(0xFF421111)),
        BattleGroundStyle(2, "Neon Cyber", Color(0xFF0C071A), Color(0xFF28134E)),
        BattleGroundStyle(3, "Glacial Abyss", Color(0xFF05171F), Color(0xFF143F52)),
        BattleGroundStyle(4, "Toxic Reservoir", Color(0xFF161B05), Color(0xFF3F4E12)),
        BattleGroundStyle(5, "Cosmic Sandbox", Color(0xFF1A1207), Color(0xFF4E3714)),
        BattleGroundStyle(6, "Carbon Hive", Color(0xFF111111), Color(0xFF2E2E2E)),
        BattleGroundStyle(7, "Royal Amethyst", Color(0xFF14071B), Color(0xFF3E134F)),
        BattleGroundStyle(8, "Desert Dunes", Color(0xFF1A1A0C), Color(0xFF4C4C24)),
        BattleGroundStyle(9, "Crimson Wasteland", Color(0xFF1E0A0A), Color(0xFF501C1C)),
        BattleGroundStyle(10, "Midnight Ocean", Color(0xFF040A18), Color(0xFF0E224E)),
        BattleGroundStyle(11, "Golden Temple", Color(0xFF1E1805), Color(0xFF544310)),
        BattleGroundStyle(12, "Steel Foundry", Color(0xFF1C1D1F), Color(0xFF45494E)),
        BattleGroundStyle(13, "Plague Swamplands", Color(0xFF0E140C), Color(0xFF2B3A25)),
        BattleGroundStyle(14, "Retro Gridwave", Color(0xFF0C020D), Color(0xFFEC407A).copy(alpha = 0.3f)),
        BattleGroundStyle(15, "Solar Flare", Color(0xFF240E05), Color(0xFF5C2B14)),
        BattleGroundStyle(16, "Static Static", Color(0xFF121416), Color(0xFF30353A)),
        BattleGroundStyle(17, "Sky High", Color(0xFF071720), Color(0xFF183D50)),
        BattleGroundStyle(18, "Copper Mines", Color(0xFF1B110B), Color(0xFF452B1B)),
        BattleGroundStyle(19, "Minty Fresh", Color(0xFF051B12), Color(0xFF114C35)),
        BattleGroundStyle(20, "Orchid Oasis", Color(0xFF1A0A1F), Color(0xFF4A1E59)),
        BattleGroundStyle(21, "Electric Shore", Color(0xFF051A1D), Color(0xFF144D55)),
        BattleGroundStyle(22, "Magma Cavern", Color(0xFF1F0B05), Color(0xFF521C11)),
        BattleGroundStyle(23, "Graphite Vault", Color(0xFF141517), Color(0xFF36393F)),
        BattleGroundStyle(24, "Clover Meadow", Color(0xFF071B0A), Color(0xFF144B1C))
    )

    fun getStyleById(id: Int): BattleGroundStyle {
        return styles.firstOrNull { it.id == id } ?: styles[0]
    }
}
