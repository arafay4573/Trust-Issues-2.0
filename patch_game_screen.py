import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove SAFE_SHARK platforms in setupLevel7 Chunk 2
old_setup = """                // Swinging Safe Sharks (3 sharks)
                // Left Shark
                sharks.add(Shark(300f, 350f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(300f, 350f, 120f, 60f), PlatformType.SAFE_SHARK))
                // Middle Shark
                sharks.add(Shark(580f, 450f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(580f, 450f, 120f, 60f), PlatformType.SAFE_SHARK))
                // Right Shark
                sharks.add(Shark(860f, 600f, 0f, 0f, 1280f))
                platforms.add(Platform(Rectangle(860f, 600f, 120f, 60f), PlatformType.SAFE_SHARK))"""

new_setup = """                // Swinging Sharks (3 sharks)
                // Left Shark
                sharks.add(Shark(300f, 350f, 0f, 0f, 1280f))
                // Middle Shark
                sharks.add(Shark(580f, 450f, 0f, 0f, 1280f))
                // Right Shark
                sharks.add(Shark(860f, 600f, 0f, 0f, 1280f))"""
content = content.replace(old_setup, new_setup)

# 2. Update buttons in setupLevel7 Chunk 2
old_buttons = """                // Yellow lines (GameButtons) above the sharks (width 120f, height 250f to guarantee collision)
                gameButtons.add(GameButton(Rectangle(340f, 410f, 120f, 250f))) // Above Left Shark
                gameButtons.add(GameButton(Rectangle(620f, 510f, 120f, 250f))) // Above Middle Shark
                gameButtons.add(GameButton(Rectangle(900f, 660f, 120f, 250f))) // Above Right Shark"""

new_buttons = """                // Yellow lines (GameButtons) above the sharks
                gameButtons.add(GameButton(Rectangle(340f, 410f, 120f, 30f)) {
                    if (lasers.size > 0) lasers[0].rect.x = -5000f
                }) // Above Left Shark

                gameButtons.add(GameButton(Rectangle(620f, 510f, 120f, 30f)) {
                    if (lasers.size > 2) lasers[2].rect.x = -5000f
                    isMaskFreefalling = true
                }) // Above Middle Shark

                gameButtons.add(GameButton(Rectangle(900f, 660f, 120f, 30f)) {
                    if (lasers.size > 1) lasers[1].rect.x = -5000f
                }) // Above Right Shark"""
content = content.replace(old_buttons, new_buttons)

# 3. Update sharks platforms sync in update loop
old_sync = """            // Sync SAFE_SHARK platforms & GameButtons (yellow lines)
            if (platforms.size >= 4) {
                platforms[1].rect.x = shark1X
                platforms[1].rect.y = shark1Y
                platforms[2].rect.x = shark2X
                platforms[2].rect.y = shark2Y
                platforms[3].rect.x = shark3X
                platforms[3].rect.y = shark3Y
            }
            if (gameButtons.size >= 3) {
                // Ensure buttons have a massive vertical and horizontal hitbox to guarantee they trigger
                // Covers the full width of the shark (120f) and extends extremely high (250f)
                gameButtons[0].rect.set(shark1X, shark1Y + 60f, 120f, 250f)
                gameButtons[1].rect.set(shark2X, shark2Y + 60f, 120f, 250f)
                gameButtons[2].rect.set(shark3X, shark3Y + 60f, 120f, 250f)
            }"""

new_sync = """            if (gameButtons.size >= 3) {
                gameButtons[0].rect.set(shark1X, shark1Y + 90f, 120f, 30f)
                gameButtons[1].rect.set(shark2X, shark2Y + 90f, 120f, 30f)
                gameButtons[2].rect.set(shark3X, shark3Y + 90f, 120f, 30f)
            }"""
content = content.replace(old_sync, new_sync)

# 4. Remove laser logic in update loop
old_laser_logic = """            // Buttons & Laser Cage Logic
            if (gameButtons.size >= 3) {
                // Left Shark Button -> Left Laser
                if (!gameButtons[0].isPressed && Intersector.overlaps(playerRect, gameButtons[0].rect)) {
                    gameButtons[0].isPressed = true
                    if (lasers.size > 0) lasers[0].rect.x = -5000f
                }
                // Middle Shark Button -> Bottom Laser (Frees mask)
                if (!gameButtons[1].isPressed && Intersector.overlaps(playerRect, gameButtons[1].rect)) {
                    gameButtons[1].isPressed = true
                    if (lasers.size > 2) lasers[2].rect.x = -5000f
                    isMaskFreefalling = true // Mask starts falling
                }
                // Right Shark Button -> Right Laser
                if (!gameButtons[2].isPressed && Intersector.overlaps(playerRect, gameButtons[2].rect)) {
                    gameButtons[2].isPressed = true
                    if (lasers.size > 1) lasers[1].rect.x = -5000f
                }
            }"""
content = content.replace(old_laser_logic, "")

# 5. Fix isSafeSharkLevel5
old_isSafeShark = "val isSafeSharkLevel5 = (currentLevel == 5 && currentChunk == 2) || (currentLevel == 7)"
new_isSafeShark = "val isSafeSharkLevel5 = (currentLevel == 5 && currentChunk == 2) || (currentLevel == 7 && currentChunk == 1)"
content = content.replace(old_isSafeShark, new_isSafeShark)

# 6. Fix Draw logic
old_draw = "shapeRenderer.rect(btn.rect.x + 40f + renderOffset, btn.rect.y + 30f, 40f, 10f)"
new_draw = "shapeRenderer.rect(btn.rect.x + 40f + renderOffset, btn.rect.y + 10f, 40f, 10f)"
content = content.replace(old_draw, new_draw)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)

print("Patch applied successfully.")
