import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'if ((currentLevel == 2 || currentLevel == 3 || currentLevel == 4 || currentLevel == 5) && plat.type == PlatformType.CRUMBLING) {',
    'if ((currentLevel == 2 || currentLevel == 3 || currentLevel == 4 || currentLevel == 5 || currentLevel == 6) && plat.type == PlatformType.CRUMBLING) {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
