import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.runBlocking
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameConverter
import org.junit.Rule
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import kotlin.time.measureTime


class TestRuleRecordingExample {

    @get:Rule
    val testRule = createComposeRule()

    @Test
    fun test1() = runBlocking {

        val recorder = FFmpegFrameRecorder(File("test.mp4"), 500, 500)
        recorder.videoCodec = avcodec.AV_CODEC_ID_H264
        recorder.format = "mp4"
        recorder.frameRate = 60.0
        recorder.videoBitrate = 25000000
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
        recorder.start()

        val converter = Java2DFrameConverter()

        testRule.setContent {
            Surface(
                modifier = Modifier.padding(30.dp)
            ) {

                val animatedRotation = rememberInfiniteTransition()
                    .animateFloat(0f, 1f, infiniteRepeatable(tween())).value

                Text(
                    text = "test",
                    modifier = Modifier.graphicsLayer { rotationZ = animatedRotation },
                    fontSize = 200.sp
                )

                println("animatedRotation[$animatedRotation]")

            }
        }
        testRule.mainClock.autoAdvance = false

        repeat(200) { i ->
            val image: BufferedImage
            val time = measureTime {
                image = testRule.onRoot().captureToImage().toAwtImage()
                recorder.record(converter.convert(image))
            }
            testRule.mainClock.advanceTimeByFrame()
            testRule.waitForIdle()
            println(time)
        }

        recorder.stop()
        recorder.release()

    }

}