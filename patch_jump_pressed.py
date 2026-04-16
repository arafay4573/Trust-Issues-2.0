import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Add isJumpPressed var
content = re.sub(
    r'(private var isRightPressed = false)',
    r'\1\n    private var isJumpPressed = false',
    content
)

# Update jump listener
jump_replace = """
        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                isJumpPressed = true
                if (!isPaused && !isDead && !isLevelComplete) {
                    val currentJumpStrength = if (currentLevel == 5 && (currentChunk == 2 || currentChunk == 3)) 500f else jumpStrength
                    // "tap the jump button again and again to fly ofk like flappy bird"
                    if ((currentLevel == 5 && (currentChunk == 1 || currentChunk == 2 || currentChunk == 3)) || (currentLevel == 6 && currentChunk == 1)) {
                        if (reverseGravity) {
                            velocityY = -currentJumpStrength
                        } else {
                            velocityY = currentJumpStrength
                        }
                    } else {
                        if (reverseGravity) {
                            if (canJump) velocityY = -currentJumpStrength
                        } else {
                            if (canJump) velocityY = currentJumpStrength
                        }
                    }
                }
                return true
            }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                isJumpPressed = false
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    flapsRemaining = 2
                    flapTimer = 0.5f // Start timer for the first auto-flap
                }
            }
        })
"""

content = re.sub(
    r'        val jumpZone = Actor\(\).*?flapTimer = 0\.5f // Start timer for the first auto-flap\n                \}\n            \}\n        \}\)',
    jump_replace,
    content,
    flags=re.MULTILINE | re.DOTALL
)

# update death logic to use isJumpPressed
content = content.replace(
    'val activelyHolding = isLeftPressed || isRightPressed',
    'val activelyHolding = isLeftPressed || isRightPressed || isJumpPressed'
)


with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
