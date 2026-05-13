import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val isExemptLevel = (currentLevel == 3 && currentChunk == 3) || (currentLevel == 4 && currentChunk == 3) || (currentLevel == 6 && currentChunk == 1) || (currentLevel == 6 && currentChunk == 2) || (currentLevel == 8 && currentChunk == 1) || (currentLevel == 8 && currentChunk == 2) || (currentLevel == 8 && currentChunk == 3)',
    'val isExemptLevel = (currentLevel == 3 && currentChunk == 3) || (currentLevel == 4 && currentChunk == 3) || (currentLevel == 6 && currentChunk == 1) || (currentLevel == 6 && currentChunk == 2) || (currentLevel == 8 && currentChunk == 1) || (currentLevel == 8 && currentChunk == 2) || (currentLevel == 8 && currentChunk == 3) || (currentLevel == 9 && currentChunk == 1)'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
