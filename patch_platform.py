import re

with open('core/src/main/kotlin/com/trustissues/Platform.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'var state: PlatformState = PlatformState.ACTIVE',
    'var state: PlatformState = PlatformState.ACTIVE,\n    var label: String? = null'
)

with open('core/src/main/kotlin/com/trustissues/Platform.kt', 'w') as f:
    f.write(content)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '''    data class MovingWall(
        val rect: Rectangle,
        var speed: Float,
        var isActive: Boolean
    )''',
    '''    data class MovingWall(
        val rect: Rectangle,
        var speed: Float,
        var isActive: Boolean,
        var label: String? = null
    )'''
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
