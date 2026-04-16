import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Make moving wall handle level 6 chunk 1 death
wall_logic = """
        // Moving Walls
        for (wall in movingWalls) {
            if (wall.isActive) {
                wall.rect.x += wall.speed * delta
                // Check if wall crushes player
                if (Intersector.overlaps(playerRect, wall.rect)) {
                    if (currentLevel == 5 && currentChunk == 2) die("You ain't no Newton")
                    else if (currentLevel == 6 && currentChunk == 1) die("Stop fighting the drift. Trust the void.")
                    else die("Squished like a bug. And just as insignificant.")
                }
            }
        }
"""
content = re.sub(
    r'        // Moving Walls\n        for \(wall in movingWalls\) \{\n            if \(wall\.isActive\) \{\n                wall\.rect\.x \+= wall\.speed \* delta\n                // Check if wall crushes player\n                if \(Intersector\.overlaps\(playerRect, wall\.rect\)\) \{\n                    if \(currentLevel == 5 && currentChunk == 2\) die\("You ain\'t no Newton"\)\n                    else die\("Squished like a bug\. And just as insignificant\."\)\n                \}\n            \}\n        \}',
    wall_logic,
    content
)


# Mirror death handling in level 6
content = content.replace(
    'if (currentLevel == 5 && currentChunk == 2) {',
    'if ((currentLevel == 5 && currentChunk == 2) || (currentLevel == 6 && currentChunk == 1)) {'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
