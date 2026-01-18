package com.trustissues

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.viewport.FitViewport

class TrustIssuesGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var assetManager: AssetManager
    lateinit var viewport: FitViewport

    override fun create() {
        batch = SpriteBatch()
        assetManager = AssetManager()
        // FitViewport 1280x720
        viewport = FitViewport(1280f, 720f)

        setScreen(StudioSplashScreen(this))
    }

    fun generateFont(size: Int): BitmapFont {
        val generator = FreeTypeFontGenerator(Gdx.files.internal("pixel.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = size
        val font = generator.generateFont(parameter)
        generator.dispose()
        return font
    }

    override fun dispose() {
        batch.dispose()
        assetManager.dispose()
        // Only dispose the screen if it exists.
        // Note: Game.dispose() calls screen.hide(), but not dispose.
        screen?.dispose()
    }
}
