package com.swifterstudio.trustissues

import android.os.Bundle
import com.trustissues.TrustIssuesGame
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true
        initialize(TrustIssuesGame(), config)
    }
}
