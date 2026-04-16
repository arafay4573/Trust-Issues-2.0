import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

death_logic = """
        // --- LEVEL 6 CHUNK 1 LOGIC (Sticky Drift & Hardware Betrayal) ---
        if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
            // Mirror collision death
            if (mirrorActive && Intersector.overlaps(playerRect, mirrorRect)) {
                die("Stop fighting the drift. Trust the void.")
                stateTimer = -9999f
                return
            }

            // Laser Cage Trap logic
            if (Intersector.overlaps(playerRect, maskRect)) {
                // Determine if drifting or actively holding
                // Drift is true if driftTimer > 0
                val activelyHolding = isLeftPressed || isRightPressed
                // if they are actively holding OR jump is held down OR we are NOT drifting, they die
                if (activelyHolding || !isDrifting) {
                    die("You can't even control your own thumbs, let alone this game.")
                    stateTimer = -9999f
                    return
                } else {
                    win()
                }
            }
        }
        // ----------------------------------------------------------------
"""

content = re.sub(
    r'(        // --- LEVEL 5 CHUNK 1 LOGIC \(Flappy Bird\) ---)',
    death_logic + r'\n\1',
    content
)

# Replace default win logic to exclude level 6 chunk 1 trap mask logic
content = content.replace(
    '} else if (currentLevel != 4 || currentChunk != 3) {',
    '} else if ((currentLevel != 4 || currentChunk != 3) && !(currentLevel == 6 && currentChunk == 1)) {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
