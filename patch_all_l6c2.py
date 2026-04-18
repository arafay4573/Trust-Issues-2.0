import re

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "r") as f:
    content = f.read()

# 1. State vars
state_vars = """    // Level 6 Mechanics
    private var hasSwappedIdentity = false
    private var ghostX = -999f
    private var ghostY = -999f"""
content = re.sub(r"    // Level 6 Mechanics", state_vars, content)

# 2. Reset vars
reset_block = """        isControlsInverted = false
        mirrorActive = false
        hasSwappedIdentity = false
        ghostX = -999f
        ghostY = -999f

        renderOffset = 0f"""
content = re.sub(r"        isControlsInverted = false\n        mirrorActive = false\n\n        renderOffset = 0f", reset_block, content)

# 3. Setup Chunk Geometry
setup_level_6_block = r"""        when \(chunk\) \{
            1 -> \{
                // Chunk 1: The Infinite Loop Portal"""

new_setup_level_6_block = """        when (chunk) {
            2 -> {
                // Chunk 2: The Mirror Swap Portal
                platforms.clear()
                lasers.clear()
                movingWalls.clear()
                gameButtons.clear()
                gravitySwitches.clear()
                sharks.clear()

                playerX = 100f
                playerY = 280f
                velocityY = 0f
                reverseGravity = false
                hasSwappedIdentity = false
                mirrorActive = true // Start with mirror logic active

                // First Mask (Trigger)
                maskX = 300f
                maskY = 400f

                // Safe platforms to start/jump
                platforms.add(Platform(Rectangle(50f, 280f, 150f, 20f), PlatformType.NORMAL))
                platforms.add(Platform(Rectangle(1080f, 280f, 150f, 20f), PlatformType.NORMAL))

                // The Squeeze: Symmetrical Red Walls close in from the edges at 30f speed.
                movingWalls.add(MovingWall(Rectangle(-200f, 0f, 200f, 1500f), speed = 30f, isActive = true))
                movingWalls.add(MovingWall(Rectangle(1280f, 0f, 200f, 1500f), speed = -30f, isActive = true))

                // The Platforms: Crumbling Platforms (y=500f, 600f, 700f)
                platforms.add(Platform(Rectangle(300f, 380f, 100f, 20f), PlatformType.NORMAL)) // Extra for mask
                platforms.add(Platform(Rectangle(900f, 500f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(1050f, 600f, 100f, 20f), PlatformType.CRUMBLING))
                platforms.add(Platform(Rectangle(900f, 700f, 100f, 20f), PlatformType.CRUMBLING))
                // Platform near Goal Mask
                platforms.add(Platform(Rectangle(950f, 780f, 100f, 20f), PlatformType.CRUMBLING))
            }
            1 -> {
                // Chunk 1: The Infinite Loop Portal"""

content = re.sub(setup_level_6_block, new_setup_level_6_block, content)

# 4. Update Logic
update_hook = r"""        // --- FORCED CHUNK 2 LOGIC \(BRUTE FORCE\) ---
        if \(currentLevel == 4 && currentChunk == 2\) \{"""

l6c2_logic = """        // --- LEVEL 6 CHUNK 2 LOGIC (The Mirror Swap Portal) ---
        if (currentLevel == 6 && currentChunk == 2 && !isDead && !isLevelComplete) {
            if (!hasSwappedIdentity) {
                // Standard mirror tracking logic
                mirrorRect.set(1280f - playerWidth - playerX, playerY, playerWidth, playerHeight)

                // Swap Portal Event
                if (Intersector.overlaps(playerRect, maskRect)) {
                    hasSwappedIdentity = true
                    ghostX = playerX
                    ghostY = playerY
                    playerX = 1280f - playerWidth - playerX // Takeover Red body pos

                    screenFlashColor = com.badlogic.gdx.graphics.Color.WHITE
                    screenFlashTimer = 0.1f

                    // Goal Mask appears
                    maskX = 980f
                    maskY = 800f
                    maskRect.set(maskX, maskY, maskWidth, maskHeight)
                }
            } else {
                // Ghost stays stationary as a DEADLY_RED trap
                mirrorRect.set(ghostX, ghostY, playerWidth, playerHeight)

                if (Intersector.overlaps(playerRect, mirrorRect)) {
                    die("You're just a ghost in your own game now.")
                    stateTimer = -9999f
                    return
                }

                // Win Condition
                if (Intersector.overlaps(playerRect, maskRect)) {
                    win()
                }
            }
        }

        // --- FORCED CHUNK 2 LOGIC (BRUTE FORCE) ---
        if (currentLevel == 4 && currentChunk == 2) {"""

