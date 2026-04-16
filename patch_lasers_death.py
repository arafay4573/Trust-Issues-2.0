import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Make laser death handle level 6 chunk 1 specific roast
laser_death_logic = """
            if (Intersector.overlaps(playerRect, laser.rect)) {
                if (currentLevel == 6 && currentChunk == 1) {
                    val activelyHolding = isLeftPressed || isRightPressed || isJumpPressed
                    if (activelyHolding || !isDrifting) {
                        die("You can't even control your own thumbs, let alone this game.")
                        stateTimer = -9999f
                    } else {
                        win()
                    }
                } else if (currentLevel == 5 && currentChunk == 3) {
                    die("Curiosity killed the cat, lasers just grilled it.")
                } else {
                    die()
                }
            }
"""

content = re.sub(
    r'(            if \(Intersector\.overlaps\(playerRect, laser\.rect\)\) \{\n\s*if \(currentLevel == 5 && currentChunk == 3\) \{\n\s*die\("Curiosity killed the cat, lasers just grilled it\."\)\n\s*\} else \{\n\s*die\(\)\n\s*\}\n\s*\})',
    laser_death_logic,
    content
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
