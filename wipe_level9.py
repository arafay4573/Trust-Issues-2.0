import re

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove Level9Chunk1State
state_pattern = r'    private object Level9Chunk1State \{.*?\n    \}\n'
content = re.sub(state_pattern, '', content, flags=re.DOTALL)

# 2. Remove setupLevel9
setup_pattern = r'    private fun setupLevel9\(chunk: Int\) \{.*?\n    \}\n'
content = re.sub(setup_pattern, '', content, flags=re.DOTALL)

# 3. Remove update logic
update_pattern = r'        // --- LEVEL 9 CHUNK 1 LOGIC ---.*?// --- LEVEL 8 CHUNK 1 LOGIC ---'
content = re.sub(update_pattern, '        // --- LEVEL 8 CHUNK 1 LOGIC ---', content, flags=re.DOTALL)

# 4. Remove shape render logic
shape_pattern = r'        if \(currentLevel == 9 && currentChunk == 1\) \{.*?// Draw The Shrinking Void Overlay'
content = re.sub(shape_pattern, '        // Draw The Shrinking Void Overlay', content, flags=re.DOTALL)

# 5. Remove batch render logic
batch_pattern = r'        // Text Overlays for Level 9 Chunk 1.*?// Draw Mask using Texture'
content = re.sub(batch_pattern, '        // Draw Mask using Texture', content, flags=re.DOTALL)

with open('core/src/main/kotlin/com/trustissues/GameScreen.kt', 'w') as f:
    f.write(content)
