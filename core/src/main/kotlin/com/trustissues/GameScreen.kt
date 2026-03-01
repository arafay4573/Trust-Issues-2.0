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

    // Game State
    private var isDead = false
    private var isLevelComplete = false
    private var stateTimer = 0f
    private var allowScreenWrap = false
    private var isPaused = false

    // Horror Mode State
    private var horrorMode = false
    private var flashlightRadius = 300f // Default 300f
    private var strobeTimer = 0f
    private var isLightsOn = false
    private var chunk3FlashTimer = 0f // Added for Level 3 Chunk 3 One-Time Flash

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
        var movingRight: Boolean = true
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
        }

        maskRect.set(maskX, maskY, maskWidth, maskHeight)
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
                // --- EMERGENCY RESET: STEP 1 ---
                platforms.clear()
                sharks.clear()
                lasers.clear()
                gameButtons.clear()
                gravitySwitches.clear()

                // 1. Player Spawn (Ultra-Low)
                playerX = 450f
                playerY = 20f
                velocityY = 0f
                reverseGravity = false

                // 2. THE INVISIBLE SEA BED (Y=10)
                // This is the ONLY collision object for now.
                platforms.add(Platform(Rectangle(0f, 10f, 2000f, 10f), PlatformType.INVISIBLE))

                // 3. The Goal
                maskX = 100f
                maskY = 550f
                // --- END RESET ---
            }
            3 -> {
                // Chunk 3: The Compactor
                playerX = 100f; playerY = 280f

                // Walls
                val leftWall = MovingWall(Rectangle(-200f, 0f, 200f, 800f), 80f, isActive = true)
                movingWalls.add(leftWall)
                val rightWall = MovingWall(Rectangle(1400f, 0f, 200f, 800f), -80f, isActive = false)
                movingWalls.add(rightWall)

                // Staircase UP
                platforms.add(Platform(Rectangle(100f, 200f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(250f, 350f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(400f, 500f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(400f, 600f, 100f, 20f), PlatformType.CRUMBLING))

                // Roof Button (Stops Left, Starts Right)
                gameButtons.add(GameButton(Rectangle(400f, 650f, 40f, 40f), onHit = {
                    leftWall.isActive = false
                    rightWall.isActive = true
                }))

                // Staircase DOWN
                platforms.add(Platform(Rectangle(600f, 400f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(750f, 250f, 100f, 20f), PlatformType.CRUMBLING))

                // Vertical Laser Gate blocking Mask
                val gateLaser = Laser(Rectangle(1000f, 0f, 50f, 800f), isSweeping = false)
                lasers.add(gateLaser)

                // Floor Button (Stops Right, Removes Laser)
                gameButtons.add(GameButton(Rectangle(800f, 50f, 40f, 40f), onHit = {
                    rightWall.isActive = false
                    lasers.remove(gateLaser) // Open gate
                }))

                maskX = 1150f; maskY = 280f
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
        val prefs = Gdx.app.getPreferences("TrustIssues")
        val savedMaxChunk = prefs.getInteger("level_${currentLevel}_maxChunk", 1)
        if (nextChunk > savedMaxChunk && nextChunk <= 3) {
            prefs.putInteger("level_${currentLevel}_maxChunk", nextChunk).flush()
        }

        if (nextChunk > 3) {
            val nextLevel = currentLevel + 1
            val unlocked = prefs.getInteger("unlockedLevel", 1)
            if (nextLevel > unlocked) {
                prefs.putInteger("unlockedLevel", nextLevel).flush()
                prefs.putInteger("level_${nextLevel}_maxChunk", 1).flush()
            }
            game.screen = if (nextLevel > 10) LevelSelectScreen(game) else GameScreen(game, nextLevel, 1)
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
                if (!isPaused && !isDead && !isLevelComplete) {
                    if (reverseGravity) {
                        // In reverse, jump pushes DOWN
                        if (canJump) velocityY = -jumpStrength
                    } else {
                        // Normal jump
                        if (canJump) velocityY = jumpStrength
                    }
                }
                return true
            }
        })
        uiStage.addActor(jumpZone)

        val rootTable = Table(); rootTable.setFillParent(true); rootTable.bottom()
        val leftBtn = ImageButton(skin!!.get("left", ImageButton.ImageButtonStyle::class.java))
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isLeftPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) { isLeftPressed = false }
        })
        val rightBtn = ImageButton(skin!!.get("right", ImageButton.ImageButtonStyle::class.java))
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isRightPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) { isRightPressed = false }
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
                Gdx.app.postRunnable { if (isDead) setupChunk(currentChunk) else completeChunk() }
            }
            return
        }

        // Strobe Logic (Chunk 3) - The Pulse
        if (horrorMode && currentChunk == 3) {
            chunk3FlashTimer += delta
            // Lights stay ON for 2.0s, then OFF forever.
            isLightsOn = chunk3FlashTimer < 2.0f
        }

        playerHeight = if (isDownPressed) crouchHeight else normalHeight
        isWalking = false
        if (isLeftPressed) { playerX -= moveSpeed * delta; isWalking = true }
        if (isRightPressed) { playerX += moveSpeed * delta; isWalking = true }

        if (allowScreenWrap) {
             if (playerX < -40f) playerX = 1280f
             else if (playerX > 1320f) playerX = 0f
        } else {
            if (playerX < 0f || playerX > 1280f - playerWidth) die("There is no escape.")
        }

        if (isWalking) walkTime += delta * 15f else walkTime = 0f

        // Physics
        if (reverseGravity) {
            gravity = 3200f
            velocityY += gravity * delta
            playerY += velocityY * delta

            // Ceiling check
            if (playerY > 720f) die("Gravity hurts.")
        } else {
            gravity = -3200f
            velocityY += gravity * delta
            playerY += velocityY * delta

            if (playerY < floorY && currentLevel != 3) {
                playerY = floorY
                velocityY = 0f
                canJump = true
            }
        }

        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        // Platform Collision
        canJump = false // Reset per frame
        if (!reverseGravity && playerY <= floorY + 1f && currentLevel != 3) canJump = true

        // --- CRITICAL FIX: Remove destroyed platforms so player falls! ---
        platforms.removeAll { it.state == PlatformState.DESTROYED }
        // ---------------------------------------------------------------

        for (plat in platforms) {
            // Update platform state (crumble timer)
            plat.update(delta)

            // Skip if destroyed
            if (plat.state == PlatformState.DESTROYED) continue

            // 1. Trigger Crumble on Touch (Level 2, 3, 4)
            if ((currentLevel == 2 || currentLevel == 3 || currentLevel == 4) && plat.type == PlatformType.CRUMBLING) {
                if (playerRect.overlaps(plat.rect)) {
                    // DYNAMIC LIMITS
                    // Level 4 Chunk 3: 0.8s (Ultra fast)
                    // Level 4 Chunks 1 & 2: 1.0s (Fast)
                    // Level 3 Chunk 3: 0.7s (Brutal)
                    // Level 3 Chunks 1 & 2: 1.0s (Fast)
                    // Level 2: 1.5s (Standard Training)

                    // RE-WRITTEN CLEAN LIMIT LOGIC
                    val actualLimit = when {
                         currentLevel == 3 && currentChunk == 3 -> 0.7f
                         currentLevel == 4 && currentChunk == 3 -> 0.8f
                         currentLevel == 3 || currentLevel == 4 -> 1.0f
                         else -> 1.5f
                    }
                    plat.startCrumbling(actualLimit)
                }
            }

            // Physics Collision (Standard)
            if (Intersector.overlaps(playerRect, plat.rect)) {
                // Simple collision: Only land on top (or bottom if reversed?)
                // Standard: Falling down onto platform
                if (!reverseGravity && velocityY <= 0) {
                     if (playerY - velocityY * delta >= plat.rect.y + plat.rect.height) {
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
        if (!reverseGravity && playerY < -100f) {
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
                if (laser.movingRight) {
                    laser.rect.x += laser.sweepSpeed * delta
                    if (laser.rect.x > laser.maxX) laser.movingRight = false
                } else {
                    laser.rect.x -= laser.sweepSpeed * delta
                    if (laser.rect.x < laser.minX) laser.movingRight = true
                }
            }
            if (Intersector.overlaps(playerRect, laser.rect)) {
                die("Grilled to perfection. Serve with a side of failure.")
            }
        }

        // Moving Walls
        for (wall in movingWalls) {
            if (wall.isActive) {
                wall.rect.x += wall.speed * delta
                // Check if wall crushes player
                if (Intersector.overlaps(playerRect, wall.rect)) {
                    die("Squished like a bug. And just as insignificant.")
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

            // Shark Collision
            sharkRect.set(shark.x, shark.y, 120f, 60f) // approx
            if (Intersector.overlaps(playerRect, sharkRect)) die()
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

        if (!isDead && Intersector.overlaps(playerRect, maskRect)) win()
    }

    private fun die(customMessage: String? = null) {
        if (isDead) return
        isDead = true
        val roast = customMessage ?: deathRoasts.random()
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

            val isPlatformVisible = (currentLevel != 3 || currentChunk != 3) || isLightsOn || plat.state == PlatformState.CRUMBLING
            if (isPlatformVisible && isVisible(plat.rect.x, plat.rect.y)) {
                shapeRenderer.color = when(plat.type) {
                    PlatformType.DEADLY_RED -> Color.RED
                    else -> if (plat.state == PlatformState.CRUMBLING) Color.RED else Color.GREEN
                }
                shapeRenderer.rect(plat.rect.x, plat.rect.y, plat.rect.width, plat.rect.height)
            }
        }

        // Draw Switches
        for (sw in gravitySwitches) {
            if (sw.isActive && isVisible(sw.rect.x, sw.rect.y)) {
                shapeRenderer.color = Color.BLUE
                shapeRenderer.rect(sw.rect.x, sw.rect.y, sw.rect.width, sw.rect.height)
            }
        }

        // Draw Moving Walls
        shapeRenderer.color = Color.DARK_GRAY
        for (wall in movingWalls) {
            shapeRenderer.rect(wall.rect.x, wall.rect.y, wall.rect.width, wall.rect.height)
        }

        // Draw Game Buttons
        for (btn in gameButtons) {
            shapeRenderer.color = if (btn.isPressed) Color.GRAY else Color.YELLOW
            shapeRenderer.rect(btn.rect.x, btn.rect.y, btn.rect.width, btn.rect.height)
        }

        // Draw Lasers (Transparent Red)
        shapeRenderer.color = Color(1f, 0.1f, 0.1f, 0.8f)
        for (laser in lasers) {
             shapeRenderer.rect(laser.rect.x, laser.rect.y, laser.rect.width, laser.rect.height)
        }

        // Draw Player (Procedural Shapes)
        shapeRenderer.color = if (horrorMode) Color.GRAY else Color.BLACK
        val centerX = playerX + 12.5f
        val isCrouching = playerHeight < normalHeight
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f
        val legOffset = if (isCrouching) 0f else (Math.sin(walkTime.toDouble()).toFloat() * 6f)

        shapeRenderer.circle(centerX, playerY + headOffset, 6f)
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX - 6f - legOffset, playerY, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX + 6f + legOffset, playerY, 3f)

        shapeRenderer.end()
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND)

        // --- 2. TEXTURES (SpriteBatch) ---
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()

        // Draw Sharks (and Safe Sharks from platforms)
        sharkTexture?.let { tex ->
            val ratio = tex.height.toFloat() / tex.width.toFloat()
            val width = 120f
            val height = width * ratio

            // 1. Draw Real Sharks
            for (shark in sharks) {
                if (isVisible(shark.x, shark.y)) {
                    game.batch.draw(tex, shark.x, shark.y, width, height, 0, 0, tex.width, tex.height, shark.facingRight, false)
                }
            }

            // 2. Draw Safe Sharks (Platforms)
            for (plat in platforms) {
                if (plat.type == PlatformType.SAFE_SHARK && isVisible(plat.rect.x, plat.rect.y)) {
                    // Draw shark at platform position
                    game.batch.draw(tex, plat.rect.x, plat.rect.y, width, height, 0, 0, tex.width, tex.height, false, false)
                }
            }
        }

        // Draw Mask using Texture
        maskTexture?.let { tex ->
            val hideMask = (currentLevel == 3 && currentChunk == 3 && !isLightsOn)
            if (!hideMask && isVisible(maskX, maskY)) {
                game.batch.draw(tex, maskX, maskY, 32f, 32f)
            }
        }

        game.batch.end()
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
