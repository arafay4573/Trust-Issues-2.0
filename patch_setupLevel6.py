import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Add else if (currentLevel == 6) setupLevel6(chunk)
content = re.sub(
    r'(} else if \(currentLevel == 5\) \{\n\s*setupLevel5\(chunk\)\n\s*\})',
    r'\1 else if (currentLevel == 6) {\n            setupLevel6(chunk)\n        }',
    content
)

setup_lvl6 = """
    private fun setupLevel6(chunk: Int) {
        when (chunk) {
            1 -> {
                // Chunk 1: The Sticky Drift & Hardware Betrayal
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 640f
                playerY = 100f
                velocityY = 0f
                reverseGravity = false
                mirrorActive = false

                // Spawn Point: bottom-center
                platforms.add(Platform(Rectangle(600f, 80f, 80f, 20f), PlatformType.NORMAL))

                // Phase 1: The Crumbling Staircase (y=200f to y=500f, zig-zag)
                platforms.add(Platform(Rectangle(540f, 200f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(660f, 275f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(540f, 350f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(660f, 425f, 80f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(540f, 500f, 80f, 20f), PlatformType.CRUMBLING))

                // Set crumbling duration to 1.2s for these platforms in update loop or via state management

                // Phase 2: The Mirror & Squeeze
                // "At y=550f, split the screen visually and spawn the MirrorPlayer" -> we trigger mirror when y>=550, or spawn mirror offscreen?
                // Actually the prompt says "At y=550f, split the screen visually and spawn the MirrorPlayer"
                // It's probably easier to just enable mirrorActive and let update handle the y threshold, or set the initial mirror pos.
                // Or maybe just enable it at the start but let the player know?
                // The requirements say "At y=550f, split the screen visually and spawn the MirrorPlayer" - this means it happens during the ascent.

                // Moving walls (Symmetrical Red Walls closing in at 25f)
                movingWalls.add(MovingWall(Rectangle(-200f, 550f, 200f, 1500f), speed = 25f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 550f, 200f, 1500f), speed = -25f, isActive = true))

                // Phase 3: The Shark Ferries (y=700f)
                // Two Safe Sharks patrolling horizontally.
                sharks.add(Shark(400f, 700f, 100f, 200f, 600f))
                sharks.add(Shark(880f, 700f, -100f, 680f, 1080f))
                // Add unique SAFE_SHARK platforms for these ferries
                platforms.add(Platform(Rectangle(400f, 700f, 119.9f, 20f), PlatformType.SAFE_SHARK))
                platforms.add(Platform(Rectangle(880f, 700f, 120.1f, 20f), PlatformType.SAFE_SHARK))

                // 3. THE FINISH: THE CAGE OF TRUST
                maskX = 640f
                maskY = 850f

                // Laser Cage: Surround Mask with 4 thin lasers
                lasers.add(Laser(Rectangle(620f, 840f, 2f, 50f), isSweeping = false)) // Left
                lasers.add(Laser(Rectangle(678f, 840f, 2f, 50f), isSweeping = false)) // Right
                lasers.add(Laser(Rectangle(620f, 840f, 60f, 2f), isSweeping = false)) // Bottom
                lasers.add(Laser(Rectangle(620f, 890f, 60f, 2f), isSweeping = false)) // Top
            }
        }
    }
"""

content = re.sub(
    r'(private fun setupLevel5.*?^    })',
    r'\1\n\n' + setup_lvl6,
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
