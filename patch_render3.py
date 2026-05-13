import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

batch_fix = '''
                    game.batch.transformMatrix = mat
                    game.batch.begin()

                    // Save original scale
                    val boxScaleX = font.data.scaleX
                    val boxScaleY = font.data.scaleY

                    // Title Bar Text (Small and Crisp)
                    font.data.setScale(boxScaleX * 0.8f, boxScaleY * 0.8f)
                    font.color = Color.WHITE
                    // Align left on title bar
                    font.draw(game.batch, "Android Security Update", boxCenterX - boxWidth/2f + 10f, boxCenterY + boxHeight/2f - 8f)

                    // Body Text
                    font.color = Color.BLACK
                    font.draw(game.batch, "A critical update is required to continue.", boxCenterX - boxWidth/2f + 20f, boxCenterY + 80f)
                    font.draw(game.batch, "Your system might restart multiple times.", boxCenterX - boxWidth/2f + 20f, boxCenterY + 40f)

                    // Progress Text (Above progress bar)
                    font.data.setScale(boxScaleX * 0.7f, boxScaleY * 0.7f)
                    font.draw(game.batch, "Installing Update... 0%", boxCenterX - boxWidth/2f + 20f, boxCenterY - 45f)

                    // Time remaining text
                    font.draw(game.batch, "Estimated time remaining: Calculating...", boxCenterX - boxWidth/2f + 20f, boxCenterY - 95f)

                    // Restore original scale
                    font.data.setScale(boxScaleX, boxScaleY)

                    game.batch.end()
                    game.batch.transformMatrix = oldTransform
                    game.batch.begin()
'''

content = re.sub(r'                    game\.batch\.transformMatrix = mat\n                    game\.batch\.begin\(\).*?game\.batch\.begin\(\)\n', batch_fix + '\n', content, flags=re.DOTALL)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
