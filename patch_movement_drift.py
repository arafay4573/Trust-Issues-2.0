import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

drift_update = """
        // Level 6 Chunk 1: Sticky Drift Mechanic
        if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
            // Auto-Flap
            if (flapsRemaining > 0) {
                flapTimer -= delta
                if (flapTimer <= 0f) {
                    val currentJumpStrength = if (currentLevel == 5 && (currentChunk == 2 || currentChunk == 3)) 500f else jumpStrength // Flappy strength logic remains standard
                    if (reverseGravity) {
                        velocityY = -currentJumpStrength
                    } else {
                        velocityY = currentJumpStrength
                    }
                    flapsRemaining--
                    if (flapsRemaining > 0) flapTimer = 0.5f
                }
            }

            // Movement Drift
            if (isDrifting && driftTimer > 0f) {
                driftTimer -= delta
                val driftSpeed = moveSpeed * 0.7f
                var appliedSpeed = driftSpeed

                // Input Conflict
                if ((driftDirection == -1 && rightInput) || (driftDirection == 1 && leftInput)) {
                    // Holding opposite direction while drifting -> fight the drift but don't stop immediately
                    // Since leftInput/rightInput also apply their full force below, this just means they
                    // will counteract each other somewhat. To explicitly slow them down without stopping:
                    appliedSpeed = driftSpeed * 0.5f // Reduce drift force during conflict
                }

                playerX += driftDirection * appliedSpeed * delta
                isWalking = true

                if (driftTimer <= 0f) {
                    isDrifting = false
                }
            }
        }
"""

content = re.sub(
    r'(val leftInput = if \(isControlsInverted\) isRightPressed else isLeftPressed\n\s*val rightInput = if \(isControlsInverted\) isLeftPressed else isRightPressed\n\n\s*if \(leftInput\) \{ playerX -= moveSpeed \* delta; isWalking = true \}\n\s*if \(rightInput\) \{ playerX \+= moveSpeed \* delta; isWalking = true \})',
    r'\1\n' + drift_update,
    content
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
