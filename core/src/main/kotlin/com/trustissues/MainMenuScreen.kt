package com.trustissues

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ScreenUtils

class MainMenuScreen(private val game: TrustIssuesGame) : ScreenAdapter() {
    override fun render(delta: Float) {
        ScreenUtils.clear(Color.DARK_GRAY)
    }
}
