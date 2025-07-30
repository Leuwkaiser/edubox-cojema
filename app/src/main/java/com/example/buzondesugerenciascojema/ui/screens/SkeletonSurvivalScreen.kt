package com.example.buzondesugerenciascojema.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Divider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.util.SoundGenerator
import com.example.buzondesugerenciascojema.util.MusicPlayer
import com.example.buzondesugerenciascojema.data.RankingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import com.example.buzondesugerenciascojema.data.RankingEntry

// Constantes del juego
const val GAME_AREA_SIZE = 400
const val PLAYER_SIZE = 20
const val SKELETON_SIZE = 16
const val BULLET_SIZE = 4
const val MELEE_RANGE = 30
const val INITIAL_PLAYER_SPEED = 3f
const val INITIAL_SKELETON_SPEED = 1.5f

// Colores del juego y paisaje
val GRASS_COLOR = Color(0xFF4CAF50)
val GRASS_DARK_COLOR = Color(0xFF388E3C)
val DIRT_COLOR = Color(0xFF8D6E63)
val DIRT_LIGHT_COLOR = Color(0xFFBCAAA4)
val SKELETON_SURVIVAL_PLAYER_COLOR = Color(0xFF2196F3)
val SKELETON_COLOR = Color(0xFFF5F5F5)
val BLOOD_COLOR = Color(0xFFD32F2F)
val SKELETON_SURVIVAL_BULLET_COLOR = Color(0xFFFFEB3B)
val MELEE_COLOR = Color(0xFF9C27B0)

// Colores del paisaje
val TREE_TRUNK_COLOR = Color(0xFF5D4037)
val TREE_LEAVES_COLOR = Color(0xFF2E7D32)
val TREE_LEAVES_LIGHT_COLOR = Color(0xFF4CAF50)
val ROCK_COLOR = Color(0xFF757575)
val ROCK_DARK_COLOR = Color(0xFF424242)
val FLOWER_COLOR = Color(0xFFE91E63)
val FLOWER_YELLOW_COLOR = Color(0xFFFFEB3B)
val FLOWER_PURPLE_COLOR = Color(0xFF9C27B0)
val WATER_COLOR = Color(0xFF2196F3)
val WATER_DARK_COLOR = Color(0xFF1976D2)
val SKY_COLOR = Color(0xFF87CEEB)
val CLOUD_COLOR = Color(0xFFFFFFFF)

// Tipos de armas
enum class WeaponType {
    MACHETE, PISTOL, SHOTGUN, RIFLE, UZI, ROCKET_LAUNCHER, LASER_GUN, FLAMETHROWER,
    SNIPER_RIFLE, MINIGUN, PLASMA_CANNON, ELECTRIC_STAFF, ICE_BLASTER, POISON_DART,
    GRAVITY_GUN, TIME_WARP, NUCLEAR_LAUNCHER, DRAGON_BREATH, PHANTOM_BLADE
}

data class Weapon(
    val type: WeaponType,
    val name: String,
    val damage: Int,
    val range: Float,
    val fireRate: Long,
    val isMelee: Boolean,
    val bulletCount: Int = 1,
    val spread: Float = 0f,
    val autoFire: Boolean = false,
    val specialEffect: String = ""
)

data class Player(
    var x: Float = GAME_AREA_SIZE / 2f,
    var y: Float = GAME_AREA_SIZE / 2f,
    var health: Int = 100,
    var maxHealth: Int = 100,
    var speed: Float = INITIAL_PLAYER_SPEED,
    var currentWeapon: Weapon = Weapon(
        type = WeaponType.MACHETE,
        name = "Machete",
        damage = 25,
        range = MELEE_RANGE.toFloat(),
        fireRate = 500,
        isMelee = true,
        autoFire = true
    ),
    var weapons: MutableList<Weapon> = mutableListOf(),
    var lastShot: Long = 0,
    var joystickX: Float = 0f,
    var joystickY: Float = 0f,
    var isMoving: Boolean = false
)

data class Skeleton(
    var x: Float,
    var y: Float,
    var health: Int = 30,
    var maxHealth: Int = 30,
    var speed: Float = INITIAL_SKELETON_SPEED,
    var angle: Float = 0f,
    var isDead: Boolean = false,
    var deathAnimation: Float = 1f,
    var skeletonType: SkeletonType = SkeletonType.NORMAL,
    var attackCooldown: Long = 0,
    var lastAttack: Long = 0
)

enum class SkeletonType {
    NORMAL, ARCHER, MAGE, TANK, BOSS
}

data class Boss(
    var x: Float,
    var y: Float,
    var health: Int = 500,
    var maxHealth: Int = 500,
    var speed: Float = 2f,
    var angle: Float = 0f,
    var isDead: Boolean = false,
    var bossType: BossType = BossType.SKELETON_LORD,
    var attackCooldown: Long = 0,
    var lastAttack: Long = 0,
    var specialAttackCooldown: Long = 0,
    var lastSpecialAttack: Long = 0,
    var phase: Int = 1
)

enum class BossType {
    SKELETON_LORD, NECROMANCER, BONE_GOLEM, DEATH_KNIGHT, SHADOW_EMPEROR
}

data class SkeletonSurvivalBullet(
    var x: Float,
    var y: Float,
    var angle: Float,
    var speed: Float = 8f,
    var damage: Int = 20,
    var isPlayerBullet: Boolean = true
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1f,
    var color: Color = BLOOD_COLOR
)

// Elementos del paisaje
data class Tree(
    val x: Float,
    val y: Float,
    val size: Float,
    val type: TreeType = TreeType.OAK
)

data class Rock(
    val x: Float,
    val y: Float,
    val size: Float,
    val type: RockType = RockType.SMALL
)

data class Flower(
    val x: Float,
    val y: Float,
    val type: FlowerType = FlowerType.RED
)

