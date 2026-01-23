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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class GameScreen(private val game: TrustIssuesGame) : ScreenAdapter() {

    // Game World
    private val gameViewport = FitViewport(1280f, 720f)
    private val shapeRenderer = ShapeRenderer()

    // Collision Rects
    private val playerRect = Rectangle()
    private val sharkRect = Rectangle()
    private val maskRect = Rectangle()

    // Player Stats
    private val playerWidth = 25f
    private var playerHeight = 50f // Variable for crouching
    private val normalHeight = 50f
    private val crouchHeight = 25f

    private var playerX = 100f
    private var playerY = 200f
    private var velocityY = 0f
    private val gravity = -3200f // Tuned for snappier fall
    private val jumpStrength = 1050f // Tuned for snappier jump
    private val moveSpeed = 350f
    private val floorY = 100f

    // Animation State
    private var walkTime = 0f
    private var isWalking = false

    // Game State
    private var isDead = false
    private var deathTimer = 0f

    // Assets
    private var sharkTexture: Texture? = null

    // Shark Stats
    private var sharkX = 700f
    private val sharkY = 100f
    private val sharkSpeed = 200f
    private val sharkPatrolRight = 1000f
    private val sharkPatrolLeft = 600f
    private var sharkFacingRight = false

    // Mask Stats
    private val maskX = 1100f
    private val maskY = 200f
    private val maskWidth = 30f
    private val maskHeight = 30f

    // Atmosphere
    private val bubbles = mutableListOf<Bubble>()
    private val maxBubbles = 20
    private var bubbleSpawnTimer = 0f

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var messageLabel: Label? = null

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

        playerRect.set(playerX, playerY, playerWidth, playerHeight)
        sharkRect.set(sharkX, sharkY, 120f, 60f)
        maskRect.set(maskX, maskY, maskWidth, maskHeight)

        // Init Bubbles
        for (i in 0 until 10) {
            spawnBubble(MathUtils.random(720f))
        }
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

        // Jump
        val jumpBtn = TextButton("UP", skin) // Changed text to UP for clarity or JUMP
        jumpBtn.setText("JUMP")
        jumpBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!isDead && playerY <= floorY + 1f) {
                    velocityY = jumpStrength
                }
            }
        })

        // New Layout: Left/Right on bottom-left. Jump on bottom-right.
        val leftControls = Table()
        leftControls.add(leftBtn).size(100f, 100f).padRight(20f)
        leftControls.add(rightBtn).size(100f, 100f)

        val rightControls = Table()
        // Removed Down Button for Level 1
        rightControls.add(jumpBtn).size(150f, 100f)

        rootTable.add(leftControls).left().pad(20f).expandX()
        rootTable.add(rightControls).right().pad(20f)

        messageLabel = Label("You fed the shark.", skin)
        messageLabel!!.isVisible = false
        messageLabel!!.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)

        uiStage.addActor(messageLabel!!)
        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        update(delta)
        draw()
    }

    private fun update(delta: Float) {
        if (isDead) {
            deathTimer += delta
            if (deathTimer >= 1f) {
                resetPlayer()
            }
            return
        }

        // Crouch Logic
        if (isDownPressed) {
            playerHeight = crouchHeight
        } else {
            // Only stand up if we don't implement ceiling check yet, assuming open air
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

        // "Snappy" Stop (High Friction simulation): if no input, velocity is effectively 0 (position doesn't change)
        // Since we modify position directly based on input, this is already "snappy".
        // We just need to make sure we don't have residual velocity if we were using a velocity-based system.
        // Current implementation is position-based for X, so it stops instantly on release. Correct.

        if (isWalking) {
            walkTime += delta * 15f // Animation speed
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
        if (sharkFacingRight) {
            sharkX += sharkSpeed * delta
            if (sharkX > sharkPatrolRight) sharkFacingRight = false
        } else {
            sharkX -= sharkSpeed * delta
            if (sharkX < sharkPatrolLeft) sharkFacingRight = true
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
             sharkRect.set(sharkX, sharkY, width, height)
        }

        if (Intersector.overlaps(playerRect, sharkRect)) die()
        if (Intersector.overlaps(playerRect, maskRect)) {
            println("CHUNK COMPLETE")
            resetPlayer()
        }

        uiStage.act(delta)
    }

    private fun die() {
        isDead = true
        deathTimer = 0f
        messageLabel?.isVisible = true
    }

    private fun resetPlayer() {
        playerX = 100f
        playerY = 200f
        velocityY = 0f
        isDead = false
        messageLabel?.isVisible = false
        playerHeight = normalHeight
    }

    private fun draw() {
        // Gradient Background
        shapeRenderer.projectionMatrix = uiStage.viewport.camera.combined // Use UI projection for full screen rect
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        // Vertical Gradient: Bottom #001f3f -> Top #0074D9
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

        // Sprites
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let {
            val ratio = it.height.toFloat() / it.width.toFloat()
            val width = 120f
            val height = width * ratio
            game.batch.draw(it, sharkX, sharkY, width, height, 0, 0, it.width, it.height, sharkFacingRight, false)
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
        // Tuned for 50f height: Head ~44, Neck ~38, Waist ~18. Radius 6.
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f

        // Head
        shapeRenderer.circle(centerX, playerY + headOffset, 6f)

        // Body
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)

        // Arms
        if (isCrouching) {
             // Arms held lower
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
