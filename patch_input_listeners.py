import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Modify jump button to record flaps
jump_replace = """
        val jumpZone = Actor()
        jumpZone.setBounds(640f, 0f, 640f, 720f)
        jumpZone.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
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
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    flapsRemaining = 2
                    flapTimer = 0.5f // Start timer for the first auto-flap
                }
            }
        })
"""

content = re.sub(
    r'val jumpZone = Actor\(\).*?jumpZone\.addListener\(object : InputListener\(\) \{.*?\}\)\n',
    jump_replace,
    content,
    flags=re.MULTILINE | re.DOTALL
)

# Modify left/right buttons to set drift
left_right_replace = """
        val leftBtn = ImageButton(skin!!.get("left", ImageButton.ImageButtonStyle::class.java))
        leftBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isLeftPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) {
                isLeftPressed = false
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    if (isControlsInverted) {
                        driftDirection = 1
                    } else {
                        driftDirection = -1
                    }
                    driftTimer = 2.0f
                    isDrifting = true
                }
            }
        })
        val rightBtn = ImageButton(skin!!.get("right", ImageButton.ImageButtonStyle::class.java))
        rightBtn.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, p: Int, b: Int): Boolean { isRightPressed = true; return true }
            override fun touchUp(event: InputEvent?, x: Float, y: Float, p: Int, b: Int) {
                isRightPressed = false
                if (currentLevel == 6 && currentChunk == 1 && !isDead && !isLevelComplete) {
                    if (isControlsInverted) {
                        driftDirection = -1
                    } else {
                        driftDirection = 1
                    }
                    driftTimer = 2.0f
                    isDrifting = true
                }
            }
        })
"""

content = re.sub(
    r'val leftBtn = ImageButton\(skin!!\.get\("left", ImageButton\.ImageButtonStyle::class\.java\)\).*?touchUp.*?touchUp.*?\}\)\n',
    left_right_replace,
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