data class Water(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class Cloud(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float = 0.5f
)

enum class TreeType {
    OAK, PINE, MAPLE, PALM
}

enum class RockType {
    SMALL, MEDIUM, LARGE
}

enum class FlowerType {
    RED, YELLOW, PURPLE, WHITE
}

data class SkeletonSurvivalGameState(
    val player: Player = Player(),
    val skeletons: MutableList<Skeleton> = mutableListOf(),
    val bosses: MutableList<Boss> = mutableListOf(),
    val bullets: MutableList<SkeletonSurvivalBullet> = mutableListOf(),
    val particles: MutableList<Particle> = mutableListOf(),
    val trees: List<Tree> = generateTrees(),
    val rocks: List<Rock> = generateRocks(),
    val flowers: List<Flower> = generateFlowers(),
    val water: List<Water> = generateWater(),
    val clouds: MutableList<Cloud> = generateClouds().toMutableList(),
    val round: Int = 1,
    val score: Int = 0,
    val skeletonsKilled: Int = 0,
    val bossesKilled: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val showUpgradeMenu: Boolean = false,
    val availableUpgrades: List<Weapon> = emptyList(),
    val autoFireEnabled: Boolean = true
)

// Armas disponibles
val WEAPONS = mapOf(
    WeaponType.MACHETE to Weapon(
        type = WeaponType.MACHETE,
        name = "Machete",
        damage = 25,
        range = MELEE_RANGE.toFloat(),
        fireRate = 500,
        isMelee = true,
        autoFire = true,
        specialEffect = "Corte rápido"
    ),
    WeaponType.PISTOL to Weapon(
        type = WeaponType.PISTOL,
        name = "Pistola",
        damage = 35,
        range = 200f,
        fireRate = 400,
        isMelee = false,
        autoFire = true
    ),
    WeaponType.SHOTGUN to Weapon(
        type = WeaponType.SHOTGUN,
        name = "Escopeta",
        damage = 60,
        range = 150f,
        fireRate = 800,
        isMelee = false,
        bulletCount = 5,
        spread = 30f,
        autoFire = true
    ),
    WeaponType.RIFLE to Weapon(
        type = WeaponType.RIFLE,
        name = "Rifle",
        damage = 45,
        range = 300f,
        fireRate = 200,
        isMelee = false,
        autoFire = true
    ),
    WeaponType.UZI to Weapon(
        type = WeaponType.UZI,
        name = "UZI",
        damage = 20,
        range = 180f,
        fireRate = 100,
        isMelee = false,
        autoFire = true
    ),
    WeaponType.ROCKET_LAUNCHER to Weapon(
        type = WeaponType.ROCKET_LAUNCHER,
        name = "Lanzacohetes",
        damage = 150,
        range = 250f,
        fireRate = 1500,
        isMelee = false,
        autoFire = true,
        specialEffect = "Explosión"
    ),
    WeaponType.LASER_GUN to Weapon(
        type = WeaponType.LASER_GUN,
        name = "Láser",
        damage = 40,
        range = 400f,
        fireRate = 150,
        isMelee = false,
        autoFire = true,
        specialEffect = "Penetración"
    ),
    WeaponType.FLAMETHROWER to Weapon(
        type = WeaponType.FLAMETHROWER,
        name = "Lanzallamas",
        damage = 30,
        range = 120f,
        fireRate = 50,
        isMelee = false,
        bulletCount = 3,
        spread = 20f,
        autoFire = true,
        specialEffect = "Quemadura"
    ),
    WeaponType.SNIPER_RIFLE to Weapon(
        type = WeaponType.SNIPER_RIFLE,
        name = "Rifle de Precisión",
        damage = 120,
        range = 600f,
        fireRate = 1200,
        isMelee = false,
        autoFire = true,
        specialEffect = "Crítico"
    ),
    WeaponType.MINIGUN to Weapon(
        type = WeaponType.MINIGUN,
        name = "Minigun",
        damage = 15,
        range = 250f,
        fireRate = 50,
        isMelee = false,
        autoFire = true,
        specialEffect = "Barrido"
    ),
    WeaponType.PLASMA_CANNON to Weapon(
        type = WeaponType.PLASMA_CANNON,
        name = "Cañón de Plasma",
        damage = 80,
        range = 350f,
        fireRate = 600,
        isMelee = false,
        autoFire = true,
        specialEffect = "Plasma"
    ),
    WeaponType.ELECTRIC_STAFF to Weapon(
        type = WeaponType.ELECTRIC_STAFF,
        name = "Bastón Eléctrico",
        damage = 45,
        range = MELEE_RANGE.toFloat() * 1.5f,
        fireRate = 300,
        isMelee = true,
        autoFire = true,
        specialEffect = "Eléctrico"
    ),
    WeaponType.ICE_BLASTER to Weapon(
        type = WeaponType.ICE_BLASTER,
        name = "Lanzahielo",
        damage = 35,
        range = 200f,
        fireRate = 400,
        isMelee = false,
        bulletCount = 1,
        autoFire = true,
        specialEffect = "Congelación"
    ),
    WeaponType.POISON_DART to Weapon(
        type = WeaponType.POISON_DART,
        name = "Dardo Venenoso",
        damage = 25,
        range = 300f,
        fireRate = 300,
        isMelee = false,
        autoFire = true,
        specialEffect = "Veneno"
    ),
    WeaponType.GRAVITY_GUN to Weapon(
        type = WeaponType.GRAVITY_GUN,
        name = "Pistola de Gravedad",
        damage = 50,
        range = 180f,
        fireRate = 800,
        isMelee = false,
        autoFire = true,
        specialEffect = "Gravedad"
    ),
    WeaponType.TIME_WARP to Weapon(
        type = WeaponType.TIME_WARP,
        name = "Distorsión Temporal",
        damage = 40,
        range = 150f,
        fireRate = 1000,
        isMelee = false,
        autoFire = true,
        specialEffect = "Ralentizar"
    ),
    WeaponType.NUCLEAR_LAUNCHER to Weapon(
        type = WeaponType.NUCLEAR_LAUNCHER,
        name = "Lanzador Nuclear",
        damage = 300,
        range = 400f,
        fireRate = 3000,
        isMelee = false,
        autoFire = true,
        specialEffect = "Nuclear"
    ),
    WeaponType.DRAGON_BREATH to Weapon(
        type = WeaponType.DRAGON_BREATH,
        name = "Aliento de Dragón",
        damage = 70,
        range = 200f,
        fireRate = 200,
        isMelee = false,
        bulletCount = 5,
        spread = 25f,
        autoFire = true,
        specialEffect = "Fuego"
    ),
    WeaponType.PHANTOM_BLADE to Weapon(
        type = WeaponType.PHANTOM_BLADE,
        name = "Espada Fantasma",
        damage = 60,
        range = MELEE_RANGE.toFloat() * 2f,
        fireRate = 200,
        isMelee = true,
        autoFire = true,
        specialEffect = "Fantasmal"
    )
)

// Funciones auxiliares del juego
fun spawnSkeletons(gameState: SkeletonSurvivalGameState, count: Int) {
    repeat(count) {
        val side = (Math.random() * 4).toInt()
        val x = when (side) {
            0 -> -SKELETON_SIZE.toFloat() // Izquierda
            1 -> GAME_AREA_SIZE.toFloat() // Derecha
            2 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Arriba
            else -> (Math.random() * GAME_AREA_SIZE).toFloat() // Abajo
        }
        val y = when (side) {
            0 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Izquierda
            1 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Derecha
            2 -> -SKELETON_SIZE.toFloat() // Arriba
            else -> GAME_AREA_SIZE.toFloat() // Abajo
        }
        
        val skeletonType = when {
            gameState.round > 20 -> SkeletonType.TANK
            gameState.round > 15 -> SkeletonType.MAGE
            gameState.round > 10 -> SkeletonType.ARCHER
            else -> SkeletonType.NORMAL
        }
        
        gameState.skeletons.add(
            Skeleton(
                x = x,
                y = y,
                health = when (skeletonType) {
                    SkeletonType.NORMAL -> 30
                    SkeletonType.ARCHER -> 40
                    SkeletonType.MAGE -> 50
                    SkeletonType.TANK -> 80
                    SkeletonType.BOSS -> 100
                },
                maxHealth = when (skeletonType) {
                    SkeletonType.NORMAL -> 30
                    SkeletonType.ARCHER -> 40
                    SkeletonType.MAGE -> 50
                    SkeletonType.TANK -> 80
                    SkeletonType.BOSS -> 100
                },
                speed = INITIAL_SKELETON_SPEED + (gameState.round - 1) * 0.2f,
                skeletonType = skeletonType
            )
        )
    }
}

fun spawnBoss(gameState: SkeletonSurvivalGameState, bossLevel: Int) {
    val bossType = when (bossLevel) {
        1 -> BossType.SKELETON_LORD
        2 -> BossType.NECROMANCER
        3 -> BossType.BONE_GOLEM
        4 -> BossType.DEATH_KNIGHT
        else -> BossType.SHADOW_EMPEROR
    }
    
    val side = (Math.random() * 4).toInt()
    val x = when (side) {
        0 -> -50f // Izquierda
        1 -> GAME_AREA_SIZE + 50f // Derecha
        2 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Arriba
        else -> (Math.random() * GAME_AREA_SIZE).toFloat() // Abajo
    }
    val y = when (side) {
        0 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Izquierda
        1 -> (Math.random() * GAME_AREA_SIZE).toFloat() // Derecha
        2 -> -50f // Arriba
        else -> GAME_AREA_SIZE + 50f // Abajo
    }
    
    gameState.bosses.add(
        Boss(
            x = x,
            y = y,
            health = 500 + (bossLevel - 1) * 200,
            maxHealth = 500 + (bossLevel - 1) * 200,
            speed = 2f + bossLevel * 0.5f,
            bossType = bossType
        )
    )
}

fun getAvailableUpgrades(currentWeapons: List<Weapon>): List<Weapon> {
    val availableWeapons = WEAPONS.values.filter { weapon ->
        currentWeapons.none { it.type == weapon.type }
    }
    
    return availableWeapons.take(3) // Mostrar 3 opciones
}

fun createDeathParticles(gameState: SkeletonSurvivalGameState, x: Float, y: Float, isBoss: Boolean = false) {
    val particleCount = if (isBoss) 15 else 8
    repeat(particleCount) {
        gameState.particles.add(
            Particle(
                x = x + ((Math.random() - 0.5) * 30).toFloat(),
                y = y + ((Math.random() - 0.5) * 30).toFloat(),
                vx = ((Math.random() - 0.5) * 150).toFloat(),
                vy = ((Math.random() - 0.5) * 150).toFloat(),
                color = if (isBoss) Color(0xFFFFD700) else BLOOD_COLOR
            )
        )
    }
}

fun findNearestEnemy(gameState: SkeletonSurvivalGameState): Pair<Float, Float>? {
    var nearestDistance = Float.MAX_VALUE
    var nearestEnemy: Pair<Float, Float>? = null
    
    // Buscar en esqueletos
    gameState.skeletons.forEach { skeleton ->
        if (!skeleton.isDead) {
            val distance = sqrt(
                (gameState.player.x - skeleton.x).pow(2) + 
                (gameState.player.y - skeleton.y).pow(2)
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestEnemy = Pair(skeleton.x, skeleton.y)
            }
        }
    }
    
    // Buscar en jefes
    gameState.bosses.forEach { boss ->
        if (!boss.isDead) {
            val distance = sqrt(
                (gameState.player.x - boss.x).pow(2) + 
                (gameState.player.y - boss.y).pow(2)
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestEnemy = Pair(boss.x, boss.y)
            }
        }
    }
    
    return nearestEnemy
}

// Funciones para generar el paisaje
fun generateTrees(): List<Tree> {
    val trees = mutableListOf<Tree>()
    repeat(15) {
        trees.add(
            Tree(
                x = (Math.random() * GAME_AREA_SIZE).toFloat(),
                y = (Math.random() * GAME_AREA_SIZE).toFloat(),
                size = (20 + Math.random() * 30).toFloat(),
                type = TreeType.values()[(Math.random() * TreeType.values().size).toInt()]
            )
        )
    }
    return trees
}

fun generateRocks(): List<Rock> {
    val rocks = mutableListOf<Rock>()
    repeat(8) {
        rocks.add(
            Rock(
                x = (Math.random() * GAME_AREA_SIZE).toFloat(),
                y = (Math.random() * GAME_AREA_SIZE).toFloat(),
                size = (8 + Math.random() * 15).toFloat(),
                type = RockType.values()[(Math.random() * RockType.values().size).toInt()]
            )
        )
    }
    return rocks
}

fun generateFlowers(): List<Flower> {
    val flowers = mutableListOf<Flower>()
    repeat(25) {
        flowers.add(
            Flower(
                x = (Math.random() * GAME_AREA_SIZE).toFloat(),
                y = (Math.random() * GAME_AREA_SIZE).toFloat(),
                type = FlowerType.values()[(Math.random() * FlowerType.values().size).toInt()]
            )
        )
    }
    return flowers
}

fun generateWater(): List<Water> {
    val water = mutableListOf<Water>()
    // Lago principal
    water.add(
        Water(
            x = 50f,
            y = 50f,
            width = 80f,
            height = 60f
        )
    )
    // Río pequeño
    water.add(
        Water(
            x = 300f,
            y = 100f,
            width = 40f,
            height = 200f
        )
    )
    return water
}

fun generateClouds(): List<Cloud> {
    val clouds = mutableListOf<Cloud>()
    repeat(5) {
        clouds.add(
            Cloud(
                x = (Math.random() * GAME_AREA_SIZE).toFloat(),
                y = (20 + Math.random() * 40).toFloat(),
                size = (15 + Math.random() * 20).toFloat(),
                speed = (0.3f + Math.random() * 0.7f).toFloat()
            )
        )
    }
    return clouds
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkeletonSurvivalScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val soundGenerator = remember { SoundGenerator(context) }
    val musicPlayer = remember { MusicPlayer(context) }
    val rankingService = remember { RankingService() }
    val scope = rememberCoroutineScope()
    
    var gameState by remember { mutableStateOf(SkeletonSurvivalGameState()) }
    var lastUpdate by remember { mutableStateOf(System.currentTimeMillis()) }
    var globalRanking by remember { mutableStateOf<List<com.example.buzondesugerenciascojema.data.RankingEntry>>(emptyList()) }
    var isLoadingRanking by remember { mutableStateOf(false) }
    var userPosition by remember { mutableStateOf(-1) }
    var showRanking by remember { mutableStateOf(false) }
    
    // Inicializar jugador con machete
    LaunchedEffect(Unit) {
        gameState.player.weapons.add(gameState.player.currentWeapon)
        spawnSkeletons(gameState, 3)
        musicPlayer.playActionMusic()
        
        // Cargar ranking
        isLoadingRanking = true
        try {
            globalRanking = rankingService.obtenerTopRanking("skeleton_survival", 5)
        } catch (e: Exception) {
            println("Error al cargar ranking: ${e.message}")
        } finally {
            isLoadingRanking = false
        }
    }
    
    // Detener música al salir
    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.stopMusic()
        }
    }
    
    // Game loop
    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastUpdate) / 1000f
            lastUpdate = currentTime
            
            if (!gameState.isPaused && !gameState.isGameOver) {
                gameState = updateGame(gameState, deltaTime, soundGenerator)
                
                // Verificar si se completó la ronda
                if (gameState.skeletons.all { it.isDead } && gameState.bosses.all { it.isDead }) {
                    gameState = gameState.copy(
                        round = gameState.round + 1,
                        score = gameState.score + (gameState.round + 1) * 100
                    )
                    
                    // Cada 10 rondas, spawnear un jefe
                    if (gameState.round % 10 == 0) {
                        spawnBoss(gameState, gameState.round / 10)
                    } else {
                        // Cada 5 rondas, mostrar menú de mejoras
                        if (gameState.round % 5 == 0) {
                            val newWeapons = getAvailableUpgrades(gameState.player.weapons)
                            gameState = gameState.copy(
                                showUpgradeMenu = true,
                                availableUpgrades = newWeapons
                            )
                        } else {
                            spawnSkeletons(gameState, 3 + gameState.round)
                        }
                    }
                }
            }
            
            delay(16) // ~60 FPS
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "SKELETON SURVIVAL", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        soundGenerator.playClick()
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        soundGenerator.playClick()
                        gameState = gameState.copy(isPaused = !gameState.isPaused)
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (gameState.isPaused) R.drawable.play else R.drawable.pausa
                            ),
                            contentDescription = if (gameState.isPaused) "Jugar" else "Pausar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E7D32))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GRASS_COLOR)
        ) {
            // Información del juego
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Estadísticas del jugador
                    Column {
                        Text(
                            text = "Ronda: ${gameState.round}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Puntuación: ${gameState.score}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Esqueletos: ${gameState.skeletonsKilled}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Arma actual
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = gameState.player.currentWeapon.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daño: ${gameState.player.currentWeapon.damage}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (gameState.player.currentWeapon.isMelee) "Melee" else "Rango: ${gameState.player.currentWeapon.range.toInt()}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Barra de vida
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = gameState.player.health.toFloat() / gameState.player.maxHealth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFD32F2F)
                )
                Text(
                    text = "${gameState.player.health}/${gameState.player.maxHealth}",
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Área de juego
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.Center)
                    .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                // Renderizar el juego
                GameCanvas(
                    gameState = gameState,
                    onTouch = { position ->
                        // Auto-fire system
                        if (gameState.autoFireEnabled && System.currentTimeMillis() - gameState.player.lastShot > gameState.player.currentWeapon.fireRate) {
                            autoFire(gameState, soundGenerator)
                            gameState.player.lastShot = System.currentTimeMillis()
                        }
                    },
                    onTouchEnd = { }
                )
            }
            
            // Joystick fijo inline
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(120.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                gameState.player.isMoving = true
                            },
                            onDrag = { change, _ ->
                                val center = 60f
                                val distance = sqrt(
                                    (change.position.x - center).pow(2) + 
                                    (change.position.y - center).pow(2)
                                )
                                val maxDistance = 40f
                                
                                val normalizedX = if (distance <= maxDistance) {
                                    (change.position.x - center) / maxDistance
                                } else {
                                    val angle = atan2(change.position.y - center, change.position.x - center)
                                    cos(angle).toFloat()
                                }
                                
                                val normalizedY = if (distance <= maxDistance) {
                                    (change.position.y - center) / maxDistance
                                } else {
                                    val angle = atan2(change.position.y - center, change.position.x - center)
                                    sin(angle).toFloat()
                                }
                                
                                gameState.player.joystickX = normalizedX
                                gameState.player.joystickY = normalizedY
                                gameState.player.isMoving = true
                            },
                            onDragEnd = {
                                gameState.player.joystickX = 0f
                                gameState.player.joystickY = 0f
                                gameState.player.isMoving = false
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = 60f
                    
                    // Base circular
                    drawCircle(
                        color = Color(0xFF424242).copy(alpha = 0.7f),
                        radius = 60f,
                        center = Offset(center, center)
                    )
                    
                    // Borde de la base
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 60f,
                        center = Offset(center, center),
                        style = Stroke(width = 3f)
                    )
                    
                    // Thumb del joystick
                    val thumbX = center + gameState.player.joystickX * 40f
                    val thumbY = center + gameState.player.joystickY * 40f
                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 20f,
                        center = Offset(thumbX, thumbY)
                    )
                    
                    // Borde del thumb
                    drawCircle(
                        color = Color.White,
                        radius = 20f,
                        center = Offset(thumbX, thumbY),
                        style = Stroke(width = 2f)
                    )
                }
            }
            
            // Botón de ranking
            IconButton(
                onClick = { 
                    soundGenerator.playClick()
                    showRanking = !showRanking 
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color(0xFF2E7D32), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logro),
                    contentDescription = "Ver ranking",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Ranking overlay inline
            if (showRanking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏆 RANKING - SKELETON SURVIVAL 🏆",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isLoadingRanking) {
                                CircularProgressIndicator(color = Color.White)
                            } else if (globalRanking.isEmpty()) {
                                Text(
                                    text = "No hay puntuaciones aún",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            } else {
                                globalRanking.forEachIndexed { index, ranking ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(40.dp)
                                        )
                                        
                                        Text(
                                            text = ranking.nombreUsuario ?: "Anónimo",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Text(
                                            text = "${ranking.puntuacion} pts",
                                            color = Color(0xFFFFD700),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (index < globalRanking.size - 1) {
                                        Divider(color = Color.White.copy(alpha = 0.3f))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showRanking = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CERRAR",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // Menú de mejoras
            if (gameState.showUpgradeMenu) {
                UpgradeMenu(
                    upgrades = gameState.availableUpgrades,
                    onSelectUpgrade = { weapon ->
                        gameState.player.weapons.add(weapon)
                        gameState.player.currentWeapon = weapon
                        gameState = gameState.copy(showUpgradeMenu = false)
                        soundGenerator.playUpgrade()
                        spawnSkeletons(gameState, 3 + gameState.round)
                    }
                )
            }
            
            // Game Over
            if (gameState.isGameOver) {
                GameOverScreen(
                    score = gameState.score,
                    round = gameState.round,
                    onRestart = {
                        // Guardar puntuación en el ranking
                        scope.launch {
                            try {
                                rankingService.guardarPuntuacion(
                                    juego = "skeleton_survival",
                                    puntuacion = gameState.score,
                                    nombreUsuario = "Jugador",
                                    emailUsuario = "jugador@ejemplo.com"
                                )
                            } catch (e: Exception) {
                                println("Error al guardar puntuación: ${e.message}")
                            }
                        }
                        
                        gameState = SkeletonSurvivalGameState()
                        gameState.player.weapons.add(gameState.player.currentWeapon)
                        spawnSkeletons(gameState, 3)
                        soundGenerator.playClick()
                    }
                )
            }
        }
    }
}

@Composable
fun GameCanvas(
    gameState: SkeletonSurvivalGameState,
    onTouch: (Offset) -> Unit,
    onTouchEnd: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Dibujar fondo con patrón de hierba
        drawLandscape(gameState, centerX, centerY)
        
        // Dibujar jugador (militar)
        drawPlayer(gameState.player, centerX, centerY)
        
        // Dibujar esqueletos
        gameState.skeletons.forEach { skeleton ->
            if (!skeleton.isDead) {
                drawSkeleton(skeleton, centerX, centerY)
            } else {
                drawDeadSkeleton(skeleton, centerX, centerY)
            }
        }
        
        // Dibujar jefes
        gameState.bosses.forEach { boss ->
            if (!boss.isDead) {
                drawBoss(boss, centerX, centerY)
            }
        }
        
        // Dibujar balas
        gameState.bullets.forEach { bullet ->
            drawBullet(bullet, centerX, centerY)
        }
        
        // Dibujar partículas
        gameState.particles.forEach { particle ->
            drawParticle(particle, centerX, centerY)
        }
        
        // Dibujar efecto de melee si es necesario
        if (gameState.player.currentWeapon.isMelee) {
            drawMeleeEffect(gameState.player, centerX, centerY)
        }
    }
}

fun DrawScope.drawGrassPattern() {
    // Patrón de hierba mejorado
    for (x in 0..size.width.toInt() step 15) {
        for (y in 0..size.height.toInt() step 15) {
            val grassColor = if (Math.random() > 0.5) GRASS_COLOR else GRASS_DARK_COLOR
            drawCircle(
                color = grassColor.copy(alpha = 0.4f),
                radius = (1 + Math.random() * 3).toFloat(),
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
    }
}

fun DrawScope.drawLandscape(gameState: SkeletonSurvivalGameState, centerX: Float, centerY: Float) {
    // Dibujar cielo
    drawRect(
        color = SKY_COLOR,
        topLeft = Offset(0f, 0f),
        size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.3f)
    )
    
    // Dibujar nubes
    gameState.clouds.forEach { cloud ->
        drawCloud(cloud, centerX, centerY)
    }
    
    // Dibujar agua
    gameState.water.forEach { water ->
        drawWater(water, centerX, centerY)
    }
    
    // Dibujar árboles
    gameState.trees.forEach { tree ->
        drawTree(tree, centerX, centerY)
    }
    
    // Dibujar rocas
    gameState.rocks.forEach { rock ->
        drawRock(rock, centerX, centerY)
    }
    
    // Dibujar flores
    gameState.flowers.forEach { flower ->
        drawFlower(flower, centerX, centerY)
    }
}

fun DrawScope.drawCloud(cloud: Cloud, centerX: Float, centerY: Float) {
    val cloudX = centerX + cloud.x - GAME_AREA_SIZE / 2f
    val cloudY = centerY + cloud.y - GAME_AREA_SIZE / 2f
    
    // Sombra de la nube
    drawCircle(
        color = CLOUD_COLOR.copy(alpha = 0.3f),
        radius = cloud.size,
        center = Offset(cloudX + 2, cloudY + 2)
    )
    
    // Nube principal
    drawCircle(
        color = CLOUD_COLOR,
        radius = cloud.size,
        center = Offset(cloudX, cloudY)
    )
    
    // Detalles de la nube
    drawCircle(
        color = CLOUD_COLOR.copy(alpha = 0.8f),
        radius = cloud.size * 0.7f,
        center = Offset(cloudX - cloud.size * 0.3f, cloudY)
    )
    drawCircle(
        color = CLOUD_COLOR.copy(alpha = 0.8f),
        radius = cloud.size * 0.7f,
        center = Offset(cloudX + cloud.size * 0.3f, cloudY)
    )
}

fun DrawScope.drawWater(water: Water, centerX: Float, centerY: Float) {
    val waterX = centerX + water.x - GAME_AREA_SIZE / 2f
    val waterY = centerY + water.y - GAME_AREA_SIZE / 2f
    
    // Sombra del agua
    drawRect(
        color = WATER_DARK_COLOR.copy(alpha = 0.3f),
        topLeft = Offset(waterX + 2, waterY + 2),
        size = androidx.compose.ui.geometry.Size(water.width, water.height)
    )
    
    // Agua principal
    drawRect(
        color = WATER_COLOR.copy(alpha = 0.7f),
        topLeft = Offset(waterX, waterY),
        size = androidx.compose.ui.geometry.Size(water.width, water.height)
    )
    
    // Reflejos en el agua
    for (i in 0..3) {
        drawRect(
            color = WATER_COLOR.copy(alpha = 0.3f),
            topLeft = Offset(waterX + i * 10f, waterY + i * 5f),
            size = androidx.compose.ui.geometry.Size(water.width - i * 20f, 2f)
        )
    }
}

fun DrawScope.drawTree(tree: Tree, centerX: Float, centerY: Float) {
    val treeX = centerX + tree.x - GAME_AREA_SIZE / 2f
    val treeY = centerY + tree.y - GAME_AREA_SIZE / 2f
    
    // Sombra del árbol
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = tree.size,
        center = Offset(treeX + 3, treeY + 3)
    )
    
    // Tronco del árbol
    drawRect(
        color = TREE_TRUNK_COLOR,
        topLeft = Offset(treeX - tree.size * 0.2f, treeY + tree.size * 0.3f),
        size = androidx.compose.ui.geometry.Size(tree.size * 0.4f, tree.size * 0.7f)
    )
    
    // Hojas del árbol
    when (tree.type) {
        TreeType.OAK -> {
            // Árbol de roble - forma redonda
            drawCircle(
                color = TREE_LEAVES_COLOR,
                radius = tree.size * 0.8f,
                center = Offset(treeX, treeY)
            )
            drawCircle(
                color = TREE_LEAVES_LIGHT_COLOR,
                radius = tree.size * 0.6f,
                center = Offset(treeX, treeY)
            )
        }
        TreeType.PINE -> {
            // Pino - forma triangular
            val path = Path().apply {
                moveTo(treeX, treeY - tree.size)
                lineTo(treeX - tree.size * 0.6f, treeY + tree.size * 0.2f)
                lineTo(treeX + tree.size * 0.6f, treeY + tree.size * 0.2f)
                close()
            }
            drawPath(path, TREE_LEAVES_COLOR)
        }
        TreeType.MAPLE -> {
            // Arce - forma de abanico
            drawCircle(
                color = TREE_LEAVES_COLOR,
                radius = tree.size * 0.7f,
                center = Offset(treeX, treeY)
            )
            // Detalles del arce
            for (i in 0..4) {
                val angle = i * 72f * PI / 180f
                drawCircle(
                    color = TREE_LEAVES_LIGHT_COLOR,
                    radius = tree.size * 0.2f,
                    center = Offset(
                        treeX + cos(angle).toFloat() * tree.size * 0.4f,
                        treeY + sin(angle).toFloat() * tree.size * 0.4f
                    )
                )
            }
        }
        TreeType.PALM -> {
            // Palma - forma de palmera
            drawCircle(
                color = TREE_LEAVES_COLOR,
                radius = tree.size * 0.3f,
                center = Offset(treeX, treeY)
            )
            // Hojas de palma
            for (i in 0..6) {
                val angle = i * 60f * PI / 180f
                drawLine(
                    color = TREE_LEAVES_LIGHT_COLOR,
                    start = Offset(treeX, treeY),
                    end = Offset(
                        treeX + cos(angle).toFloat() * tree.size * 0.8f,
                        treeY + sin(angle).toFloat() * tree.size * 0.8f
                    ),
                    strokeWidth = 3f
                )
            }
        }
    }
}

fun DrawScope.drawRock(rock: Rock, centerX: Float, centerY: Float) {
    val rockX = centerX + rock.x - GAME_AREA_SIZE / 2f
    val rockY = centerY + rock.y - GAME_AREA_SIZE / 2f
    
    // Sombra de la roca
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = rock.size,
        center = Offset(rockX + 2, rockY + 2)
    )
    
    when (rock.type) {
        RockType.SMALL -> {
            // Roca pequeña
            drawCircle(
                color = ROCK_COLOR,
                radius = rock.size,
                center = Offset(rockX, rockY)
            )
            drawCircle(
                color = ROCK_DARK_COLOR,
                radius = rock.size * 0.7f,
                center = Offset(rockX, rockY)
            )
        }
        RockType.MEDIUM -> {
            // Roca mediana
            drawCircle(
                color = ROCK_COLOR,
                radius = rock.size,
                center = Offset(rockX, rockY)
            )
            drawCircle(
                color = ROCK_DARK_COLOR,
                radius = rock.size * 0.8f,
                center = Offset(rockX, rockY)
            )
            drawCircle(
                color = ROCK_COLOR.copy(alpha = 0.7f),
                radius = rock.size * 0.5f,
                center = Offset(rockX - rock.size * 0.2f, rockY - rock.size * 0.2f)
            )
        }
        RockType.LARGE -> {
            // Roca grande
            drawCircle(
                color = ROCK_COLOR,
                radius = rock.size,
                center = Offset(rockX, rockY)
            )
            drawCircle(
                color = ROCK_DARK_COLOR,
                radius = rock.size * 0.9f,
                center = Offset(rockX, rockY)
            )
            drawCircle(
                color = ROCK_COLOR.copy(alpha = 0.6f),
                radius = rock.size * 0.6f,
                center = Offset(rockX - rock.size * 0.3f, rockY - rock.size * 0.3f)
            )
            drawCircle(
                color = ROCK_COLOR.copy(alpha = 0.8f),
                radius = rock.size * 0.4f,
                center = Offset(rockX + rock.size * 0.2f, rockY + rock.size * 0.2f)
            )
        }
    }
}

fun DrawScope.drawFlower(flower: Flower, centerX: Float, centerY: Float) {
    val flowerX = centerX + flower.x - GAME_AREA_SIZE / 2f
    val flowerY = centerY + flower.y - GAME_AREA_SIZE / 2f
    
    // Tallo de la flor
    drawLine(
        color = Color(0xFF4CAF50),
        start = Offset(flowerX, flowerY + 8),
        end = Offset(flowerX, flowerY),
        strokeWidth = 2f
    )
    
    // Hojas
    drawCircle(
        color = Color(0xFF4CAF50),
        radius = 3f,
        center = Offset(flowerX - 4, flowerY + 4)
    )
    drawCircle(
        color = Color(0xFF4CAF50),
        radius = 3f,
        center = Offset(flowerX + 4, flowerY + 4)
    )
    
    // Pétalos de la flor
    val flowerColor = when (flower.type) {
        FlowerType.RED -> FLOWER_COLOR
        FlowerType.YELLOW -> FLOWER_YELLOW_COLOR
        FlowerType.PURPLE -> FLOWER_PURPLE_COLOR
        FlowerType.WHITE -> Color.White
    }
    
    // Centro de la flor
    drawCircle(
        color = Color(0xFFFFD700),
        radius = 2f,
        center = Offset(flowerX, flowerY)
    )
    
    // Pétalos
    for (i in 0..5) {
        val angle = i * 60f * PI / 180f
        drawCircle(
            color = flowerColor,
            radius = 3f,
            center = Offset(
                flowerX + cos(angle).toFloat() * 4f,
                flowerY + sin(angle).toFloat() * 4f
            )
        )
    }
}

fun DrawScope.drawPlayer(player: Player, centerX: Float, centerY: Float) {
    val playerX = centerX + player.x - GAME_AREA_SIZE / 2f
    val playerY = centerY + player.y - GAME_AREA_SIZE / 2f
    
    // Sombra del jugador
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = PLAYER_SIZE.toFloat(),
        center = Offset(playerX + 2, playerY + 2)
    )
    
    // Cuerpo del militar (chaleco táctico)
    drawCircle(
        color = Color(0xFF1565C0),
        radius = PLAYER_SIZE.toFloat(),
        center = Offset(playerX, playerY)
    )
    
    // Uniforme interior
    drawCircle(
        color = Color(0xFF0D47A1),
        radius = (PLAYER_SIZE - 3).toFloat(),
        center = Offset(playerX, playerY)
    )
    
    // Casco militar
    drawCircle(
        color = Color(0xFF424242),
        radius = (PLAYER_SIZE - 2).toFloat(),
        center = Offset(playerX, playerY - 8)
    )
    
    // Visor del casco
    drawRect(
        color = Color(0xFF1A1A1A),
        topLeft = Offset(playerX - 6, playerY - 10),
        size = androidx.compose.ui.geometry.Size(12f, 4f)
    )
    
    // Reflejo del visor
    drawRect(
        color = Color(0xFF666666),
        topLeft = Offset(playerX - 5, playerY - 9),
        size = androidx.compose.ui.geometry.Size(2f, 2f)
    )
    
    // Detalles del chaleco
    drawCircle(
        color = Color(0xFF0D47A1),
        radius = 3f,
        center = Offset(playerX - 8, playerY - 5)
    )
    drawCircle(
        color = Color(0xFF0D47A1),
        radius = 3f,
        center = Offset(playerX + 8, playerY - 5)
    )
    
    // Arma
    drawWeapon(player, playerX, playerY)
    
    // Efecto de movimiento
    if (player.isMoving) {
        drawCircle(
            color = Color(0xFF2196F3).copy(alpha = 0.3f),
            radius = PLAYER_SIZE.toFloat() + 5,
            center = Offset(playerX, playerY)
        )
    }
}

