import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

bg_code = '''
        if (currentLevel == 9 && currentChunk == 1 && Level9Chunk1State.phase == 1) {
            shapeRenderer.color = Color.BLACK
            shapeRenderer.rect(0f + renderOffset, 0f, 1280f, 720f)
        }
'''

content = content.replace(
    '// Draw Mirror Player / Ghost for Level 5/6/8',
    bg_code + '\n        // Draw Mirror Player / Ghost for Level 5/6/8'
)

shape_code = '''
        if (currentLevel == 9 && currentChunk == 1 && Level9Chunk1State.phase == 1) {
            val boxCenterX = 640f + renderOffset
            val boxCenterY = 250f
            val boxWidth = 400f
            val boxHeight = 200f

            val oldTransform = shapeRenderer.transformMatrix.cpy()
            shapeRenderer.translate(boxCenterX, boxCenterY, 0f)
            shapeRenderer.rotate(0f, 0f, 1f, Level9Chunk1State.boxAngle)
            shapeRenderer.translate(-boxCenterX, -boxCenterY, 0f)

            // Box Body
            shapeRenderer.color = Color(0.9f, 0.9f, 0.9f, 1f) // Light Grey
            shapeRenderer.rect(boxCenterX - boxWidth/2f, boxCenterY - boxHeight/2f, boxWidth, boxHeight)

            // Blue Title Bar (Windows XP style)
            shapeRenderer.color = Color(0.1f, 0.4f, 0.8f, 1f) // Classic Blue
            shapeRenderer.rect(boxCenterX - boxWidth/2f, boxCenterY + boxHeight/2f - 30f, boxWidth, 30f)

            // Red Close Button
            shapeRenderer.color = Color(0.8f, 0.2f, 0.2f, 1f)
            shapeRenderer.rect(boxCenterX + boxWidth/2f - 30f, boxCenterY + boxHeight/2f - 30f, 30f, 30f)

            // White X in close button
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rectLine(boxCenterX + boxWidth/2f - 25f, boxCenterY + boxHeight/2f - 25f, boxCenterX + boxWidth/2f - 5f, boxCenterY + boxHeight/2f - 5f, 2f)
            shapeRenderer.rectLine(boxCenterX + boxWidth/2f - 25f, boxCenterY + boxHeight/2f - 5f, boxCenterX + boxWidth/2f - 5f, boxCenterY + boxHeight/2f - 25f, 2f)

            shapeRenderer.transformMatrix = oldTransform
        }
'''

content = content.replace(
    '// Draw The Shrinking Void Overlay (Level 7 Chunk 3)',
    shape_code + '\n        // Draw The Shrinking Void Overlay (Level 7 Chunk 3)'
)

batch_code = '''
        if (currentLevel == 9 && currentChunk == 1) {
            // Recolor Spawn Platform
            if (Level9Chunk1State.phase == 1) {
                // If we had a specific texture, we'd draw it here, but ShapeRenderer handles basic shapes.
                // We will let ShapeRenderer draw the platform, we just need to ensure the text on the box rotates.
                buttonFont?.let { font ->
                    val boxCenterX = 640f + renderOffset
                    val boxCenterY = 250f

                    val oldTransform = game.batch.transformMatrix.cpy()
                    game.batch.end() // End to apply transform safely? No, set transform.

                    val mat = com.badlogic.gdx.math.Matrix4()
                    mat.setToTranslation(boxCenterX, boxCenterY, 0f)
                    mat.rotate(0f, 0f, 1f, Level9Chunk1State.boxAngle)
                    mat.translate(-boxCenterX, -boxCenterY, 0f)

                    game.batch.transformMatrix = game.batch.projectionMatrix.cpy().mul(mat)
                    game.batch.begin()

                    font.color = Color.WHITE
                    font.draw(game.batch, "Android Security Update", boxCenterX - 190f, boxCenterY + 95f)

                    font.color = Color.BLACK
                    font.draw(game.batch, "A critical update is required.", boxCenterX - 180f, boxCenterY + 40f)
                    font.draw(game.batch, "Installing...", boxCenterX - 180f, boxCenterY)

                    game.batch.end()
                    game.batch.projectionMatrix = gameViewport.camera.combined
                    game.batch.transformMatrix = oldTransform // Or just identity if we didn't use projection override. Actually standard is to reset transformMatrix to identity.
                    game.batch.begin()
                }
            }
        }
'''

content = content.replace(
    '// Draw Mask using Texture',
    batch_code + '\n        // Draw Mask using Texture'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
