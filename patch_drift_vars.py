import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

drift_vars = """
    // Level 6 Mechanics
    private var driftTimer = 0f
    private var driftDirection = 0 // -1 for left, 1 for right
    private var isDrifting = false
    private var flapsRemaining = 0
    private var flapTimer = 0f
"""

content = re.sub(
    r'(    // Game State)',
    drift_vars + r'\n\1',
    content
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