fun DrawScope.drawWeapon(player: Player, x: Float, y: Float) {
    when (player.currentWeapon.type) {
        WeaponType.MACHETE -> {
            // Machete
            drawRect(
                color = Color(0xFF757575),
                topLeft = Offset(x + 8, y - 4),
                size = androidx.compose.ui.geometry.Size(12f, 2f)
            )
        }
        WeaponType.PISTOL, WeaponType.RIFLE -> {
            // Pistola/Rifle
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(x + 8, y - 2),
                size = androidx.compose.ui.geometry.Size(8f, 4f)
            )
        }
        WeaponType.SHOTGUN -> {
            // Escopeta
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(x + 8, y - 3),
                size = androidx.compose.ui.geometry.Size(10f, 6f)
            )
        }
        WeaponType.UZI -> {
            // UZI
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(x + 8, y - 2),
                size = androidx.compose.ui.geometry.Size(12f, 3f)
            )
        }
        WeaponType.ROCKET_LAUNCHER -> {
            // Lanzacohetes
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(x + 8, y - 4),
                size = androidx.compose.ui.geometry.Size(16f, 8f)
            )
        }
        WeaponType.LASER_GUN -> {
            // Láser
            drawRect(
                color = Color(0xFF00BCD4),
                topLeft = Offset(x + 8, y - 2),
                size = androidx.compose.ui.geometry.Size(10f, 4f)
            )
        }
        WeaponType.FLAMETHROWER -> {
            // Lanzallamas
            drawRect(
                color = Color(0xFFFF5722),
                topLeft = Offset(x + 8, y - 3),
                size = androidx.compose.ui.geometry.Size(14f, 6f)
            )
        }
        else -> {
            // Armas adicionales - representación genérica
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(x + 8, y - 2),
                size = androidx.compose.ui.geometry.Size(10f, 4f)
            )
        }
    }
}

