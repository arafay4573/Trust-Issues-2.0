import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

deadly_red_logic = """
        // Check Deadly Red Collision specifically for player
        if ((currentLevel == 5 && currentChunk == 2) || (currentLevel == 6 && currentChunk == 1)) {
            for (plat in platforms) {
                if (plat.type == PlatformType.DEADLY_RED && Intersector.overlaps(playerRect, plat.rect)) {
                    if (currentLevel == 6 && currentChunk == 1) die("Is your screen dirty, or is it just your lack of skill?")
                    else die("You ain't no Newton")
                }
            }
        }
"""

content = re.sub(
    r'(        // Check Deadly Red Collision specifically for player\n        if \(\(currentLevel == 5 && currentChunk == 2\) \|\| \(currentLevel == 6 && currentChunk == 1\)\) \{\n            for \(plat in platforms\) \{\n                if \(plat\.type == PlatformType\.DEADLY_RED && Intersector\.overlaps\(playerRect, plat\.rect\)\) \{\n                    die\("You ain\'t no Newton"\)\n                \}\n            \}\n        \})',
    deadly_red_logic,
    content
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
