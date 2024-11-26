package ua.syt0r.kanji.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.presentation.screen.main.DeepLinkHandler

open class KanjiDojoActivity : AppCompatActivity() {

    private val deepLinkHandler by inject<DeepLinkHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("intentData[${intent.dataString}]")
        enableEdgeToEdge()
        intent.dataString?.let { deepLinkHandler.notifyDeepLink(it) }
        setContent {
            KanjiDojoApp(
                deepLinkHandler = deepLinkHandler
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.d("intentData[${intent.dataString}]")
        intent.dataString?.let { deepLinkHandler.notifyDeepLink(it) }
    }

}