fun DrawScope.drawSkeleton(skeleton: Skeleton, centerX: Float, centerY: Float) {
    val skeletonX = centerX + skeleton.x - GAME_AREA_SIZE / 2f
    val skeletonY = centerY + skeleton.y - GAME_AREA_SIZE / 2f
    
    // Sombra del esqueleto
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = SKELETON_SIZE.toFloat(),
        center = Offset(skeletonX + 1, skeletonY + 1)
    )
    
    // Color base según el tipo
    val baseColor = when (skeleton.skeletonType) {
        SkeletonType.NORMAL -> SKELETON_COLOR
        SkeletonType.ARCHER -> Color(0xFF8D6E63)
        SkeletonType.MAGE -> Color(0xFF6A4C93)
        SkeletonType.TANK -> Color(0xFF424242)
        SkeletonType.BOSS -> Color(0xFFD32F2F)
    }
    
    // Cuerpo del esqueleto
    drawCircle(
        color = baseColor,
        radius = SKELETON_SIZE.toFloat(),
        center = Offset(skeletonX, skeletonY)
    )
    
    // Detalles del cuerpo según el tipo
    when (skeleton.skeletonType) {
        SkeletonType.ARCHER -> {
            // Arco
            drawArc(
                color = Color(0xFF8D6E63),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(skeletonX - 8, skeletonY - 4),
                size = androidx.compose.ui.geometry.Size(16f, 8f),
                style = Stroke(width = 2f)
            )
        }
        SkeletonType.MAGE -> {
            // Aura mágica
            drawCircle(
                color = Color(0xFF6A4C93).copy(alpha = 0.5f),
                radius = SKELETON_SIZE.toFloat() + 3,
                center = Offset(skeletonX, skeletonY)
            )
            // Orbe mágico
            drawCircle(
                color = Color(0xFFE1BEE7),
                radius = 4f,
                center = Offset(skeletonX + 8, skeletonY - 8)
            )
        }
        SkeletonType.TANK -> {
            // Armadura
            drawCircle(
                color = Color(0xFF424242),
                radius = SKELETON_SIZE.toFloat() + 2,
                center = Offset(skeletonX, skeletonY),
                style = Stroke(width = 3f)
            )
            // Escudo
            drawRect(
                color = Color(0xFF757575),
                topLeft = Offset(skeletonX - 10, skeletonY - 6),
                size = androidx.compose.ui.geometry.Size(8f, 12f)
            )
        }
        else -> {
            // Huesos normales
            drawRect(
                color = Color(0xFFE0E0E0),
                topLeft = Offset(skeletonX - 2, skeletonY - 8),
                size = androidx.compose.ui.geometry.Size(4f, 16f)
            )
        }
    }
    
    // Cráneo
    drawCircle(
        color = baseColor,
        radius = (SKELETON_SIZE - 4).toFloat(),
        center = Offset(skeletonX, skeletonY - 12)
    )
    
    // Ojos según el tipo
    val eyeColor = when (skeleton.skeletonType) {
        SkeletonType.NORMAL -> Color(0xFFD32F2F)
        SkeletonType.ARCHER -> Color(0xFF4CAF50)
        SkeletonType.MAGE -> Color(0xFF9C27B0)
        SkeletonType.TANK -> Color(0xFFFF9800)
        SkeletonType.BOSS -> Color(0xFFFF0000)
    }
    
    drawCircle(
        color = eyeColor,
        radius = 2f,
        center = Offset(skeletonX - 3, skeletonY - 12)
    )
    drawCircle(
        color = eyeColor,
        radius = 2f,
        center = Offset(skeletonX + 3, skeletonY - 12)
    )
    
    // Barra de vida
    val healthPercentage = skeleton.health.toFloat() / skeleton.maxHealth.toFloat()
    drawRect(
        color = Color(0xFFD32F2F),
        topLeft = Offset(skeletonX - 8, skeletonY - 20),
        size = androidx.compose.ui.geometry.Size(16f, 3f)
    )
    drawRect(
        color = Color(0xFF4CAF50),
        topLeft = Offset(skeletonX - 8, skeletonY - 20),
        size = androidx.compose.ui.geometry.Size(16f * healthPercentage, 3f)
    )
}

