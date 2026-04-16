import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Crumbling platforms roast "Is your screen dirty, or is it just your lack of skill?"
# This triggers when falling into void due to crumbling platform? The prompt says:
# "They must turn Red and disappear 1.2 seconds after contact"
# Wait, the prompt implies "Death Checks: Apply to both Player and Mirror for: Red Walls, Crumbling Platforms, Mirror-Player collision, and the Laser Cage"
# And it lists roasts:
# "Is your screen dirty, or is it just your lack of skill?" -> This might be for Red Walls or Crumbling Platforms falling.
# Let's add the roast logic to the die() method or the specific falling checks.
# Wait, "Crumbling Platforms" death check usually means falling through them and hitting the void (gravity).
# Or maybe turning red means becoming deadly red?
# The prompt says: "place a zig-zag of 5 Crumbling Platforms. They must turn Red and disappear 1.2 seconds after contact."
# If they turn red... our platform logic has `PlatformState.CRUMBLING` draw them red already, or does it?
# In TrustIssues, when platforms crumble, they change color? Let's check draw logic.
