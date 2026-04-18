import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

bad_render = r"""        // Draw Player \(Procedural Shapes\)
        if \(currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity\) \{
            shapeRenderer\.color = Color\.RED
        \} else \{
            shapeRenderer\.color = if \(horrorMode\) Color\.GRAY else Color\.GREEN
        \}
        val centerX = playerX \+ 12\.5f \+ renderOffset"""

good_render = """        // Draw Player (Procedural Shapes)
        if (currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity) {
            shapeRenderer.color = Color.RED
        } else {
            shapeRenderer.color = if (horrorMode) Color.GRAY else Color.GREEN
        }
        if (isDead) shapeRenderer.color = Color.GRAY
        val centerX = playerX + 12.5f + renderOffset"""

content = re.sub(bad_render, good_render, content)

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
