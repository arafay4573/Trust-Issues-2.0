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
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
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
    private val sharkRect = Rectangle() // Temp rect for calculations

    // Player Stats
    private val playerWidth = 25f
    private var playerHeight = 50f
    private val normalHeight = 50f
    private val crouchHeight = 25f

    private var playerX = 100f
    private var playerY = 200f
    private var velocityY = 0f
    private val gravity = -3200f
    private val jumpStrength = 1050f
    private val moveSpeed = 350f
    private val floorY = 100f

    // Animation State
    private var walkTime = 0f
    private var isWalking = false

    // Game State
    private var isDead = false
    private var isLevelComplete = false
    private var stateTimer = 0f

    // Assets
    private var sharkTexture: Texture? = null

    // Shark Stats
    data class Shark(
        var x: Float,
        var y: Float,
        var speed: Float,
        val patrolLeft: Float,
        val patrolRight: Float,
        var facingRight: Boolean = false,
        var isSleeper: Boolean = false,
        var isAwake: Boolean = false
    )

    private val sharks = mutableListOf<Shark>()

    // Mask Stats
    private var maskX = 1100f
    private var maskY = 200f
    private val maskWidth = 30f
    private val maskHeight = 30f

    // Atmosphere
    private val bubbles = mutableListOf<Bubble>()
    private val maxBubbles = 20
    private var bubbleSpawnTimer = 0f

    // Roasts
    private val deathRoasts = listOf(
        "You fed the shark.",
        "Ocean's tax collector.",
        "He smelled confidence.",
        "Sharp teeth, bad trust.",
        "That wasn't a dolphin."
    )

    private val winRoasts = listOf(
        "Don't relax.",
        "That was bait.",
        "One step closer to regret.",
        "Still breathing? Weird."
    )

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var messageLabel: Label? = null
    private var levelLabel: Label? = null

    // Controls
    private var isLeftPressed = false
    private var isRightPressed = false
    private var isDownPressed = false

    // Inner class for Bubble
    data class Bubble(var x: Float, var y: Float, var speed: Float, var radius: Float)

    override fun show() {
        sharkTexture = Texture(Gdx.files.internal("shark.png"))
        Gdx.input.inputProcessor = uiStage
        createUi()

        setupChunk(currentChunk)

        // Init Bubbles
        for (i in 0 until 10) {
            spawnBubble(MathUtils.random(720f))
        }
    }

    private fun setupChunk(chunk: Int) {
        sharks.clear()
        playerX = 100f
        playerY = 200f
        velocityY = 0f
        isDead = false
        isLevelComplete = false
        stateTimer = 0f
        messageLabel?.isVisible = false

        if (levelLabel != null) {
            levelLabel!!.setText("Level $currentLevel-$chunk")
        }

        // Logic for Level 1 Chunks
        if (currentLevel == 1) {
            when (chunk) {
                1 -> {
                    // One normal shark
                    sharks.add(Shark(600f, 100f, 250f, 300f, 900f))
                    maskX = 1100f
                }
                2 -> {
                    // Two sharks
                    sharks.add(Shark(500f, 100f, 200f, 300f, 700f))
                    sharks.add(Shark(900f, 100f, 280f, 800f, 1100f))
                    maskX = 1150f
                }
                3 -> {
                    // Sleeper shark
                    sharks.add(Shark(800f, 100f, 0f, 0f, 1280f, isSleeper = true, facingRight = false))
                    maskX = 1200f
                }
                else -> {
                    // Level Completed
                    completeLevel()
                }
            }
        } else {
             // Placeholder for other levels
             sharks.add(Shark(600f, 100f, 250f, 300f, 900f))
        }

        maskRect.set(maskX, maskY, maskWidth, maskHeight)
    }

    private fun completeLevel() {
        // Unlock next level (2) if we just beat Level 1
        val unlocked = Gdx.app.getPreferences("TrustIssues").getInteger("unlockedLevel", 1)
        if (currentLevel >= unlocked && currentLevel < 11) {
            Gdx.app.getPreferences("TrustIssues").putInteger("unlockedLevel", currentLevel + 1).flush()
        }
        game.screen = LevelSelectScreen(game)
    }

    private fun spawnBubble(startY: Float = -20f) {
        val x = MathUtils.random(1280f)
        val speed = MathUtils.random(50f, 150f)
        val radius = MathUtils.random(2f, 8f)
        bubbles.add(Bubble(x, startY, speed, radius))
    }

    private fun createUi() {
        skin = Skin()
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()
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

        // HUD: Level Info
        val hudStyle = Label.LabelStyle(buttonFont, Color.YELLOW)
        levelLabel = Label("Level $currentLevel-$currentChunk", hudStyle)
        levelLabel!!.setPosition(20f, 720f - 50f)
        uiStage.addActor(levelLabel!!)

        // Big Jump Zone (Right Half of Screen)
        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isDead && !isLevelComplete && playerY <= floorY + 1f) {
                    velocityY = jumpStrength
                }
                return true
            }
        })
        uiStage.addActor(jumpZone)

        // Button Controls
        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.bottom()

        // Left
        val leftBtn = TextButton("<", skin)
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isLeftPressed = true
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isLeftPressed = false
            }
        })

        // Right
        val rightBtn = TextButton(">", skin)
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isRightPressed = true
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isRightPressed = false
            }
        })

        val leftControls = Table()
        leftControls.add(leftBtn).size(100f, 100f).padRight(20f)
        leftControls.add(rightBtn).size(100f, 100f)

        rootTable.add(leftControls).left().pad(20f).expandX()

        messageLabel = Label("You fed the shark.", skin)
        messageLabel!!.isVisible = false
        messageLabel!!.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
        messageLabel!!.setAlignment(com.badlogic.gdx.utils.Align.center)

        uiStage.addActor(messageLabel!!)
        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        update(delta)
        draw()
    }

    private fun update(delta: Float) {
        // Handle transitions for Death or Win
        if (isDead || isLevelComplete) {
            stateTimer += delta
            if (stateTimer >= 2.0f) {
                if (isDead) {
                    setupChunk(currentChunk) // Restart same chunk
                } else {
                    // Go to next chunk
                    if (currentLevel == 1 && currentChunk >= 3) {
                         completeLevel()
                    } else {
                         // We can't simply replace the screen easily without potentially passing data.
                         // But we can just create a new screen.
                         game.screen = GameScreen(game, currentLevel, currentChunk + 1)
                         dispose()
                    }
                }
            }
            return
        }

        // Crouch Logic
        if (isDownPressed) {
            playerHeight = crouchHeight
        } else {
            playerHeight = normalHeight
        }

        // Movement & Physics
        isWalking = false
        if (isLeftPressed) {
            playerX -= moveSpeed * delta
            isWalking = true
        }
        if (isRightPressed) {
            playerX += moveSpeed * delta
            isWalking = true
        }

        if (isWalking) {
            walkTime += delta * 15f
        } else {
            walkTime = 0f
        }

        // Gravity
        velocityY += gravity * delta
        playerY += velocityY * delta

        if (playerY < floorY) {
            playerY = floorY
            velocityY = 0f
        }

        // Shark AI
        for (shark in sharks) {
            if (shark.isSleeper) {
                if (!shark.isAwake) {
                    // Check trigger
                    if (playerX > 400f) {
                        shark.isAwake = true
                        shark.speed = 600f // Lunges
                    }
                }

                if (shark.isAwake) {
                    // Moves left towards player area generally, but here just simple logic
                    // The prompt says "Lunges". Let's assume it charges Left.
                    shark.x -= shark.speed * delta
                    shark.facingRight = false
                }
            } else {
                // Normal Patrol
                if (shark.facingRight) {
                    shark.x += shark.speed * delta
                    if (shark.x > shark.patrolRight) shark.facingRight = false
                } else {
                    shark.x -= shark.speed * delta
                    if (shark.x < shark.patrolLeft) shark.facingRight = true
                }
            }
        }

        // Atmosphere: Bubbles
        bubbleSpawnTimer += delta
        if (bubbleSpawnTimer > 0.5f && bubbles.size < maxBubbles) {
            spawnBubble()
            bubbleSpawnTimer = 0f
        }

        val iter = bubbles.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            b.y += b.speed * delta
            if (b.y > 720f) iter.remove()
        }

        // Collisions
        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        sharkTexture?.let {
             val ratio = it.height.toFloat() / it.width.toFloat()
             val width = 120f
             val height = width * ratio

             for (shark in sharks) {
                 sharkRect.set(shark.x, shark.y, width, height)
                 if (Intersector.overlaps(playerRect, sharkRect)) {
                     die()
                     break
                 }
             }
        }

        if (!isDead && Intersector.overlaps(playerRect, maskRect)) {
            win()
        }

        uiStage.act(delta)
    }

    private fun die() {
        if (isDead) return
        isDead = true
        val roast = deathRoasts.random()
        messageLabel?.setText(roast)
        messageLabel?.color = Color.RED
        messageLabel?.isVisible = true

        // Recenter label
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun win() {
        if (isLevelComplete) return
        isLevelComplete = true
        val roast = winRoasts.random()
        messageLabel?.setText(roast)
        messageLabel?.color = Color.GREEN
        messageLabel?.isVisible = true

        // Recenter label
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun draw() {
        // Gradient Background
        shapeRenderer.projectionMatrix = uiStage.viewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rect(0f, 0f, 1280f, 720f,
            Color.valueOf("001f3f"), Color.valueOf("001f3f"),
            Color.valueOf("0074D9"), Color.valueOf("0074D9"))
        shapeRenderer.end()

        gameViewport.apply()

        // Bubbles
        shapeRenderer.projectionMatrix = gameViewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        for (b in bubbles) {
            shapeRenderer.circle(b.x, b.y, b.radius)
        }
        shapeRenderer.end()

        // Sprites (Sharks)
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let { tex ->
            val ratio = tex.height.toFloat() / tex.width.toFloat()
            val width = 120f
            val height = width * ratio

            for (shark in sharks) {
                game.batch.draw(tex, shark.x, shark.y, width, height, 0, 0, tex.width, tex.height, shark.facingRight, false)
            }
        }
        game.batch.end()

        // Procedural Shapes (Player & Mask)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Mask
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(maskX + maskWidth/2, maskY + maskHeight/2, maskWidth/2)
        shapeRenderer.color = Color.CYAN
        shapeRenderer.rect(maskX, maskY + maskHeight/2 - 2, maskWidth, 4f)

        // Player (Stickman)
        shapeRenderer.color = Color.BLACK
        val centerX = playerX + 12.5f

        // Crouch offsets
        val isCrouching = playerHeight < normalHeight
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f

        // Head
        shapeRenderer.circle(centerX, playerY + headOffset, 6f)

        // Body
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)

        // Arms
        if (isCrouching) {
             shapeRenderer.rectLine(centerX - 10f, playerY + 12f, centerX + 10f, playerY + 12f, 3f)
        } else {
             shapeRenderer.rectLine(centerX - 10f, playerY + 30f, centerX + 10f, playerY + 30f, 3f)
        }

        // Legs (Animated)
        val legOffset = if (isCrouching) 0f else (Math.sin(walkTime.toDouble()).toFloat() * 6f)

        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX - 6f - legOffset, playerY, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX + 6f + legOffset, playerY, 3f)

        shapeRenderer.end()

        // UI
        uiStage.viewport.apply()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        sharkTexture?.dispose()
        uiStage.dispose()
        skin?.dispose()
        whiteTexture?.dispose()
        buttonFont?.dispose()
    }
}
