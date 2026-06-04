import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

conflict_regex = re.compile(r'<<<<<<< HEAD.*?=======\n.*?>>>>>>> origin/app-thumbnail-icon-10241708330282315673\n', re.DOTALL)
for match in conflict_regex.finditer(content):
    print(match.group(0))
