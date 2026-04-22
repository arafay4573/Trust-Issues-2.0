1. **Right Mask (Walls crush)**: Remove the laser cage entirely. Touching it makes walls move at 20x speed (e.g., `900f`).
2. **Center Mask**: Change 9s back to 7s. If touched before 7s, "throw lasers as soon as u touch ... burn u with lasers" -> create a laser sweeping from top to bottom (or a static laser grid that kills the player) and call `die()`.
3. **Left Mask**: "raining sharks" -> Instead of spawning one shark that falls once, we keep spawning falling sharks whenever the left mask is touched (or turn on a "shark rain" state).
4. **Roasts**: Update roasts to be "really hilarious" tailored to each death (squished, burned by lasers, shark rain, winning by doing nothing).
