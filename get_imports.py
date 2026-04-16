import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    lines = f.readlines()

for line in lines:
    if line.startswith("import "):
        print(line, end="")
