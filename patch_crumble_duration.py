import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

crumble_logic = """
            if (plat.type == PlatformType.CRUMBLING) {
                if (currentLevel == 6 && currentChunk == 1) {
                    plat.startCrumbling(1.2f)
                } else if (currentLevel == 3 && currentChunk == 2 && plat.rect.y == 550f) {
                    plat.startCrumbling(0.1f) // INSTANT FALL TRAP
                } else {
                    plat.startCrumbling(1.5f)
                }
            }
"""

content = re.sub(
    r'(            if \(plat\.type == PlatformType\.CRUMBLING\) \{\n\s*if \(currentLevel == 3 && currentChunk == 2 && plat\.rect\.y == 550f\) \{\n\s*plat\.startCrumbling\(0\.1f\) // INSTANT FALL TRAP\n\s*\} else \{\n\s*plat\.startCrumbling\(1\.5f\)\n\s*\}\n\s*\})',
    crumble_logic,
    content
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
