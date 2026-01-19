package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
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

    // Assets
    private var sharkTexture: Texture? = null

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null

    // Controls
    private var isLeftPressed = false
    private var isRightPressed = false

    override fun show() {
        // Load Game Assets
        // Assuming shark.png exists as per instructions.
        // Ideally handled by AssetManager, but specific instruction said "Load shark.png into a Texture" locally or similar context implies simple loading.
        // We will check if manager has it or load directly. To be safe, let's load directly as "AssetManager" wasn't explicitly mandated for this specific file in the prompt text ("Load shark.png into a Texture").
        // But better practice is to use the manager if available.
        // Let's lazy load it directly for simplicity as per "Implement The Shark" instruction.
        sharkTexture = Texture(Gdx.files.internal("shark.png"))

        // UI Setup
        Gdx.input.inputProcessor = uiStage
        createUi()
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
                if (playerY <= floorY + 1f) { // Simple ground check
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

        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        update(delta)
        draw()
    }

    private fun update(delta: Float) {
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

        uiStage.act(delta)
    }

    private fun draw() {
        ScreenUtils.clear(Color.valueOf("006994")) // Deep Blue Ocean

        // 1. Draw Game World
        gameViewport.apply()

        // Draw Shark (SpriteBatch)
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let {
            // Resize to 200px width, maintain aspect ratio
            val ratio = it.height.toFloat() / it.width.toFloat()
            val width = 200f
            val height = width * ratio
            game.batch.draw(it, 800f, 100f, width, height)
        }
        game.batch.end()

        // Draw Player (ShapeRenderer)
        shapeRenderer.projectionMatrix = gameViewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(playerX, playerY, playerWidth, playerHeight)
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
