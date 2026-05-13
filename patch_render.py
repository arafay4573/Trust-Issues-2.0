import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Add blackout at start of shapeRenderer
blackout_code = '''
        if (currentLevel == 9 && currentChunk == 1 && Level9Chunk1State.phase == 1) {
            shapeRenderer.color = Color.BLACK
            shapeRenderer.rect(0f + renderOffset, 0f, 1280f, 720f)
        }
'''

content = content.replace(
    '''        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw Catch Platforms (Slightly Transparent/Darker)''',
    '''        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

''' + blackout_code + '''
        // Draw Catch Platforms (Slightly Transparent/Darker)'''
)

# Change spawn platform color
platform_draw_code = '''
            // Normal Platform Color
            var r = 0.5f; var g = 1f; var b = 0.5f; var a = 1f
            if (currentLevel == 3 && currentChunk == 3) {
                if (isLightsOn) { r = 0.8f; g = 0.8f; b = 0.8f; a = 1f }
                else { r = 0.2f; g = 0.2f; b = 0.2f; a = 0.5f }
            }
            if (currentLevel == 9 && currentChunk == 1) {
                if (Level9Chunk1State.phase == 1) {
                    r = 0.1f; g = 0.4f; b = 0.8f // Blue after blackout
                }
            }
'''

content = content.replace(
    '''            // Normal Platform Color
            var r = 0.5f; var g = 1f; var b = 0.5f; var a = 1f
            if (currentLevel == 3 && currentChunk == 3) {
                if (isLightsOn) { r = 0.8f; g = 0.8f; b = 0.8f; a = 1f }
                else { r = 0.2f; g = 0.2f; b = 0.2f; a = 0.5f }
            }''',
    platform_draw_code
)

# Draw the box
box_shape_code = '''
        if (currentLevel == 9 && currentChunk == 1 && Level9Chunk1State.phase == 1) {
            val boxCenterX = 640f + renderOffset
            val boxCenterY = 360f
            val boxWidth = 600f
            val boxHeight = 300f

            val shapeOldTransform = shapeRenderer.transformMatrix.cpy()
            shapeRenderer.translate(boxCenterX, boxCenterY, 0f)
            shapeRenderer.rotate(0f, 0f, 1f, Level9Chunk1State.boxAngle)
            shapeRenderer.translate(-boxCenterX, -boxCenterY, 0f)

            // Box Body
            shapeRenderer.color = Color(0.85f, 0.85f, 0.85f, 1f) // Classic Windows Grey
            shapeRenderer.rect(boxCenterX - boxWidth/2f, boxCenterY - boxHeight/2f, boxWidth, boxHeight)

            // Box Inner Border (Dark Grey)
            shapeRenderer.color = Color.DARK_GRAY
            shapeRenderer.rectLine(boxCenterX - boxWidth/2f, boxCenterY - boxHeight/2f, boxCenterX + boxWidth/2f, boxCenterY - boxHeight/2f, 2f)
            shapeRenderer.rectLine(boxCenterX + boxWidth/2f, boxCenterY - boxHeight/2f, boxCenterX + boxWidth/2f, boxCenterY + boxHeight/2f, 2f)
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rectLine(boxCenterX - boxWidth/2f, boxCenterY - boxHeight/2f, boxCenterX - boxWidth/2f, boxCenterY + boxHeight/2f, 2f)
            shapeRenderer.rectLine(boxCenterX - boxWidth/2f, boxCenterY + boxHeight/2f, boxCenterX + boxWidth/2f, boxCenterY + boxHeight/2f, 2f)

            // Blue Title Bar
            val titleBarHeight = 35f
            shapeRenderer.color = Color(0.0f, 0.3f, 0.8f, 1f) // Classic XP Blue
            shapeRenderer.rect(boxCenterX - boxWidth/2f + 2f, boxCenterY + boxHeight/2f - titleBarHeight, boxWidth - 4f, titleBarHeight - 2f)

            // Red Close Button
            val closeBtnSize = 25f
            val closeBtnX = boxCenterX + boxWidth/2f - closeBtnSize - 5f
            val closeBtnY = boxCenterY + boxHeight/2f - closeBtnSize - 5f
            shapeRenderer.color = Color(0.9f, 0.2f, 0.1f, 1f)
            shapeRenderer.rect(closeBtnX, closeBtnY, closeBtnSize, closeBtnSize)

            // White X in close button
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rectLine(closeBtnX + 5f, closeBtnY + 5f, closeBtnX + closeBtnSize - 5f, closeBtnY + closeBtnSize - 5f, 3f)
            shapeRenderer.rectLine(closeBtnX + 5f, closeBtnY + closeBtnSize - 5f, closeBtnX + closeBtnSize - 5f, closeBtnY + 5f, 3f)

            // Visual Empty Progress Bar Inside Box
            val progBarWidth = 500f
            val progBarHeight = 25f
            val progBarX = boxCenterX - progBarWidth/2f
            val progBarY = boxCenterY - 80f

            // Progress Bar Outer Bevel (Dark Grey)
            shapeRenderer.color = Color(0.6f, 0.6f, 0.6f, 1f)
            shapeRenderer.rect(progBarX, progBarY, progBarWidth, progBarHeight)
            // Progress Bar Inner Empty (White)
            shapeRenderer.color = Color.WHITE
            shapeRenderer.rect(progBarX + 2f, progBarY + 2f, progBarWidth - 4f, progBarHeight - 4f)

            shapeRenderer.transformMatrix = shapeOldTransform
        }
'''

