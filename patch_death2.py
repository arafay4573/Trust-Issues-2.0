import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

roast_code = '''
        if (currentLevel == 9) {
            roast = listOf(
                "Error 404: Skill Not Found.",
                "I'm deleting your high score... Just kidding. Or am I?",
                "Access Denied. Please uninstall your brain."
            ).random()
        }
'''

content = content.replace(
    'if (currentLevel == 8) {',
    roast_code + '\n        if (currentLevel == 8) {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
