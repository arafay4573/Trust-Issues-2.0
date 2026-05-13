import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

state_code = '''
    private object Level9Chunk1State {
        var phase = 0 // 0=Initial, 1=Security Update Box
        var boxAngle = 0f
        var boxAngleVel = 0f
        var isPlayerOnBox = false

        fun reset() {
            phase = 0
            boxAngle = 0f
            boxAngleVel = 0f
            isPlayerOnBox = false
        }
    }
'''

content = content.replace(
    'private object Level8Chunk3State {',
    state_code + '\n    private object Level8Chunk3State {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
