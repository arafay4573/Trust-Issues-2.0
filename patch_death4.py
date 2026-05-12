import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# Fix shadowed oldTransform
content = content.replace(
    'val oldTransform = shapeRenderer.transformMatrix.cpy()',
    'val shapeOldTransform = shapeRenderer.transformMatrix.cpy()'
).replace(
    'shapeRenderer.transformMatrix = oldTransform',
    'shapeRenderer.transformMatrix = shapeOldTransform'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
