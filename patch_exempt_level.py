import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val isExemptLevel = (currentLevel == 4 && (currentChunk == 2 || currentChunk == 3)) || currentLevel == 3 || currentLevel == 5',
    'val isExemptLevel = (currentLevel == 4 && (currentChunk == 2 || currentChunk == 3)) || currentLevel == 3 || currentLevel == 5 || currentLevel == 6'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
