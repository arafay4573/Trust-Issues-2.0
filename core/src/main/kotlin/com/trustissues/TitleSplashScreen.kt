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
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.ScreenUtils

class TitleSplashScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(game.viewport, game.batch)
    private var font: BitmapFont? = null

    override fun show() {
        // Retrieve loaded asset
        val titleTexture = if (game.assetManager.isLoaded("game_title.png")) {
            game.assetManager.get("game_title.png", Texture::class.java)
        } else {
            null
        }

        font = BitmapFont()

        val table = Table()
        table.setFillParent(true)
        table.center()

        if (titleTexture != null) {
            val titleImage = Image(titleTexture)
            titleImage.setScaling(Scaling.fit)
            // Constrain size to ensure it fits nicely in the viewport
            table.add(titleImage).size(900f, 300f).center()
        } else {
             // Fallback if asset missing
             val errorLabel = Label("TRUST ISSUES", Label.LabelStyle(font, Color.YELLOW))
             errorLabel.setFontScale(2f)
             table.add(errorLabel).center()
        }

        table.row()
        val subtitle = Label("Nothing is what it looks like", Label.LabelStyle(font, Color.WHITE))
        table.add(subtitle).padTop(20f)

        stage.addActor(table)

        // Initial state: Transparent (for Fade In)
        stage.root.color.a = 0f

        // Logic: Fade In (0.5s) -> Hold (2.0s) -> Fade Out (0.5s) -> MainMenuScreen
        stage.addAction(Actions.sequence(
            Actions.fadeIn(0.5f),
            Actions.delay(2.0f),
            Actions.fadeOut(0.5f),
            Actions.run {
                Gdx.app.postRunnable {
                    game.screen = MainMenuScreen(game)
                    dispose()
                }
            }
        ))
    }

    override fun render(delta: Float) {
        // Ensure background matches
        ScreenUtils.clear(Color.valueOf("01579B"))

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        font?.dispose()
        // Do NOT dispose titleTexture as it is managed by AssetManager
    }
}