content = content.replace(
    '// Draw The Shrinking Void Overlay (Level 7 Chunk 3)',
    box_shape_code + '\n        // Draw The Shrinking Void Overlay (Level 7 Chunk 3)'
)

# Text rendering in SpriteBatch
batch_code = '''
        if (currentLevel == 9 && currentChunk == 1) {
            if (Level9Chunk1State.phase == 1) {
                buttonFont?.let { font ->
                    val boxCenterX = 640f + renderOffset
                    val boxCenterY = 360f
                    val boxWidth = 600f
                    val boxHeight = 300f

                    val oldTransform = game.batch.transformMatrix.cpy()
                    val mat = com.badlogic.gdx.math.Matrix4()
                    mat.setToTranslation(boxCenterX, boxCenterY, 0f)
                    mat.rotate(0f, 0f, 1f, Level9Chunk1State.boxAngle)
                    mat.translate(-boxCenterX, -boxCenterY, 0f)

                    game.batch.transformMatrix = game.batch.projectionMatrix.cpy().mul(mat)
                    game.batch.begin()

                    // Save original scale
                    val origScaleX = font.data.scaleX
                    val origScaleY = font.data.scaleY

                    // Title Bar Text (Small and Crisp)
                    font.data.setScale(origScaleX * 0.8f, origScaleY * 0.8f)
                    font.color = Color.WHITE
                    // Align left on title bar
                    font.draw(game.batch, "Android Security Update", boxCenterX - boxWidth/2f + 10f, boxCenterY + boxHeight/2f - 8f)

                    // Body Text
                    font.color = Color.BLACK
                    font.draw(game.batch, "A critical update is required to continue.", boxCenterX - boxWidth/2f + 20f, boxCenterY + 80f)
                    font.draw(game.batch, "Your system might restart multiple times.", boxCenterX - boxWidth/2f + 20f, boxCenterY + 40f)

                    // Progress Text (Above progress bar)
                    font.data.setScale(origScaleX * 0.7f, origScaleY * 0.7f)
                    font.draw(game.batch, "Installing Update... 0%", boxCenterX - boxWidth/2f + 20f, boxCenterY - 45f)

                    // Time remaining text
                    font.draw(game.batch, "Estimated time remaining: Calculating...", boxCenterX - boxWidth/2f + 20f, boxCenterY - 95f)

                    // Restore original scale
                    font.data.setScale(origScaleX, origScaleY)

                    game.batch.end()
                    game.batch.transformMatrix = oldTransform
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
