package com.trustissues

import com.badlogic.gdx.math.Rectangle

enum class PlatformType {
    NORMAL, CRUMBLE_SLOW, CRUMBLE_FAST, GHOST, CRUMBLING, INVISIBLE, DEADLY_RED, SAFE_SHARK
}

enum class PlatformState {
    ACTIVE, CRUMBLING, DESTROYED
}

class Platform(
    val rect: Rectangle,
    var type: PlatformType,
    var state: PlatformState = PlatformState.ACTIVE
) {
    var crumbleTimer: Float = 0f

    // Legacy support: some code might use `timer` or `isCrumbling` properties if I don't remove them from GameScreen usage.
    // I should make sure GameScreen uses `state` and `crumbleTimer`.
    // The previous data class had `timer` and `isCrumbling`. I will remove them and use `state` and `crumbleTimer`.

    fun startCrumbling(duration: Float) {
        if (state == PlatformState.ACTIVE) {
            state = PlatformState.CRUMBLING
            crumbleTimer = duration
        }
    }

    fun update(delta: Float) {
        if (state == PlatformState.CRUMBLING) {
            crumbleTimer -= delta
            if (crumbleTimer <= 0f) {
                state = PlatformState.DESTROYED
            }
        }
    }
}
