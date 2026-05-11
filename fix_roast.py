import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# I accidentally placed the roast code in `setupChunk` instead of `die()`
content = content.replace('''
        if (currentLevel == 9) {
            roast = listOf(
                "Error 404: Skill Not Found.",
                "I'm deleting your high score... Just kidding. Or am I?",
                "Access Denied. Please uninstall your brain."
            ).random()
        }

        if (currentLevel == 8) {''', '''        if (currentLevel == 8) {''')

content = content.replace('''        if (currentLevel == 8) {
            roast = when (currentChunk) {''', '''        if (currentLevel == 9) {
            roast = listOf(
                "Error 404: Skill Not Found.",
                "I'm deleting your high score... Just kidding. Or am I?",
                "Access Denied. Please uninstall your brain."
            ).random()
        }

        if (currentLevel == 8) {
            roast = when (currentChunk) {''')

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
