import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

update_code = '''
        // --- LEVEL 9 CHUNK 1 LOGIC ---
        if (currentLevel == 9 && currentChunk == 1 && !isDead && !isLevelComplete) {
            chunkTime += delta
            Level9Chunk1State.updateProgress += delta * 0.05f // Slow progress

            // Phase A: Treadmill Bar Physics
            val progressBar = platforms.find { it.label == "PROGRESS_BAR" }
            if (progressBar != null) {
                // If touching the top of the progress bar
                if (Intersector.overlaps(playerRect, progressBar.rect)) {
                    playerX -= 150f * delta // Treadmill pushes left
                    // Flappy jump is enabled natively via canJump = true, but we need to ensure they can jump while on it
                }
            }

            // Phase B: System Pop-ups every 5 seconds
            Level9Chunk1State.popupTimer += delta
            if (Level9Chunk1State.popupTimer >= 5.0f && Level9Chunk1State.popupsSpawned < 3) {
                Level9Chunk1State.popupTimer = 0f
                Level9Chunk1State.popupsSpawned++

                // Spawn a popup that blocks the path. The player must jump over it.
                val popupX = MathUtils.random(400f, 800f)
                val popupY = 420f // Right on top of the treadmill
                val label = if (MathUtils.randomBoolean()) "LOW BATTERY" else "NO SIGNAL"
                platforms.add(Platform(Rectangle(popupX, popupY, 150f, 80f), PlatformType.NORMAL, label = label))
            }

            // Phase C: Finale (Hidden Close Button)
            if (chunkTime >= 20.0f && !Level9Chunk1State.isXSpawned) {
                Level9Chunk1State.isXSpawned = true
                Level9Chunk1State.phase = 1
            }

            // The lethal ACCEPT button & mask
            val acceptRect = gameButtons.firstOrNull()?.rect
            if (acceptRect != null && Intersector.overlaps(playerRect, acceptRect)) {
                die("You should have read the Terms of Service.")
            }
            maskRect.set(maskX, maskY, maskWidth, maskHeight)
            if (Intersector.overlaps(playerRect, maskRect)) {
                die("Update Failed: User is obsolete.")
            }

            // The Win (Hidden X)
            if (Level9Chunk1State.isXSpawned) {
                val xRect = Rectangle(1100f, 650f, 30f, 30f)
                if (Intersector.overlaps(playerRect, xRect)) {
                    win()
                }
            }

            // Abyss Death
            if (playerY < 0f || playerY > 720f) {
                die("Your battery is fine, but your skill is at 0%.")
            }
        }
'''

content = content.replace(
    '// --- LEVEL 8 CHUNK 1 LOGIC ---',
    update_code + '\n        // --- LEVEL 8 CHUNK 1 LOGIC ---'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
