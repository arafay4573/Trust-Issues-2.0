import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

roast_code = '''
        if (currentLevel == 9) {
            roast = customMessage ?: listOf(
                "Error 404: Skill Not Found.",
                "I'm deleting your high score... Just kidding. Or am I?",
                "Access Denied. Please uninstall your brain."
            ).random()
        }
'''

content = content.replace(
    '''        if (currentLevel == 7 && currentChunk == 2) {
            roast = customMessage ?: listOf("Newton is laughing at your lack of coordination.", "You're falling for the same tricks... literally.").random()
        }''',
    '''        if (currentLevel == 7 && currentChunk == 2) {
            roast = customMessage ?: listOf("Newton is laughing at your lack of coordination.", "You're falling for the same tricks... literally.").random()
        }''' + '\n' + roast_code
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
