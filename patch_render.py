import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Add CRT scan lines and text platforms logic to drawing
draw_code = '''
        // Text Platforms (SpriteBatch needs to be open)
        if (currentLevel == 9) {
            buttonFont?.let { font ->
                font.color = Color.GREEN
                for (plat in platforms) {
                    if (plat.label != null && plat.state != PlatformState.DESTROYED) {
                        font.draw(game.batch, plat.label, plat.rect.x + renderOffset, plat.rect.y + plat.rect.height)
                    }
                }
                for (wall in movingWalls) {
                    if (wall.label != null && wall.isActive) {
                        // Draw vertically or just repeat? Let's just repeat it vertically.
                        for (i in 0..30) {
                            font.draw(game.batch, wall.label, wall.rect.x + renderOffset, i * 50f)
                        }
                    }
                }

                // Console Bar
                if (Level9Chunk1State.consoleText.isNotEmpty()) {
                    // It says semi-transparent black bar, but we are in SpriteBatch here.
                    // We can just draw text for now, we'll do the black bar in ShapeRenderer if needed.
                    font.color = Color.GREEN
                    font.draw(game.batch, Level9Chunk1State.consoleText, 300f + renderOffset, 700f)
                }
            }
        }
'''

content = content.replace(
    '// Level 8 Chunk 1 custom text (if any)',
    draw_code + '\n        // Level 8 Chunk 1 custom text (if any)'
)

shape_code = '''
        if (currentLevel == 9 && currentChunk == 1) {
            // Semi-transparent console background
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.7f)
            shapeRenderer.rect(0f + renderOffset, 660f, 1280f, 60f)

            // CRT Lines
            shapeRenderer.color = Color(0f, 1f, 0f, 0.1f)
            for (i in 0..720 step 4) {
                shapeRenderer.rectLine(0f + renderOffset, i.toFloat(), 1280f + renderOffset, i.toFloat(), 1f)
            }
            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
        }
'''

content = content.replace(
    '// Draw The Shrinking Void Overlay (Level 7 Chunk 3)',
    shape_code + '\n        // Draw The Shrinking Void Overlay (Level 7 Chunk 3)'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
