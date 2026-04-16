import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'currentLevel == 5 && currentChunk == 1 -> 1.2f // Level 5-1: 1.2s',
    'currentLevel == 5 && currentChunk == 1 -> 1.2f // Level 5-1: 1.2s\n                         currentLevel == 6 && currentChunk == 1 -> 1.2f // Level 6-1: 1.2s'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
