import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

if 'isJumpPressed' in content:
    print('isJumpPressed exists')
else:
    print('no isJumpPressed')
