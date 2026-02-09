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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
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
    private val sharkRect = Rectangle()

    // Player Stats
    private val playerWidth = 25f
    private var playerHeight = 50f
    private val normalHeight = 50f
    private val crouchHeight = 25f

    private var playerX = 100f
    private var playerY = 280f
    private var velocityY = 0f
    private var gravity = -3200f // Now variable
    private val baseGravity = -3200f
    private val jumpStrength = 1050f
    private val moveSpeed = 350f
    private val floorY = 280f
    private var reverseGravity = false

    // Animation State
    private var walkTime = 0f
    private var isWalking = false
    private var canJump = false

    // Game State
    private var isDead = false
    private var isLevelComplete = false
    private var stateTimer = 0f
    private var allowScreenWrap = false
    private var isPaused = false

    // Horror Mode State
    private var horrorMode = false
    private var flashlightRadius = 250f
    private var strobeTimer = 0f
    private var isLightsOn = false

    // Assets
    private var sharkTexture: Texture? = null

    // Entities
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

    enum class PlatformType { NORMAL, CRUMBLE_SLOW, CRUMBLE_FAST, GHOST }
    data class Platform(
        val rect: Rectangle,
        val type: PlatformType,
        var state: String = "ACTIVE",
        var timer: Float = 0f
    )
    private val platforms = mutableListOf<Platform>()

    data class GravitySwitch(val rect: Rectangle, var isActive: Boolean = true)
    private val gravitySwitches = mutableListOf<GravitySwitch>()

    private var maskX = 1100f
    private var maskY = 280f + 50f // Default
    private val maskWidth = 30f
    private val maskHeight = 30f

    // Atmosphere
    data class Bubble(var x: Float, var y: Float, var speed: Float, var radius: Float)
    private val bubbles = mutableListOf<Bubble>()
    private val maxBubbles = 20
    private var bubbleSpawnTimer = 0f

    // Roasts
    private val deathRoasts = listOf(
        "You fed the shark.", "Ocean's tax collector.", "He smelled confidence.",
        "Sharp teeth, bad trust.", "That wasn't a dolphin.", "There is no escape.",
        "Darkness consumes you.", "Did you hear that?", "Gravity hurts."
    )
    private val winRoasts = listOf(
        "Don't relax.", "That was bait.", "One step closer to regret.", "Still breathing? Weird."
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

    override fun show() {
        sharkTexture = Texture(Gdx.files.internal("shark.png"))
        Gdx.input.inputProcessor = uiStage
        createUi()

        setupChunk(currentChunk)

        // Init Bubbles
        for (i in 0 until 10) spawnBubble(MathUtils.random(720f))
    }

    private fun setupChunk(chunk: Int) {
        sharks.clear()
        platforms.clear()
        gravitySwitches.clear()

        playerX = 100f
        playerY = 280f
        velocityY = 0f

        isDead = false
        isLevelComplete = false
        stateTimer = 0f
        allowScreenWrap = false
        reverseGravity = false
        gravity = baseGravity

        isPaused = false
        pauseGroup?.isVisible = false
        messageLabel?.isVisible = false

        horrorMode = (currentLevel == 3)
        strobeTimer = 0f
        isLightsOn = false // Start dark for strobe

        if (levelLabel != null) levelLabel!!.setText("Level $currentLevel-$chunk")

        if (currentLevel == 1) {
            setupLevel1(chunk)
        } else if (currentLevel == 2) {
            setupLevel2(chunk)
        } else if (currentLevel == 3) {
            setupLevel3(chunk)
        }

        maskRect.set(maskX, maskY, maskWidth, maskHeight)
    }

    private fun setupLevel1(chunk: Int) {
        maskY = 280f + 50f
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
                sharks.add(Shark(800f, 280f, 300f, 0f, 1280f, isStalker = true))
                maskX = 1200f
            }
        }
    }

    private fun setupLevel2(chunk: Int) {
        maskY = 280f + 50f
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
    }

    private fun setupLevel3(chunk: Int) {
        // Pitch Black Theme
        // Floor is set to 0 for this level to allow full verticality
        // But let's keep consistent floorY=280 for now unless we need more space.
        // Actually, let's use the full screen. Floor at Y=0.

        // Wait, standard floor is 280. Changing it might break common logic.
        // Let's keep floor at 280 but use platforms.

        when (chunk) {
            1 -> {
                // Flashlight Mode
                maskX = 1100f
                maskY = 280f + 50f
                sharks.add(Shark(600f, 280f, 150f, 400f, 800f)) // Slow shark, hard to see
                platforms.add(Platform(Rectangle(400f, 350f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(700f, 400f, 100f, 20f), PlatformType.NORMAL))
            }
            2 -> {
                // Gravity Switches
                maskX = 1200f
                maskY = 100f // Near bottom

                // Switch to flip gravity UP
                gravitySwitches.add(GravitySwitch(Rectangle(300f, 280f, 40f, 40f)))

                // Platform high up to catch player
                platforms.add(Platform(Rectangle(400f, 600f, 400f, 20f), PlatformType.NORMAL))

                // Switch to flip gravity DOWN
                gravitySwitches.add(GravitySwitch(Rectangle(700f, 560f, 40f, 40f)))

                // Shark patrolling the ceiling platform
                sharks.add(Shark(500f, 600f, 200f, 400f, 800f))
            }
            3 -> {
                // Strobe Light
                maskX = 1100f
                maskY = 280f + 50f
                sharks.add(Shark(400f, 280f, 400f, 0f, 1280f, isStalker = true)) // Fast stalker
                platforms.add(Platform(Rectangle(300f, 350f, 100f, 20f), PlatformType.GHOST))
                platforms.add(Platform(Rectangle(600f, 450f, 100f, 20f), PlatformType.GHOST))
                platforms.add(Platform(Rectangle(900f, 350f, 100f, 20f), PlatformType.GHOST))
            }
        }
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
            game.screen = if (nextLevel > 10) LevelSelectScreen(game) else GameScreen(game, nextLevel, 1)
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

    // ... UI Creation Code (Condensed for brevity, assumed same as before) ...
    // Note: In real output, I must include the full code. I'll paste the procedural texture and UI creation methods here.

    private fun createProceduralTexture(type: String): Texture {
        val size = 120
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.CLEAR)
        pixmap.fill()
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(size/2, size/2, size/2 - 2)
        pixmap.setColor(Color.BLACK)
        val cx = size/2; val cy = size/2
        when(type) {
            "LEFT" -> {
                val offset = 20
                for(i in 0..5) pixmap.drawLine(cx + offset - i, cy - offset, cx - offset - i, cy)
                for(i in 0..5) pixmap.drawLine(cx - offset - i, cy, cx + offset - i, cy + offset)
            }
            "RIGHT" -> {
                val offset = 20
                for(i in 0..5) pixmap.drawLine(cx - offset + i, cy - offset, cx + offset + i, cy)
                for(i in 0..5) pixmap.drawLine(cx + offset + i, cy, cx - offset + i, cy + offset)
            }
            "PAUSE" -> {
                val w = 10; val h = 30; val gap = 10
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
        pixmap.setColor(Color.WHITE); pixmap.fill()
        whiteTexture = Texture(pixmap); pixmap.dispose()
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

        val hudStyle = Label.LabelStyle(buttonFont, Color.YELLOW)
        levelLabel = Label("Level $currentLevel-$currentChunk", hudStyle)
        levelLabel!!.setPosition(20f, 720f - 50f)
        uiStage.addActor(levelLabel!!)

        val pauseBtn = ImageButton(skin!!.get("pause", ImageButton.ImageButtonStyle::class.java))
        pauseBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isDead && !isLevelComplete) {
                    isPaused = true
                    pauseGroup?.isVisible = true
                }
                return true
            }
        })
        pauseBtn.setPosition(20f, 720f - 120f)
        pauseBtn.setSize(60f, 60f)
        uiStage.addActor(pauseBtn)

        pauseGroup = Table()
        pauseGroup!!.setFillParent(true)
        pauseGroup!!.isVisible = false
        val dimPix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        dimPix.setColor(0f, 0f, 0f, 0.7f); dimPix.fill()
        val dimTex = Texture(dimPix); dimPix.dispose()
        pauseGroup!!.background = TextureRegionDrawable(com.badlogic.gdx.graphics.g2d.TextureRegion(dimTex))

        val resumeBtn = TextButton("RESUME", skin)
        resumeBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isPaused = false
                pauseGroup?.isVisible = false
                return true
            }
        })
        val homeBtn = TextButton("HOME", skin)
        homeBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                Gdx.app.postRunnable { game.screen = LevelSelectScreen(game); dispose() }
                return true
            }
        })
        val pauseCenter = Table()
        pauseCenter.add(Label("PAUSED", hudStyle)).padBottom(50f).row()
        pauseCenter.add(resumeBtn).size(200f, 80f).padBottom(20f).row()
        pauseCenter.add(homeBtn).size(200f, 80f)
        pauseGroup!!.add(pauseCenter).center()
        uiStage.addActor(pauseGroup!!)

        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isPaused && !isDead && !isLevelComplete) {
                    if (reverseGravity) {
                        // In reverse, jump pushes DOWN
                        if (canJump) velocityY = -jumpStrength
                    } else {
                        // Normal jump
                        if (canJump) velocityY = jumpStrength
                    }
                }
                return true
            }
        })
        uiStage.addActor(jumpZone)

        val rootTable = Table(); rootTable.setFillParent(true); rootTable.bottom()
        val leftBtn = ImageButton(skin!!.get("left", ImageButton.ImageButtonStyle::class.java))
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isLeftPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) { isLeftPressed = false }
        })
        val rightBtn = ImageButton(skin!!.get("right", ImageButton.ImageButtonStyle::class.java))
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isRightPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) { isRightPressed = false }
        })
        val leftControls = Table()
        leftControls.add(leftBtn).size(120f, 120f).padRight(60f)
        leftControls.add(rightBtn).size(120f, 120f)
        rootTable.add(leftControls).left().pad(20f).expandX()

        messageLabel = Label("", skin)
        messageLabel!!.isVisible = false
        uiStage.addActor(messageLabel!!)
        uiStage.addActor(rootTable)
    }

    override fun render(delta: Float) {
        // Pitch Black Background for Horror Mode
        if (horrorMode) {
            ScreenUtils.clear(0f, 0f, 0f, 1f)
        } else {
            ScreenUtils.clear(0f, 0f, 0.2f, 1f)
        }

        uiStage.act(Gdx.graphics.deltaTime)

        if (!isPaused) {
            update(delta)
        } else {
            Gdx.input.inputProcessor = uiStage
        }

        draw() // Draw calls now handle visibility internally
        uiStage.draw()
    }

    private fun update(delta: Float) {
        if (isDead || isLevelComplete) {
            stateTimer += delta
            if (stateTimer >= 2.0f) {
                Gdx.app.postRunnable { if (isDead) setupChunk(currentChunk) else completeChunk() }
            }
            return
        }

        // Strobe Logic
        if (horrorMode && currentChunk == 3) {
            strobeTimer += delta
            if (isLightsOn) {
                if (strobeTimer > 0.5f) {
                    isLightsOn = false
                    strobeTimer = 0f
                }
            } else {
                if (strobeTimer > 2.0f) {
                    isLightsOn = true
                    strobeTimer = 0f
                }
            }
        }

        playerHeight = if (isDownPressed) crouchHeight else normalHeight
        isWalking = false
        if (isLeftPressed) { playerX -= moveSpeed * delta; isWalking = true }
        if (isRightPressed) { playerX += moveSpeed * delta; isWalking = true }

        if (allowScreenWrap) {
             if (playerX < -40f) playerX = 1280f
             else if (playerX > 1320f) playerX = 0f
        } else {
            if (playerX < 0f || playerX > 1280f - playerWidth) die("There is no escape.")
        }

        if (isWalking) walkTime += delta * 15f else walkTime = 0f

        // Physics
        if (reverseGravity) {
            gravity = 3200f
            velocityY += gravity * delta
            playerY += velocityY * delta

            // Ceiling as Floor? No, need platforms logic.
            // If no platforms, fall up forever?
            if (playerY > 720f) die("Gravity hurts.")
        } else {
            gravity = -3200f
            velocityY += gravity * delta
            playerY += velocityY * delta

            if (playerY < floorY && currentLevel != 3) {
                playerY = floorY
                velocityY = 0f
                canJump = true
            } else if (playerY < -100f) {
                // Pit death
                die("Darkness consumes you.")
            }
        }

        playerRect.set(playerX, playerY, playerWidth, playerHeight)

        // Platform Collision
        canJump = false // Reset per frame
        if (!reverseGravity && playerY <= floorY + 1f && currentLevel != 3) canJump = true

        for (plat in platforms) {
            if (plat.state == "BROKEN") continue
            if (plat.state == "CRUMBLING") {
                plat.timer -= delta
                if (plat.timer <= 0f) plat.state = "BROKEN"
            }

            if (Intersector.overlaps(playerRect, plat.rect)) {
                // Simple collision: Only land on top (or bottom if reversed?)
                // Standard: Falling down onto platform
                if (!reverseGravity && velocityY <= 0) {
                     if (playerY - velocityY * delta >= plat.rect.y + plat.rect.height) {
                         playerY = plat.rect.y + plat.rect.height
                         velocityY = 0f
                         canJump = true
                         if (plat.type.name.startsWith("CRUMBLE") && plat.state == "ACTIVE") {
                             plat.state = "CRUMBLING"
                             plat.timer = if (plat.type == PlatformType.CRUMBLE_SLOW) 1.0f else 0.5f
                         }
                     }
                }
                // Reverse: Falling UP onto platform bottom
                else if (reverseGravity && velocityY >= 0) {
                    if (playerY - velocityY * delta <= plat.rect.y - playerHeight) {
                        playerY = plat.rect.y - playerHeight
                        velocityY = 0f
                        canJump = true
                    }
                }
            }
        }

        // Gravity Switches
        for (switch in gravitySwitches) {
            if (switch.isActive && Intersector.overlaps(playerRect, switch.rect)) {
                reverseGravity = !reverseGravity
                switch.isActive = false // Trigger once? Or timer?
                // Let's make it single use or needs reset.
                // For puzzle, maybe toggle is fine but need cooldown so we don't spam.
                // Simple: Disappear after use?
                // Let's say it disappears for 1 second.
                // For now, let's just make it NOT active.
            }
        }

        // Shark AI
        for (shark in sharks) {
            // ... (Shark logic mostly same, just standard patrol for now) ...
             if (shark.isStalker) {
                var targetDir = if (playerX > shark.x) 1 else -1
                shark.x += shark.speed * delta * targetDir
                shark.facingRight = (targetDir > 0)
            } else if (shark.isSleeper) {
                // ...
            } else {
                if (shark.facingRight) {
                    shark.x += shark.speed * delta
                    if (shark.x > shark.patrolRight) shark.facingRight = false
                } else {
                    shark.x -= shark.speed * delta
                    if (shark.x < shark.patrolLeft) shark.facingRight = true
                }
            }

            // Shark Collision
            sharkRect.set(shark.x, shark.y, 120f, 60f) // approx
            if (Intersector.overlaps(playerRect, sharkRect)) die()
        }

        // Bubbles
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

        if (!isDead && Intersector.overlaps(playerRect, maskRect)) win()
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

    private fun isVisible(x: Float, y: Float): Boolean {
        if (!horrorMode) return true

        // Chunk 3: Global Strobe
        if (currentChunk == 3) {
            return isLightsOn || Vector2.dst(playerX, playerY, x, y) < 100f // Always small radius for player
        }

        // Chunk 1 & 2: Flashlight
        val dist = Vector2.dst(playerX, playerY, x, y)
        return dist < flashlightRadius
    }

    private fun draw() {
        // Draw Background (Bubbles)
        shapeRenderer.projectionMatrix = gameViewport.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        for (b in bubbles) {
            if (isVisible(b.x, b.y)) shapeRenderer.circle(b.x, b.y, b.radius)
        }
        shapeRenderer.end()

        // Draw Platforms
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (plat in platforms) {
            if (plat.state == "BROKEN") continue
            // Check center visibility
            if (isVisible(plat.rect.x + plat.rect.width/2, plat.rect.y + plat.rect.height/2)) {
                shapeRenderer.color = Color.GREEN
                shapeRenderer.rect(plat.rect.x, plat.rect.y, plat.rect.width, plat.rect.height)
            }
        }

        // Draw Switches
        for (sw in gravitySwitches) {
            if (sw.isActive && isVisible(sw.rect.x, sw.rect.y)) {
                shapeRenderer.color = Color.BLUE
                shapeRenderer.rect(sw.rect.x, sw.rect.y, sw.rect.width, sw.rect.height)
            }
        }
        shapeRenderer.end()

        // Draw Sharks
        game.batch.projectionMatrix = gameViewport.camera.combined
        game.batch.begin()
        sharkTexture?.let { tex ->
            val ratio = tex.height.toFloat() / tex.width.toFloat()
            val width = 120f
            val height = width * ratio

            for (shark in sharks) {
                if (isVisible(shark.x, shark.y)) {
                    game.batch.draw(tex, shark.x, shark.y, width, height, 0, 0, tex.width, tex.height, shark.facingRight, false)
                }
            }
        }
        game.batch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw Goal
        if (isVisible(maskX, maskY)) {
            shapeRenderer.color = Color.WHITE
            shapeRenderer.circle(maskX + maskWidth/2, maskY + maskHeight/2, maskWidth/2)
            shapeRenderer.color = Color.CYAN
            shapeRenderer.rect(maskX, maskY + maskHeight/2 - 2, maskWidth, 4f)
        }

        // Draw Player (Always visible to self)
        shapeRenderer.color = Color.BLACK // Wait, if background is black, player invisible?
        // Player should be WHITE in horror mode? Or Gray?
        if (horrorMode) shapeRenderer.color = Color.GRAY else shapeRenderer.color = Color.BLACK

        val centerX = playerX + 12.5f
        // ... (Player drawing code, adjusted for color) ...
        // Simplification for brevity in this thought, will write full code in tool
        val isCrouching = playerHeight < normalHeight
        val headOffset = if (isCrouching) 22f else 44f
        val neckOffset = if (isCrouching) 15f else 38f
        val waistOffset = if (isCrouching) 5f else 18f

        shapeRenderer.circle(centerX, playerY + headOffset, 6f)
        shapeRenderer.rectLine(centerX, playerY + neckOffset, centerX, playerY + waistOffset, 3f)

        val legOffset = if (isCrouching) 0f else (Math.sin(walkTime.toDouble()).toFloat() * 6f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX - 6f - legOffset, playerY, 3f)
        shapeRenderer.rectLine(centerX, playerY + waistOffset, centerX + 6f + legOffset, playerY, 3f)

        // Flashlight beam?
        // Rendering a light cone is hard with ShapeRenderer.
        // The "pitch black" is achieved by clearing to black and only drawing what is isVisible.

        shapeRenderer.end()
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
