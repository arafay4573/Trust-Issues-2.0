import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

setup_code = '''
    private fun setupLevel9(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: Android Security Update
                platforms.clear()
                movingWalls.clear()
                sharks.clear()
                lasers.clear()
                gameButtons.clear()
                bubbles.clear()
                gravitySwitches.clear()
                worldTilt = 0f
                isControlsInverted = false
                canJump = true
                launchVelocityX = 0f

                Level9Chunk1State.reset()

                playerX = 50f
                playerY = 280f // 280f is standard floor level, spawn platform at 260f
                lastPlayerX = 50f
                lastPlayerY = 280f
                velocityY = 0f
                reverseGravity = false
                chunkTime = 0f

                // Initial Spawn Platform (Green)
                platforms.add(Platform(Rectangle(0f, 260f, 200f, 20f), PlatformType.NORMAL))

                // Dummy obstacles for the first 2 seconds
                platforms.add(Platform(Rectangle(300f, 260f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(500f, 320f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(700f, 380f, 100f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(900f, 320f, 100f, 20f), PlatformType.NORMAL))

                // Mask at far right
                maskX = 1150f
                maskY = 300f
            }
        }
    }
'''

content = content.replace(
    'private fun setupLevel8(chunk: Int) {',
    setup_code + '\n    private fun setupLevel8(chunk: Int) {'
)

content = content.replace(
    '8 -> setupLevel8(chunk)',
    '8 -> setupLevel8(chunk)\n            9 -> setupLevel9(chunk)'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
