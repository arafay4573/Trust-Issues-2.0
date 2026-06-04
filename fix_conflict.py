import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

conflict_regex = re.compile(r'<<<<<<< HEAD\n            \n=======\n\n>>>>>>> origin/app-thumbnail-icon-10241708330282315673\n', re.DOTALL)
content = conflict_regex.sub('\n', content)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
