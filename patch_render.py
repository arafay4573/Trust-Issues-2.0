import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

# Update Player rendering color
render_player = r"""        // Draw Player \(Green\)
        shapeRenderer\.color = Color\.GREEN
        if \(isDead\) shapeRenderer\.color = Color\.GRAY
        val centerX = playerX \+ 12\.5f \+ renderOffset"""

new_render_player = """        // Draw Player (Green, or Red if swapped in Level 6-2)
        if (currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity) {
            shapeRenderer.color = Color.RED
        } else {
            shapeRenderer.color = Color.GREEN
        }
        if (isDead) shapeRenderer.color = Color.GRAY
        val centerX = playerX + 12.5f + renderOffset"""

content = re.sub(render_player, new_render_player, content)

# Update Mirror rendering condition
render_mirror = r"""        // Draw Mirror Player \(Deadly Red\) for Level 5 Chunk 2
        if \(currentLevel == 5 && currentChunk == 2 && mirrorActive\) \{"""

new_render_mirror = """        // Draw Mirror Player / Ghost
        if ((currentLevel == 5 && currentChunk == 2 && mirrorActive) || (currentLevel == 6 && currentChunk == 2)) {"""

content = re.sub(render_mirror, new_render_mirror, content)


with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
