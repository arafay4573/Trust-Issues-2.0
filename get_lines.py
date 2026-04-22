import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

print(content.find('playerX = 640f'))
