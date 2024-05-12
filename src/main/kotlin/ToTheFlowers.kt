import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.grayscale
import org.openrndr.draw.loadImage
import org.openrndr.draw.tint
import org.openrndr.extra.color.presets.MISTY_ROSE
import org.openrndr.extra.fx.blur.BoxBlur
import org.openrndr.extra.fx.color.ChromaticAberration
import org.openrndr.extra.fx.color.Sepia
import org.openrndr.extra.noise.Random
import kotlin.math.abs
import kotlin.math.cos

fun main() = application {

    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    program {

        val fontList = listOf(
            "data/fonts/1ubuntu_sans.ttf",
            "data/fonts/2UbuntuSans[wdth,wght].ttf",
            "data/fonts/default.otf",
            "data/fonts/DejaVuSerif.ttf",
           "data/fonts/NotoSerifDisplay-Italic.ttf",
            "data/fonts/Ubuntu[wdth,wght].ttf",
            "data/fonts/UbuntuSans[wdth,wght].ttf",
           "data/fonts/UbuntuSans-Italic[wdth,wght].ttf",
            "data/fonts/URWBookman-DemiItalic.otf",
        )
        var background: ColorBuffer
        val backgroundFrameValueList = listOf(
            "untitled_00001.jpg",
            "untitled_00002.jpg",
/*            "untitled_00003.jpg",
            "untitled_00004.jpg",
            "untitled_00005.jpg",
            "untitled_00006.jpg",
            "untitled_00007.jpg",
            "untitled_00008.jpg",*/
        )
        var backgroundOffsetXValue = 2.0
        var backgroundOffsetYValue = 2.0

        val blurred = colorBuffer(width, height)
        val blur = BoxBlur()
        val filtered = colorBuffer(width, height)
        val filter = Sepia()
        val aberrationFiltered = colorBuffer(width, height)
        val aberrationFilter = ChromaticAberration()


        extend {

            val evenSecondsFlag = seconds.toInt() % 2 == 0
            val time = abs(cos(seconds)*1000).toInt()

            /** TIME BASED CALCULATIONS */
            if (evenSecondsFlag) {

                //BackgroundOffset
                backgroundOffsetXValue = when (Random.bool(probability = 0.1)) {
                    true -> { Random.double(1.97,2.03) }
                    false -> { 2.0 }
                }
                backgroundOffsetYValue = when (Random.bool(probability = 0.05)) {
                    true -> { Random.double(1.97,2.03) }
                    false -> { 2.0 }
                }
            }

            background = when (time) {
                in 0..333 -> {
                    loadImage("data/images/1/${Random.pick(backgroundFrameValueList)}")
                }
                in 333..666 -> {
                    loadImage("data/images/2/${Random.pick(backgroundFrameValueList)}")
                }
                in 666..999 -> {
                    loadImage("data/images/3/${Random.pick(backgroundFrameValueList)}")
                }
                else -> {
                    loadImage("data/images/3/${Random.pick(backgroundFrameValueList)}")
                }
            }

            /** DRAWING */
            drawer.clear(ColorRGBa.BLACK)

            // BACKGROUND
            drawer.drawStyle.colorMatrix = grayscale(
                r = 1.0 / Random.double(Random.double(2.0,2.5),3.0),
                g = 1.0 / Random.double(Random.double(2.0,2.5),3.0),
                b = 1.0 / Random.double(Random.double(2.0,2.5),3.0)
            )
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.MISTY_ROSE.shade(0.5))
            drawer.image(
                colorBuffer = background,
                x = (drawer.bounds.center.x - (background.width / backgroundOffsetXValue)),
                y = (drawer.bounds.center.y - (background.height / backgroundOffsetYValue))
            )
            // Blurred Background
            if (Random.bool(probability = 0.2)){
                blur.apply(background, blurred)
                filter.amount = 1.0
                filter.apply(blurred, filtered)
                if (Random.bool(probability = 0.65)){
                    aberrationFilter.aberrationFactor = 16.0
                    aberrationFilter.apply(filtered, aberrationFiltered)
                    drawer.image(aberrationFiltered)
                } else {
                    drawer.image(filtered)
                }

            }
        }
    }
}