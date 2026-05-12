import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

state_code = '''
    private object Level9Chunk1State {
        var phase = 0 // 0=Treadmill, 1=Finale (Wait for X to appear)
        var updateProgress = 0f
        var popupTimer = 0f
        var popupsSpawned = 0
        var isXSpawned = false
        var uiScale = 1f

        fun reset() {
            phase = 0
            updateProgress = 0f
            popupTimer = 0f
            popupsSpawned = 0
            isXSpawned = false
            uiScale = 1f
        }
    }
'''

content = content.replace(
    'private object Level8Chunk3State {',
    state_code + '\n    private object Level8Chunk3State {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
