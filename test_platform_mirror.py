import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Check mirror platform death logic
print(content.count('Mirror Platform Collision'))
