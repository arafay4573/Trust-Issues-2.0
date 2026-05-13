import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

update_code = '''
        // --- LEVEL 9 CHUNK 1 LOGIC ---
        if (currentLevel == 9 && currentChunk == 1 && !isDead && !isLevelComplete) {
            chunkTime += delta

            if (Level9Chunk1State.phase == 0) {
                if (chunkTime >= 2.0f) {
                    Level9Chunk1State.phase = 1

                    // Clear dummy obstacles, leave the spawn platform
                    val spawnPlat = platforms.firstOrNull { it.rect.x == 0f }
                    platforms.clear()
                    if (spawnPlat != null) platforms.add(spawnPlat)
                }
            } else if (Level9Chunk1State.phase == 1) {
                // The Android Security Update Box (Center: 640, 360, Size: 600x300)
                val boxCenterX = 640f
                val boxCenterY = 360f
                val boxWidth = 600f
                val boxHeight = 300f
                val topSurfaceY = boxCenterY + boxHeight / 2f

                Level9Chunk1State.isPlayerOnBox = false

                // Check if player is above the box bounds
                if (playerX + playerWidth > boxCenterX - boxWidth / 2f && playerX < boxCenterX + boxWidth / 2f) {
                    // Calculate the Y coordinate of the box's top surface at the player's X
                    val relativeX = (playerX + playerWidth / 2f) - boxCenterX
                    val angleRad = Math.toRadians(Level9Chunk1State.boxAngle.toDouble())
                    // Surface Y equation: y = tan(angle) * x + topSurfaceY
                    val currentSurfaceY = (Math.tan(angleRad) * relativeX).toFloat() + topSurfaceY

                    // If player is falling onto it or walking on it
                    if (playerY <= currentSurfaceY && lastPlayerY >= currentSurfaceY - 20f && velocityY <= 0f) {
                        Level9Chunk1State.isPlayerOnBox = true
                        playerY = currentSurfaceY
                        velocityY = 0f
                        canJump = true // Allow jump

                        // Apply torque based on player distance from center
                        val torque = relativeX * -0.5f // Negative because right side (positive X) tilts angle negative (clockwise)
                        Level9Chunk1State.boxAngleVel += torque * delta

                        // Apply sliding due to slope
                        val slideForce = Math.sin(angleRad).toFloat() * -400f * delta
                        playerX += slideForce
                    }
                }

                // Add some damping/gravity to the box itself
                Level9Chunk1State.boxAngleVel *= 0.95f // Friction
                Level9Chunk1State.boxAngle += Level9Chunk1State.boxAngleVel * delta

                // Limit the angle so it doesn't spin uncontrollably
                Level9Chunk1State.boxAngle = MathUtils.clamp(Level9Chunk1State.boxAngle, -80f, 80f)

                // Win Condition
                maskRect.set(maskX, maskY, maskWidth, maskHeight)
                if (Intersector.overlaps(playerRect, maskRect)) {
                    win()
                }
            }

            // Abyss Death
            if (playerY < 0f) {
                die("The update crashed your system.")
            }
        }
'''

content = content.replace(
    '// --- LEVEL 8 CHUNK 1 LOGIC ---',
    update_code + '\n        // --- LEVEL 8 CHUNK 1 LOGIC ---'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
