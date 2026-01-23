package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class LevelSelectScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(FitViewport(1280f, 720f), game.batch)
    private val shapeRenderer = ShapeRenderer()
    private var skin: Skin? = null

    // Manage assets
    private val disposables = mutableListOf<Texture>()
    private val fonts = mutableListOf<BitmapFont>()

    override fun show() {
        Gdx.input.inputProcessor = stage
        createSkin()

        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.center()

        // Title
        val titleFont = game.generateFont(50)
        fonts.add(titleFont)
        val titleStyle = Label.LabelStyle(titleFont, Color.CYAN) // Cyan title
        val titleLabel = Label("SELECT LEVEL", titleStyle)
        rootTable.add(titleLabel).colspan(5).padBottom(50f).row()

        // Levels Logic
        val unlockedLevel = Gdx.app.getPreferences("TrustIssues").getInteger("unlockedLevel", 1)

        // Staggered Layout
        // Row 1: Left aligned (padRight)
        // Row 2: Right aligned (padLeft)
        // Row 3: Center

        for (i in 1..10) {
            val btn = createLevelButton(i, i <= unlockedLevel)

            // Stagger logic:
            // Odd numbers (1, 3, 5): "Left"
            // Even numbers (2, 4, 6): "Right"
            // Actually, let's do 3 columns per row, but shift the whole row.

            val cell = rootTable.add(btn).size(120f, 120f).pad(15f)

            // Layout logic: 5 columns max
            if (i % 5 == 0) rootTable.row()
        }

        // Boss Level (Bubble)
        val bossBtn = createLevelButton(11, 11 <= unlockedLevel, isBoss = true)
        rootTable.add(bossBtn).colspan(5).size(150f, 150f).padTop(30f)

        stage.addActor(rootTable)
    }

    private fun createLevelButton(level: Int, unlocked: Boolean, isBoss: Boolean = false): TextButton {
        val styleName = if (unlocked) "default" else "locked"
        val text = if (isBoss) "BOSS" else "$level"
        val btn = TextButton(text, skin, styleName)

        if (unlocked) {
            btn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                     Gdx.app.postRunnable {
                        game.screen = GameScreen(game, level, 1)
                        dispose()
                    }
                }
            })
        }

        return btn
    }

    private fun createSkin() {
        skin = Skin()

        // Create Circular Bubble Texture
        val size = 64
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.CLEAR)
        pixmap.fill()

        // Draw Circle
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(size/2, size/2, size/2 - 2)

        val bubbleTexture = Texture(pixmap)
        pixmap.dispose()
        disposables.add(bubbleTexture)
        skin!!.add("bubble", bubbleTexture)

        val buttonFont = game.generateFont(32)
        fonts.add(buttonFont)
        skin!!.add("default-font", buttonFont)

        // Unlocked Style (Cyan Bubble)
        val unlockedStyle = TextButton.TextButtonStyle()
        unlockedStyle.up = skin!!.newDrawable("bubble", Color.CYAN)
        unlockedStyle.down = skin!!.newDrawable("bubble", Color.TEAL)
        unlockedStyle.font = buttonFont
        unlockedStyle.fontColor = Color.BLACK
        skin!!.add("default", unlockedStyle)

        // Locked Style (Dark Blue/Grey Bubble)
        val lockedStyle = TextButton.TextButtonStyle()
        lockedStyle.up = skin!!.newDrawable("bubble", Color.valueOf("455A64")) // Blue Grey
        lockedStyle.down = skin!!.newDrawable("bubble", Color.DARK_GRAY)
        lockedStyle.font = buttonFont
        lockedStyle.fontColor = Color.GRAY
        skin!!.add("locked", lockedStyle)
    }

    override fun render(delta: Float) {
        // Gradient Background
        shapeRenderer.projectionMatrix = stage.viewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        // Light Blue to Deep Abyss
        shapeRenderer.rect(0f, 0f, 1280f, 720f,
            Color.valueOf("000033"), Color.valueOf("000033"), // Bottom (Abyss)
            Color.valueOf("4FC3F7"), Color.valueOf("4FC3F7")) // Top (Light Blue)
        shapeRenderer.end()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        shapeRenderer.dispose()
        skin?.dispose()
        disposables.forEach { it.dispose() }
        fonts.forEach { it.dispose() }
    }
}
