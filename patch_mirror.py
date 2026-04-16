import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

mirror_update = """
        // Mirror activation for Level 6
        if (currentLevel == 6 && currentChunk == 1 && playerY >= 550f && !mirrorActive) {
            mirrorActive = true
            mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)
        }
"""

content = re.sub(
    r'(playerRect\.set\(playerX, playerY, playerWidth, playerHeight\))',
    r'\1\n' + mirror_update,
    content
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
