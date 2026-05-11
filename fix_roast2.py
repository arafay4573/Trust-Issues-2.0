import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Check where the roast is actually located now
print("Roast block level 9:\n")
lines = content.split('\n')
for i, line in enumerate(lines):
    if 'currentLevel == 9' in line and 'roast = listOf' in lines[i+1]:
        print(f"Line {i}: {line}")
        print(f"Line {i+1}: {lines[i+1]}")
