package com.trustissues

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music

object AudioManager {
    var backgroundMusic: Music? = null
    var deathMusic: Music? = null
    var victoryMusic: Music? = null

    private var initialized = false

    fun initAudio() {
        if (initialized) return
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/game_theme.mp3"))
            backgroundMusic?.isLooping = true
            backgroundMusic?.volume = 0.5f

            deathMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/fahhhh.mp3"))
            deathMusic?.isLooping = false
            deathMusic?.volume = 0.8f

            victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/victory.mp3"))
            victoryMusic?.isLooping = false
            victoryMusic?.volume = 0.8f

            initialized = true
        } catch (e: Exception) {
            println("Error loading audio: \${e.message}")
        }
    }

    fun playBackgroundMusic() {
        val prefs = Gdx.app.getPreferences("TrustIssues")
        if (prefs.getBoolean("soundEnabled", true)) {
            backgroundMusic?.play()
        }
    }

    fun stopBackgroundMusic() {
        backgroundMusic?.stop()
    }

    fun pauseBackgroundMusic() {
        backgroundMusic?.pause()
    }

    fun playDeathMusic() {
        val prefs = Gdx.app.getPreferences("TrustIssues")
        if (prefs.getBoolean("soundEnabled", true)) {
            deathMusic?.stop()
            deathMusic?.position = 0f
            deathMusic?.play()
        }
    }

    fun stopDeathMusic() {
        deathMusic?.stop()
    }

    fun playVictoryMusic() {
        val prefs = Gdx.app.getPreferences("TrustIssues")
        if (prefs.getBoolean("soundEnabled", true)) {
            victoryMusic?.stop()
            victoryMusic?.position = 0f
            victoryMusic?.play()
        }
    }

    fun stopVictoryMusic() {
        victoryMusic?.stop()
    }

    fun resetBackgroundMusic() {
        val prefs = Gdx.app.getPreferences("TrustIssues")
        backgroundMusic?.stop()
        if (prefs.getBoolean("soundEnabled", true)) {
            backgroundMusic?.position = 0f
            backgroundMusic?.play()
        }
    }
}
