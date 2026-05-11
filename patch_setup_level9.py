import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

setup_code = '''
    private fun setupLevel9(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Glitch in the Deep
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

                playerX = 100f
                playerY = 150f
                lastPlayerX = 100f
                lastPlayerY = 150f
                velocityY = 0f
                reverseGravity = false
                chunkTime = 0f

                // Loading Platform
                platforms.add(Platform(Rectangle(40f, 130f, 150f, 20f), PlatformType.NORMAL, label = "[LOADING_LEVEL_9...]"))

                // Symmetrical Red Walls (Flickering Text)
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 40f, isActive = true, label = "DELETE_SYSTEM_FILE_32..."))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -40f, isActive = true, label = "DELETE_SYSTEM_FILE_32..."))

                // Fake Mask
                maskX = -2000f
                maskY = -2000f
            }
        }
    }
'''

content = content.replace(
    'private fun setupLevel8(chunk: Int) {',
    setup_code + '\n    private fun setupLevel8(chunk: Int) {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
