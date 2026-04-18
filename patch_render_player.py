import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

render_player = r"""        // Draw Player \(Procedural Shapes\)
        shapeRenderer\.color = if \(horrorMode\) Color\.GRAY else Color\.BLACK"""

new_render_player = """        // Draw Player (Procedural Shapes)
        if (currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity) {
            shapeRenderer.color = Color.RED
        } else {
            shapeRenderer.color = if (horrorMode) Color.GRAY else Color.GREEN
        }"""

content = re.sub(render_player, new_render_player, content)

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
