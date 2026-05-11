import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '8 -> setupLevel8(chunk)',
    '8 -> setupLevel8(chunk)\n            9 -> setupLevel9(chunk)'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
