import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

bubble_code = '''
            if (currentLevel == 9) {
                // Draw binary 0s and 1s instead of circles
                buttonFont?.let { font ->
                    font.color = Color.GREEN
                    for (b in bubbles) {
                        val text = if (b.radius > 5f) "1" else "0"
                        font.draw(game.batch, text, b.x + renderOffset, b.y)
                    }
                }
            } else {
                for (b in bubbles) {
                    shapeRenderer.circle(b.x + renderOffset, b.y, b.radius)
                }
            }
'''

# Find the loop that draws bubbles in ShapeRenderer
content = content.replace(
    '''for (b in bubbles) {
            shapeRenderer.circle(b.x + renderOffset, b.y, b.radius)
        }''',
    '''// Moved bubble drawing to be handled by SpriteBatch or ShapeRenderer conditionally.
        // Actually, we can't draw text in ShapeRenderer.
        // Let's remove the ShapeRenderer circle drawing for Level 9 and do it in SpriteBatch.
        if (currentLevel != 9) {
            for (b in bubbles) {
                shapeRenderer.circle(b.x + renderOffset, b.y, b.radius)
            }
        }'''
)

# Add to sprite batch
batch_bubble_code = '''
        if (currentLevel == 9) {
            buttonFont?.let { font ->
                font.color = Color.GREEN
                for (b in bubbles) {
                    val text = if (b.radius > 5f) "1" else "0"
                    font.draw(game.batch, text, b.x + renderOffset, b.y)
                }
            }
        }
'''

content = content.replace(
    '// Draw Mask using Texture',
    batch_bubble_code + '\n        // Draw Mask using Texture'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
