import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

shape_code = '''
        if (currentLevel == 9 && currentChunk == 1) {
            // Draw large gray UI window
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA)

            // Blurred background effect (dark overlay)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.5f)
            shapeRenderer.rect(0f + renderOffset, 0f, 1280f, 720f)

            // Main OS Window
            shapeRenderer.color = Color(0.8f, 0.8f, 0.8f, 1f) // Light gray
            shapeRenderer.rect(240f + renderOffset, 160f, 800f, 400f)

            // Window Title Bar
            shapeRenderer.color = Color(0.6f, 0.6f, 0.6f, 1f)
            shapeRenderer.rect(240f + renderOffset, 520f, 800f, 40f)

            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND)

            // Draw fake "X" if spawned
            if (Level9Chunk1State.isXSpawned) {
                shapeRenderer.color = if (MathUtils.randomBoolean(0.8f)) Color.RED else Color.DARK_GRAY
                shapeRenderer.rectLine(1100f + renderOffset, 650f, 1130f + renderOffset, 680f, 4f)
                shapeRenderer.rectLine(1100f + renderOffset, 680f, 1130f + renderOffset, 650f, 4f)
            }
        }
'''

content = content.replace(
    '// Draw The Shrinking Void Overlay (Level 7 Chunk 3)',
    shape_code + '\n        // Draw The Shrinking Void Overlay (Level 7 Chunk 3)'
)

draw_code = '''
        // Text Overlays for Level 9 Chunk 1
        if (currentLevel == 9 && currentChunk == 1) {
            buttonFont?.let { font ->
                font.color = Color.BLACK
                font.draw(game.batch, "SYSTEM UPDATE REQUIRES YOUR ATTENTION", 400f + renderOffset, 550f)

                // Draw platform labels
                for (plat in platforms) {
                    if (plat.label != null && plat.state != PlatformState.DESTROYED) {
                        if (plat.label == "PROGRESS_BAR") {
                            // Draw scrolling effect
                            font.draw(game.batch, "INSTALLING... " + (Level9Chunk1State.updateProgress * 100).toInt() + "%", 500f + renderOffset, 390f)
                        } else {
                            font.draw(game.batch, plat.label, plat.rect.x + renderOffset + 10f, plat.rect.y + plat.rect.height - 10f)
                        }
                    }
                }

                // Draw Accept Button Label
                font.color = Color.WHITE
                font.draw(game.batch, "[ACCEPT]", 1055f + renderOffset, 225f)
            }
        }
'''

content = content.replace(
    '// Draw Mask using Texture',
    draw_code + '\n        // Draw Mask using Texture'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
