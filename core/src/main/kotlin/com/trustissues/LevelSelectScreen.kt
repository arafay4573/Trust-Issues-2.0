package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
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
        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val titleLabel = Label("SELECT LEVEL", titleStyle)
        rootTable.add(titleLabel).colspan(5).padBottom(50f).row()

        // Levels Logic
        val unlockedLevel = Gdx.app.getPreferences("TrustIssues").getInteger("unlockedLevel", 1)

        // Grid Layout: 5 columns
        // Row 1: 1-5
        // Row 2: 6-10
        // Row 3: 11 (Boss)

        for (i in 1..10) {
            val btn = createLevelButton(i, i <= unlockedLevel)
            rootTable.add(btn).size(100f, 100f).pad(20f)
            if (i % 5 == 0) rootTable.row()
        }

        // Boss Level
        val bossBtn = createLevelButton(11, 11 <= unlockedLevel, isBoss = true)
        rootTable.add(bossBtn).colspan(5).size(300f, 100f).padTop(20f)

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
        } else {
            // Optional: Add toast or visual feedback for locked levels
             btn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    println("Level $level is locked!")
                }
            })
        }

        return btn
    }

    private fun createSkin() {
        skin = Skin()

        // White pixel for button backgrounds
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val whiteTexture = Texture(pixmap)
        pixmap.dispose()
        disposables.add(whiteTexture)
        skin!!.add("white", whiteTexture)

        val buttonFont = game.generateFont(32)
        fonts.add(buttonFont)
        skin!!.add("default-font", buttonFont)

        // Unlocked Style (Green/Orange)
        val unlockedStyle = TextButton.TextButtonStyle()
        unlockedStyle.up = skin!!.newDrawable("white", Color.valueOf("4CAF50")) // Green
        unlockedStyle.down = skin!!.newDrawable("white", Color.valueOf("388E3C"))
        unlockedStyle.font = buttonFont
        unlockedStyle.fontColor = Color.WHITE
        skin!!.add("default", unlockedStyle)

        // Locked Style (Grey)
        val lockedStyle = TextButton.TextButtonStyle()
        lockedStyle.up = skin!!.newDrawable("white", Color.GRAY)
        lockedStyle.down = skin!!.newDrawable("white", Color.DARK_GRAY)
        lockedStyle.font = buttonFont
        lockedStyle.fontColor = Color.LIGHT_GRAY
        skin!!.add("locked", lockedStyle)
    }

    override fun render(delta: Float) {
        // Dark Blue Gradient Background
        ScreenUtils.clear(Color.valueOf("000033")) // Deep dark blue
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        skin?.dispose()
        disposables.forEach { it.dispose() }
        fonts.forEach { it.dispose() }
    }
}