fun DrawScope.drawDeadSkeleton(skeleton: Skeleton, centerX: Float, centerY: Float) {
    val skeletonX = centerX + skeleton.x - GAME_AREA_SIZE / 2f
    val skeletonY = centerY + skeleton.y - GAME_AREA_SIZE / 2f
    
    // Esqueleto muerto (más pequeño y gris)
    drawCircle(
        color = Color(0xFF9E9E9E),
        radius = (SKELETON_SIZE * skeleton.deathAnimation).toFloat(),
        center = Offset(skeletonX, skeletonY)
    )
}

fun DrawScope.drawBullet(bullet: SkeletonSurvivalBullet, centerX: Float, centerY: Float) {
    val bulletX = centerX + bullet.x - GAME_AREA_SIZE / 2f
    val bulletY = centerY + bullet.y - GAME_AREA_SIZE / 2f
    
    drawCircle(
        color = if (bullet.isPlayerBullet) SKELETON_SURVIVAL_BULLET_COLOR else Color(0xFFFF5722),
        radius = BULLET_SIZE.toFloat(),
        center = Offset(bulletX, bulletY)
    )
}

fun DrawScope.drawParticle(particle: Particle, centerX: Float, centerY: Float) {
    val particleX = centerX + particle.x - GAME_AREA_SIZE / 2f
    val particleY = centerY + particle.y - GAME_AREA_SIZE / 2f
    
    drawCircle(
        color = particle.color.copy(alpha = particle.life),
        radius = 3f * particle.life,
        center = Offset(particleX, particleY)
    )
}

