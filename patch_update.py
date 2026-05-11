import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

update_code = '''
        // --- LEVEL 9 CHUNK 1 LOGIC ---
        if (currentLevel == 9 && currentChunk == 1 && !isDead && !isLevelComplete) {
            chunkTime += delta

            // Console Typewriter Effect
            if (Level9Chunk1State.textIndex < Level9Chunk1State.consoleFullText.length) {
                Level9Chunk1State.consoleTimer += delta
                if (Level9Chunk1State.consoleTimer >= 0.05f) { // Typing speed
                    Level9Chunk1State.consoleTimer = 0f
                    Level9Chunk1State.consoleText += Level9Chunk1State.consoleFullText[Level9Chunk1State.textIndex]
                    Level9Chunk1State.textIndex++
                }
            }

            if (Level9Chunk1State.phase == 0) {
                if (chunkTime >= 3f && !Level9Chunk1State.loadingCrumbled) {
                    Level9Chunk1State.loadingCrumbled = true
                    // Find loading platform and turn to red
                    val loadPlat = platforms.find { it.label == "[LOADING_LEVEL_9...]" }
                    if (loadPlat != null) {
                        loadPlat.type = PlatformType.CRUMBLING
                        loadPlat.startCrumbling(0.1f) // Vanish quickly
                    }

                    Level9Chunk1State.consoleFullText = "OSIRIS: Let's see how you handle garbage data."
                    Level9Chunk1State.consoleText = ""
                    Level9Chunk1State.textIndex = 0

                    // Spawn new solid data platforms
                    platforms.add(Platform(Rectangle(300f, 200f, 100f, 20f), PlatformType.NORMAL, label = "[DATA]"))
                    platforms.add(Platform(Rectangle(500f, 300f, 100f, 20f), PlatformType.NORMAL, label = "[VOID]"))
                    platforms.add(Platform(Rectangle(700f, 400f, 100f, 20f), PlatformType.NORMAL, label = "[ERROR]"))
                    platforms.add(Platform(Rectangle(900f, 500f, 100f, 20f), PlatformType.NORMAL, label = "[TEMP]"))

                    Level9Chunk1State.phase = 1
                }
            } else if (Level9Chunk1State.phase == 1) {
                if (playerX > 640f) {
                    Level9Chunk1State.phase = 2
                    Level9Chunk1State.consoleFullText = "OSIRIS: Reversing Gravity and Input..."
                    Level9Chunk1State.consoleText = ""
                    Level9Chunk1State.textIndex = 0

                    isControlsInverted = true
                    reverseGravity = true
                    Level9Chunk1State.cameraShakeTimer = 1.0f // 1 second shake

                    // Spawn fake mask
                    maskX = 1150f
                    maskY = 600f

                    // Next phase timer handles OSIRIS backdoor text
                    chunkTime = 0f
                }
            } else if (Level9Chunk1State.phase == 2) {
                if (chunkTime >= 2.0f && Level9Chunk1State.consoleFullText != "OSIRIS: Backdoor opened: [EXIT_POINT]") {
                    Level9Chunk1State.consoleFullText = "OSIRIS: Backdoor opened: [EXIT_POINT]"
                    Level9Chunk1State.consoleText = ""
                    Level9Chunk1State.textIndex = 0
                }

                // Camera shake
                if (Level9Chunk1State.cameraShakeTimer > 0f) {
                    Level9Chunk1State.cameraShakeTimer -= delta
                    val shakeX = MathUtils.random(-10f, 10f)
                    val shakeY = MathUtils.random(-10f, 10f)
                    gameViewport.camera.position.set(640f + shakeX, 360f + shakeY, 0f)
                    val zoom = MathUtils.random(0.9f, 1.1f)
                    (gameViewport.camera as com.badlogic.gdx.graphics.OrthographicCamera).zoom = zoom
                } else {
                    gameViewport.camera.position.set(640f, 360f, 0f)
                    (gameViewport.camera as com.badlogic.gdx.graphics.OrthographicCamera).zoom = 1.0f
                }
                gameViewport.camera.update()

                // Win Collision
                if (Level9Chunk1State.consoleFullText == "OSIRIS: Backdoor opened: [EXIT_POINT]" && Level9Chunk1State.textIndex >= Level9Chunk1State.consoleFullText.length) {
                    val exitRect = Rectangle(640f - 100f, 680f, 200f, 40f)
                    if (Intersector.overlaps(playerRect, exitRect)) {
                        win()
                    }
                }

                // Fake mask death
                maskRect.set(maskX, maskY, maskWidth, maskHeight)
                if (Intersector.overlaps(playerRect, maskRect)) {
                    die("OSIRIS: Nice try, User.")
                }
            }

            // Abyss Death
            if (playerY < 0f || playerY > 720f) {
                die("Error 404: Skill Not Found.")
            }
        }
'''

content = content.replace(
    '// --- LEVEL 8 CHUNK 1 LOGIC ---',
    update_code + '\n        // --- LEVEL 8 CHUNK 1 LOGIC ---'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
