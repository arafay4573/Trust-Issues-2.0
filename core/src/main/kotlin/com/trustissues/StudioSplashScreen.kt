package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ScreenUtils

class StudioSplashScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(game.viewport, game.batch)
    private var titleFont: BitmapFont? = null
    private var subtitleFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var transitionStarted = false
    private var timeSeconds = 0f

    override fun show() {
        // Generate custom fonts
        titleFont = game.generateFont(40)
        subtitleFont = game.generateFont(24)

        // Create a 1x1 white texture for the loading bar
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()

        val table = Table()
        table.setFillParent(true)
        table.center()

        // Text 1: THE SWIFTERS STUDIO
        val titleStyle = Label.LabelStyle(titleFont, Color.YELLOW)
        val titleLabel = Label("THE SWIFTERS STUDIO", titleStyle)

        // Text 2: A Swifters Studio Production
        val subtitleStyle = Label.LabelStyle(subtitleFont, Color.WHITE)
        val subtitleLabel = Label("A Swifters Studio Production", subtitleStyle)

        // Loading Bar (Visual only)
        val loadingBar = Image(whiteTexture)
        loadingBar.color = Color.GREEN

        table.add(titleLabel).padBottom(10f).row()
        table.add(subtitleLabel).padBottom(50f).row()
        table.add(loadingBar).width(400f).height(10f)

        stage.addActor(table)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(Color.valueOf("29B6F6"))

        timeSeconds += delta

        // Update asset manager (even if empty, good practice)
        val assetsLoaded = game.assetManager.update()

        if (assetsLoaded && timeSeconds >= 2f && !transitionStarted) {
            transitionStarted = true
            stage.addAction(Actions.sequence(
                Actions.fadeOut(0.5f),
                Actions.run {
                    Gdx.app.postRunnable {
                        game.screen = TitleSplashScreen(game)
                        dispose()
                    }
                }
            ))
        }

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        titleFont?.dispose()
        subtitleFont?.dispose()
        whiteTexture?.dispose()
    }
}
