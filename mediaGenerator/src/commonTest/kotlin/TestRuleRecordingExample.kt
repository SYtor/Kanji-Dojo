import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.di.appModules
import ua.syt0r.kanji.presentation.KanjiDojoApp
import ua.syt0r.kanji.presentation.common.ui.Orientation


class TestRuleRecordingExample {

    @Test
    fun test2() = testRecording("test_video") {

        startKoin { loadKoinModules(appModules) }

        KanjiDojoApp(Orientation.Landscape)

        LaunchedEffect(Unit) {
            startRecording()
            delay(1000)
            stopRecording()
        }

    }

}