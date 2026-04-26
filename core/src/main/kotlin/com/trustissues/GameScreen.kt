package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class GameScreen(
    private val game: TrustIssuesGame,
    private val currentLevel: Int = 1,
    private val currentChunk: Int = 1
) : ScreenAdapter() {

    // Game World
    private val gameViewport = FitViewport(1280f, 720f)
    private val shapeRenderer = ShapeRenderer()

    // Collision Rects
    private val playerRect = Rectangle()
    private val maskRect = Rectangle()
    private val sharkRect = Rectangle()
    private val echoRect = Rectangle()

    // Player Stats
    private val playerWidth = 25f
    private var playerHeight = 50f
    private val normalHeight = 50f
    private val crouchHeight = 25f

    private var playerX = 100f
    private var playerY = 280f
    private var velocityY = 0f
    private var gravity = -3200f // Now variable
    private val baseGravity = -3200f
    private val jumpStrength = 1050f
    private val moveSpeed = 350f
    private val floorY = 280f
    private var reverseGravity = false

    // Animation State
    private var walkTime = 0f
    private var isWalking = false
    private var canJump = false


    // Level 6 Mechanics
    private var hasSwappedIdentity = false
    private var ghostX = -999f
    private var ghostY = -999f
    private var blinkTimer = 0f
    private var blinkStep = false
    private var driftTimer = 0f
    private var driftDirection = 0 // -1 for left, 1 for right
    private var isDrifting = false
    private var flapsRemaining = 0
    private var flapTimer = 0f

    // Game State
    private var isDead = false
    private var isLevelComplete = false
    private var stateTimer = 0f
    private var allowScreenWrap = false
    private var isPaused = false

    // Refraction Engine State
    private var renderOffset = 0f
    private var renderOffsetY = 0f
    private var tideTimer = 0f
    private var tideTargetOffset = 50f

    // Horror Mode State
    private var horrorMode = false
    private var flashlightRadius = 300f // Default 300f
    private var strobeTimer = 0f
    private var isLightsOn = false
    private var chunk3FlashTimer = 0f // Added for Level 3 Chunk 3 One-Time Flash

    // Custom Level Specific State
    private var tideSpeed = 30f // Used for Level 4 Chunk 3 Rising Tide

    // Level 5 Specific State
    data class PlayerRecord(val time: Float, val x: Float, val y: Float, val isCrouching: Boolean)
    private val playerPath = mutableListOf<PlayerRecord>()
    private var chunkTime = 0f

    // Level 6 Chunk 1 variables
    private var isPortalLoopActive = false
    private var hiddenPlatformSpawned = false
    private var hiddenPlatformTime = 0f
    private var realMaskSpawned = false
    private var leftMaskX = 0f
    private var leftMaskY = 0f
    private val leftMaskRect = Rectangle()
    private var hasIdentitySwapped = false
    private var lastGravityFlipTime = 0f
    private var screenFlashColor: Color? = null
    private var screenFlashTimer = 0f
    private var echoX = 640f
    private var echoY = 100f
    private var echoHeight = 50f
    private var echoActive = false

    // Level 5 Chunk 2
    private var isControlsInverted = false
    private val mirrorRect = Rectangle()
    private var mirrorActive = false

    // Level 5 Chunk 3
    private var isCageActive = false
    private var cageAngle = 0f
    private var fakeMaskTouched = false
    private var ceilingLaserDrop = false

    // Level 6 Chunk 3 variables
    private var centerMaskX = 0f
    private var centerMaskY = 0f
    private val centerMaskRect = Rectangle()
    private var hasTouchedRightMask = false

    // --- Mask Physics Specific ---
    private var maskVelocityX = 0f
    private var maskVelocityY = 0f
    private var isMaskFreefalling = false
    private var isSharkRainActive = false

    // Level 7 Chunk 1 Variables
    private var worldTilt = 0f

    // Assets
    private var sharkTexture: Texture? = null
    private var maskTexture: Texture? = null

    // Entities
    data class Shark(
        var x: Float,
        var y: Float,
        var speed: Float,
        val patrolLeft: Float,
        val patrolRight: Float,
        var facingRight: Boolean = false,
        var isSleeper: Boolean = false,
        var isAwake: Boolean = false,
        var isStalker: Boolean = false
    )
    private val sharks = mutableListOf<Shark>()

    private val platforms = mutableListOf<Platform>()

    data class GravitySwitch(val rect: Rectangle, var isActive: Boolean = true)
    private val gravitySwitches = mutableListOf<GravitySwitch>()

    // Level 4 Entities
    data class Laser(
        val rect: Rectangle,
        var isSweeping: Boolean = false,
        var sweepSpeed: Float = 200f,
        var minX: Float = 0f,
        var maxX: Float = 0f,
        var movingRight: Boolean = true,
        var minY: Float = 0f,
        var maxY: Float = 0f
    )
    private val lasers = mutableListOf<Laser>()

    data class MovingWall(
        val rect: Rectangle,
        var speed: Float,
        var isActive: Boolean
    )
    private val movingWalls = mutableListOf<MovingWall>()

    data class GameButton(
        val rect: Rectangle,
        var isPressed: Boolean = false,
        var onHit: (() -> Unit)? = null
    )
    private val gameButtons = mutableListOf<GameButton>()

    private var maskX = 1100f
    private var maskY = 280f + 50f // Default
    private val maskWidth = 30f
    private val maskHeight = 30f

    // Atmosphere
    data class Bubble(var x: Float, var y: Float, var speed: Float, var radius: Float)
    private val bubbles = mutableListOf<Bubble>()
    private val maxBubbles = 20
    private var bubbleSpawnTimer = 0f

    // Roasts
    private val deathRoasts = mutableListOf(
        "You fed the shark.", "Ocean's tax collector.", "He smelled confidence.",
        "Sharp teeth, bad trust.", "That wasn't a dolphin.", "There is no escape.",
        "Darkness consumes you.", "Did you hear that?", "Gravity hurts.",
        // Level 4 specific
        "Grilled to perfection. Serve with a side of failure.",
        "You have the spatial awareness of a broken Roomba.",
        "Squished like a bug. And just as insignificant."
    )
    private val winRoasts = listOf(
        "Don't relax.", "That was bait.", "One step closer to regret.", "Still breathing? Weird."
    )

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var messageLabel: Label? = null
    private var levelLabel: Label? = null
    private var pauseGroup: Table? = null

    // Controls
    private var isLeftPressed = false
    private var isRightPressed = false
    private var isJumpPressed = false
    private var isDownPressed = false

    override fun show() {
        sharkTexture = Texture(Gdx.files.internal("shark.png"))

        // Create Procedural Mask Texture (Cyan Circle)
        val maskPix = Pixmap(32, 32, Pixmap.Format.RGBA8888)
        maskPix.setColor(Color.CLEAR)
        maskPix.fill()
        maskPix.setColor(Color.WHITE)
        maskPix.fillCircle(16, 16, 16)
        maskPix.setColor(Color.CYAN)
        maskPix.fillCircle(16, 16, 12)
        maskTexture = Texture(maskPix)
        maskPix.dispose()

        Gdx.input.inputProcessor = uiStage
        createUi()

        setupChunk(currentChunk)

        // Init Bubbles
        for (i in 0 until 10) spawnBubble(MathUtils.random(720f))
    }

    private fun setupChunk(chunk: Int) {
        sharks.clear()
        platforms.clear()
        gravitySwitches.clear()
        lasers.clear()
        movingWalls.clear()
        gameButtons.clear()

        playerX = 100f
        playerY = 280f
        velocityY = 0f

        isDead = false
        isLevelComplete = false
        stateTimer = 0f
        allowScreenWrap = false
        reverseGravity = false
        gravity = baseGravity

        isPaused = false
        pauseGroup?.isVisible = false
        messageLabel?.isVisible = false
        isControlsInverted = false
        mirrorActive = false
        hasSwappedIdentity = false
        ghostX = -999f
        ghostY = -999f
        blinkTimer = 0f
        blinkStep = false

        worldTilt = 0f
        renderOffset = 0f
        renderOffsetY = 0f
        tideTimer = 0f
        tideTargetOffset = 50f

        horrorMode = (currentLevel == 3)
        strobeTimer = 0f
        isLightsOn = false
        flashlightRadius = 300f // Reset default
        chunk3FlashTimer = 0f // Reset

        if (levelLabel != null) levelLabel!!.setText("Level $currentLevel-$chunk")

        if (currentLevel == 1) {
            setupLevel1(chunk)
        } else if (currentLevel == 2) {
            setupLevel2(chunk)
        } else if (currentLevel == 3) {
            setupLevel3(chunk)
        } else if (currentLevel == 4) {
            setupLevel4(chunk)
        } else if (currentLevel == 5) {
            setupLevel5(chunk)
        } else if (currentLevel == 6) {
            setupLevel6(chunk)
        } else if (currentLevel == 7) {
            setupLevel7(chunk)
        }

        maskRect.set(maskX, maskY, maskWidth, maskHeight)
        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        // Mirror activation for Level 6
        if (currentLevel == 6 && currentChunk == 1 && playerY >= 550f && !mirrorActive) {
            mirrorActive = true
            mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)
        }

    }

    private fun setupLevel1(chunk: Int) {
        maskY = 280f + 50f
        when (chunk) {
            1 -> {
                sharks.add(Shark(600f, 280f, 250f, 300f, 900f))
                maskX = 1100f
            }
            2 -> {
                sharks.add(Shark(500f, 280f, 200f, 300f, 700f))
                sharks.add(Shark(900f, 280f, 280f, 800f, 1100f))
                maskX = 1150f
            }
            3 -> {
                sharks.add(Shark(800f, 280f, 300f, 0f, 1280f, isStalker = true))
                maskX = 1200f
            }
        }
    }

    private fun setupLevel5(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Flappy Bird Escape
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()
                playerPath.clear()

                playerX = 640f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false // Start normal gravity
                chunkTime = 0f
                hasIdentitySwapped = false
                lastGravityFlipTime = 0f
                screenFlashColor = null
                screenFlashTimer = 0f
                echoActive = true // "climb up before ur shadow"
                mirrorActive = false

                // Safe platform at start to land on when gravity reverts
                platforms.add(Platform(Rectangle(600f, 80f, 80f, 20f), PlatformType.NORMAL))

                // The Walls: Symmetrical Red Laser Walls moving inward at 25f
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 25f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -25f, isActive = true))

                // Platforms to "stick" on (climbing up)
                platforms.add(Platform(Rectangle(540f, 200f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(660f, 300f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(540f, 400f, 80f, 20f), PlatformType.CRUMBLING))

                // 2nd last top platform
                platforms.add(Platform(Rectangle(660f, 500f, 100f, 20f), PlatformType.CRUMBLING))

                // top platform (underneath it when gravity reverses)
                platforms.add(Platform(Rectangle(540f, 600f, 100f, 20f), PlatformType.CRUMBLING))

                // The button that appears at start when gravity reverts
                // Initially hide it far away
                val button = GameButton(Rectangle(-2000f, 100f, 40f, 40f), false) {
                    // "mask appears in the left side of the screen about to be cruhed by the left wall approaching it"
                    // Left wall starts at -200, moves 25f/s. After ~6s, it's at x= -50.
                    // Let's spawn mask at x=200, y=400 so player flies up to it.
                    maskX = 350f
                    maskY = 400f
                }
                gameButtons.add(button)

                maskX = -2000f
                maskY = 800f
            }
            2 -> {
                // Chunk 2: The Mirror Maze & Shark Sync
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 540f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false
                isControlsInverted = false
                mirrorActive = true

                // Set initial mirror pos
                mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, normalHeight)

                // Center faint line - maybe no explicit object, just part of background, or invisible laser? We can just not draw it, instruction says "faint vertical line". Let's add an inactive laser as a visual.
                lasers.add(Laser(Rectangle(639f, 0f, 2f, 720f), isSweeping = false)) // Just visual if we don't check collision

                // Safe platforms at start
                platforms.add(Platform(Rectangle(490f, 80f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(690f, 80f, 100f, 20f), PlatformType.NORMAL))

                // Crushing Walls from x=0 and x=1280
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 25f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -25f, isActive = true))

                // Horizontal DEADLY_RED laser at the top (Ceiling)
                platforms.add(Platform(Rectangle(0f, 700f, 1280f, 20f), PlatformType.DEADLY_RED))

                // Staircase of Crumbling Platforms
                platforms.add(Platform(Rectangle(400f, 180f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(800f, 180f, 80f, 20f), PlatformType.CRUMBLING))

                platforms.add(Platform(Rectangle(300f, 280f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(900f, 280f, 80f, 20f), PlatformType.CRUMBLING))

                platforms.add(Platform(Rectangle(400f, 380f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(800f, 380f, 80f, 20f), PlatformType.CRUMBLING))

                // The Inversion Gate platforms
                val leftPlat = Platform(Rectangle(350f, 500f, 100f, 20f), PlatformType.NORMAL)
                val rightPlat = Platform(Rectangle(830f, 500f, 100f, 20f), PlatformType.NORMAL)
                platforms.add(leftPlat)
                platforms.add(rightPlat)

                // The Mask in the center
                maskX = 640f - 16f
                maskY = 550f

                // The Button
                val button = GameButton(Rectangle(380f, 520f, 40f, 40f), false) {
                    isControlsInverted = true
                    // Activate Two Safe Sharks
                    val sharkA = Shark(350f, 440f, 100f, 350f, 520f, facingRight = true)
                    val sharkB = Shark(810f, 440f, 100f, 640f, 810f, facingRight = false)
                    sharks.add(sharkA)
                    sharks.add(sharkB)

                    val sharkPlatA = Platform(Rectangle(350f, 440f, 120f, 60f), PlatformType.SAFE_SHARK)
                    val sharkPlatB = Platform(Rectangle(810f, 440f, 120f, 60f), PlatformType.SAFE_SHARK)
                    // Mark an identifier to help with mapping just in case
                    sharkPlatA.rect.width = 119.9f // Unique width mapping
                    sharkPlatB.rect.width = 120.1f
                    platforms.add(sharkPlatA)
                    platforms.add(sharkPlatB)
                }
                gameButtons.add(button)
            }
            3 -> {
                // Chunk 3: The Grand Betrayal
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()
                playerPath.clear()

                playerX = 640f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false
                isControlsInverted = false
                mirrorActive = false
                echoActive = true
                chunkTime = 0f
                isCageActive = true
                cageAngle = 0f
                fakeMaskTouched = false
                ceilingLaserDrop = false

                // Safe platform at start to land on
                platforms.add(Platform(Rectangle(600f, 80f, 80f, 20f), PlatformType.NORMAL))

                // The Shark
                val centerShark = Shark(580f, 360f, 150f, 300f, 860f)
                sharks.add(centerShark)
                val centerSharkPlatform = Platform(Rectangle(580f, 360f, 120f, 60f), PlatformType.SAFE_SHARK)
                centerSharkPlatform.rect.width = 120.2f // Unique identifier for syncing
                platforms.add(centerSharkPlatform)

                // The Walls
                val leftWall = MovingWall(Rectangle(-400f, 0f, 400f, 1500f), speed = 50f, isActive = true)
                val rightWall = MovingWall(Rectangle(1280f, 0f, 400f, 1500f), speed = -50f, isActive = true)
                movingWalls.add(leftWall)
                movingWalls.add(rightWall)

                // Ceiling Laser
                lasers.add(Laser(Rectangle(0f, 740f, 1280f, 15f), isSweeping = false))

                // The Mask
                maskX = 640f - 16f
                maskY = 600f

                // Buttons
                // Button 1: Stops the Crusher Walls (Bottom Left)
                val btn1 = GameButton(Rectangle(100f, 100f, 40f, 40f), false) {
                    leftWall.speed = 0f
                    rightWall.speed = 0f
                }
                gameButtons.add(btn1)

                // Button 2: Stops the spinning laser cage around the Mask (Top Left)
                val btn2 = GameButton(Rectangle(100f, 600f, 40f, 40f), false) {
                    isCageActive = false
                }
                gameButtons.add(btn2)

                // Button 3: Spawns a second "Mirror Shark" to help you reach the top (Bottom Right), and speeds up walls
                val btn3 = GameButton(Rectangle(1140f, 100f, 40f, 40f), false) {
                    sharks.add(Shark(900f, 480f, 0f, 900f, 900f))
                    platforms.add(Platform(Rectangle(900f, 480f, 120f, 60f), PlatformType.SAFE_SHARK))
                    leftWall.speed = 100f
                    rightWall.speed = -100f
                }
                gameButtons.add(btn3)

                // Button 4: Opens the "Gate" to the Mask (Top Right)
                val btn4 = GameButton(Rectangle(1140f, 600f, 40f, 40f), false) {
                    // We can visually represent this by removing a platform blocking the mask, or just as a requirement.
                    // For now, it just clicks.
                }
                gameButtons.add(btn4)
            }
        }
    }


    private fun setupLevel7(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Tilt Engine
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 640f
                playerY = 160f
                velocityY = 0f
                reverseGravity = false
                isControlsInverted = false
                worldTilt = 0f

                // Base platform
                platforms.add(Platform(Rectangle(540f, 130f, 200f, 20f), PlatformType.CRUMBLING))

                // The Sliding Staircase (Crumbling)
                platforms.add(Platform(Rectangle(400f, 250f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(800f, 350f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(400f, 450f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(800f, 550f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(400f, 650f, 80f, 20f), PlatformType.CRUMBLING))

                // The Symmetrical Shark Slide
                // Safe shark trapped behind a deadly laser
                sharks.add(Shark(640f, 500f, 0f, -2000f, 2000f))
                platforms.add(Platform(Rectangle(640f, 500f, 120f, 60f), PlatformType.SAFE_SHARK))

                // Deadly laser wall trapping the shark
                lasers.add(Laser(Rectangle(620f, 480f, 15f, 100f), isSweeping = false))
                lasers.add(Laser(Rectangle(765f, 480f, 15f, 100f), isSweeping = false))

                // Symmetrical Red Walls closing in at 25f
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 55f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -55f, isActive = true))

                // The Final Goal Mask (The Weight of Trust)
                maskX = 640f + (120f - 30f) / 2f
                maskY = 500f + (60f - 30f) / 2f
            }
            2 -> {
                // Chunk 2: The Gravity Pendulum
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 640f
                playerY = 160f // Safe spawn
                velocityY = 0f
                reverseGravity = false
                isControlsInverted = false
                worldTilt = 0f
                chunkTime = 0f
                maskVelocityX = 0f
                maskVelocityY = 0f
                isMaskFreefalling = false

                // Solid base platform
                platforms.add(Platform(Rectangle(540f, 130f, 200f, 20f), PlatformType.NORMAL))

                // Swinging Safe Sharks (3 sharks)
                // Left Shark
                sharks.add(Shark(300f, 350f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(300f, 350f, 120f, 60f), PlatformType.SAFE_SHARK))
                // Middle Shark
                sharks.add(Shark(580f, 450f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(580f, 450f, 120f, 60f), PlatformType.SAFE_SHARK))
                // Right Shark
                sharks.add(Shark(860f, 600f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(860f, 600f, 120f, 60f), PlatformType.SAFE_SHARK))

                // Symmetrical Red Laser Walls closing in
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 30f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -30f, isActive = true))

                // Laser Cage for Mask
                lasers.add(Laser(Rectangle(610f, 830f, 10f, 70f), isSweeping = false)) // Left cage wall
                lasers.add(Laser(Rectangle(690f, 830f, 10f, 70f), isSweeping = false)) // Right cage wall
                lasers.add(Laser(Rectangle(620f, 830f, 70f, 10f), isSweeping = false)) // Bottom cage wall
                lasers.add(Laser(Rectangle(620f, 890f, 70f, 10f), isSweeping = false)) // Top cage wall

                maskX = 640f
                maskY = 850f

                // Yellow lines (GameButtons) above the sharks
                gameButtons.add(GameButton(Rectangle(340f, 410f, 40f, 10f))) // Above Left Shark
                gameButtons.add(GameButton(Rectangle(620f, 510f, 40f, 10f))) // Above Middle Shark
                gameButtons.add(GameButton(Rectangle(900f, 660f, 40f, 10f))) // Above Right Shark
            }
        }
    }

    private fun setupLevel6(chunk: Int) {
        when (chunk) {
            3 -> {
                // Chunk 3: The Paradox
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 640f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false
                isControlsInverted = true
                mirrorActive = false // We handle coloring directly via chunk checks
                hasTouchedRightMask = false

                // The Squeeze: Symmetrical Red Walls close in from edges at 45f speed.
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 45f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -45f, isActive = true))

                // The Three Masks
                leftMaskX = 300f
                leftMaskY = 450f
                centerMaskX = 640f
                centerMaskY = 450f
                maskX = 980f // Right mask is the real one initially
                maskY = 450f

                // Base platform to stand on
                platforms.add(Platform(Rectangle(540f, 80f, 200f, 20f), PlatformType.NORMAL))
                playerY = 100f
                playerX = 640f - playerWidth / 2f

                isCageActive = false // No more cage around right mask
                cageAngle = 0f
                fakeMaskTouched = false
                chunkTime = 0f
                isSharkRainActive = true
            }
            2 -> {
                // Chunk 2: The Mirror Swap Portal
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 100f
                playerY = 320f
                velocityY = 0f
                reverseGravity = false
                hasSwappedIdentity = false
                mirrorActive = true // Start with mirror logic active

                // First Mask (Trigger)
                maskX = 300f
                maskY = 400f

                // All platforms are crumbling
                platforms.add(Platform(Rectangle(50f, 280f, 150f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(1080f, 280f, 150f, 20f), PlatformType.CRUMBLING))

                // The Squeeze: Symmetrical Red Walls close in from the edges at 30f speed.
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 30f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -30f, isActive = true))

                // Platform near Trigger Mask
                platforms.add(Platform(Rectangle(280f, 380f, 100f, 20f), PlatformType.CRUMBLING))
            }
            1 -> {
                // Chunk 1: The Infinite Loop Portal
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 620f
                playerY = 250f
                velocityY = 0f
                reverseGravity = false
                isPortalLoopActive = false
                hiddenPlatformSpawned = false
                hiddenPlatformTime = 0f
                realMaskSpawned = false

                // Base platform at y=150f (wide enough to easily catch player)
                platforms.add(Platform(Rectangle(540f, 150f, 200f, 20f), PlatformType.CRUMBLING))

                // The Squeeze: Two Symmetrical Red Laser Walls moving at 15f
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 15f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -15f, isActive = true))

                // The Path: 4 Crumbling platforms zig-zagging up
                platforms.add(Platform(Rectangle(380f, 230f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(760f, 310f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(420f, 390f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(600f, 470f, 100f, 20f), PlatformType.CRUMBLING))

                // Fake Mask on the left of the highest platform
                maskX = 550f
                maskY = 480f
            }
        }
    }


    private fun setupLevel4(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Troll Setup (Physics Fix)
                // --- START CHUNK 1 GAP TUNE ---
                // Clear the board
                platforms.clear()
                lasers.clear()
                gravitySwitches.clear()
                gameButtons.clear()
                sharks.clear()

                // 1. Player Spawn (y=350)
                playerX = 50f
                playerY = 350f
                velocityY = 0f
                reverseGravity = false
                isLevelComplete = false

                // 2. The Platforms (y=300)
                platforms.add(Platform(Rectangle(0f, 300f, 150f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(250f, 300f, 150f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(500f, 300f, 150f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(750f, 300f, 150f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(1000f, 300f, 200f, 20f), PlatformType.CRUMBLING))

                // 3. SHARK LINING (2px Gap)
                // Y=238f. Height=60f. Top=298f.
                // Platform Bottom=300f.
                // Visually distinct from the platform (2px gap), but still instant death on fall.
                for (i in 0..1200 step 80) {
                    sharks.add(Shark(i.toFloat(), 238f, 0f, 0f, 0f))
                }

                // 4. The Mask & Laser (Speed 580f)
                maskX = 1100f
                maskY = 320f
                lasers.add(Laser(Rectangle(200f, 300f, 15f, 310f), isSweeping = true, sweepSpeed = 580f, minX = 100f, maxX = 1200f))

                // 5. The Hidden Ceiling & Escape Switch
                val ceilingPlat = Platform(Rectangle(-2000f, 680f, 300f, 500f), PlatformType.CRUMBLING)
                platforms.add(ceilingPlat)

                val downSwitch = GameButton(Rectangle(-2000f, 610f, 40f, 40f), false) {
                    reverseGravity = false
                }
                gameButtons.add(downSwitch)

                // 6. The Troll Switches
                val fakeSwitch = GameButton(Rectangle(300f, 450f, 40f, 40f), false) {
                    sharks.add(Shark(300f, 450f, 0f, 300f, 300f))
                }
                gameButtons.add(fakeSwitch)

                val realSwitch = GameButton(Rectangle(380f, 450f, 40f, 40f), false) {
                    reverseGravity = true
                    ceilingPlat.rect.x = 250f
                    ceilingPlat.rect.y = 680f
                    downSwitch.rect.x = 450f
                    downSwitch.rect.y = 610f
                }
                gameButtons.add(realSwitch)
                // --- END CHUNK 1 GAP TUNE ---
            }
            2 -> {
                // --- START CHUNK 2 FINAL FIX ---
                platforms.clear()
                sharks.clear()
                lasers.clear()
                gameButtons.clear()
                gravitySwitches.clear()

                // 1. Absolute Bottom Spawn (beneath the first layer)
                playerX = 640f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false

                // 2. Base Floor
                platforms.add(Platform(Rectangle(0f, 80f, 1280f, 20f), PlatformType.NORMAL))

                // 3. The Red Crusher Walls (Lethal)
                // They start at edges and move towards the center
                platforms.add(Platform(Rectangle(-200f, 0f, 200f, 1000f), PlatformType.DEADLY_RED))
                platforms.add(Platform(Rectangle(1280f, 0f, 200f, 1000f), PlatformType.DEADLY_RED))

                // 4. The First Layer (Layer 1) - Must be higher than 100f to be "above" the player
                val layer1Y = 220f
                platforms.add(Platform(Rectangle(200f, layer1Y, 150f, 20f), PlatformType.DEADLY_RED)) // Fall-through trap
                platforms.add(Platform(Rectangle(500f, layer1Y, 80f, 60f), PlatformType.SAFE_SHARK)) // Solid safe point
                sharks.add(Shark(800f, layer1Y, 0f, 800f, 800f)) // Deadly shark trap
                platforms.add(Platform(Rectangle(1000f, layer1Y, 150f, 20f), PlatformType.CRUMBLE_FAST)) // Instant crumble

                // 5. The Second Layer (Layer 2) - 3 green platforms (crumbling)
                val layer2Y = 340f
                platforms.add(Platform(Rectangle(300f, layer2Y, 150f, 20f), PlatformType.CRUMBLE_FAST))
                platforms.add(Platform(Rectangle(600f, layer2Y, 150f, 20f), PlatformType.CRUMBLE_FAST))
                platforms.add(Platform(Rectangle(900f, layer2Y, 150f, 20f), PlatformType.CRUMBLE_FAST))

                // 6. The Third Layer (Layer 3) - 1 safe shark and 1 deadly green platform
                val layer3Y = 460f
                platforms.add(Platform(Rectangle(400f, layer3Y, 80f, 60f), PlatformType.SAFE_SHARK)) // Safe
                platforms.add(Platform(Rectangle(700f, layer3Y, 150f, 20f), PlatformType.DEADLY_RED)) // Deadly platform

                // 7. Symmetrical moving lasers
                // Laser on left sweeping right
                lasers.add(Laser(Rectangle(0f, 80f, 15f, 600f), isSweeping = true, sweepSpeed = 80f, minX = 0f, maxX = 640f))
                // Laser on right sweeping left
                lasers.add(Laser(Rectangle(1280f, 80f, 15f, 600f), isSweeping = true, sweepSpeed = -80f, minX = 640f, maxX = 1280f))

                // 8. The Goal (High up as if it's the 4th layer)
                maskX = 640f
                maskY = 600f
                // --- END CHUNK 2 FINAL FIX ---
            }
            3 -> {
                // Chunk 3: The Ultimate Troll
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                // 1. SPAWN & ENVIRONMENT
                // Grounded Spawn: Ensure the player starts at y=100f on a solid green platform located at y=80f. No hovering.
                playerX = 100f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false

                platforms.add(Platform(Rectangle(50f, 80f, 200f, 20f), PlatformType.NORMAL))

                // Layout: The "Troll Path" - horizontal series at y=200, then climbing left.
                val trollPlatforms = mutableListOf<Platform>()

                // 1. Button 1 Platform
                trollPlatforms.add(Platform(Rectangle(250f, 200f, 100f, 20f), PlatformType.CRUMBLING))
                // 2. The Shark Platform (Small, requires jump over)
                trollPlatforms.add(Platform(Rectangle(430f, 200f, 80f, 20f), PlatformType.CRUMBLING))
                // 3. Mask Platform (formerly Button 2 Platform)
                trollPlatforms.add(Platform(Rectangle(580f, 200f, 100f, 20f), PlatformType.CRUMBLING))

                // 4. The Climb (Back left towards the Button 2)
                trollPlatforms.add(Platform(Rectangle(480f, 320f, 150f, 20f), PlatformType.CRUMBLING))
                trollPlatforms.add(Platform(Rectangle(280f, 440f, 150f, 20f), PlatformType.CRUMBLING))

                // 5. Button 2 Platform (Extreme left above)
                trollPlatforms.add(Platform(Rectangle(50f, 560f, 150f, 20f), PlatformType.CRUMBLING))

                platforms.addAll(trollPlatforms)

                // 2. THE HAZARDS (SYMMETRICAL SWEEP)
                // Use strictly positive sweepSpeed values (Initial: 15f)
                // They start off-screen to give the player breathing room and move towards each other.
                // Distance to cover is 372.5 for both.
                val topLaser = Laser(Rectangle(0f, 740f, 1280f, 15f), isSweeping = true, sweepSpeed = 15f, minY = 367.5f, maxY = 740f, movingRight = false) // movingRight = false means moving DOWN for vertical lasers
                lasers.add(topLaser)

                val bottomLaser = Laser(Rectangle(0f, -20f, 1280f, 15f), isSweeping = true, sweepSpeed = 15f, minY = -20f, maxY = 352.5f, movingRight = true) // movingRight = true means moving UP for vertical lasers
                lasers.add(bottomLaser)

                // The Mask starts hidden off-screen
                maskX = -2000f
                maskY = 220f

                // 3. THE BUTTON LOGIC
                // Button 2 (Platform 6, top left) - initially hidden
                val button2 = GameButton(Rectangle(-2000f, 580f, 40f, 40f), false) {
                    // Spawn Mask at Platform 3 (bottom right)
                    maskX = 610f
                    maskY = 220f

                    // Increase laser speed significantly for the frantic race back down
                    topLaser.sweepSpeed = 65f
                    bottomLaser.sweepSpeed = 65f

                    // Respawn platforms and reset them to ACTIVE so the player can go back
                    for (plat in trollPlatforms) {
                        plat.state = PlatformState.ACTIVE
                        plat.crumbleTimer = 0f
                        if (!platforms.contains(plat)) {
                            platforms.add(plat)
                        }
                    }
                }
                gameButtons.add(button2)

                // Button 1 (Platform 1)
                val button1 = GameButton(Rectangle(280f, 220f, 40f, 40f), false) {
                    // Make the laser "turtle slow" for the climb up
                    topLaser.sweepSpeed = 5f
                    bottomLaser.sweepSpeed = 5f

                    // Spawn a Shark at the center of Platform 2 (x=410f, y=220f).
                    // The player must jump over it from Platform 1 to Platform 3.
                    sharks.add(Shark(410f, 220f, 0f, 410f, 410f))

                    // Spawn Button 2 on Platform 6 (top left)
                    button2.rect.x = 100f
                    button2.rect.y = 580f
                }
                gameButtons.add(button1)
            }
        }
    }

    private fun setupLevel2(chunk: Int) {
        maskY = 280f + 50f
        when (chunk) {
            1 -> {
                sharks.add(Shark(400f, 280f, 350f, 200f, 800f))
                sharks.add(Shark(700f, 280f, 350f, 500f, 1100f))
                platforms.add(Platform(Rectangle(600f, 360f, 150f, 20f), PlatformType.CRUMBLING))
                maskX = 1100f
            }
            2 -> {
                sharks.add(Shark(500f, 280f, 400f, 300f, 900f))
                sharks.add(Shark(900f, 280f, 400f, 700f, 1200f))
                platforms.add(Platform(Rectangle(400f, 380f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(800f, 380f, 100f, 20f), PlatformType.CRUMBLING))
                maskX = 1150f
            }
            3 -> {
                allowScreenWrap = true
                sharks.add(Shark(640f, 280f, 320f, 0f, 1280f, isStalker = true))
                platforms.add(Platform(Rectangle(600f, 350f, 150f, 20f), PlatformType.CRUMBLING))
                maskX = 1150f
            }
        }
    }

    private fun setupLevel3(chunk: Int) {
        // EMERGENCY OVERRIDE - Replace Level 3 Logic
        // Pitch Black Theme
        horrorMode = true
        // Floor is NOT present (pit death), so platforms are critical.
        playerX = 100f
        playerY = 350f // Safe Spawn Height

        when (chunk) {
            1 -> {
                // Chunk 1: Two Sharks (Harder)
                playerX = 100f; playerY = 350f
                platforms.add(Platform(Rectangle(100f, 200f, 100f, 20f), PlatformType.NORMAL)) // Safe start

                // Crumbling Path - CHANGED TO CRUMBLING
                platforms.add(Platform(Rectangle(300f, 300f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(500f, 400f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(700f, 300f, 100f, 20f), PlatformType.CRUMBLING))

                // Hazards
                // TUNED: Increased speed to 160f for tighter pressure
                sharks.add(Shark(400f, 300f, 160f, 300f, 500f)) // Shark 1 - Patrol
                sharks.add(Shark(800f, 300f, 160f, 700f, 900f)) // Shark 2 - Patrol

                maskX = 1000f; maskY = 300f
            }
            2 -> {
                // Chunk 2: The Gravity Combo (Structured)
                playerX = 100f; playerY = 350f
                platforms.add(Platform(Rectangle(100f, 200f, 100f, 20f), PlatformType.NORMAL)) // Start

                // The Combo Layout
                gravitySwitches.add(GravitySwitch(Rectangle(400f, 250f, 40f, 40f))) // Switch A (Up)

                // TOP PLATFORM: Must be CRUMBLING (Red/Vanish)
                platforms.add(Platform(Rectangle(500f, 550f, 150f, 20f), PlatformType.CRUMBLING)) // Ceiling Catch

                gravitySwitches.add(GravitySwitch(Rectangle(700f, 350f, 40f, 40f))) // Switch B (Down)

                // BOTTOM PLATFORM: Moved to x=700 to catch the fall
                platforms.add(Platform(Rectangle(700f, 150f, 150f, 20f), PlatformType.CRUMBLING)) // Floor Catch

                // Mask: Move to 900f (Was 1100f)
                maskX = 900f; maskY = 200f

                // The Stalker: FAST (230f) + Spawn Off-Screen (-200f)
                sharks.add(Shark(-200f, 300f, 230f, 0f, 1280f, isStalker = true))
            }
            3 -> {
                // Chunk 3: The Pulse (Keep existing logic)
                isLightsOn = true // START ON for 2.0s
                chunk3FlashTimer = 0f

                maskX = 1150f
                maskY = 280f + 50f

                // Safe Start
                platforms.add(Platform(Rectangle(100f, 200f, 100f, 20f), PlatformType.NORMAL))

                // Staircase Pattern (Up, Down, Up) - ALL CRUMBLING
                // Up
                platforms.add(Platform(Rectangle(300f, 300f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(450f, 400f, 100f, 20f), PlatformType.CRUMBLING))

                // Down (with Stationary Sharks)
                platforms.add(Platform(Rectangle(600f, 300f, 100f, 20f), PlatformType.CRUMBLING))
                sharks.add(Shark(600f, 350f, 0f, 600f, 700f)) // Sentry

                platforms.add(Platform(Rectangle(750f, 200f, 100f, 20f), PlatformType.CRUMBLING))
                // Shark REMOVED here to create safe landing rhythm

                // Up to Exit
                platforms.add(Platform(Rectangle(900f, 300f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(1050f, 400f, 150f, 20f), PlatformType.CRUMBLING))
            }
        }
    }

    private fun completeChunk() {
        val nextChunk = currentChunk + 1
        val isEndOfLevel = nextChunk > 3 || (currentLevel == 7 && nextChunk > 2)

        val prefs = Gdx.app.getPreferences("TrustIssues")
        val savedMaxChunk = prefs.getInteger("level_${currentLevel}_maxChunk", 1)
        if (nextChunk > savedMaxChunk && !isEndOfLevel) {
            prefs.putInteger("level_${currentLevel}_maxChunk", nextChunk).flush()
        }

        if (isEndOfLevel) {
            val nextLevel = currentLevel + 1
            val unlocked = prefs.getInteger("unlockedLevel", 1)
            if (nextLevel > unlocked) {
                prefs.putInteger("unlockedLevel", nextLevel).flush()
                prefs.putInteger("level_${nextLevel}_maxChunk", 1).flush()
            }
            game.screen = if (nextLevel > 7) LevelSelectScreen(game) else GameScreen(game, nextLevel, 1)
        } else {
            game.screen = GameScreen(game, currentLevel, nextChunk)
        }
        dispose()
    }

    private fun spawnBubble(startY: Float = -20f) {
        val x = MathUtils.random(1280f)
        val speed = MathUtils.random(50f, 150f)
        val radius = MathUtils.random(2f, 8f)
        val actualY = if (startY == -20f) MathUtils.random(-50f, 280f) else startY
        bubbles.add(Bubble(x, actualY, speed, radius))
    }

    private fun createProceduralTexture(type: String): Texture {
        val size = 120
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.CLEAR)
        pixmap.fill()
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(size/2, size/2, size/2 - 2)
        pixmap.setColor(Color.BLACK)
        val cx = size/2; val cy = size/2
        when(type) {
            "LEFT" -> {
                val offset = 20
                for(i in 0..5) pixmap.drawLine(cx + offset - i, cy - offset, cx - offset - i, cy)
                for(i in 0..5) pixmap.drawLine(cx - offset - i, cy, cx + offset - i, cy + offset)
            }
            "RIGHT" -> {
                val offset = 20
                for(i in 0..5) pixmap.drawLine(cx - offset + i, cy - offset, cx + offset + i, cy)
                for(i in 0..5) pixmap.drawLine(cx + offset + i, cy, cx - offset + i, cy + offset)
            }
            "PAUSE" -> {
                val w = 10; val h = 30; val gap = 10
                pixmap.fillRectangle(cx - gap - w, cy - h/2, w, h)
                pixmap.fillRectangle(cx + gap, cy - h/2, w, h)
            }
        }
        val tex = Texture(pixmap)
        pixmap.dispose()
        return tex
    }

    private fun createUi() {
        skin = Skin()
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE); pixmap.fill()
        whiteTexture = Texture(pixmap); pixmap.dispose()
        skin!!.add("white", whiteTexture)

        buttonFont = game.generateFont(40)
        skin!!.add("default-font", buttonFont)

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin!!.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.down = skin!!.newDrawable("white", Color.GRAY)
        textButtonStyle.font = buttonFont
        textButtonStyle.fontColor = Color.WHITE
        skin!!.add("default", textButtonStyle)

        val labelStyle = Label.LabelStyle(buttonFont, Color.RED)
        skin!!.add("default", labelStyle)

        val leftTex = createProceduralTexture("LEFT")
        val rightTex = createProceduralTexture("RIGHT")
        val pauseTex = createProceduralTexture("PAUSE")

        val imageBtnStyle = ImageButton.ImageButtonStyle()
        imageBtnStyle.up = TextureRegionDrawable(leftTex)
        imageBtnStyle.down = TextureRegionDrawable(leftTex).tint(Color.GRAY)
        skin!!.add("left", imageBtnStyle)

        val rightStyle = ImageButton.ImageButtonStyle()
        rightStyle.up = TextureRegionDrawable(rightTex)
        rightStyle.down = TextureRegionDrawable(rightTex).tint(Color.GRAY)
        skin!!.add("right", rightStyle)

        val pauseStyle = ImageButton.ImageButtonStyle()
        pauseStyle.up = TextureRegionDrawable(pauseTex)
        pauseStyle.down = TextureRegionDrawable(pauseTex).tint(Color.GRAY)
        skin!!.add("pause", pauseStyle)

        val hudStyle = Label.LabelStyle(buttonFont, Color.YELLOW)
        levelLabel = Label("Level $currentLevel-$currentChunk", hudStyle)
        levelLabel!!.setPosition(20f, 720f - 50f)
        uiStage.addActor(levelLabel!!)

        val pauseBtn = ImageButton(skin!!.get("pause", ImageButton.ImageButtonStyle::class.java))
        pauseBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isDead && !isLevelComplete) {
                    isPaused = true
                    pauseGroup?.isVisible = true
                }
                return true
            }
        })
        pauseBtn.setPosition(20f, 720f - 120f)
        pauseBtn.setSize(60f, 60f)
        uiStage.addActor(pauseBtn)

        pauseGroup = Table()
        pauseGroup!!.setFillParent(true)
        pauseGroup!!.isVisible = false
        val dimPix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        dimPix.setColor(0f, 0f, 0f, 0.7f); dimPix.fill()
        val dimTex = Texture(dimPix); dimPix.dispose()
        pauseGroup!!.background = TextureRegionDrawable(com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex))

        val resumeBtn = TextButton("RESUME", skin)
        resumeBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isPaused = false
                pauseGroup?.isVisible = false
                return true
            }
        })
        val homeBtn = TextButton("HOME", skin)
        homeBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                Gdx.app.postRunnable { game.screen = LevelSelectScreen(game); dispose() }
                return true
            }
        })
        val pauseCenter = Table()
        pauseCenter.add(Label("PAUSED", hudStyle)).padBottom(50f).row()
        pauseCenter.add(resumeBtn).size(200f, 80f).padBottom(20f).row()
        pauseCenter.add(homeBtn).size(200f, 80f)
        pauseGroup!!.add(pauseCenter).center()
        uiStage.addActor(pauseGroup!!)



        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isJumpPressed = true
                if (!isPaused && !isDead && !isLevelComplete) {
                    val currentJumpStrength = if (currentLevel == 5 && (currentChunk == 2 || currentChunk == 3)) 500f else jumpStrength
                    // "tap the jump button again and again to fly ofk like flappy bird"
                    if ((currentLevel == 5 && (currentChunk == 1 || currentChunk == 2 || currentChunk == 3)) ||
                        (currentLevel == 6 && currentChunk == 3) ||
                        (currentLevel == 7 && (currentChunk == 1 || currentChunk == 2))) {
                        if (reverseGravity) {
                            velocityY = -currentJumpStrength
                        } else {
                            velocityY = currentJumpStrength
                        }
                    } else {
                        if (reverseGravity) {
                            if (canJump) velocityY = -currentJumpStrength
                        } else {
                            if (canJump) velocityY = currentJumpStrength
                        }
                    }
                }
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isJumpPressed = false

            }
        })

        uiStage.addActor(jumpZone)

        val rootTable = Table(); rootTable.setFillParent(true); rootTable.bottom()

        val leftBtn = ImageButton(skin!!.get("left", ImageButton.ImageButtonStyle::class.java))
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isLeftPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) {
                isLeftPressed = false
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    if (isControlsInverted) {
                        driftDirection = 1
                    } else {
                        driftDirection = -1
                    }
                    // driftTimer = 2.0f
                    // isDrifting = true
                }
            }
        })
        val rightBtn = ImageButton(skin!!.get("right", ImageButton.ImageButtonStyle::class.java))
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isRightPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) {
                isRightPressed = false
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    if (isControlsInverted) {
                        driftDirection = -1
                    } else {
                        driftDirection = 1
                    }
                    // driftTimer = 2.0f
                    // isDrifting = true
                }
            }
        })
        val leftControls = Table()
        leftControls.add(leftBtn).size(120f, 120f).padRight(60f)
        leftControls.add(rightBtn).size(120f, 120f)
        rootTable.add(leftControls).left().pad(20f).expandX()

        messageLabel = Label("", skin)
        messageLabel!!.isVisible = false
        uiStage.addActor(messageLabel!!)
        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        // Fix for Global Black Screen Bug:
        // Explicitly check currentLevel to determine background color.
        if (currentLevel == 3) {
            ScreenUtils.clear(0f, 0f, 0f, 1f) // Pitch Black (Horror)
        } else {
            ScreenUtils.clear(0.2f, 0.4f, 0.8f, 1f) // Standard Ocean Blue
        }

        uiStage.act(Gdx.graphics.deltaTime)

        if (!isPaused) {
            update(delta)
        } else {
            Gdx.input.inputProcessor = uiStage
        }

        draw() // Draw calls now handle visibility internally
        uiStage.draw()
    }

    private fun update(delta: Float) {
        if (isDead || isLevelComplete) {
            stateTimer += delta
            if (stateTimer >= 2.0f) {
                // Prevent queue spam by resetting stateTimer below the threshold immediately
                stateTimer = -9999f

                // User requested: "the game doesnt restarts after death and thers is just roast on the screen...fix it"
                // So we SHOULD restart.
                val wasDead = isDead
                Gdx.app.postRunnable {
                    if (wasDead) setupChunk(currentChunk) else completeChunk()
                }
            }
            return
        }

        // --- LEVEL 6 CHUNK 3 LOGIC (The Paradox) ---
        if (currentLevel == 6 && currentChunk == 3 && !isDead && !isLevelComplete) {
            leftMaskRect.set(leftMaskX, leftMaskY, maskWidth, maskHeight)
            centerMaskRect.set(centerMaskX, centerMaskY, maskWidth, maskHeight)

            // 1. Flappy Bird Mechanics
            canJump = true

            // 2. The 6 Second Secret
            chunkTime += delta
            if (chunkTime >= 6f && centerMaskY > playerY) {
                // The middle mask falls on the player
                centerMaskY -= 400f * delta
                // Make it safe
                if (Intersector.overlaps(playerRect, centerMaskRect)) {
                    win()
                    return
                }
                // Also check if it hits the floor (y=100) or player
                if (centerMaskY <= 100f) {
                    centerMaskY = 100f
                }
            } else if (chunkTime < 6f && Intersector.overlaps(playerRect, centerMaskRect)) {
                // "burn u with lasers as soon as u touch it before the 7th second"
                lasers.add(Laser(Rectangle(playerX - 10f, playerY, 40f, 800f), isSweeping = false))
                die("Grilled to perfection. Serve with a side of impatience.")
                return
            }

            // Left Mask (First Mask): "the raining stops and u can get back whereever u want"
            if (Intersector.overlaps(playerRect, leftMaskRect)) {
                isSharkRainActive = false
            }

            if (isSharkRainActive) {
                // "raining sharks all over the screen but gives u windown"
                if (MathUtils.randomBoolean(0.05f)) { // Adjusted for a fairer window
                    val randomX = MathUtils.random(0f, 1280f)
                    sharks.add(Shark(randomX, 720f, 0f, randomX, randomX))
                }
                val iter = sharks.iterator()
                while(iter.hasNext()) {
                    val shark = iter.next()
                    shark.y -= 800f * delta
                    if (Intersector.overlaps(playerRect, Rectangle(shark.x, shark.y, 120f, 60f))) {
                        die("Cloudy with a chance of meat-eating predators!")
                        return
                    }
                    if (shark.y < -100f) {
                        iter.remove()
                    }
                }
            } else {
                // Raining stopped. Clear the ones that are still falling offscreen
                val iter = sharks.iterator()
                while(iter.hasNext()) {
                    val shark = iter.next()
                    shark.y -= 800f * delta
                    if (Intersector.overlaps(playerRect, Rectangle(shark.x, shark.y, 120f, 60f))) {
                        die("Cloudy with a chance of meat-eating predators!")
                        return
                    }
                    if (shark.y < -100f) {
                        iter.remove()
                    }
                }
            }

            // Lasered Mask (Right Mask): "speed up the wall so fast that u die by squishing"
            // Wait, he said "lasered mask... speed up wall". So no reverse gravity anymore??
            // The prompt originally said "Physics Betrayal". "if u obtain the lasered mask...it eventually speed up the wall so fast that u die by squishing".
            // So touching right mask does NOT win anymore? It squishes you? Then how do you win? "if u wait for exactly 7 seconds on the platfrom the middle mask is just gonna fall on u and ur chunk is successfully completed". So that's the ONLY win condition.
            if (Intersector.overlaps(playerRect, maskRect)) {
                hasTouchedRightMask = true
                for (wall in movingWalls) {
                    if (wall.speed > 0) wall.speed = 900f
                    else wall.speed = -900f
                }
            }

            // Re-bind masks
            leftMaskRect.set(leftMaskX, leftMaskY, maskWidth, maskHeight)
            centerMaskRect.set(centerMaskX, centerMaskY, maskWidth, maskHeight)
        }

        // --- LEVEL 6 CHUNK 2 LOGIC (The Mirror Swap Portal) ---
        if (currentLevel == 6 && currentChunk == 2 && !isDead && !isLevelComplete) {
            if (!hasSwappedIdentity) {
                // Standard mirror tracking logic
                mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)

                // Swap Portal Event
                if (Intersector.overlaps(playerRect, maskRect)) {
                    hasSwappedIdentity = true
                    ghostX = playerX
                    ghostY = playerY
                    playerX = 1280f - playerWidth - playerX // Takeover Red body pos
                    isControlsInverted = true

                    screenFlashColor = com.badlogic.gdx.graphics.Color.WHITE
                    screenFlashTimer = 0.1f

                    // Goal Mask appears at top left
                    maskX = 200f
                    maskY = 660f
                    maskRect.set(maskX, maskY, maskWidth, maskHeight)

                    // Spawn catch platform for the falling red mirror body
                    platforms.add(Platform(Rectangle(800f, 100f, 400f, 20f), PlatformType.CRUMBLING))

                    // Initial stairway state: Spawn first steps immediately
                    blinkStep = true
                    blinkTimer = 0f
                    platforms.add(Platform(Rectangle(700f, 250f, 99f, 20f), PlatformType.CRUMBLING))
                    platforms.add(Platform(Rectangle(300f, 550f, 99f, 20f), PlatformType.CRUMBLING))
                }
            } else {
                // Ghost stays stationary as a DEADLY_RED trap
                mirrorRect.set(ghostX, ghostY, playerWidth, playerHeight)

                if (Intersector.overlaps(playerRect, mirrorRect)) {
                    die("You're just a ghost in your own game now.")
                    return
                }

                // Blinking Stairway Logic
                blinkTimer += delta
                if (blinkTimer > 1.5f) {
                    blinkTimer = 0f
                    blinkStep = !blinkStep

                    // Clear old stairway platforms using unique width 99f to avoid removing the catch platform
                    platforms.removeAll { it.rect.width == 99f }

                    if (blinkStep) {
                        platforms.add(Platform(Rectangle(700f, 250f, 99f, 20f), PlatformType.CRUMBLING))
                        platforms.add(Platform(Rectangle(300f, 550f, 99f, 20f), PlatformType.CRUMBLING))
                    } else {
                        platforms.add(Platform(Rectangle(500f, 400f, 99f, 20f), PlatformType.CRUMBLING))
                        platforms.add(Platform(Rectangle(200f, 640f, 99f, 20f), PlatformType.CRUMBLING)) // Platform near goal
                    }
                }

                // Win Condition
                if (Intersector.overlaps(playerRect, maskRect)) {
                    win()
                }
            }
        }

        // --- FORCED CHUNK 2 LOGIC (BRUTE FORCE) ---
        if (currentLevel == 4 && currentChunk == 2) {
             // 1. Move Walls (Speed 45f)
             platforms.forEach { p ->
                 if (p.rect.height == 1000f) {
                     if (p.rect.x < 400f) p.rect.x += 45f * delta
                     if (p.rect.x > 400f) p.rect.x -= 45f * delta
                 }
             }
             // 2. Instant Death (Kill, don't hard reset immediately)
             if (platforms.any { it.type == PlatformType.DEADLY_RED && playerRect.overlaps(it.rect) }) {
                 die()
                 return
             }
             // 3. Fall through death
             if (playerY < 0f) {
                 die("Dropped like a stone.")
                 return
             }
        }
        // ------------------------------------------

        // Update mask collision rect constantly (in case it moves, like in Level 4-3)
        maskRect.set(maskX, maskY, maskWidth, maskHeight)




        // --- LEVEL 6 CHUNK 1 LOGIC (Sticky Drift & Hardware Betrayal) ---
        if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
            // Mirror collision death
            if (mirrorActive && Intersector.overlaps(playerRect, mirrorRect)) {
                die("Stop fighting the drift. Trust the void.")
                // stateTimer = -9999f
                return
            }

            // Laser Cage Trap logic
            if (Intersector.overlaps(playerRect, maskRect)) {
                // Determine if drifting or actively holding
                // Drift is true if driftTimer > 0
                val activelyHolding = isLeftPressed || isRightPressed || isJumpPressed
                // if they are actively holding OR jump is held down OR we are NOT drifting, they die
                if (activelyHolding ) {
                    die("You can't even control your own thumbs, let alone this game.")
                    // stateTimer = -9999f
                    return
                } else {
                    win()
                }
            }
        }
        // ----------------------------------------------------------------

        // --- LEVEL 5 CHUNK 1 LOGIC (Flappy Bird) ---
        if (currentLevel == 5 && currentChunk == 1 && !isDead && !isLevelComplete) {
            chunkTime += delta

            // "antigravity appears a lil late...it should appear after 3 sec in the game....and then after 3 the gravity happens again"
            if (chunkTime >= 3.0f && chunkTime < 6.0f && !reverseGravity) {
                reverseGravity = true
                screenFlashColor = com.badlogic.gdx.graphics.Color.CYAN
                screenFlashTimer = 0.1f
            } else if (chunkTime >= 6.0f && reverseGravity) {
                reverseGravity = false
                // "as soon as gravity happens after antigravity there must appear a button on the first platform"
                if (gameButtons.isNotEmpty()) {
                    gameButtons[0].rect.x = 620f
                    gameButtons[0].rect.y = 100f
                }
                screenFlashColor = com.badlogic.gdx.graphics.Color.CYAN
                screenFlashTimer = 0.1f
            }

            // "if u fly off the screen u die" (top ceiling death)
            if (playerY > 720f) {
                die("Flew too close to the sun.")
                return
            }

            // The echo continues to follow the player
            playerPath.add(PlayerRecord(chunkTime, playerX, playerY, playerHeight < normalHeight))
            while (playerPath.isNotEmpty() && chunkTime - playerPath.first().time > 2.5f) {
                playerPath.removeAt(0)
            }

            val echoTargetTime = chunkTime - 2.0f
            if (echoTargetTime >= 0f && echoActive) {
                var closestRecord = playerPath.first()
                for (record in playerPath) {
                    if (record.time <= echoTargetTime) {
                        closestRecord = record
                    } else {
                        break
                    }
                }
                echoX = closestRecord.x
                echoY = closestRecord.y
                echoHeight = if (closestRecord.isCrouching) crouchHeight else normalHeight

                echoRect.set(echoX, echoY, playerWidth, echoHeight)
                if (com.badlogic.gdx.math.Intersector.overlaps(playerRect, echoRect)) {
                    die("Your past caught up to you.")
                    return
                }
            } else {
                echoX = 640f
                echoY = 100f
                echoHeight = normalHeight
            }
        }

        // --- LEVEL 5 CHUNK 3 LOGIC (The Grand Betrayal) ---
        if (currentLevel == 5 && currentChunk == 3 && !isDead && !isLevelComplete) {
            chunkTime += delta

            // The Echo follows with 3-second delay
            playerPath.add(PlayerRecord(chunkTime, playerX, playerY, playerHeight < normalHeight))
            while (playerPath.isNotEmpty() && chunkTime - playerPath.first().time > 3.5f) {
                playerPath.removeAt(0)
            }

            val echoTargetTime = chunkTime - 3.0f
            if (echoTargetTime >= 0f && echoActive) {
                var closestRecord = playerPath.first()
                for (record in playerPath) {
                    if (record.time <= echoTargetTime) {
                        closestRecord = record
                    } else {
                        break
                    }
                }
                echoX = closestRecord.x
                echoY = closestRecord.y
                echoHeight = if (closestRecord.isCrouching) crouchHeight else normalHeight

                echoRect.set(echoX, echoY, playerWidth, echoHeight)
                if (com.badlogic.gdx.math.Intersector.overlaps(playerRect, echoRect)) {
                    die("Your past caught up to you.")
                    return
                }
            } else {
                echoX = 640f
                echoY = 100f
                echoHeight = normalHeight
            }

            // Spinning Laser Cage
            if (isCageActive && !fakeMaskTouched) {
                cageAngle += 90f * delta

                // Collision with spinning cage
                val cx = maskX + maskWidth / 2f
                val cy = maskY + maskHeight / 2f
                val radius = 50f
                var hitCage = false
                for (i in 0..3) {
                    val angle = cageAngle + i * 90f
                    val rad = Math.toRadians(angle.toDouble())
                    val endX = cx + (Math.cos(rad) * radius).toFloat()
                    val endY = cy + (Math.sin(rad) * radius).toFloat()

                    // Simple segment intersection with playerRect
                    if (Intersector.intersectSegmentRectangle(
                            com.badlogic.gdx.math.Vector2(cx, cy),
                            com.badlogic.gdx.math.Vector2(endX, endY),
                            playerRect
                        )) {
                        hitCage = true
                    }
                }
                if (hitCage) {
                    die("Curiosity killed the cat, lasers just grilled it.")
                    return
                }
            }

            // Real Win: The mask actually kills you
            if (!fakeMaskTouched && Intersector.overlaps(playerRect, maskRect)) {
                die("The mask was a lie.")
                return
            }

            // Stand on the center shark to win (only if all buttons are pressed)
            if (!isDead && gameButtons.all { it.isPressed }) {
                for (shark in sharks) {
                    // The center patrolling shark, OR the spawned top shark (y=480f)
                    if (shark.y == 360f || shark.y == 480f) {
                        sharkRect.set(shark.x, shark.y, 120f, 60f)
                        if (Intersector.overlaps(playerRect, sharkRect)) {
                            win()
                        }
                    }
                }
            }
        }
        // ------------------------------------------

        // --- FORCED CHUNK 3 LOGIC (THE ULTIMATE TROLL) ---
        if (currentLevel == 4 && currentChunk == 3) {
             // Fall through death (if falling off screen)
             if (playerY < -50f) {
                 die("Dropped like a stone.")
                 return
             }

             // The Ultimate Troll: Mask is the trap!
             if (!isDead && Intersector.overlaps(playerRect, maskRect)) {
                 val trollRoasts = listOf(
                     "All this for a drop of blood",
                     "You ain't no Newton",
                     "Yeah you trusted the wrong thing just like You do in your life"
                 )
                 die(trollRoasts.random())
                 return
             }
        }
        // ------------------------------------------

        // Strobe Logic (Chunk 3) - The Pulse
        if (horrorMode && currentChunk == 3) {
            chunk3FlashTimer += delta
            // Lights stay ON for 2.0s, then OFF forever.
            isLightsOn = chunk3FlashTimer < 2.0f
        }

        playerHeight = if (isDownPressed) crouchHeight else normalHeight
        isWalking = false
        val leftInput = if (isControlsInverted) isRightPressed else isLeftPressed
        val rightInput = if (isControlsInverted) isLeftPressed else isRightPressed

        if (leftInput) { playerX -= moveSpeed * delta; isWalking = true }
        if (rightInput) { playerX += moveSpeed * delta; isWalking = true }

        // Level 6 Chunk 1: Sticky Drift Mechanic
        if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
            // Auto-Flap
            if (flapsRemaining > 0) {
                flapTimer -= delta
                if (flapTimer <= 0f) {
                    val currentJumpStrength = if (currentLevel == 5 && (currentChunk == 2 || currentChunk == 3)) 500f else jumpStrength // Flappy strength logic remains standard
                    if (reverseGravity) {
                        velocityY = -currentJumpStrength
                    } else {
                        velocityY = currentJumpStrength
                    }
                    flapsRemaining--
                    if (flapsRemaining > 0) flapTimer = 0.5f
                }
            }

            // Movement Drift logic removed
        }


        if (allowScreenWrap) {
             if (playerX < -40f) playerX = 1280f
             else if (playerX > 1320f) playerX = 0f
        } else {
            if (playerX < 0f || playerX > 1280f - playerWidth) die("There is no escape.")
        }

        if (isWalking) walkTime += delta * 15f else walkTime = 0f

        // Physics
        val isExemptLevel = (currentLevel == 4 && (currentChunk == 2 || currentChunk == 3)) || currentLevel == 3 || currentLevel == 5 || currentLevel == 6 || currentLevel == 7

        val currentGravity = if (currentLevel == 5 && (currentChunk == 2 || currentChunk == 3)) -1800f else if (currentLevel == 6 && currentChunk == 1 && isPortalLoopActive) -3200f * 3f else -3200f

        // --- LEVEL 7 CHUNK 2 LOGIC (The Gravity Pendulum) ---
        if (currentLevel == 7 && currentChunk == 2 && !isDead && !isLevelComplete) {
            // Re-bind laser cage
            for (laser in lasers) laser.rect.set(laser.rect.x, laser.rect.y, laser.rect.width, laser.rect.height)

            // Re-bind mask
            maskRect.set(maskX, maskY, maskWidth, maskHeight)

            // Tilt Controls
            if (isRightPressed) {
                worldTilt += 48f * delta
            } else if (isLeftPressed) {
                worldTilt -= 48f * delta
            }

            // Cap the tilt
            if (worldTilt > 20f) worldTilt = 20f
            if (worldTilt < -20f) worldTilt = -20f

            // The Pendulum Physics: simulated dynamic gravity X-pull
            val slideForce = 800f * MathUtils.sinDeg(worldTilt)
            playerX += slideForce * delta

            chunkTime += delta

            // Swinging Sharks (Automatic true pendulums)
            // Tethered at their pivot points high above the screen, swinging in a true arc.
            val swingAngle = MathUtils.sin(chunkTime * 1.2f) * 45f // Slower +/- 45 degree swing

            val pivotY = 900f

            // Shark 1: pivotX = 300f, length = 550f (y=350)
            val len1 = 550f
            val shark1X = 300f + len1 * MathUtils.sinDeg(swingAngle)
            val shark1Y = pivotY - len1 * MathUtils.cosDeg(swingAngle)

            // Shark 2: pivotX = 580f, length = 450f (y=450)
            val len2 = 450f
            val shark2X = 580f + len2 * MathUtils.sinDeg(swingAngle)
            val shark2Y = pivotY - len2 * MathUtils.cosDeg(swingAngle)

            // Shark 3: pivotX = 860f, length = 300f (y=600)
            val len3 = 300f
            val shark3X = 860f + len3 * MathUtils.sinDeg(swingAngle)
            val shark3Y = pivotY - len3 * MathUtils.cosDeg(swingAngle)

            if (sharks.size >= 3) {
                sharks[0].x = shark1X
                sharks[0].y = shark1Y
                sharks[1].x = shark2X
                sharks[1].y = shark2Y
                sharks[2].x = shark3X
                sharks[2].y = shark3Y
            }

            // Sync SAFE_SHARK platforms & GameButtons (yellow lines)
            if (platforms.size >= 4) {
                platforms[1].rect.x = shark1X
                platforms[1].rect.y = shark1Y
                platforms[2].rect.x = shark2X
                platforms[2].rect.y = shark2Y
                platforms[3].rect.x = shark3X
                platforms[3].rect.y = shark3Y
            }
            if (gameButtons.size >= 3) {
                gameButtons[0].rect.x = shark1X + 40f
                gameButtons[0].rect.y = shark1Y + 90f
                gameButtons[1].rect.x = shark2X + 40f
                gameButtons[1].rect.y = shark2Y + 90f
                gameButtons[2].rect.x = shark3X + 40f
                gameButtons[2].rect.y = shark3Y + 90f
            }

            // Symmetrical Acceleration Walls
            if (movingWalls.size >= 2) {
                val baseSpeed = 30f
                movingWalls[0].speed = baseSpeed + worldTilt // Left wall speeds up if worldTilt > 0
                movingWalls[1].speed = -baseSpeed + worldTilt // Right wall speeds up (magnitude wise) if worldTilt < 0
            }

            // Buttons & Laser Cage Logic
            if (gameButtons.size >= 3) {
                // Left Shark Button -> Left Laser
                if (!gameButtons[0].isPressed && Intersector.overlaps(playerRect, gameButtons[0].rect)) {
                    gameButtons[0].isPressed = true
                    if (lasers.size > 0) lasers[0].rect.set(0f, 0f, 0f, 0f)
                }
                // Middle Shark Button -> Bottom Laser (Frees mask)
                if (!gameButtons[1].isPressed && Intersector.overlaps(playerRect, gameButtons[1].rect)) {
                    gameButtons[1].isPressed = true
                    if (lasers.size > 2) lasers[2].rect.set(0f, 0f, 0f, 0f)
                    isMaskFreefalling = true // Mask starts falling
                }
                // Right Shark Button -> Right Laser
                if (!gameButtons[2].isPressed && Intersector.overlaps(playerRect, gameButtons[2].rect)) {
                    gameButtons[2].isPressed = true
                    if (lasers.size > 1) lasers[1].rect.set(0f, 0f, 0f, 0f)
                }
            }

            // Mask Freefall Physics
            if (isMaskFreefalling) {
                maskVelocityY -= 800f * delta // gravity
                maskVelocityX += slideForce * delta // affected by world tilt

                maskX += maskVelocityX * delta
                maskY += maskVelocityY * delta

                // Check wall collision for Mask
                for (wall in movingWalls) {
                    if (wall.isActive && Intersector.overlaps(maskRect, wall.rect)) {
                        die("The mask shattered into pieces!")
                    }
                }
            }

            // The Precision Gate logic is removed, just standard overlap for Mask win
            if (Intersector.overlaps(playerRect, maskRect)) {
                win()
            }
        }

        // --- LEVEL 7 CHUNK 1 LOGIC (The Tilt Engine) ---
        if (currentLevel == 7 && currentChunk == 1 && !isDead && !isLevelComplete) {
            canJump = true

            // Tilt Controls
            if (isRightPressed) {
                worldTilt += 48f * delta // approx 0.8 deg per frame at 60fps
            } else if (isLeftPressed) {
                worldTilt -= 48f * delta
            }

            // Cap the tilt
            if (worldTilt > 20f) worldTilt = 20f
            if (worldTilt < -20f) worldTilt = -20f

            // Apply "Sliding Force" proportional to sin(theta)
            // e.g. at 20 deg, sin(20) ~ 0.34. Let's make sliding noticeable: ~300f * sin
            val slideForce = 350f * MathUtils.sinDeg(worldTilt)

            // Slide player
            playerX += slideForce * delta

            // Slide safe sharks and crumbling platforms
            for (shark in sharks) {
                if (shark.speed == 0f) shark.x += slideForce * delta
            }
            // Move safe shark platforms too
            for (plat in platforms) {
                if (plat.type == PlatformType.SAFE_SHARK || plat.type == PlatformType.CRUMBLING) {
                    plat.rect.x += slideForce * delta
                }
            }

            // Goal Mask (Weight of Trust)
            if (Intersector.overlaps(playerRect, maskRect)) {
                win()
            }
        }
        if (reverseGravity) {
            gravity = -currentGravity // Flip gravity positive
            velocityY += gravity * delta
            playerY += velocityY * delta


            // Ceiling check (bypass for L7C2 and L5)
            if (playerY > 720f && currentLevel != 5 && !(currentLevel == 7 && currentChunk == 2)) die("Gravity hurts.")
        } else {
            gravity = currentGravity
            velocityY += gravity * delta
            playerY += velocityY * delta
        if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
            if (isPortalLoopActive) {
                // Wrap around
                if (playerY < 0f) {
                    playerY = 1000f
                }

                // Spawn hidden platform slightly to the left
                if (!hiddenPlatformSpawned) {
                    hiddenPlatformSpawned = true
                    val hiddenX = Math.max(50f, playerX - 100f) // a lil to the left
                    val hiddenPlat = Platform(Rectangle(hiddenX, 500f, 60f, 20f), PlatformType.CRUMBLING)
                    platforms.add(hiddenPlat)
                }

                // Check if standing on hidden platform (must be the only platform left)
                var standingOnHidden = false
                for (plat in platforms) {
                    if (plat.type == PlatformType.CRUMBLING && plat.state != PlatformState.DESTROYED && plat.rect.width == 60f) {
                        if (Intersector.overlaps(playerRect, plat.rect)) {
                            standingOnHidden = true
                            break
                        }
                    }
                }

                if (standingOnHidden) {
                    hiddenPlatformTime += delta
                    if (hiddenPlatformTime >= 0.1f && !realMaskSpawned) {
                        realMaskSpawned = true
                        // Right mask is the real one
                        maskX = playerX + 150f
                        maskY = playerY
                        maskRect.set(maskX, maskY, maskWidth, maskHeight)

                        // Left mask is the death one
                        leftMaskX = playerX - 150f
                        leftMaskY = playerY
                        leftMaskRect.set(leftMaskX, leftMaskY, maskWidth, maskHeight)
                    }
                } else {
                    hiddenPlatformTime = 0f // Reset time if they fall off
                }
            } else {
                // If loop not active and they fall into void, they die
                if (playerY < -50f) {
                    if (Math.random() < 0.5) {
                        die("Enjoy the fall. It's the only thing you're good at.")
                    } else {
                        die("Infinite falling for an infinite failure.")
                    }
                    // stateTimer = -9999f
                }
            }

            // Check Death Mask Collision
            if (isPortalLoopActive && realMaskSpawned) {
                leftMaskRect.set(leftMaskX, leftMaskY, maskWidth, maskHeight)
                if (Intersector.overlaps(playerRect, leftMaskRect)) {
                    die("You chose poorly.")
                    // stateTimer = -9999f
                }
            }
        }

            if (playerY < floorY && !isExemptLevel) {
                playerY = floorY
                velocityY = 0f
                canJump = true
            }
        }

        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        // Mirror activation for Level 6
        if (currentLevel == 6 && currentChunk == 1 && playerY >= 550f && !mirrorActive) {
            mirrorActive = true
            mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)
        }


        // Platform Collision
        canJump = false // Reset per frame
        if (currentLevel == 6 && currentChunk == 3) canJump = true
        if (!reverseGravity && playerY <= floorY + 1f && !isExemptLevel) canJump = true

        // --- CRITICAL FIX: Remove destroyed platforms so player falls! ---
        platforms.removeAll { it.state == PlatformState.DESTROYED }
        // ---------------------------------------------------------------

        for (plat in platforms) {
            // Update platform state (crumble timer)
            plat.update(delta)

            // Skip if destroyed
            if (plat.state == PlatformState.DESTROYED) continue

            // 1. Trigger Crumble on Touch (Level 2, 3, 4, 5)
            if ((currentLevel == 2 || currentLevel == 3 || currentLevel == 4 || currentLevel == 5 || currentLevel == 6 || currentLevel == 7) && plat.type == PlatformType.CRUMBLING) {
                // Determine if Player or Echo overlaps (Level 5)
                val isTouchedByPlayer = playerRect.overlaps(plat.rect)
                val isTouchedByEcho = (currentLevel == 5 && currentChunk == 1 && echoActive && echoRect.overlaps(plat.rect))
                val isTouchedByMirror = (currentLevel == 5 && currentChunk == 2) && mirrorActive && mirrorRect.overlaps(plat.rect)

                if (isTouchedByPlayer || isTouchedByEcho || isTouchedByMirror) {
                    // DYNAMIC LIMITS
                    val actualLimit = when {
                         currentLevel == 5 && currentChunk == 1 -> 1.2f // Level 5-1: 1.2s
                         currentLevel == 6 && currentChunk == 1 -> 1.0f // Level 6-1: 1.0s

                         currentLevel == 5 && currentChunk == 2 -> 1.0f // Level 5-2: 1.0s
                         currentLevel == 3 && currentChunk == 3 -> 0.7f
                         currentLevel == 3 || currentLevel == 4 -> 1.0f
                         currentLevel == 7 && currentChunk == 1 -> 1.5f // Level 7-1: 1.5s
                         else -> 1.5f
                    }
                    plat.startCrumbling(actualLimit)
                }
            }

            // Physics Collision (Standard)
            if (plat.type != PlatformType.DEADLY_RED && Intersector.overlaps(playerRect, plat.rect)) {
                // Simple collision: Only land on top (or bottom if reversed?)
                // Standard: Falling down onto platform
                // Added -20f tolerance to make it easier to land on, especially for SAFE_SHARK
                if (!reverseGravity && velocityY <= 0) {
                     val tolerance = if (plat.type == PlatformType.SAFE_SHARK) 20f else 0f
                     if (playerY - velocityY * delta >= plat.rect.y + plat.rect.height - tolerance) {
                         playerY = plat.rect.y + plat.rect.height
                         velocityY = 0f
                         canJump = true

                         // Legacy Crumble Trigger
                         if (currentLevel != 3 && (plat.type == PlatformType.CRUMBLE_SLOW || plat.type == PlatformType.CRUMBLE_FAST)
                             && plat.state == PlatformState.ACTIVE) {
                             val duration = if (plat.type == PlatformType.CRUMBLE_SLOW) 1.0f else 0.5f
                             plat.startCrumbling(duration)
                         }
                     }
                }
                // Reverse: Falling UP onto platform bottom
                else if (reverseGravity && velocityY >= 0) {
                    if (playerY - velocityY * delta <= plat.rect.y - playerHeight) {
                        playerY = plat.rect.y - playerHeight
                        velocityY = 0f
                        canJump = true
                    }
                }
            }
        }

        // Floor Death Check - AFTER Platform Collision (Fix for Soft-Lock)
        // Level 5 requires a deeper floor death check because of the identity swap to the bottom
        val bottomLimit = if (currentLevel == 5) -200f else -100f
        if (!reverseGravity && playerY < bottomLimit) {
            die("Darkness consumes you.")
        }

        // 4. CRITICAL: Ghost Floor Fix
        // Remove destroyed platforms to prevent walking on air.
        platforms.removeAll { it.type == PlatformType.CRUMBLING && it.state == PlatformState.DESTROYED }
        // ----------------------------

        // Gravity Switches
        for (switch in gravitySwitches) {
            if (switch.isActive && Intersector.overlaps(playerRect, switch.rect)) {
                reverseGravity = !reverseGravity
                switch.isActive = false // Trigger once
            }
        }

        // Level 4 Mechanics
        // Lasers
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val laser = laserIter.next()
            if (laser.isSweeping) {
                if (laser.maxX > laser.minX) {
                    // Horizontal sweeping
                    if (laser.movingRight) {
                        laser.rect.x += laser.sweepSpeed * delta
                        if (laser.rect.x > laser.maxX) laser.movingRight = false
                    } else {
                        laser.rect.x -= laser.sweepSpeed * delta
                        if (laser.rect.x < laser.minX) laser.movingRight = true
                    }
                } else if (laser.maxY > laser.minY) {
                    // Vertical sweeping
                    if (laser.movingRight) { // reusing movingRight for movingUp
                        laser.rect.y += laser.sweepSpeed * delta
                        if (laser.rect.y > laser.maxY) laser.movingRight = false
                    } else {
                        laser.rect.y -= laser.sweepSpeed * delta
                        if (laser.rect.y < laser.minY) laser.movingRight = true
                    }
                }
            }
            if (Intersector.overlaps(playerRect, laser.rect)) {
                // If Level 7 Chunk 2, the laser cage is safe when the tilt is exactly between 14-16 degrees
                if (currentLevel == 7 && currentChunk == 2 && Math.abs(worldTilt) in 14f..16f) {
                    // Safe!
                } else if (currentLevel == 5 && currentChunk == 2) {
                    die("You ain't no Newton")
                } else {
                    die("Grilled to perfection. Serve with a side of failure.")
                }
            }
        }


        // Moving Walls
        for (wall in movingWalls) {
            if (wall.isActive) {
                wall.rect.x += wall.speed * delta
                // Check if wall crushes player
                if (Intersector.overlaps(playerRect, wall.rect)) {
                    if (currentLevel == 5 && currentChunk == 2) die("You ain't no Newton")
                    else if (currentLevel == 6 && currentChunk == 1) {
                        die("Did you think the Mask was your friend? Cute.")
                        // stateTimer = -9999f
                    } else die("Squished like a bug. And just as insignificant.")
                }
            }
        }


        // Buttons
        for (btn in gameButtons) {
            if (!btn.isPressed && Intersector.overlaps(playerRect, btn.rect)) {
                btn.isPressed = true
                btn.onHit?.invoke()
            }
        }


        // Check Deadly Red Collision specifically for player
        if ((currentLevel == 5 && currentChunk == 2) || (currentLevel == 6 && currentChunk == 1)) {
            for (plat in platforms) {
                if (plat.type == PlatformType.DEADLY_RED && Intersector.overlaps(playerRect, plat.rect)) {
                    if (currentLevel == 6 && currentChunk == 1) die("Is your screen dirty, or is it just your lack of skill?")
                    else die("You ain't no Newton")
                }
            }
        }


        // Mirror Logic Level 5 Chunk 2
        if (currentLevel == 5 && currentChunk == 2 && mirrorActive) {
            mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)

            // Mirror collisions
            if (Intersector.overlaps(playerRect, mirrorRect)) {
                die("You ain't no Newton")
            }

            // Check lasers and walls for mirror
            for (laser in lasers) {
                if (Intersector.overlaps(mirrorRect, laser.rect)) {
                    die("You ain't no Newton")
                }
            }
            for (wall in movingWalls) {
                if (wall.isActive && Intersector.overlaps(mirrorRect, wall.rect)) {
                    die("You ain't no Newton")
                }
            }

            // Mirror Platform Collision
            for (plat in platforms) {
                if (plat.state == PlatformState.DESTROYED) continue

                if (plat.type == PlatformType.DEADLY_RED && Intersector.overlaps(mirrorRect, plat.rect)) {
                    if (currentLevel == 6 && currentChunk == 1) die("Stop fighting the drift. Trust the void.")
                    else
                    die("You ain't no Newton")
                }
            }
        }

        // Shark AI
        for (shark in sharks) {
            if (shark.isStalker) {
                // LEVEL 3 CHUNK 2 EXCLUSIVE: "The Creepy Drift"
                if (currentLevel == 3 && currentChunk == 2) {
                    shark.speed = 230f // UPDATED: 230f as requested

                    // 1. Constant Forward Drift (X-Axis)
                    shark.x += shark.speed * delta

                    // 2. Slow Vertical Tracking (Y-Axis)
                    // Wait, stalker Y logic was simplified in previous step.
                    // Let's keep it simple: Chase Y
                    if (playerY > shark.y) shark.y += 60f * delta // Keep vertical slow
                    else shark.y -= 60f * delta

                    shark.facingRight = true
                }
                // LEVEL 1 CHUNK 3: The Ambush Loop (Keep existing)
                else if (currentLevel == 1 && currentChunk == 3) {
                    var targetDir = if (playerX > shark.x) 1 else -1

                    // MOMENTUM LOCK (The Fix):
                    if (!shark.facingRight && shark.x < 640f) {
                        targetDir = -1
                    }

                    // Apply Movement
                    shark.x += shark.speed * delta * targetDir

                    // Update Facing (Only visual)
                    shark.facingRight = (targetDir > 0)

                    // TELEPORT LOGIC (Infinite Loop)
                    val sharkWidth = 120f
                    if (shark.x < -sharkWidth - 50f) {
                        shark.x = 1280f
                        shark.speed = 950f
                    }
                    if (shark.x > 1280f + 50f) {
                        shark.x = -sharkWidth
                        shark.speed = 950f
                    }
                }
                // STANDARD STALKER (Keep existing)
                else {
                    var targetDir = if (playerX > shark.x) 1 else -1
                    shark.x += shark.speed * delta * targetDir
                    shark.facingRight = (targetDir > 0)
                    // Clamp
                    val sharkWidth = 120f
                    if (shark.x < 0f) shark.x = 0f
                    if (shark.x > 1280f - sharkWidth) shark.x = 1280f - sharkWidth
                }
            } else if (shark.isSleeper) {
                if (!shark.isAwake) {
                    if (playerX > 400f) {
                        shark.isAwake = true
                        shark.speed = 600f
                    }
                }
                if (shark.isAwake) {
                    shark.x -= shark.speed * delta
                    shark.facingRight = false
                }
            } else {
                if (shark.facingRight) {
                    shark.x += shark.speed * delta
                    if (shark.x > shark.patrolRight) shark.facingRight = false
                } else {
                    shark.x -= shark.speed * delta
                    if (shark.x < shark.patrolLeft) shark.facingRight = true
                }
            }

            // Sync SAFE_SHARK platforms
            if ((currentLevel == 5 && currentChunk == 2) || (currentLevel == 6 && currentChunk == 1)) {
                for (plat in platforms) {
                    if (plat.type == PlatformType.SAFE_SHARK) {
                        // Use exact unique widths to sync
                        if (shark.patrolLeft < 640f && plat.rect.width == 119.9f) {
                            plat.rect.x = shark.x
                        } else if (shark.patrolLeft >= 640f && plat.rect.width == 120.1f) {
                            plat.rect.x = shark.x
                        }
                    }
                }
            } else if (currentLevel == 5 && currentChunk == 3) {
                for (plat in platforms) {
                    if (plat.type == PlatformType.SAFE_SHARK && plat.rect.width == 120.2f && shark.y == 360f) {
                        plat.rect.x = shark.x
                    }
                }
            }

            // Shark Collision
            // The Ultimate Troll: In Level 4 Chunk 3, hitting the shark at y=220f means winning
            if (currentLevel == 4 && currentChunk == 3 && shark.y == 220f) {
                sharkRect.set(shark.x, shark.y, 120f, 60f)
                if (Intersector.overlaps(playerRect, sharkRect)) {
                    win() // Touch the shark to win!
                }
            } else {
                // Safe Shark Logic: Skip collision if shark is at x=600 (Chunk 2 Friendly Shark)
                // Also skip deadly collision for Level 5 Chunk 2 Safe Sharks
                val isSafeSharkLevel5 = (currentLevel == 5 && currentChunk == 2)
            if ((currentLevel != 4 || currentChunk != 2 || shark.x != 600f) && !isSafeSharkLevel5) {
                    sharkRect.set(shark.x, shark.y, 120f, 60f) // approx
                    if (Intersector.overlaps(playerRect, sharkRect)) {
                        // In Level 5 Chunk 3, the sharks are deadly if buttons aren't pressed,
                        // but if all buttons are pressed, touching either the center or spawned shark wins the game.
                        if (currentLevel == 5 && currentChunk == 3 && (shark.y == 360f || shark.y == 480f) && gameButtons.all { it.isPressed }) {
                            // Do not die
                        } else {
                            die()
                        }
                    }
                }
            }
        }

        // Bubbles
        bubbleSpawnTimer += delta
        if (bubbleSpawnTimer > 0.5f && bubbles.size < maxBubbles) {
            spawnBubble()
            bubbleSpawnTimer = 0f
        }
        val bubbleIter = bubbles.iterator() // RENAMED to fix conflict
        while (bubbleIter.hasNext()) {
            val b = bubbleIter.next()
            b.y += b.speed * delta
            if (b.y > 720f) bubbleIter.remove()
        }

        // Ceiling drop is handled by normal sweeping logic.

        // Default Win Condition (Ignore in Level 4 Chunk 3)
        if (currentLevel == 6 && currentChunk == 1) {
            if (!isDead && !isLevelComplete) {
                if (!isPortalLoopActive && Intersector.overlaps(playerRect, maskRect)) {
                    // Trap triggered!
                    isPortalLoopActive = true
                    // Speed up the moving walls to create high pressure
                    for (wall in movingWalls) {
                        if (wall.speed > 0) wall.speed = 150f else if (wall.speed < 0) wall.speed = -150f
                    }
                    playerY = 1000f
                    velocityY = -500f // Fall rapidly

                    // Hide fake mask by moving it out of bounds
                    maskY = -9999f
                    maskRect.set(maskX, maskY, maskWidth, maskHeight)
                } else if (isPortalLoopActive && realMaskSpawned && Intersector.overlaps(playerRect, maskRect)) {
                    win()
                }
            }
        } else if (currentLevel == 5 && currentChunk == 2) {
            if (!isDead && Intersector.overlaps(playerRect, maskRect) && Intersector.overlaps(mirrorRect, maskRect)) {
                win()
            }
        } else if (currentLevel == 5 && currentChunk == 3) {
            // Ignore default win, handled in logic block
        } else if (currentLevel == 6 && currentChunk == 2) {
            // Handled explicitly in update loop (only trigger if swapped)
        } else if (currentLevel == 6 && currentChunk == 3) {
            // Handled explicitly in update loop
        } else if (currentLevel == 7 && currentChunk == 1) {
            // Handled explicitly in update loop
        } else if (currentLevel != 4 || currentChunk != 3) {
            if (!isDead && Intersector.overlaps(playerRect, maskRect)) win()
        }
    }

    private fun die(customMessage: String? = null) {
        if (isDead) return
        isDead = true
        var roast = customMessage ?: deathRoasts.random()

        if (currentLevel == 6 && currentChunk == 2) {
            roast = customMessage ?: listOf("Look at you... you've become the very thing you feared.", "Identity crisis much?", "You're just a ghost in your own game now.").random()
        }

        if (currentLevel == 6 && currentChunk == 3) {
            roast = customMessage ?: listOf("You chose... poorly.", "Squished like a pancake.", "Cloudy with a chance of meat-eating predators!", "Grilled to perfection. Serve with a side of impatience.", "You have the survival instincts of a lemming.", "A wall? Really?", "I've seen potatoes with better reaction times.").random()
        }

        if (currentLevel == 7 && currentChunk == 1) {
            roast = customMessage ?: listOf("Can't even keep your balance? Pathetic.", "The world is literally leaning in your favor and you still failed.", "Newton is rolling in his grave watching you slide.").random()
        }

        if (currentLevel == 7 && currentChunk == 2) {
            roast = customMessage ?: listOf("Newton is laughing at your lack of coordination.", "You're falling for the same tricks... literally.").random()
        }

        messageLabel?.setText(roast)
        messageLabel?.color = Color.RED
        messageLabel?.isVisible = true
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun win() {
        if (isLevelComplete) return
        isLevelComplete = true

        var roast = winRoasts.random()

        if (currentLevel == 6 && currentChunk == 3) {
            roast = listOf("Wow, you stood still for 6 seconds. Truly a gaming legend.", "The hardest mechanic in gaming: doing absolutely nothing.", "Luigi wins by doing absolutely nothing.", "You literally did nothing and won. I'm so proud.", "Pro-gamer move: AFK.").random()
        }

        if (currentLevel == 4) {
            roast = when (currentChunk) {
                1 -> "Wow, you dodged a laser. Want a medal for basic motor skills?"
                2 -> "Not too high, not too low... just perfectly mediocre."
                3 -> "You escaped the compactor. Unfortunately, you're still garbage."
                else -> roast
            }
        }

        messageLabel?.setText(roast)
        if (currentLevel == 3) {
            messageLabel?.color = Color.WHITE
        } else {
            messageLabel?.color = Color.BLACK
        }
        messageLabel?.isVisible = true
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun isVisible(x: Float, y: Float): Boolean {
        // Fix for Visibility Bug:
        // If not Level 3, EVERYTHING is visible (Standard Mode)
        if (currentLevel != 3) return true
        if (!horrorMode) return true

        // Chunk 3: Global Strobe (Updated for One-Time Flash)
        if (currentChunk == 3) {
            // isLightsOn handles the 2.0s initial flash.
            // Also visible if close to player (radius 100f)
            return isLightsOn || Vector2.dst(playerX, playerY, x, y) < 100f
        }

        // Chunk 1 & 2: Flashlight
        val dist = Vector2.dst(playerX, playerY, x, y)
        return dist < flashlightRadius
    }

    private fun draw() {
        // Apply world tilt to camera
        if (currentLevel == 7 && (currentChunk == 1 || currentChunk == 2)) {
            gameViewport.camera.up.set(0f, 1f, 0f)
            gameViewport.camera.direction.set(0f, 0f, -1f)
            gameViewport.camera.rotate(worldTilt, 0f, 0f, 1f)
            if (currentChunk == 2) {
                val targetY = Math.max(360f, Math.min(playerY, 850f))
                gameViewport.camera.position.y = targetY
            }
            gameViewport.camera.update()
        }

        // --- 1. SHAPES (Filled & Line) ---
        shapeRenderer.projectionMatrix = gameViewport.camera.combined

        // Bubbles (Lines)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        for (b in bubbles) {
            if (isVisible(b.x, b.y)) shapeRenderer.circle(b.x, b.y, b.radius)
        }
        shapeRenderer.end()

        // Platforms, Walls, Lasers, Player (Filled)
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw Platforms
        for (plat in platforms) {
            if (plat.state == PlatformState.DESTROYED) continue
            if (plat.type == PlatformType.INVISIBLE) continue // Do not draw invisible platforms
            if (plat.type == PlatformType.SAFE_SHARK) continue // Drawn in SpriteBatch
            // Skip the deadly trap shark drawing as platform
            if (plat.type == PlatformType.DEADLY_RED && plat.rect.width == 120.4f) continue

            val isPlatformVisible = (currentLevel != 3 || currentChunk != 3) || isLightsOn || plat.state == PlatformState.CRUMBLING
            if (isPlatformVisible && isVisible(plat.rect.x, plat.rect.y)) {
                shapeRenderer.color = when(plat.type) {
                    PlatformType.DEADLY_RED -> Color.RED
                    else -> if (plat.state == PlatformState.CRUMBLING) Color.RED else Color.GREEN
                }
                shapeRenderer.rect(plat.rect.x + renderOffset, plat.rect.y, plat.rect.width, plat.rect.height)
            }
        }

        // Draw Switches
        for (sw in gravitySwitches) {
            if (sw.isActive && isVisible(sw.rect.x, sw.rect.y)) {
                shapeRenderer.color = Color.BLUE
                shapeRenderer.rect(sw.rect.x + renderOffset, sw.rect.y, sw.rect.width, sw.rect.height)
            }
        }

        // Draw Moving Walls
        shapeRenderer.color = Color.DARK_GRAY
        for (wall in movingWalls) {
            shapeRenderer.rect(wall.rect.x + renderOffset, wall.rect.y, wall.rect.width, wall.rect.height)
        }

        // Draw Game Buttons
        for (btn in gameButtons) {
            shapeRenderer.color = if (btn.isPressed) Color.GRAY else Color.YELLOW
            shapeRenderer.rect(btn.rect.x + renderOffset, btn.rect.y, btn.rect.width, btn.rect.height)
        }

        // Draw Lasers (Transparent Red)
        shapeRenderer.color = Color(1f, 0.1f, 0.1f, 0.8f)
        for (laser in lasers) {
             shapeRenderer.rect(laser.rect.x + renderOffset, laser.rect.y, laser.rect.width, laser.rect.height)
        }

        // Draw Player (Procedural Shapes)
        if (currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity) {
            shapeRenderer.color = Color.RED
        } else {
            shapeRenderer.color = if (horrorMode) Color.GRAY else Color.GREEN
        }
        if (isDead) shapeRenderer.color = Color.GRAY
        val centerX = playerX + 12.5f + renderOffset
        val isCrouching = playerHeight < normalHeight
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f
        val legOffset = if (isCrouching) 0f else (Math.sin(walkTime.toDouble()).toFloat() * 6f)

        shapeRenderer.circle(centerX, playerY + headOffset, 6f)
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX - 6f - legOffset, playerY, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX + 6f + legOffset, playerY, 3f)

        // Draw Echo (Transparent Red) for Level 5
        if ((currentLevel == 5 && (currentChunk == 1 || currentChunk == 3)) && echoActive) {
            shapeRenderer.color = Color(1f, 0f, 0f, 0.5f) // Transparent Red
            val eCenterX = echoX + 12.5f + renderOffset
            val eCrouch = echoHeight < normalHeight
            val eHead = if (eCrouch) 22f else 44f
            val eNeck = if (eCrouch) 15f else 38f
            val eWaist = if (eCrouch) 5f else 18f
            // Echo legs don't strictly need animation but we can leave them static or use walkTime
            shapeRenderer.circle(eCenterX, echoY + eHead, 6f)
            shapeRenderer.rectLine(eCenterX, echoY + eNeck, eCenterX, echoY + eWaist, 3f)
            shapeRenderer.rectLine(eCenterX, echoY + eWaist, eCenterX - 6f, echoY, 3f)
            shapeRenderer.rectLine(eCenterX, echoY + eWaist, eCenterX + 6f, echoY, 3f)
        }

        // Draw Mirror Player / Ghost for Level 5/6
        if ((currentLevel == 5 && currentChunk == 2 && mirrorActive) || (currentLevel == 6 && currentChunk == 2)) {
            shapeRenderer.color = Color.RED // Deadly Red
            val mCenterX = mirrorRect.x + 12.5f + renderOffset
            val mCrouch = mirrorRect.height < normalHeight
            val mHead = if (mCrouch) 22f else 44f
            val mNeck = if (mCrouch) 15f else 38f
            val mWaist = if (mCrouch) 5f else 18f
            // Mirror legs animation is opposite phase or same? Let's keep it same or inverse
            val mLegOffset = if (mCrouch) 0f else (Math.sin(walkTime.toDouble()).toFloat() * -6f)

            shapeRenderer.circle(mCenterX, mirrorRect.y + mHead, 6f)
            shapeRenderer.rectLine(mCenterX, mirrorRect.y + mNeck, mCenterX, mirrorRect.y + mWaist, 3f)
            shapeRenderer.rectLine(mCenterX, mirrorRect.y + mWaist, mCenterX - 6f - mLegOffset, mirrorRect.y, 3f)
            shapeRenderer.rectLine(mCenterX, mirrorRect.y + mWaist, mCenterX + 6f + mLegOffset, mirrorRect.y, 3f)
        }

        // Draw spinning laser cage for Level 5 Chunk 3
        if ((currentLevel == 5 && currentChunk == 3 && isCageActive && !fakeMaskTouched) ||
            (currentLevel == 6 && currentChunk == 3 && isCageActive && !fakeMaskTouched)) {
            shapeRenderer.color = Color(1f, 0.1f, 0.1f, 0.8f) // Same transparent red as lasers
            val centerX = maskX + maskWidth / 2f + renderOffset
            val centerY = maskY + maskHeight / 2f
            val radius = 50f
            for (i in 0..3) {
                val angle = cageAngle + i * 90f
                val rad = Math.toRadians(angle.toDouble())
                val endX = centerX + (Math.cos(rad) * radius).toFloat()
                val endY = centerY + (Math.sin(rad) * radius).toFloat()
                // Just draw a line or thin rect, since we need to check collision let's draw a rect line
                shapeRenderer.rectLine(centerX, centerY, endX, endY, 4f)
            }
        }

        shapeRenderer.end()
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND)

        // --- 2. TEXTURES (SpriteBatch) ---
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()

        // Draw Mask using Texture
        maskTexture?.let { tex ->
            val hideMask = (currentLevel == 3 && currentChunk == 3 && !isLightsOn)
            if (!hideMask && isVisible(maskX, maskY)) {
                game.batch.draw(tex, maskX + renderOffset, maskY + renderOffsetY, 32f, 32f)
            }

            // Draw Left Mask for Level 6 Chunk 1
            if (currentLevel == 6 && currentChunk == 1 && realMaskSpawned && isVisible(leftMaskX, leftMaskY)) {
                game.batch.draw(tex, leftMaskX + renderOffset, leftMaskY + renderOffsetY, 32f, 32f)
            }

            // Draw Level 6 Chunk 3 Extra Masks
            if (currentLevel == 6 && currentChunk == 3 && isVisible(leftMaskX, leftMaskY)) {
                game.batch.draw(tex, leftMaskX + renderOffset, leftMaskY + renderOffsetY, 32f, 32f)
            }
            if (currentLevel == 6 && currentChunk == 3 && isVisible(centerMaskX, centerMaskY)) {
                game.batch.draw(tex, centerMaskX + renderOffset, centerMaskY + renderOffsetY, 32f, 32f)
            }
        }

        // Draw Sharks (and Safe Sharks from platforms)
        sharkTexture?.let { tex ->
            val ratio = tex.height.toFloat() / tex.width.toFloat()
            val width = 120f
            val height = width * ratio

            // 1. Draw Real Sharks
            for (shark in sharks) {
                if (isVisible(shark.x, shark.y)) {
                    game.batch.draw(tex, shark.x + renderOffset, shark.y, width, height, 0, 0, tex.width, tex.height, shark.facingRight, false)
                }
            }

            // 2. Draw Safe Sharks (Platforms)
            for (plat in platforms) {
                if ((plat.type == PlatformType.SAFE_SHARK || (plat.type == PlatformType.DEADLY_RED && plat.rect.width == 120.4f)) && isVisible(plat.rect.x, plat.rect.y)) {
                    // Draw shark at platform position
                    game.batch.draw(tex, plat.rect.x + renderOffset, plat.rect.y, width, height, 0, 0, tex.width, tex.height, false, false)
                }
            }
        }

        game.batch.end()

        // Reset camera tilt
        if (currentLevel == 7 && (currentChunk == 1 || currentChunk == 2)) {
            gameViewport.camera.up.set(0f, 1f, 0f)
            gameViewport.camera.direction.set(0f, 0f, -1f)
            gameViewport.camera.position.y = 360f // Reset Y
            gameViewport.camera.update()
        }
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        sharkTexture?.dispose()
        maskTexture?.dispose() // Dispose mask
        uiStage.dispose()
        skin?.dispose()
        whiteTexture?.dispose()
        buttonFont?.dispose()
    }
}
