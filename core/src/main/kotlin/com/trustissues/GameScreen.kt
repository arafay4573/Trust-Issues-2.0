package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class GameScreen(
    private val game: TrustIssuesGame,
    private val currentLevel: Int = 1,
    private val currentChunk: Int = 1
) : ScreenAdapter() {

    // Game World
    private val gameViewport = FitViewport(1280f, 720f)
    private val shapeRenderer = ShapeRenderer()

    // Collision Rects
    private val playerRect = Rectangle()
    private val maskRect = Rectangle()
    private val sharkRect = Rectangle() // Temp rect for calculations

    // Player Stats
    private val playerWidth = 25f
    private var playerHeight = 50f
    private val normalHeight = 50f
    private val crouchHeight = 25f

    private var playerX = 100f
    private var playerY = 280f
    private var velocityY = 0f
    private val gravity = -3200f
    private val jumpStrength = 1050f
    private val moveSpeed = 350f
    private val floorY = 280f

    // Animation State
    private var walkTime = 0f
    private var isWalking = false

    // Game State
    private var isDead = false
    private var isLevelComplete = false
    private var stateTimer = 0f
    private var allowScreenWrap = false
    private var isPaused = false

    // Assets
    private var sharkTexture: Texture? = null

    // Shark Stats
    data class Shark(
        var x: Float,
        var y: Float,
        var speed: Float,
        val patrolLeft: Float,
        val patrolRight: Float,
        var facingRight: Boolean = false,
        var isSleeper: Boolean = false,
        var isAwake: Boolean = false,
        var isStalker: Boolean = false
    )

    private val sharks = mutableListOf<Shark>()

    // Platform Stats
    enum class PlatformType { NORMAL, CRUMBLE_SLOW, CRUMBLE_FAST, GHOST }
    data class Platform(
        val rect: Rectangle,
        val type: PlatformType,
        var state: String = "ACTIVE", // ACTIVE, BROKEN
        var timer: Float = 0f
    )

    private val platforms = mutableListOf<Platform>()

    // Mask Stats
    private var maskX = 1100f
    private var maskY = 200f
    private val maskWidth = 30f
    private val maskHeight = 30f

    // Atmosphere
    private val bubbles = mutableListOf<Bubble>()
    private val maxBubbles = 20
    private var bubbleSpawnTimer = 0f

    // Roasts
    private val deathRoasts = listOf(
        "You fed the shark.",
        "Ocean's tax collector.",
        "He smelled confidence.",
        "Sharp teeth, bad trust.",
        "That wasn't a dolphin.",
        "There is no escape."
    )

    private val winRoasts = listOf(
        "Don't relax.",
        "That was bait.",
        "One step closer to regret.",
        "Still breathing? Weird."
    )

    // UI
    private val uiStage = Stage(FitViewport(1280f, 720f), game.batch)
    private var skin: Skin? = null
    private var buttonFont: BitmapFont? = null
    private var whiteTexture: Texture? = null
    private var messageLabel: Label? = null
    private var levelLabel: Label? = null
    private var pauseGroup: Table? = null

    // Controls
    private var isLeftPressed = false
    private var isRightPressed = false
    private var isDownPressed = false

    // Inner class for Bubble
    data class Bubble(var x: Float, var y: Float, var speed: Float, var radius: Float)

    override fun show() {
        sharkTexture = Texture(Gdx.files.internal("shark.png"))
        Gdx.input.inputProcessor = uiStage
        createUi()

        setupChunk(currentChunk)

        // Init Bubbles
        for (i in 0 until 10) {
            spawnBubble(MathUtils.random(720f))
        }
    }

    private fun setupChunk(chunk: Int) {
        sharks.clear()
        platforms.clear()
        playerX = 100f
        playerY = 280f
        velocityY = 0f
        isDead = false
        isLevelComplete = false
        stateTimer = 0f
        allowScreenWrap = false
        isPaused = false
        pauseGroup?.isVisible = false
        messageLabel?.isVisible = false

        if (levelLabel != null) {
            levelLabel!!.setText("Level $currentLevel-$chunk")
        }

        // Logic for Levels
        if (currentLevel == 1) {
            when (chunk) {
                1 -> {
                    sharks.add(Shark(600f, 280f, 250f, 300f, 900f))
                    maskX = 1100f
                }
                2 -> {
                    sharks.add(Shark(500f, 280f, 200f, 300f, 700f))
                    sharks.add(Shark(900f, 280f, 280f, 800f, 1100f))
                    maskX = 1150f
                }
                3 -> {
                    // Stalker Shark for Ambush - Initial speed 300 to let player get close
                    sharks.add(Shark(800f, 280f, 300f, 0f, 1280f, isStalker = true))
                    maskX = 1200f
                }
            }
        } else if (currentLevel == 2) {
            when (chunk) {
                1 -> {
                    sharks.add(Shark(400f, 280f, 350f, 200f, 800f))
                    sharks.add(Shark(700f, 280f, 350f, 500f, 1100f))
                    platforms.add(Platform(Rectangle(600f, 360f, 150f, 20f), PlatformType.CRUMBLE_SLOW))
                    maskX = 1100f
                }
                2 -> {
                    sharks.add(Shark(500f, 280f, 400f, 300f, 900f))
                    sharks.add(Shark(900f, 280f, 400f, 700f, 1200f))
                    platforms.add(Platform(Rectangle(400f, 380f, 100f, 20f), PlatformType.CRUMBLE_FAST))
                    platforms.add(Platform(Rectangle(800f, 380f, 100f, 20f), PlatformType.CRUMBLE_FAST))
                    maskX = 1150f
                }
                3 -> {
                    allowScreenWrap = true
                    sharks.add(Shark(640f, 280f, 320f, 0f, 1280f, isStalker = true))
                    platforms.add(Platform(Rectangle(600f, 350f, 150f, 20f), PlatformType.GHOST))
                    maskX = 1150f
                }
            }
        } else {
             sharks.add(Shark(600f, 280f, 250f, 300f, 900f))
             maskX = 1100f
        }

        maskY = 280f + 50f
        maskRect.set(maskX, maskY, maskWidth, maskHeight)
    }

    private fun completeChunk() {
        val nextChunk = currentChunk + 1

        val prefs = Gdx.app.getPreferences("TrustIssues")
        val savedMaxChunk = prefs.getInteger("level_${currentLevel}_maxChunk", 1)
        if (nextChunk > savedMaxChunk && nextChunk <= 3) {
            prefs.putInteger("level_${currentLevel}_maxChunk", nextChunk).flush()
        }

        if (nextChunk > 3) {
            val nextLevel = currentLevel + 1
            val unlocked = prefs.getInteger("unlockedLevel", 1)
            if (nextLevel > unlocked) {
                prefs.putInteger("unlockedLevel", nextLevel).flush()
                prefs.putInteger("level_${nextLevel}_maxChunk", 1).flush()
            }

            if (nextLevel > 10) {
                 game.screen = LevelSelectScreen(game)
            } else {
                 game.screen = GameScreen(game, nextLevel, 1)
            }
        } else {
            game.screen = GameScreen(game, currentLevel, nextChunk)
        }
        dispose()
    }

    private fun spawnBubble(startY: Float = -20f) {
        val x = MathUtils.random(1280f)
        val speed = MathUtils.random(50f, 150f)
        val radius = MathUtils.random(2f, 8f)
        val actualY = if (startY == -20f) MathUtils.random(-50f, 280f) else startY
        bubbles.add(Bubble(x, actualY, speed, radius))
    }

    private fun createProceduralTexture(type: String): Texture {
        val size = 120
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.CLEAR)
        pixmap.fill()

        // White Circle
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(size/2, size/2, size/2 - 2)

        // Content
        pixmap.setColor(Color.BLACK)
        when(type) {
            "LEFT" -> {
                // <
                // Draw lines for triangle pointing left
                val cx = size/2
                val cy = size/2
                val offset = 20

                // Manually draw simple arrow lines since fillTriangle isn't guaranteed
                // Top leg
                for(i in 0..5) pixmap.drawLine(cx + offset - i, cy - offset, cx - offset - i, cy)
                // Bottom leg
                for(i in 0..5) pixmap.drawLine(cx - offset - i, cy, cx + offset - i, cy + offset)
            }
            "RIGHT" -> {
                // >
                val cx = size/2
                val cy = size/2
                val offset = 20

                // Top leg
                for(i in 0..5) pixmap.drawLine(cx - offset + i, cy - offset, cx + offset + i, cy)
                // Bottom leg
                for(i in 0..5) pixmap.drawLine(cx + offset + i, cy, cx - offset + i, cy + offset)
            }
            "PAUSE" -> {
                // ||
                val cx = size/2
                val cy = size/2
                val w = 10
                val h = 30
                val gap = 10

                pixmap.fillRectangle(cx - gap - w, cy - h/2, w, h)
                pixmap.fillRectangle(cx + gap, cy - h/2, w, h)
            }
        }

        val tex = Texture(pixmap)
        pixmap.dispose()
        return tex
    }

    private fun createUi() {
        skin = Skin()
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()
        skin!!.add("white", whiteTexture)

        buttonFont = game.generateFont(40)
        skin!!.add("default-font", buttonFont)

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin!!.newDrawable("white", Color.DARK_GRAY)
        textButtonStyle.down = skin!!.newDrawable("white", Color.GRAY)
        textButtonStyle.font = buttonFont
        textButtonStyle.fontColor = Color.WHITE
        skin!!.add("default", textButtonStyle)

        val labelStyle = Label.LabelStyle(buttonFont, Color.RED)
        skin!!.add("default", labelStyle)

        // Generate Textures
        val leftTex = createProceduralTexture("LEFT")
        val rightTex = createProceduralTexture("RIGHT")
        val pauseTex = createProceduralTexture("PAUSE")

        val imageBtnStyle = ImageButton.ImageButtonStyle()
        imageBtnStyle.up = TextureRegionDrawable(leftTex)
        imageBtnStyle.down = TextureRegionDrawable(leftTex).tint(Color.GRAY)
        skin!!.add("left", imageBtnStyle)

        val rightStyle = ImageButton.ImageButtonStyle()
        rightStyle.up = TextureRegionDrawable(rightTex)
        rightStyle.down = TextureRegionDrawable(rightTex).tint(Color.GRAY)
        skin!!.add("right", rightStyle)

        val pauseStyle = ImageButton.ImageButtonStyle()
        pauseStyle.up = TextureRegionDrawable(pauseTex)
        pauseStyle.down = TextureRegionDrawable(pauseTex).tint(Color.GRAY)
        skin!!.add("pause", pauseStyle)

        // HUD
        val hudStyle = Label.LabelStyle(buttonFont, Color.YELLOW)
        levelLabel = Label("Level $currentLevel-$currentChunk", hudStyle)
        levelLabel!!.setPosition(20f, 720f - 50f)
        uiStage.addActor(levelLabel!!)

        // Pause Button
        val pauseBtn = ImageButton(skin!!.get("pause", ImageButton.ImageButtonStyle::class.java))
        pauseBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!isDead && !isLevelComplete) {
                    isPaused = true
                    pauseGroup?.isVisible = true
                }
            }
        })
        pauseBtn.setPosition(20f, 720f - 120f)
        pauseBtn.setSize(60f, 60f)
        uiStage.addActor(pauseBtn)

        // Pause Menu Group
        pauseGroup = Table()
        pauseGroup!!.setFillParent(true)
        pauseGroup!!.isVisible = false

        val dimPix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        dimPix.setColor(0f, 0f, 0f, 0.7f)
        dimPix.fill()
        val dimTex = Texture(dimPix)
        dimPix.dispose()
        pauseGroup!!.background = TextureRegionDrawable(com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex))

        val resumeBtn = TextButton("RESUME", skin)
        resumeBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                isPaused = false
                pauseGroup?.isVisible = false
            }
        })

        val homeBtn = TextButton("HOME", skin)
        homeBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.postRunnable {
                    game.screen = LevelSelectScreen(game)
                    dispose()
                }
            }
        })

        // Centered Buttons for Pause Menu
        val pauseCenter = Table()
        pauseCenter.add(Label("PAUSED", hudStyle)).padBottom(50f).row()
        pauseCenter.add(resumeBtn).size(200f, 80f).padBottom(20f).row()
        pauseCenter.add(homeBtn).size(200f, 80f)
        pauseGroup!!.add(pauseCenter).center()

        uiStage.addActor(pauseGroup!!)

        // Big Jump Zone
        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isPaused && !isDead && !isLevelComplete && playerY <= floorY + 1f) {
                    velocityY = jumpStrength
                } else if (!isPaused && !isDead && !isLevelComplete) {
                     if (Math.abs(velocityY) < 10f) {
                         velocityY = jumpStrength
                     }
                }
                return true
            }
        })
        uiStage.addActor(jumpZone)

        // Controls
        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.bottom()

        val leftBtn = ImageButton(skin!!.get("left", ImageButton.ImageButtonStyle::class.java))
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isLeftPressed = true
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isLeftPressed = false
            }
        })

        val rightBtn = ImageButton(skin!!.get("right", ImageButton.ImageButtonStyle::class.java))
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isRightPressed = true
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isRightPressed = false
            }
        })

        val leftControls = Table()
        leftControls.add(leftBtn).size(120f, 120f).padRight(60f)
        leftControls.add(rightBtn).size(120f, 120f)

        rootTable.add(leftControls).left().pad(20f).expandX()

        messageLabel = Label("You fed the shark.", skin)
        messageLabel!!.isVisible = false
        messageLabel!!.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
        messageLabel!!.setAlignment(Align.center)

        uiStage.addActor(messageLabel!!)
        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0.2f, 1f)

        // Always act and draw UI (fix unresponsive pause buttons)
        // Use Gdx.graphics.deltaTime for UI to ensure it updates even if game time is manipulated
        uiStage.act(Gdx.graphics.deltaTime)

        if (!isPaused) {
            update(delta)
            draw()
        } else {
            // Force input processor to UI when paused to ensure clicks work
            Gdx.input.inputProcessor = uiStage
            draw()
        }

        uiStage.draw()
    }

    private fun update(delta: Float) {
        if (isDead || isLevelComplete) {
            stateTimer += delta
            if (stateTimer >= 2.0f) {
                Gdx.app.postRunnable {
                    if (isDead) {
                        setupChunk(currentChunk)
                    } else {
                        completeChunk()
                    }
                }
            }
            return
        }

        if (isDownPressed) {
            playerHeight = crouchHeight
        } else {
            playerHeight = normalHeight
        }

        isWalking = false
        if (isLeftPressed) {
            playerX -= moveSpeed * delta
            isWalking = true
        }
        if (isRightPressed) {
            playerX += moveSpeed * delta
            isWalking = true
        }

        if (allowScreenWrap) {
             if (playerX < -40f) {
                 playerX = 1280f
                 for (shark in sharks) {
                     if (shark.isStalker) shark.speed = 600f
                 }
             } else if (playerX > 1320f) {
                 playerX = 0f
             }
        } else {
            // Strictly enforce bounds for levels like 1-3
            if (playerX < 0f || playerX > 1280f - playerWidth) {
                die(customMessage = "There is no escape.")
            }
        }

        if (isWalking) {
            walkTime += delta * 15f
        } else {
            walkTime = 0f
        }

        velocityY += gravity * delta
        playerY += velocityY * delta

        if (playerY < floorY) {
            playerY = floorY
            velocityY = 0f
        }

        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        for (plat in platforms) {
            if (plat.state == "BROKEN") continue

            if (plat.state == "CRUMBLING") {
                plat.timer -= delta
                if (plat.timer <= 0f) {
                    plat.state = "BROKEN"
                    continue
                }
            }

            if (velocityY <= 0 && plat.type != PlatformType.GHOST) {
                if (playerY >= plat.rect.y + plat.rect.height - 10f &&
                    playerX + playerWidth > plat.rect.x && playerX < plat.rect.x + plat.rect.width) {

                     if (playerY + velocityY * delta <= plat.rect.y + plat.rect.height) {
                         playerY = plat.rect.y + plat.rect.height
                         velocityY = 0f

                         if ((plat.type == PlatformType.CRUMBLE_SLOW || plat.type == PlatformType.CRUMBLE_FAST)
                             && plat.state == "ACTIVE") {
                             plat.state = "CRUMBLING"
                             plat.timer = if (plat.type == PlatformType.CRUMBLE_SLOW) 1.0f else 0.5f
                         }
                     }
                }
            }
        }

        // Shark AI
        for (shark in sharks) {
            if (shark.isStalker) {
                // DEFAULT: Chase the player
                var targetDir = if (playerX > shark.x) 1 else -1

                // --- LEVEL 1 CHUNK 3 EXCLUSIVE REWRITE ---
                if (currentLevel == 1 && currentChunk == 3) {

                    // MOMENTUM LOCK (The Fix):
                    // If the shark is already moving LEFT (chasing you to the wall)...
                    // AND he is past the middle of the screen (x < 640)...
                    // HE MUST IGNORE THE PLAYER. He keeps running Left until he hits the wall.
                    if (!shark.facingRight && shark.x < 640f) {
                        targetDir = -1 // Force Left (Ignore Player Jump)
                    }

                    // Apply Movement
                    shark.x += shark.speed * delta * targetDir

                    // Update Facing (Only visual)
                    shark.facingRight = (targetDir > 0)

                    // TELEPORT LOGIC (Infinite Loop)
                    val sharkWidth = 120f
                    // Hit Left Wall -> Teleport Right -> SPRINT
                    if (shark.x < -sharkWidth - 50f) {
                        shark.x = 1280f
                        shark.speed = 950f
                    }
                    // Hit Right Wall -> Teleport Left
                    if (shark.x > 1280f + 50f) {
                        shark.x = -sharkWidth
                        shark.speed = 950f
                    }
                }
                // --- ALL OTHER LEVELS (Standard Logic) ---
                else {
                    shark.x += shark.speed * delta * targetDir
                    shark.facingRight = (targetDir > 0)
                    // Clamp
                    val sharkWidth = 120f
                    if (shark.x < 0f) shark.x = 0f
                    if (shark.x > 1280f - sharkWidth) shark.x = 1280f - sharkWidth
                }
            } else if (shark.isSleeper) {
                if (!shark.isAwake) {
                    if (playerX > 400f) {
                        shark.isAwake = true
                        shark.speed = 600f
                    }
                }
                if (shark.isAwake) {
                    shark.x -= shark.speed * delta
                    shark.facingRight = false
                }
            } else {
                if (shark.facingRight) {
                    shark.x += shark.speed * delta
                    if (shark.x > shark.patrolRight) shark.facingRight = false
                } else {
                    shark.x -= shark.speed * delta
                    if (shark.x < shark.patrolLeft) shark.facingRight = true
                }
            }
        }

        bubbleSpawnTimer += delta
        if (bubbleSpawnTimer > 0.5f && bubbles.size < maxBubbles) {
            spawnBubble()
            bubbleSpawnTimer = 0f
        }

        val iter = bubbles.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            b.y += b.speed * delta
            if (b.y > 720f) iter.remove()
        }

        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        sharkTexture?.let {
             val ratio = it.height.toFloat() / it.width.toFloat()
             val width = 120f
             val height = width * ratio

             for (shark in sharks) {
                 sharkRect.set(shark.x, shark.y, width, height)
                 if (Intersector.overlaps(playerRect, sharkRect)) {
                     die()
                     break
                 }
             }
        }

        if (!isDead && Intersector.overlaps(playerRect, maskRect)) {
            win()
        }

        uiStage.act(delta)
    }

    private fun die(customMessage: String? = null) {
        if (isDead) return
        isDead = true
        val roast = customMessage ?: deathRoasts.random()
        messageLabel?.setText(roast)
        messageLabel?.color = Color.RED
        messageLabel?.isVisible = true
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun win() {
        if (isLevelComplete) return
        isLevelComplete = true
        val roast = winRoasts.random()
        messageLabel?.setText(roast)
        messageLabel?.color = Color.GREEN
        messageLabel?.isVisible = true
        messageLabel?.pack()
        messageLabel?.setPosition(1280f / 2 - messageLabel!!.width / 2, 500f)
    }

    private fun draw() {
        shapeRenderer.projectionMatrix = uiStage.viewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.rect(0f, 0f, 1280f, 720f,
            Color.valueOf("001f3f"), Color.valueOf("001f3f"),
            Color.valueOf("0074D9"), Color.valueOf("0074D9"))
        shapeRenderer.end()

        gameViewport.apply()

        shapeRenderer.projectionMatrix = gameViewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        for (b in bubbles) {
            shapeRenderer.circle(b.x, b.y, b.radius)
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (plat in platforms) {
            if (plat.state == "BROKEN") continue
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(plat.rect.x, plat.rect.y, plat.rect.width, plat.rect.height)
        }
        shapeRenderer.end()

        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let { tex ->
            val ratio = tex.height.toFloat() / tex.width.toFloat()
            val width = 120f
            val height = width * ratio

            for (shark in sharks) {
                game.batch.draw(tex, shark.x, shark.y, width, height, 0, 0, tex.width, tex.height, shark.facingRight, false)
            }
        }
        game.batch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(maskX + maskWidth/2, maskY + maskHeight/2, maskWidth/2)
        shapeRenderer.color = Color.CYAN
        shapeRenderer.rect(maskX, maskY + maskHeight/2 - 2, maskWidth, 4f)

        shapeRenderer.color = Color.BLACK
        val centerX = playerX + 12.5f

        val isCrouching = playerHeight < normalHeight
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f

        shapeRenderer.circle(centerX, playerY + headOffset, 6f)
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)

        if (isCrouching) {
             shapeRenderer.rectLine(centerX - 10f, playerY + 12f, centerX + 10f, playerY + 12f, 3f)
        } else {
             shapeRenderer.rectLine(centerX - 10f, playerY + 30f, centerX + 10f, playerY + 30f, 3f)
        }

        val legOffset = if (isCrouching) 0f else (Math.sin(walkTime.toDouble()).toFloat() * 6f)

        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX - 6f - legOffset, playerY, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX + 6f + legOffset, playerY, 3f)

        shapeRenderer.end()

        uiStage.viewport.apply()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
    }

    override fun dispose() {
        shapeRenderer.dispose()
        sharkTexture?.dispose()
        uiStage.dispose()
        skin?.dispose()
        whiteTexture?.dispose()
        buttonFont?.dispose()
    }
}
