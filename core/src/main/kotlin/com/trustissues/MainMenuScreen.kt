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

    override fun show() {
        Gdx.input.inputProcessor = stage
        createBasicSkin()

        val table = Table()
        table.setFillParent(true)
        table.center()

        // Title: TRUST ISSUES
        val titleStyle = Label.LabelStyle(skin!!.getFont("default-font"), Color.WHITE)
        val titleLabel = Label("TRUST ISSUES", titleStyle)
        titleLabel.setFontScale(3f)

        // Buttons
        val playButton = TextButton("PLAY", skin)
        playButton.label.setFontScale(2f)
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                println("Start Game")
            }
        })

        val settingsButton = TextButton("SETTINGS", skin)
        settingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                println("Settings Clicked")
            }
        })

        // Layout
        table.add(titleLabel).padBottom(100f).row()
        table.add(playButton).size(300f, 100f).padBottom(20f).row()
        table.add(settingsButton).size(200f, 60f).right()

        stage.addActor(table)
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

        val font = BitmapFont()
        skin!!.add("default-font", font)

        // Configure LabelStyle
        skin!!.add("default", Label.LabelStyle(font, Color.WHITE))

        // Configure TextButtonStyle
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin!!.newDrawable("white", Color.ORANGE) // Default yellow/orange
        textButtonStyle.down = skin!!.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.checked = skin!!.newDrawable("white", Color.ORANGE)
        textButtonStyle.over = skin!!.newDrawable("white", Color.LIGHT_GRAY)
        textButtonStyle.font = font
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
    }
}
