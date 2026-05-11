import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

die_code = '''
        if (currentLevel == 9) {
            screenFlashColor = Color.GREEN
            screenFlashTimer = 0.5f // Green flash instead of red
        }
'''

content = content.replace(
    'isDead = true',
    'isDead = true\n' + die_code
)

reset_code = '''
            if (currentLevel == 9) {
                // Reset camera on death just in case
                (gameViewport.camera as com.badlogic.gdx.graphics.OrthographicCamera).zoom = 1.0f
                gameViewport.camera.position.set(640f, 360f, 0f)
                gameViewport.camera.update()
            }
'''

content = content.replace(
    'if (wasDead) setupChunk(currentChunk) else completeChunk()',
    reset_code + '\n                    if (wasDead) setupChunk(currentChunk) else completeChunk()'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
