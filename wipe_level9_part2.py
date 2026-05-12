import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# 6. Remove death logic
death_pattern = r'''
        if \(currentLevel == 9\) \{
            screenFlashColor = Color\.GREEN
            screenFlashTimer = 0\.5f // Green flash instead of red
        \}'''
content = content.replace(death_pattern, '')

# 7. Remove reset logic
reset_pattern = r'''
            if \(currentLevel == 9\) \{
                // UI Windows reset is handled by setupLevel9 naturally resetting lists
            \}'''
content = content.replace(reset_pattern, '')

# 8. Remove bubble drawing
bubble_pattern = r'''
        if \(currentLevel == 9\) \{
            buttonFont\?\.let \{ font ->
                font\.color = Color\.GREEN
                for \(b in bubbles\) \{
                    val text = if \(b\.radius > 5f\) "1" else "0"
                    font\.draw\(game\.batch, text, b\.x \+ renderOffset, b\.y\)
                \}
            \}
        \}
'''
content = content.replace(bubble_pattern, '')

# Restore old bubble drawing
old_bubble = r'''// Moved bubble drawing to be handled by SpriteBatch or ShapeRenderer conditionally.
        // Actually, we can't draw text in ShapeRenderer.
        // Let's remove the ShapeRenderer circle drawing for Level 9 and do it in SpriteBatch.
        if \(currentLevel != 9\) \{
            for \(b in bubbles\) \{
                shapeRenderer\.circle\(b\.x \+ renderOffset, b\.y, b\.radius\)
            \}
        \}'''
content = re.sub(old_bubble, '''for (b in bubbles) {\n            shapeRenderer.circle(b.x + renderOffset, b.y, b.radius)\n        }''', content, flags=re.DOTALL)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
