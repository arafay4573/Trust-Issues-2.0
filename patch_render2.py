import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# I need to fix the batch transform matrix. Default batch transform matrix is identity.
# Projection matrix is what holds the camera.
batch_fix = '''
                    game.batch.transformMatrix = mat
                    game.batch.begin()

                    font.color = Color.WHITE
                    font.draw(game.batch, "Android Security Update", boxCenterX - 190f, boxCenterY + 90f)

                    font.color = Color.BLACK
                    font.draw(game.batch, "A critical update is required.", boxCenterX - 180f, boxCenterY + 40f)
                    font.draw(game.batch, "Installing...", boxCenterX - 180f, boxCenterY)

                    game.batch.end()
                    game.batch.transformMatrix = oldTransform
                    game.batch.begin()
'''

content = re.sub(r'game\.batch\.transformMatrix = game\.batch\.projectionMatrix\.cpy\(\)\.mul\(mat\).*?game\.batch\.begin\(\)', batch_fix, content, flags=re.DOTALL)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
