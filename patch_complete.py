import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '''val isEndOfLevel = nextChunk > 3 || (currentLevel == 7 && nextChunk > 2) || (currentLevel == 8 && nextChunk > 1)''',
    '''val isEndOfLevel = nextChunk > 3 || (currentLevel == 7 && nextChunk > 2) || (currentLevel == 8 && nextChunk > 1) || (currentLevel == 9 && nextChunk > 1)'''
)

content = content.replace(
    '''game.screen = if (nextLevel > 8) LevelSelectScreen(game) else GameScreen(game, nextLevel, 1)''',
    '''game.screen = if (nextLevel > 9) LevelSelectScreen(game) else GameScreen(game, nextLevel, 1)'''
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