fun DrawScope.drawMeleeEffect(player: Player, centerX: Float, centerY: Float) {
    val playerX = centerX + player.x - GAME_AREA_SIZE / 2f
    val playerY = centerY + player.y - GAME_AREA_SIZE / 2f
    
    // Efecto de melee (círculo alrededor del jugador)
    drawCircle(
        color = MELEE_COLOR.copy(alpha = 0.3f),
        radius = MELEE_RANGE.toFloat(),
        center = Offset(playerX, playerY),
        style = Stroke(width = 2f)
    )
}

fun DrawScope.drawBoss(boss: Boss, centerX: Float, centerY: Float) {
    val bossX = centerX + boss.x - GAME_AREA_SIZE / 2f
    val bossY = centerY + boss.y - GAME_AREA_SIZE / 2f
    
    val bossSize = 30f
    
    // Sombra del jefe
    drawCircle(
        color = Color.Black.copy(alpha = 0.4f),
        radius = bossSize,
        center = Offset(bossX + 3, bossY + 3)
    )
    
    // Color base según el tipo de jefe
    val baseColor = when (boss.bossType) {
        BossType.SKELETON_LORD -> Color(0xFF8D6E63)
        BossType.NECROMANCER -> Color(0xFF6A4C93)
        BossType.BONE_GOLEM -> Color(0xFF424242)
        BossType.DEATH_KNIGHT -> Color(0xFFD32F2F)
        BossType.SHADOW_EMPEROR -> Color(0xFF1A1A1A)
    }
    
    // Cuerpo principal del jefe
    drawCircle(
        color = baseColor,
        radius = bossSize,
        center = Offset(bossX, bossY)
    )
    
    // Detalles específicos del jefe
    when (boss.bossType) {
        BossType.SKELETON_LORD -> {
            // Corona
            drawRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(bossX - 8, bossY - 35),
                size = androidx.compose.ui.geometry.Size(16f, 6f)
            )
            // Corona de picos
            repeat(5) { i ->
                drawRect(
                    color = Color(0xFFFFD700),
                    topLeft = Offset(bossX - 6 + i * 3, bossY - 38),
                    size = androidx.compose.ui.geometry.Size(2f, 4f)
                )
            }
            // Capa
            drawArc(
                color = Color(0xFF8D6E63).copy(alpha = 0.7f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(bossX - 15, bossY - 10),
                size = androidx.compose.ui.geometry.Size(30f, 20f)
            )
        }
        BossType.NECROMANCER -> {
            // Sombrero puntiagudo
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(bossX, bossY - 35)
                    lineTo(bossX - 8, bossY - 25)
                    lineTo(bossX + 8, bossY - 25)
                    close()
                },
                color = Color(0xFF6A4C93)
            )
            // Aura mágica
            drawCircle(
                color = Color(0xFF6A4C93).copy(alpha = 0.6f),
                radius = bossSize + 5,
                center = Offset(bossX, bossY)
            )
            // Orbes flotantes
            repeat(3) { i ->
                val angle = i * 120f * PI / 180f
                val orbX = bossX + cos(angle.toDouble()).toFloat() * 20f
                val orbY = bossY + sin(angle.toDouble()).toFloat() * 20f
                drawCircle(
                    color = Color(0xFFE1BEE7),
                    radius = 4f,
                    center = Offset(orbX, orbY)
                )
            }
        }
        BossType.BONE_GOLEM -> {
            // Armadura pesada
            drawCircle(
                color = Color(0xFF424242),
                radius = bossSize + 3,
                center = Offset(bossX, bossY),
                style = Stroke(width = 4f)
            )
            // Puños gigantes
            drawCircle(
                color = Color(0xFF757575),
                radius = 8f,
                center = Offset(bossX - 20, bossY)
            )
            drawCircle(
                color = Color(0xFF757575),
                radius = 8f,
                center = Offset(bossX + 20, bossY)
            )
        }
        BossType.DEATH_KNIGHT -> {
            // Casco con cuernos
            drawCircle(
                color = Color(0xFF424242),
                radius = bossSize - 5,
                center = Offset(bossX, bossY - 8)
            )
            // Cuernos
            drawRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(bossX - 12, bossY - 15),
                size = androidx.compose.ui.geometry.Size(4f, 8f)
            )
            drawRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(bossX + 8, bossY - 15),
                size = androidx.compose.ui.geometry.Size(4f, 8f)
            )
            // Espada
            drawRect(
                color = Color(0xFFC0C0C0),
                topLeft = Offset(bossX + 15, bossY - 10),
                size = androidx.compose.ui.geometry.Size(4f, 20f)
            )
        }
        BossType.SHADOW_EMPEROR -> {
            // Aura de sombra
            drawCircle(
                color = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                radius = bossSize + 8,
                center = Offset(bossX, bossY)
            )
            // Corona de sombra
            drawRect(
                color = Color(0xFF424242),
                topLeft = Offset(bossX - 10, bossY - 35),
                size = androidx.compose.ui.geometry.Size(20f, 8f)
            )
            // Ojos brillantes
            drawCircle(
                color = Color(0xFFFF0000),
                radius = 4f,
                center = Offset(bossX - 6, bossY - 8)
            )
            drawCircle(
                color = Color(0xFFFF0000),
                radius = 4f,
                center = Offset(bossX + 6, bossY - 8)
            )
        }
    }
    
    // Barra de vida del jefe
    val healthPercentage = boss.health.toFloat() / boss.maxHealth.toFloat()
    drawRect(
        color = Color(0xFFD32F2F),
        topLeft = Offset(bossX - 15, bossY - 45),
        size = androidx.compose.ui.geometry.Size(30f, 5f)
    )
    drawRect(
        color = Color(0xFF4CAF50),
        topLeft = Offset(bossX - 15, bossY - 45),
        size = androidx.compose.ui.geometry.Size(30f * healthPercentage, 5f)
    )
    
    // Nombre del jefe
    drawContext.canvas.nativeCanvas.drawText(
        when (boss.bossType) {
            BossType.SKELETON_LORD -> "SKELETON LORD"
            BossType.NECROMANCER -> "NECROMANCER"
            BossType.BONE_GOLEM -> "BONE GOLEM"
            BossType.DEATH_KNIGHT -> "DEATH KNIGHT"
            BossType.SHADOW_EMPEROR -> "SHADOW EMPEROR"
        },
        bossX - 25,
        bossY - 50,
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 8f
            isFakeBoldText = true
        }
    )
}

