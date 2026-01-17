package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
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
    private var logoTexture: Texture? = null
    private var font: BitmapFont? = null
    private var transitionStarted = false
    private var timeSeconds = 0f

    override fun show() {
        // Queue assets for next screens
        game.assetManager.load("game_title.png", Texture::class.java)

        // Load resources for this screen synchronously
        try {
            if (Gdx.files.internal("swifters_logo.png").exists()) {
                logoTexture = Texture(Gdx.files.internal("swifters_logo.png"))
                val logoImage = Image(logoTexture)

                font = BitmapFont()
                val labelStyle = Label.LabelStyle(font, Color.WHITE)
                val loadingLabel = Label("Loading...", labelStyle)

                val table = Table()
                table.setFillParent(true)
                table.center()

                table.add(logoImage).center()
                table.row()
                table.add(loadingLabel).padTop(50f)

                stage.addActor(table)
            } else {
                 Gdx.app.error("StudioSplashScreen", "swifters_logo.png not found")
                 font = BitmapFont()
                 val label = Label("Swifters Studio (Logo Missing)", Label.LabelStyle(font, Color.BLACK))
                 stage.addActor(label)
                 label.setPosition(100f, 300f)
            }
        } catch (e: Exception) {
            Gdx.app.error("StudioSplashScreen", "Failed to load splash assets", e)
        }
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(Color.valueOf("29B6F6"))

        timeSeconds += delta

        // Update asset manager
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
        logoTexture?.dispose()
        font?.dispose()
    }
}
