package ua.syt0r.kanji.mediaGenerator

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.opencv_core.Rect
import ua.syt0r.kanji.core.logger.Logger
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime


interface RecordingState {

    var recordingConfiguration: RecordingConfiguration

    suspend fun captureScreenshot(name: String)

    fun startVideoCapture(name: String)
    suspend fun stopVideoCapture()

    fun notifyCaptureRegionCoordinates(coordinates: LayoutCoordinates)

}

@Composable
fun rememberRecordingState(): DefaultRecordingState {
    val coroutineScope = rememberCoroutineScope()
    val windowInfo = LocalWindowInfo.current
    return remember { DefaultRecordingState(coroutineScope, windowInfo) }
}

@Composable
fun RecordingBox(
    state: RecordingState = rememberRecordingState(),
    content: @Composable RecordingState.() -> Unit
) {

    Box(
        modifier = Modifier.onGloballyPositioned { state.notifyCaptureRegionCoordinates(it) }
    ) {
        content(state)
    }

}

open class RecordingConfiguration(
    private val frameRate: Double = 60.0,
    val beforeRecording: (contentCoordinates: Rect) -> Unit = {}
) {

    open fun buildGrabber(): FFmpegFrameGrabber {
        val grabber = FFmpegFrameGrabber.createDefault("1")
        grabber.format = "avfoundation"
        grabber.frameRate = frameRate
        return grabber
    }

    open fun buildRecorder(
        file: File,
        width: Int,
        height: Int
    ): FFmpegFrameRecorder {
        val recorder = FFmpegFrameRecorder(file, width, height)
        recorder.videoCodec = avcodec.AV_CODEC_ID_H264
        recorder.format = "mp4"
        recorder.frameRate = frameRate
        recorder.videoBitrate = 25000000
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
        return recorder
    }


    companion object {
        val Default = RecordingConfiguration()
    }

}


class DefaultRecordingState(
    private val coroutineScope: CoroutineScope,
    private val windowInfo: WindowInfo
) : RecordingState {

    override var recordingConfiguration: RecordingConfiguration = RecordingConfiguration.Default

    private val contentLayoutCoordinatesState = mutableStateOf<LayoutCoordinates?>(null)
    private var recordingJob: Job? = null
    private val converter = OpenCVFrameConverter.ToMat()

    override suspend fun captureScreenshot(name: String) {
        val captureCoordinates = waitForCaptureCoordinates()
        val frameGrabber = recordingConfiguration.buildGrabber()
        frameGrabber.start()

        val frame = frameGrabber.grab().cropped(captureCoordinates)

        val converter = Java2DFrameConverter()
        val image: BufferedImage = converter.convert(frame)

        val output = File(name)
        withContext(Dispatchers.IO) { ImageIO.write(image, "png", output) }

        frameGrabber.stop()
        frameGrabber.release()
    }

    override fun startVideoCapture(name: String) {
        val currentJob = recordingJob
        if (currentJob != null && !currentJob.isCompleted)
            error("Previous recording didn't complete")

        recordingJob = coroutineScope.launch(Dispatchers.IO) {
            performCapture(File("$name.mp4"), recordingConfiguration)
        }
    }

    override suspend fun stopVideoCapture() {
        Logger.d("stopping video recording")
        recordingJob?.cancelAndJoin()
    }

    override fun notifyCaptureRegionCoordinates(coordinates: LayoutCoordinates) {
        contentLayoutCoordinatesState.value = coordinates
    }

    private suspend fun CoroutineScope.performCapture(
        file: File,
        configuration: RecordingConfiguration
    ) {
        val contentRectangle = waitForCaptureCoordinates()
        configuration.beforeRecording(contentRectangle)

        val grabber = configuration.buildGrabber()
        val recorder = configuration.buildRecorder(
            file,
            contentRectangle.width(),
            contentRectangle.height()
        )

        grabber.start()
        recorder.start()

        while (isActive) {
            Logger.d("capturing frame")
            val frameCaptureTime = measureTime {
                kotlin.runCatching {
                    recorder.record(grabber.grab().cropped(contentRectangle))
                }.onFailure {
                    it.printStackTrace()
                }
            }

            val delayDuration = 1.seconds.div(recorder.frameRate)
                .minus(frameCaptureTime)
                .coerceAtLeast(Duration.ZERO)

            delay(delayDuration)
        }
        Logger.d("stopping")

        recorder.stop()
        recorder.release()

        grabber.stop()
        grabber.release()
    }

    private suspend fun waitForCaptureCoordinates(): Rect =
        snapshotFlow { windowInfo.isWindowFocused }
            .filter { it }
            .flatMapLatest { snapshotFlow { contentLayoutCoordinatesState.value } }
            .filterNotNull()
            .first()
            .let { contentCoordinates ->
                val contentPosition = contentCoordinates.positionOnScreen()

                Rect(
                    contentPosition.x.roundToInt(),
                    contentPosition.y.roundToInt(),
                    contentCoordinates.size.width,
                    contentCoordinates.size.height
                )
            }

    private fun Frame.cropped(rect: Rect): Frame {
        val uncroppedMat = converter.convert(this)
        val croppedMat = uncroppedMat.apply(rect)
        return converter.convert(croppedMat)
    }

}