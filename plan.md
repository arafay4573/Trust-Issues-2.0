1. **Update Setup logic**: Raining sharks should start from the very beginning. So `leftMaskTriggered` should start as `true`, or we create a specific variable `isSharkRainActive` that starts as `true`.
2. **Update First Mask logic**: When the player touches the first mask (left mask), `isSharkRainActive` is set to `false`. "the raining stops and u can get back whereever u want".
3. **Shark spawning logic**: Update it so the sharks fall down but maybe give the player "a window" to dodge them? "raining sharks all over the screen but gives u windown". We can decrease the frequency of the spawn slightly so it's dodgable, or maybe `MathUtils.randomBoolean(0.05f)` instead of `0.1f`.
4. **Center Mask**: Still falls at 7 seconds or burns you if touched early.
5. **Right Mask**: Speeds up walls to 20x.
