import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

reset_code = '''
            if (currentLevel == 9) {
                // UI Windows reset is handled by setupLevel9 naturally resetting lists
            }
'''

content = content.replace(
    'if (wasDead) setupChunk(currentChunk) else completeChunk()',
    reset_code + '\n                    if (wasDead) setupChunk(currentChunk) else completeChunk()'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
