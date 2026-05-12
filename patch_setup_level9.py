import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

setup_code = '''
    private fun setupLevel9(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Infinite Update
                platforms.clear()
                movingWalls.clear()
                sharks.clear()
                lasers.clear()
                gameButtons.clear()
                bubbles.clear()
                gravitySwitches.clear()
                worldTilt = 0f
                isControlsInverted = false
                canJump = true // We need jump for Flappy Bird over popups
                launchVelocityX = 0f

                Level9Chunk1State.reset()

                playerX = 100f
                playerY = 200f
                lastPlayerX = 100f
                lastPlayerY = 200f
                velocityY = 0f
                reverseGravity = false
                chunkTime = 0f

                // Cancel Platform
                platforms.add(Platform(Rectangle(50f, 180f, 150f, 20f), PlatformType.NORMAL, label = "[CANCEL]"))

                // Progress Bar Treadmill (y=400f, wait, the prompt says "Place a Progress Bar at y=400f".
                // The physics says "while they are touching the Progress Bar... run right to stay in place".
                // That means the progress bar is a platform they stand on. Let's make it a long platform at y=400f.)
                // But the player spawns at y=200f. Let's spawn them at y=420f so they land on it,
                // or just leave spawn at 200f and put the bar at 400f so they have to reach it?
                // "Spawn the player on a platform labeled [CANCEL] at (100f, 200f)."
                // "Place a Progress Bar at y=400f". This bar must "scroll".
                // I will add a progress bar treadmill platform.
                platforms.add(Platform(Rectangle(300f, 400f, 600f, 20f), PlatformType.NORMAL, label = "PROGRESS_BAR"))

                // Symmetrical Red Laser Walls (Warning bars) moving in at 35f
                lasers.add(Laser(Rectangle(-200f, 0f, 200f, 1500f), isSweeping = true, sweepSpeed = 35f, minX = -200f, maxX = 640f, movingRight = true))
                lasers.add(Laser(Rectangle(1280f, 0f, 200f, 1500f), isSweeping = true, sweepSpeed = 35f, minX = 640f, maxX = 1280f, movingRight = false))

                // The lethal Accept Button
                gameButtons.add(GameButton(Rectangle(1050f, 200f, 100f, 40f)))

                // Fake Mask inside ACCEPT button
                maskX = 1050f + 50f - 15f
                maskY = 200f + 20f - 15f
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
