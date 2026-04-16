import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'if (plat.type == PlatformType.DEADLY_RED && Intersector.overlaps(mirrorRect, plat.rect)) {',
    'if (plat.type == PlatformType.DEADLY_RED && Intersector.overlaps(mirrorRect, plat.rect)) {\n                    if (currentLevel == 6 && currentChunk == 1) die("Stop fighting the drift. Trust the void.")\n                    else '
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
