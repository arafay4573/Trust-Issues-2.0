package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ScreenUtils

class TitleSplashScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(game.viewport, game.batch)
    private var font: BitmapFont? = null

    override fun show() {
        font = BitmapFont()

        val table = Table()
        table.setFillParent(true)
        table.center()

        // Title: TRUST ISSUES (Gold color)
        val goldColor = Color.valueOf("FFD700")
        val titleStyle = Label.LabelStyle(font, goldColor)
        val titleLabel = Label("TRUST ISSUES", titleStyle)
        titleLabel.setFontScale(3.0f)

        // Subtitle: Nothing is what it looks like (White)
        val subtitleStyle = Label.LabelStyle(font, Color.WHITE)
        val subtitleLabel = Label("Nothing is what it looks like", subtitleStyle)
        subtitleLabel.setFontScale(1.0f)

        table.add(titleLabel).padBottom(20f).row()
        table.add(subtitleLabel)

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
        // Dark Blue Background #0277BD
        ScreenUtils.clear(Color.valueOf("0277BD"))

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        font?.dispose()
    }
}
