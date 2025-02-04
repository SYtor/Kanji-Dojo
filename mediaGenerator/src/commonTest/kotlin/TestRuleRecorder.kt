@file:OptIn(ExperimentalTestApi::class)

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedeco.javacv.Java2DFrameConverter
import org.jetbrains.skiko.toBufferedImage
import ua.syt0r.kanji.mediaGenerator.RecordingConfiguration
import java.awt.image.BufferedImage
import java.io.File


interface RecorderScope : SemanticsNodeInteractionsProvider {
    suspend fun startRecording()
    suspend fun stopRecording()
}

val DefaultSize = IntSize(1920, 1080)

@OptIn(ExperimentalTestApi::class)
fun testRecording(
    name: String,
    size: IntSize = DefaultSize,
    content: @Composable RecorderScope.() -> Unit
) {

    runSkikoComposeUiTest(
        size = size.toSize()
    ) {

        val recorder = JavaCvRecorder(name, size, this)

        mainClock.autoAdvance = false

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                content.invoke(recorder)
            }
        }

        recorder.notifyReady()
        recorder.awaitCompletion()

    }


}

class JavaCvRecorder(
    private val name: String,
    private val size: IntSize,
    private val uiTest: SkikoComposeUiTest,
    private val configuration: RecordingConfiguration = RecordingConfiguration.Default,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
) : RecorderScope, SemanticsNodeInteractionsProvider by uiTest {

    private lateinit var recordingJob: Job
    private val prepareCompletion = CompletableDeferred<Unit>()
    private val recordingCompletion = CompletableDeferred<Unit>()

    override suspend fun startRecording() {
        recordingJob = scope.launch {
            prepareCompletion.await()

            val recorder = configuration.buildRecorder(
                file = File("$name.mp4"),
                width = size.width,
                height = size.height
            )
            recorder.start()

            val converter = Java2DFrameConverter()

            while (isActive) {
                val image: BufferedImage = uiTest.onRoot()
                    .captureToImage()
                    .asSkiaBitmap()
                    .toBufferedImage()

                val clone = BufferedImage(image.width, image.height, BufferedImage.TYPE_3BYTE_BGR)
                val graphics = clone.graphics
                graphics.drawImage(image, 0, 0, null)
                graphics.dispose()

                val frame = converter.convert(clone)

                recorder.record(frame)

                uiTest.mainClock.advanceTimeByFrame()
                uiTest.waitForIdle()
            }

            recorder.stop()
            recorder.release()
            recordingCompletion.complete(Unit)
        }

    }

    override suspend fun stopRecording() {
        recordingJob.cancelAndJoin()
    }

    fun notifyReady() = prepareCompletion.complete(Unit)
    fun awaitCompletion() = runBlocking { recordingCompletion.await() }

}