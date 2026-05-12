import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

roast_code = '''
        if (currentLevel == 9) {
            roast = customMessage ?: listOf(
                "You should have read the Terms of Service.",
                "Update Failed: User is obsolete.",
                "Your battery is fine, but your skill is at 0%."
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