content = re.sub(update_hook, l6c2_logic, content)

# 5. Render Logic
render_mirror = r"""        // Draw Mirror Player \(Deadly Red\) for Level 5 Chunk 2
        if \(currentLevel == 5 && currentChunk == 2 && mirrorActive\) \{"""

new_render_mirror = """        // Draw Mirror Player / Ghost for Level 5/6
        if ((currentLevel == 5 && currentChunk == 2 && mirrorActive) || (currentLevel == 6 && currentChunk == 2)) {"""

content = re.sub(render_mirror, new_render_mirror, content)

render_player = r"""        // Draw Player \(Procedural Shapes\)
        shapeRenderer\.color = if \(horrorMode\) Color\.GRAY else Color\.BLACK"""

new_render_player = """        // Draw Player (Procedural Shapes)
        if (currentLevel == 6 && currentChunk == 2 && hasSwappedIdentity) {
            shapeRenderer.color = Color.RED
        } else {
            shapeRenderer.color = if (horrorMode) Color.GRAY else Color.GREEN
        }"""

content = re.sub(render_player, new_render_player, content)

# 6. Death and Win Logic

crush_wall = r"""                    else if \(currentLevel == 6 && currentChunk == 1\) \{
                        die\("Did you think the Mask was your friend\? Cute\."\)

                    \} else die\("Squished like a bug\. And just as insignificant\."\)"""

new_crush_wall = """                    else if (currentLevel == 6 && currentChunk == 1) {
                        die("Did you think the Mask was your friend? Cute.")
                    } else if (currentLevel == 6 && currentChunk == 2) {
                        die(listOf("Look at you... you've become the very thing you feared.", "Identity crisis much?", "You're just a ghost in your own game now.").random())
                        stateTimer = -9999f
                    } else die("Squished like a bug. And just as insignificant.")"""
content = re.sub(crush_wall, new_crush_wall, content)


die_method = r"""    private fun die\(customMessage: String\? = null\) \{
        if \(isDead\) return
        isDead = true
        var roast = customMessage \?: deathRoasts\.random\(\)"""

new_die_method = """    private fun die(customMessage: String? = null) {
        if (isDead) return
        isDead = true
        var roast = customMessage ?: deathRoasts.random()

        if (currentLevel == 6 && currentChunk == 2) {
            roast = customMessage ?: listOf("Look at you... you've become the very thing you feared.", "Identity crisis much?", "You're just a ghost in your own game now.").random()
            stateTimer = -9999f
        }"""

content = re.sub(die_method, new_die_method, content)


default_win_check = r"""        \} else if \(currentLevel == 5 && currentChunk == 3\) \{
            // Ignore default win, handled in logic block
        \} else if \(currentLevel != 4 \|\| currentChunk != 3\) \{
            if \(!isDead && Intersector\.overlaps\(playerRect, maskRect\)\) win\(\)
        \}"""

new_default_win_check = """        } else if (currentLevel == 5 && currentChunk == 3) {
            // Ignore default win, handled in logic block
        } else if (currentLevel == 6 && currentChunk == 2) {
            // Handled explicitly in update loop (only trigger if swapped)
        } else if (currentLevel != 4 || currentChunk != 3) {
            if (!isDead && Intersector.overlaps(playerRect, maskRect)) win()
        }"""

content = re.sub(default_win_check, new_default_win_check, content)

with open("core/src/main/kotlin/com/trustissues/GameScreen.kt", "w") as f:
    f.write(content)
