import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

chunk2_setup = """            2 -> {
                // Chunk 2: The Mirror Maze
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()
                playerPath.clear()

                playerX = 100f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false
                chunkTime = 0f

                mirrorActive = true
                isControlsInverted = false
                mirrorX = 1280f - playerWidth - playerX
                mirrorY = playerY

                maskX = -2000f
                maskY = 800f

                // Central Laser Wall (prevents crossing early)
                lasers.add(Laser(com.badlogic.gdx.math.Rectangle(635f, 0f, 10f, 500f)))

                // Step 1: The Squeeze (Crumbling Platforms up both sides)
                // Left Side (Player)
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(100f, 80f, 80f, 20f), PlatformType.NORMAL)) // Start
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(200f, 200f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(100f, 320f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(250f, 440f, 80f, 20f), PlatformType.CRUMBLING))

                // Right Side (Mirror)
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(1280f - 180f, 80f, 80f, 20f), PlatformType.NORMAL)) // Mirror Start
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(1280f - 280f, 200f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(1280f - 180f, 320f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(1280f - 330f, 440f, 80f, 20f), PlatformType.CRUMBLING))

                // The Walls: Symmetrical Red Laser Walls moving inward at 10f
                movingWalls.add(MovingWall(com.badlogic.gdx.math.Rectangle(-200f, 0f, 200f, 1500f), speed = 10f, isActive = true))
                movingWalls.add(MovingWall(com.badlogic.gdx.math.Rectangle(1280f, 0f, 200f, 1500f), speed = -10f, isActive = true))

                // Step 2: The Inversion Button (Halfway, x=300, y=550)
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(280f, 550f, 60f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(1280f - 340f, 550f, 60f, 20f), PlatformType.NORMAL))

                val inversionBtn = GameButton(com.badlogic.gdx.math.Rectangle(290f, 570f, 40f, 40f), false) {
                    isControlsInverted = true
                    // Spawn mask at center for Step 3
                    maskX = 640f - 16f
                    maskY = 550f
                }
                gameButtons.add(inversionBtn)

                // Step 3: The Safe Sharks (Act as moving platforms)
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(350f, 530f, 120f, 60f), PlatformType.SAFE_SHARK))
                platforms.add(Platform(com.badlogic.gdx.math.Rectangle(810f, 530f, 120f, 60f), PlatformType.SAFE_SHARK))
            }"""

target = "                maskX = 640f\n                maskY = 850f\n            }"

content = content.replace(target, target + "\n" + chunk2_setup)

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