@Composable
fun UpgradeMenu(
    upgrades: List<Weapon>,
    onSelectUpgrade: (Weapon) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 ¡NUEVA ARMA DESBLOQUEADA! 🏆",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                upgrades.forEach { weapon ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectUpgrade(weapon) },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono del arma
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (weapon.isMelee) MELEE_COLOR else SKELETON_SURVIVAL_BULLET_COLOR,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                                                    text = when (weapon.type) {
                                    WeaponType.MACHETE -> "⚔️"
                                    WeaponType.PISTOL, WeaponType.SHOTGUN, WeaponType.RIFLE, WeaponType.UZI -> "🔫"
                                    WeaponType.ROCKET_LAUNCHER -> "🚀"
                                    WeaponType.LASER_GUN -> "⚡"
                                    WeaponType.FLAMETHROWER -> "🔥"
                                    else -> "?"
                                },
                                    fontSize = 20.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = weapon.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Daño: ${weapon.damage} | ${if (weapon.isMelee) "Melee" else "Rango: ${weapon.range.toInt()}"}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
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
fun GameOverScreen(
    score: Int,
    round: Int,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💀 GAME OVER 💀",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Puntuación Final: $score",
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Text(
                    text = "Ronda Alcanzada: $round",
                    color = Color.White,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "JUGAR DE NUEVO",
                        color = Color(0xFFD32F2F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Funciones del juego
fun updateGame(gameState: SkeletonSurvivalGameState, deltaTime: Float, soundGenerator: SoundGenerator): SkeletonSurvivalGameState {
    var updatedGameState = gameState
    
    // Actualizar movimiento del jugador
    if (gameState.player.isMoving) {
        val newX = gameState.player.x + gameState.player.joystickX * gameState.player.speed * deltaTime * 60
        val newY = gameState.player.y + gameState.player.joystickY * gameState.player.speed * deltaTime * 60
        
        // Mantener al jugador dentro del área de juego
        val clampedX = newX.coerceIn(PLAYER_SIZE.toFloat(), GAME_AREA_SIZE - PLAYER_SIZE.toFloat())
        val clampedY = newY.coerceIn(PLAYER_SIZE.toFloat(), GAME_AREA_SIZE - PLAYER_SIZE.toFloat())
        
        updatedGameState = updatedGameState.copy(
            player = gameState.player.copy(x = clampedX, y = clampedY)
        )
    }
    
    // Auto-fire system
    if (gameState.autoFireEnabled && System.currentTimeMillis() - gameState.player.lastShot > gameState.player.currentWeapon.fireRate) {
        updatedGameState = autoFire(updatedGameState, soundGenerator)
        updatedGameState = updatedGameState.copy(
            player = updatedGameState.player.copy(lastShot = System.currentTimeMillis())
        )
    }
    
    // Actualizar esqueletos
    gameState.skeletons.forEach { skeleton ->
        if (!skeleton.isDead) {
            // Mover hacia el jugador
            val dx = gameState.player.x - skeleton.x
            val dy = gameState.player.y - skeleton.y
            val distance = sqrt(dx * dx + dy * dy)
            
            if (distance > 0) {
                skeleton.x += (dx / distance) * skeleton.speed * deltaTime * 60
                skeleton.y += (dy / distance) * skeleton.speed * deltaTime * 60
            }
            
            // Atacar al jugador si está cerca
            if (distance < SKELETON_SIZE + PLAYER_SIZE) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - skeleton.lastAttack > skeleton.attackCooldown) {
                    val damage = when (skeleton.skeletonType) {
                        SkeletonType.NORMAL -> 1
                        SkeletonType.ARCHER -> 2
                        SkeletonType.MAGE -> 3
                        SkeletonType.TANK -> 5
                        SkeletonType.BOSS -> 10
                    }
                    updatedGameState = updatedGameState.copy(
                        player = updatedGameState.player.copy(health = updatedGameState.player.health - damage)
                    )
                    skeleton.lastAttack = currentTime
                    skeleton.attackCooldown = 1000L // 1 segundo entre ataques
                    
                    if (updatedGameState.player.health <= 0) {
                        updatedGameState = updatedGameState.copy(isGameOver = true)
                    }
                }
            }
        } else {
            // Animación de muerte
            skeleton.deathAnimation -= deltaTime * 2
            if (skeleton.deathAnimation <= 0) {
                skeleton.deathAnimation = 0f
            }
        }
    }
    
    // Actualizar jefes
    gameState.bosses.forEach { boss ->
        if (!boss.isDead) {
            // Comportamiento específico del jefe inline
            val currentTime = System.currentTimeMillis()
            
            when (boss.bossType) {
                BossType.SKELETON_LORD -> {
                    // Movimiento hacia el jugador
                    val dx = gameState.player.x - boss.x
                    val dy = gameState.player.y - boss.y
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance > 0) {
                        boss.x += (dx / distance) * boss.speed * deltaTime * 60
                        boss.y += (dy / distance) * boss.speed * deltaTime * 60
                    }
                    
                    // Ataque especial: Llamar esqueletos
                    if (currentTime - boss.lastSpecialAttack > boss.specialAttackCooldown) {
                        spawnSkeletons(gameState, 3)
                        boss.lastSpecialAttack = currentTime
                        boss.specialAttackCooldown = 5000L
                    }
                }
                BossType.NECROMANCER -> {
                    // Movimiento errático
                    boss.x += sin(currentTime / 1000f) * boss.speed * deltaTime * 30
                    boss.y += cos(currentTime / 1000f) * boss.speed * deltaTime * 30
                    
                    // Ataque especial: Proyectiles mágicos
                    if (currentTime - boss.lastSpecialAttack > boss.specialAttackCooldown) {
                        repeat(8) { i ->
                            val angle = i * 45f * PI / 180f
                            gameState.bullets.add(
                                SkeletonSurvivalBullet(
                                    x = boss.x,
                                    y = boss.y,
                                    angle = angle.toFloat(),
                                    damage = 20,
                                    isPlayerBullet = false
                                )
                            )
                        }
                        boss.lastSpecialAttack = currentTime
                        boss.specialAttackCooldown = 3000L
                    }
                }
                BossType.BONE_GOLEM -> {
                    // Movimiento lento pero poderoso
                    val dx = gameState.player.x - boss.x
                    val dy = gameState.player.y - boss.y
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance > 0) {
                        boss.x += (dx / distance) * (boss.speed * 0.5f) * deltaTime * 60
                        boss.y += (dy / distance) * (boss.speed * 0.5f) * deltaTime * 60
                    }
                    
                    // Ataque especial: Terremoto
                    if (currentTime - boss.lastSpecialAttack > boss.specialAttackCooldown) {
                        updatedGameState = updatedGameState.copy(
                            player = updatedGameState.player.copy(health = updatedGameState.player.health - 20)
                        )
                        boss.lastSpecialAttack = currentTime
                        boss.specialAttackCooldown = 8000L
                    }
                }
                BossType.DEATH_KNIGHT -> {
                    // Movimiento rápido y agresivo
                    val dx = gameState.player.x - boss.x
                    val dy = gameState.player.y - boss.y
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance > 0) {
                        boss.x += (dx / distance) * (boss.speed * 1.5f) * deltaTime * 60
                        boss.y += (dy / distance) * (boss.speed * 1.5f) * deltaTime * 60
                    }
                    
                    // Ataque especial: Carga
                    if (currentTime - boss.lastSpecialAttack > boss.specialAttackCooldown) {
                        boss.speed *= 3f
                        boss.lastSpecialAttack = currentTime
                        boss.specialAttackCooldown = 4000L
                    }
                }
                BossType.SHADOW_EMPEROR -> {
                    // Teletransporte y ataques poderosos
                    if (currentTime - boss.lastSpecialAttack > boss.specialAttackCooldown) {
                        boss.x = (Math.random() * GAME_AREA_SIZE).toFloat()
                        boss.y = (Math.random() * GAME_AREA_SIZE).toFloat()
                        boss.lastSpecialAttack = currentTime
                        boss.specialAttackCooldown = 6000L
                    }
                }
            }
            
            // Ataque normal del jefe
            val distanceToPlayer = sqrt(
                (gameState.player.x - boss.x).pow(2) + 
                (gameState.player.y - boss.y).pow(2)
            )
            
            if (distanceToPlayer < 50f && currentTime - boss.lastAttack > boss.attackCooldown) {
                updatedGameState = updatedGameState.copy(
                    player = updatedGameState.player.copy(health = updatedGameState.player.health - 15)
                )
                boss.lastAttack = currentTime
                boss.attackCooldown = 2000L
            }
        }
    }
    
    // Actualizar balas
    gameState.bullets.forEach { bullet ->
        bullet.x += cos(bullet.angle) * bullet.speed * deltaTime * 60
        bullet.y += sin(bullet.angle) * bullet.speed * deltaTime * 60
        
        // Verificar colisiones con esqueletos
        gameState.skeletons.forEach { skeleton ->
            if (!skeleton.isDead) {
                val dx = bullet.x - skeleton.x
                val dy = bullet.y - skeleton.y
                val distance = sqrt(dx * dx + dy * dy)
                
                if (distance < SKELETON_SIZE + BULLET_SIZE) {
                    skeleton.health -= bullet.damage
                    bullet.speed = 0f // Destruir bala
                    
                    if (skeleton.health <= 0) {
                                            skeleton.isDead = true
                    updatedGameState = updatedGameState.copy(
                        skeletonsKilled = updatedGameState.skeletonsKilled + 1,
                        score = updatedGameState.score + 50
                    )
                        soundGenerator.playKill()
                        
                        // Crear partículas de sangre
                        repeat(5) {
                            gameState.particles.add(
                                Particle(
                                    x = skeleton.x + ((Math.random() - 0.5) * 20).toFloat(),
                                    y = skeleton.y + ((Math.random() - 0.5) * 20).toFloat(),
                                    vx = ((Math.random() - 0.5) * 100).toFloat(),
                                    vy = ((Math.random() - 0.5) * 100).toFloat(),
                                    color = BLOOD_COLOR
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Limpiar balas fuera de pantalla
    gameState.bullets.removeAll { bullet ->
        bullet.x < 0 || bullet.x > GAME_AREA_SIZE || 
        bullet.y < 0 || bullet.y > GAME_AREA_SIZE || bullet.speed == 0f
    }
    
    // Actualizar partículas
    gameState.particles.forEach { particle ->
        particle.x += particle.vx * deltaTime
        particle.y += particle.vy * deltaTime
        particle.life -= deltaTime * 2
        particle.vx *= 0.95f
        particle.vy *= 0.95f
    }
    
    // Limpiar partículas muertas
    gameState.particles.removeAll { it.life <= 0 }
    
    // Animar nubes
    val updatedClouds = gameState.clouds.map { cloud ->
        val newX = cloud.x + cloud.speed * deltaTime * 30
        val finalX = if (newX > GAME_AREA_SIZE + cloud.size) -cloud.size else newX
        cloud.copy(x = finalX)
    }
    updatedGameState = updatedGameState.copy(clouds = updatedClouds.toMutableList())
    
    return updatedGameState
}

fun autoFire(gameState: SkeletonSurvivalGameState, soundGenerator: SoundGenerator): SkeletonSurvivalGameState {
    var updatedGameState = gameState
    val weapon = gameState.player.currentWeapon
    
    if (weapon.isMelee) {
        // Ataque melee automático
        gameState.skeletons.forEach { skeleton ->
            val distance = sqrt(
                (gameState.player.x - skeleton.x).pow(2) + 
                (gameState.player.y - skeleton.y).pow(2)
            )
            
            if (distance < weapon.range && !skeleton.isDead) {
                skeleton.health -= weapon.damage
                if (skeleton.health <= 0) {
                    skeleton.isDead = true
                    updatedGameState = updatedGameState.copy(
                        skeletonsKilled = updatedGameState.skeletonsKilled + 1,
                        score = updatedGameState.score + 50
                    )
                    soundGenerator.playKill()
                    
                    // Crear partículas de muerte
                    createDeathParticles(gameState, skeleton.x, skeleton.y)
                }
            }
        }
        
        // También atacar jefes
        gameState.bosses.forEach { boss ->
            val distance = sqrt(
                (gameState.player.x - boss.x).pow(2) + 
                (gameState.player.y - boss.y).pow(2)
            )
            
            if (distance < weapon.range && !boss.isDead) {
                boss.health -= weapon.damage
                if (boss.health <= 0) {
                    boss.isDead = true
                    updatedGameState = updatedGameState.copy(
                        bossesKilled = updatedGameState.bossesKilled + 1,
                        score = updatedGameState.score + 500
                    )
                    soundGenerator.playKill()
                    createDeathParticles(gameState, boss.x, boss.y, true)
                }
            }
        }
        soundGenerator.playMelee()
    } else {
        // Disparo automático hacia el enemigo más cercano
        val nearestEnemy = findNearestEnemy(gameState)
        if (nearestEnemy != null) {
            val dx = nearestEnemy.first - gameState.player.x
            val dy = nearestEnemy.second - gameState.player.y
            val angle = atan2(dy, dx)
            
            repeat(weapon.bulletCount) {
                val spreadAngle = angle + ((Math.random() - 0.5) * weapon.spread * PI / 180).toFloat()
                gameState.bullets.add(
                    SkeletonSurvivalBullet(
                        x = gameState.player.x,
                        y = gameState.player.y,
                        angle = spreadAngle.toFloat(),
                        damage = weapon.damage
                    )
                )
            }
            soundGenerator.playShoot()
        }
    }
    
    return updatedGameState
}