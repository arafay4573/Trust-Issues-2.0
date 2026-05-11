import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

state_code = '''
    private object Level9Chunk1State {
        var phase = 0 // 0=Init/Type, 1=Wait middle, 2=Finale
        var consoleText = ""
        var consoleFullText = ""
        var consoleTimer = 0f
        var textIndex = 0
        var loadingCrumbled = false
        var cameraShakeTimer = 0f

        fun reset() {
            phase = 0
            consoleText = ""
            consoleFullText = "OSIRIS: Unauthorized connection found."
            consoleTimer = 0f
            textIndex = 0
            loadingCrumbled = false
            cameraShakeTimer = 0f
        }
    }
'''

content = content.replace(
    'private object Level8Chunk3State {',
    state_code + '\n    private object Level8Chunk3State {'
)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
