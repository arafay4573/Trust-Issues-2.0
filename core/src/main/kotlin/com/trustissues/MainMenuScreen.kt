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

class MainMenuScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(game.viewport, game.batch)
    private var skin: Skin? = null

    // Manage textures created for the skin to dispose them later
    private val disposables = mutableListOf<Texture>()
    private val fonts = mutableListOf<BitmapFont>()

    private var soundEnabled = true
    private var soundLabel: Label? = null

    override fun show() {
        Gdx.input.inputProcessor = stage
        createBasicSkin()

        val table = Table()
        table.setFillParent(true)
        table.center()

        // Title: TRUST ISSUES
        val titleFont = game.generateFont(60)
        fonts.add(titleFont)
        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val titleLabel = Label("TRUST ISSUES", titleStyle)

        // Buttons
        val playButton = TextButton("PLAY", skin)
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.postRunnable {
                    game.screen = LevelSelectScreen(game)
                    dispose()
                }
            }
        })

        val settingsButton = TextButton("SETTINGS", skin)
        settingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                soundEnabled = !soundEnabled
                println("Muted the pain")
                updateSoundLabel()
            }
        })

        // Sound Label
        val labelFont = game.generateFont(24)
        fonts.add(labelFont)
        val labelStyle = Label.LabelStyle(labelFont, Color.YELLOW)
        soundLabel = Label("Sound: ON", labelStyle)

        // Layout
        table.add(titleLabel).padBottom(100f).row()
        table.add(playButton).size(300f, 100f).padBottom(20f).row()
        table.add(settingsButton).size(200f, 60f).padBottom(10f).row()
        table.add(soundLabel).padTop(10f)

        stage.addActor(table)
    }

    private fun updateSoundLabel() {
        soundLabel?.setText(if (soundEnabled) "Sound: ON" else "Sound: OFF")
    }

    private fun createBasicSkin() {
        skin = Skin()

        // Generate a 1x1 white texture and a default font
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        val whiteTexture = Texture(pixmap)
        pixmap.dispose()

        disposables.add(whiteTexture)
        skin!!.add("white", whiteTexture)

        // Generate button font
        val buttonFont = game.generateFont(30)
        fonts.add(buttonFont)
        skin!!.add("default-font", buttonFont)

        // Configure LabelStyle
        skin!!.add("default", Label.LabelStyle(buttonFont, Color.WHITE))

        // Configure TextButtonStyle
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin!!.newDrawable("white", Color.ORANGE)
        textButtonStyle.down = skin!!.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.checked = skin!!.newDrawable("white", Color.ORANGE)
        textButtonStyle.over = skin!!.newDrawable("white", Color.CORAL)
        textButtonStyle.font = buttonFont
        textButtonStyle.fontColor = Color.WHITE
        skin!!.add("default", textButtonStyle)
    }

    override fun render(delta: Float) {
        // Ocean Blue background (#006994 approx) or similar to Title (#01579B)
        ScreenUtils.clear(Color.valueOf("01579B"))

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        stage.dispose()
        skin?.dispose()
        disposables.forEach { it.dispose() }
        fonts.forEach { it.dispose() }
    }
}
