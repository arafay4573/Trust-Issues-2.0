1. **Fix Level Transitions:**
   - In `core/src/main/kotlin/com/trustissues/GameScreen.kt`, the `completeChunk` method currently hardcodes `isEndOfLevel` such that level 7 transitions to level 8 after chunk 2: `(currentLevel == 7 && nextChunk > 2)`.
   - Update `isEndOfLevel` to remove `(currentLevel == 7 && nextChunk > 2)` so it uses the default `nextChunk > 3` or explicitly add `(currentLevel == 7 && nextChunk > 3)`.
   - Also update `isEndOfLevel` for level 8. It currently says `(currentLevel == 8 && nextChunk > 1)`. Update it to `(currentLevel == 8 && nextChunk > 3)`.

2. **Fix Audio Issue (AudioManager singleton):**
   - The audio tracks (`backgroundMusic`, `deathMusic`, `victoryMusic`) stop playing because each new chunk load instantiates new `Music` objects, exhausting MediaPlayer limits.
   - Create an `AudioManager.kt` singleton or a `AudioManager` object at the package level in `GameScreen.kt` (or inside `TrustIssuesGame.kt` or `GameScreen.kt`). Wait, the memory states: "Audio tracks ... are managed via a globally accessible `AudioManager` singleton object initialized once (`initAudio()`)". So I will create an `AudioManager` object.
   - Replace the multiple `Gdx.audio.newMusic` calls in `GameScreen.kt` with a singleton `AudioManager` that initializes the music once and provides `playBackgroundMusic`, `playDeathMusic`, etc.

3. **Pre-commit Instructions:**
   - Follow pre-commit instructions before submitting.
