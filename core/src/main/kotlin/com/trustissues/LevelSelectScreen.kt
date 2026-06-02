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
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class LevelSelectScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    private val stage = Stage(FitViewport(1280f, 720f), game.batch)
    private val shapeRenderer = ShapeRenderer()
    private var skin: Skin? = null

    // Manage assets
    private val disposables = mutableListOf<Texture>()
    private val fonts = mutableListOf<BitmapFont>()

    // Popup
    private var popupTable: Table? = null

    override fun show() {
        Gdx.input.inputProcessor = stage
        createSkin()

        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.center()

        // Back Button
        val backBtn = TextButton("<", skin, "default") // Circular style if available or create simple one
        // Wait, "default" style uses bubble texture which is round. Perfect.
        backBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.postRunnable {
                    game.screen = MainMenuScreen(game)
                    dispose()
                }
            }
        })
        stage.addActor(backBtn)
        backBtn.setPosition(20f, 720f - 100f)
        backBtn.setSize(80f, 80f)

        // Title
        val titleFont = game.generateFont(50)
        fonts.add(titleFont)
        val titleStyle = Label.LabelStyle(titleFont, Color.CYAN)
        val titleLabel = Label("SELECT LEVEL", titleStyle)
        rootTable.add(titleLabel).colspan(5).padBottom(50f).row()

        // Levels Logic
        val unlockedLevel = Gdx.app.getPreferences("TrustIssues").getInteger("unlockedLevel", 1)

        for (i in 1..9) {
            val btn = createLevelButton(i, i <= unlockedLevel)
            rootTable.add(btn).size(120f, 120f).pad(15f)
            if (i % 5 == 0) rootTable.row()
        }

        // Boss Level
        val bossBtn = createLevelButton(10, 10 <= unlockedLevel, isBoss = true)
        rootTable.add(bossBtn).colspan(5).size(200f, 200f).padTop(30f).row()

        val prefs = Gdx.app.getPreferences("TrustIssues")
        if (prefs.getBoolean("devNoteUnlocked", false)) {
            val devBtn = TextButton("Dev Notes", skin, "rect-default")
            devBtn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    Gdx.app.postRunnable {
                        game.screen = GameScreen(game, 11, 1)
                        dispose()
                    }
                }
            })
            rootTable.add(devBtn).colspan(5).size(300f, 80f).padTop(20f)
        }

        stage.addActor(rootTable)

        // Create Popup (Hidden)
        createPopup()
    }

    private fun createPopup() {
        popupTable = Table()
        popupTable!!.setFillParent(true)
        popupTable!!.isVisible = false

        // Dark Blue Semi-Transparent Background
        val dimPix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        dimPix.setColor(0f, 0f, 0.2f, 0.9f) // Dark Blue
        dimPix.fill()
        val dimTex = Texture(dimPix)
        dimPix.dispose()
        disposables.add(dimTex)
        popupTable!!.background = com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex))

        // Centered alignment by default for table children
        popupTable!!.center()

        stage.addActor(popupTable!!)
    }

    private fun showChunkSelection(level: Int) {
        popupTable!!.clearChildren()
        popupTable!!.isVisible = true

        val titleFont = game.generateFont(40)
        fonts.add(titleFont)
        val titleLabel = Label("LEVEL $level", Label.LabelStyle(titleFont, Color.CYAN))
        popupTable!!.add(titleLabel).padBottom(20f).row()

        val chunkTable = Table()

        val prefs = Gdx.app.getPreferences("TrustIssues")
        val unlockedLevel = prefs.getInteger("unlockedLevel", 1)

        var maxChunk = 0
        if (level < unlockedLevel) {
            maxChunk = 3
        } else if (level == unlockedLevel) {
            maxChunk = prefs.getInteger("level_${level}_maxChunk", 1)
        }

        for (c in 1..3) {
            val unlocked = c <= maxChunk
            val btn = TextButton("$c", skin, if (unlocked) "rect-default" else "rect-locked") // Smaller text

            if (unlocked) {
                btn.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        Gdx.app.postRunnable {
                            game.screen = GameScreen(game, level, c)
                            dispose()
                        }
                    }
                })
            }

            // Smaller buttons: 100x80
            chunkTable.add(btn).size(100f, 80f).pad(15f)
        }

        popupTable!!.add(chunkTable).row()

        val closeBtn = TextButton("X", skin, "default") // Use bubble style for close
        closeBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                popupTable!!.isVisible = false
            }
        })
        popupTable!!.add(closeBtn).size(80f, 80f).padTop(30f)
    }

    private fun createLevelButton(level: Int, unlocked: Boolean, isBoss: Boolean = false): TextButton {
        val styleName = if (unlocked) "default" else "locked"
        val text = if (isBoss) "NO CAP\nTIER" else "$level"
        val btn = TextButton(text, skin, styleName)

        if (unlocked) {
            btn.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                     if (level == 10) {
                        Gdx.app.postRunnable {
                            game.screen = GameScreen(game, 10, 1)
                            dispose()
                        }
                     } else {
                         showChunkSelection(level)
                     }
                }
            })
        }

        return btn
    }

    private fun createSkin() {
        skin = Skin()

        // Bubble Texture
        val size = 64
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.CLEAR)
        pixmap.fill()
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(size/2, size/2, size/2 - 2)
        val bubbleTexture = Texture(pixmap)
        pixmap.dispose()
        disposables.add(bubbleTexture)
        skin!!.add("bubble", bubbleTexture)

        // Rect Texture for chunk buttons
        val rectPix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        rectPix.setColor(Color.WHITE)
        rectPix.fill()
        val rectTex = Texture(rectPix)
        rectPix.dispose()
        disposables.add(rectTex)
        skin!!.add("rect", rectTex)

        val buttonFont = game.generateFont(32)
        fonts.add(buttonFont)
        skin!!.add("default-font", buttonFont)

        // Bubble Styles
        val unlockedStyle = TextButton.TextButtonStyle()
        unlockedStyle.up = skin!!.newDrawable("bubble", Color.CYAN)
        unlockedStyle.down = skin!!.newDrawable("bubble", Color.TEAL)
        unlockedStyle.font = buttonFont
        unlockedStyle.fontColor = Color.BLACK
        skin!!.add("default", unlockedStyle)

        val lockedStyle = TextButton.TextButtonStyle()
        lockedStyle.up = skin!!.newDrawable("bubble", Color.valueOf("455A64"))
        lockedStyle.down = skin!!.newDrawable("bubble", Color.DARK_GRAY)
        lockedStyle.font = buttonFont
        lockedStyle.fontColor = Color.GRAY
        skin!!.add("locked", lockedStyle)

        // Chunk Button Styles (Rectangular fallback if needed, or re-use bubble logic? Let's use rect for chunks)
        // Oops, I used "default" and "locked" for chunks too in showChunkSelection.
        // But "default" uses "bubble" drawable.
        // Chunk buttons are TextButtons. 200x80. Bubble drawable is circular.
        // It will stretch weirdly.
        // Let's add specific styles for chunks (Rectangular).

        val chunkUnlocked = TextButton.TextButtonStyle()
        chunkUnlocked.up = skin!!.newDrawable("rect", Color.GREEN)
        chunkUnlocked.down = skin!!.newDrawable("rect", Color.FOREST)
        chunkUnlocked.font = buttonFont
        skin!!.add("chunk-unlocked", chunkUnlocked) // Wait, I need to use these names in showChunkSelection

        // Let's actually just update the names in showChunkSelection to use "rect-default" etc.
        // Or better, redefine "default" to be circular ONLY for level buttons.
        // But TextButton takes a style name.
        // I'll add "rect-default" and "rect-locked".

        val rectUnlocked = TextButton.TextButtonStyle()
        rectUnlocked.up = skin!!.newDrawable("rect", Color.CYAN)
        rectUnlocked.down = skin!!.newDrawable("rect", Color.TEAL)
        rectUnlocked.font = buttonFont
        rectUnlocked.fontColor = Color.BLACK
        skin!!.add("rect-default", rectUnlocked)

        val rectLocked = TextButton.TextButtonStyle()
        rectLocked.up = skin!!.newDrawable("rect", Color.GRAY)
        rectLocked.down = skin!!.newDrawable("rect", Color.DARK_GRAY)
        rectLocked.font = buttonFont
        rectLocked.fontColor = Color.LIGHT_GRAY
        skin!!.add("rect-locked", rectLocked)
    }

    override fun render(delta: Float) {
        shapeRenderer.projectionMatrix = stage.viewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rect(0f, 0f, 1280f, 720f,
            Color.valueOf("000033"), Color.valueOf("000033"),
            Color.valueOf("4FC3F7"), Color.valueOf("4FC3F7"))
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
