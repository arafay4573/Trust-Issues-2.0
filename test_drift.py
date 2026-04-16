import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Check flaps update logic
if "velocityY = currentJumpStrength" in content:
    print("Flaps logic exists")
