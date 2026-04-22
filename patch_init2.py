import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

search = """                // Spinning Laser Cage around the Right Mask
                isCageActive = true
                cageAngle = 0f
                fakeMaskTouched = false
                chunkTime = 0f"""

replace = """                isCageActive = false // No more cage around right mask
                cageAngle = 0f
                fakeMaskTouched = false
                chunkTime = 0f"""

content = content.replace(search, replace)

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
