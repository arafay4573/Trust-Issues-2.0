package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
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
    private val playerWidth = 40f
    private val playerHeight = 80f
    private var playerX = 100f
    private var playerY = 200f
    private var velocityY = 0f
    private val gravity = -800f
    private val jumpStrength = 500f
    private val moveSpeed = 300f
    private val floorY = 100f

    // Game State
    private var isDead = false
    private var deathTimer = 0f

    // Assets
    private var sharkTexture: Texture? = null

    // Shark Stats
    private var sharkX = 800f
    private val sharkY = 100f // Shark stays at the same height as player start floor roughly
    private val sharkSpeed = 200f
    private val sharkPatrolRight = 1000f
    private val sharkPatrolLeft = 600f
    private var sharkFacingRight = false

    // Mask Stats
    private val maskX = 1100f
    private val maskY = 200f
    private val maskWidth = 30f
    private val maskHeight = 30f

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var messageLabel: Label? = null

    // Controls
    private var isLeftPressed = false
    private var isRightPressed = false

    override fun show() {
        // Load Game Assets
        sharkTexture = Texture(Gdx.files.internal("shark.png"))

        // UI Setup
        Gdx.input.inputProcessor = uiStage
        createUi()

        // Init Rects
        playerRect.set(playerX, playerY, playerWidth, playerHeight)
        sharkRect.set(sharkX, sharkY, 200f, 100f) // Initial size, updated in loop
        maskRect.set(maskX, maskY, maskWidth, maskHeight)
    }

    private fun createUi() {
        skin = Skin()

        // 1x1 White Texture for button backgrounds
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()
        skin!!.add("white", whiteTexture)

        // Font
        buttonFont = game.generateFont(40)
        skin!!.add("default-font", buttonFont)

        // Button Style
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin!!.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.down = skin!!.newDrawable("white", Color.GRAY)
        textButtonStyle.font = buttonFont
        textButtonStyle.fontColor = Color.WHITE
        skin!!.add("default", textButtonStyle)

        // Label Style
        val labelStyle = Label.LabelStyle(buttonFont, Color.RED)
        skin!!.add("default", labelStyle)

        // Layout Table
        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.bottom()

        // Left Button
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

        // Right Button
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

        // Jump Button
        val jumpBtn = TextButton("JUMP", skin)
        jumpBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!isDead && playerY <= floorY + 1f) { // Simple ground check
                    velocityY = jumpStrength
                }
            }
        })

        // Controls Table
        val controlsTable = Table()
        controlsTable.add(leftBtn).size(100f, 100f).padRight(20f)
        controlsTable.add(rightBtn).size(100f, 100f)

        rootTable.add(controlsTable).left().pad(20f).expandX()
        rootTable.add(jumpBtn).size(150f, 100f).right().pad(20f)

        // Death Message Label (Hidden initially)
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

        // Horizontal Movement
        if (isLeftPressed) playerX -= moveSpeed * delta
        if (isRightPressed) playerX += moveSpeed * delta

        // Gravity
        velocityY += gravity * delta
        playerY += velocityY * delta

        // Floor Collision
        if (playerY < floorY) {
            playerY = floorY
            velocityY = 0f
        }

        // Shark AI
        if (sharkFacingRight) {
            sharkX += sharkSpeed * delta
            if (sharkX > sharkPatrolRight) {
                sharkFacingRight = false
            }
        } else {
            sharkX -= sharkSpeed * delta
            if (sharkX < sharkPatrolLeft) {
                sharkFacingRight = true
            }
        }

        // Update Collision Rects
        playerRect.setPosition(playerX, playerY)
        // Shark dimensions must match drawing (width=200, height=100 approx)
        sharkTexture?.let {
             val ratio = it.height.toFloat() / it.width.toFloat()
             val width = 200f
             val height = width * ratio
             sharkRect.set(sharkX, sharkY, width, height)
        }

        // Check Collisions
        if (Intersector.overlaps(playerRect, sharkRect)) {
            die()
        }

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
    }

    private fun draw() {
        ScreenUtils.clear(Color.valueOf("006994")) // Deep Blue Ocean

        // 1. Draw Game World
        gameViewport.apply()

        // Draw Shark (SpriteBatch)
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let {
            val ratio = it.height.toFloat() / it.width.toFloat()
            val width = 200f
            val height = width * ratio

            game.batch.draw(it, sharkX, sharkY, width, height, 0, 0, it.width, it.height, sharkFacingRight, false)
        }
        game.batch.end()

        // Draw Player & Mask (ShapeRenderer)
        shapeRenderer.projectionMatrix = gameViewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw Mask
        // White Circle
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(maskX + maskWidth/2, maskY + maskHeight/2, maskWidth/2)
        // Cyan details (Strap/Line)
        shapeRenderer.color = Color.CYAN
        shapeRenderer.rect(maskX, maskY + maskHeight/2 - 2, maskWidth, 4f)

        // Draw Player (Stickman)
        shapeRenderer.color = Color.BLACK

        val centerX = playerX + 20f

        // Head
        val headY = playerY + 70f
        shapeRenderer.circle(centerX, headY, 10f)

        // Body
        shapeRenderer.rectLine(centerX, playerY + 60f, centerX, playerY + 30f, 4f)

        // Arms
        shapeRenderer.rectLine(centerX - 15f, playerY + 50f, centerX + 15f, playerY + 50f, 4f)

        // Legs
        shapeRenderer.rectLine(centerX, playerY + 30f, centerX - 10f, playerY, 4f)
        shapeRenderer.rectLine(centerX, playerY + 30f, centerX + 10f, playerY, 4f)

        shapeRenderer.end()

        // 2. Draw UI
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
