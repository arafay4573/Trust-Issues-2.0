import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val isTouchedByMirror = (currentLevel == 5 && currentChunk == 2 && mirrorActive && mirrorRect.overlaps(plat.rect))',
    'val isTouchedByMirror = ((currentLevel == 5 && currentChunk == 2) || (currentLevel == 6 && currentChunk == 1)) && mirrorActive && mirrorRect.overlaps(plat.rect)'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
