import baseClass.text.HorizontalAlign
import baseClass.text.Text
import baseClass.text.VerticalAlign
import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.tint
import org.openrndr.drawImage
import org.openrndr.extra.color.presets.GREY
import org.openrndr.extra.color.presets.MISTY_ROSE
import org.openrndr.extra.color.presets.WHITE_SMOKE
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import kotlin.math.abs
import kotlin.math.sin

fun main() = application {

    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    program {

        val gradientBackground = drawImage(width, height) {
            drawer.shadeStyle = RadialGradient(ColorRGBa.GRAY, ColorRGBa.BLACK)
            val r = Rectangle.fromCenter(drawer.bounds.center, width.toDouble()*1.5, height.toDouble()*1.5)
            drawer.rectangle(r)
        }


        extend {

            val invertColorFlag = Random.bool(probability = 0.025)

            /** DRAWING */
            drawer.clear(ColorRGBa.GREY.shade(Random.double(0.8,1.0)))
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.MISTY_ROSE.shade(0.5))

            // GRADIENT BACKGROUND
            if (invertColorFlag) {
                drawer.image(gradientBackground)
            }

            // CIRCLE
            drawer.fill = getColorFromInvertedFlag(!invertColorFlag)
            drawer.stroke = null
            drawer.strokeWeight = 1.0
            drawer.circle((width / 2.0) + sin(seconds)*100, (height / 2.0) + sin(seconds)*100, sin(seconds)*10000)

            // TEXT
            val text =
                when (abs(sin(seconds) * 100)) {
                in 0.0..33.0 -> {
                    "TELL ME WHAT YOU'RE GOING THROUGH"
                }
                in 33.0..66.0-> {
                    "TELL ME HOW YOU FEEL"
                }

                in 66.0..90.0-> {
                    "I DON'T KNOW HOW YOU'RE FEELING"
                }
                else -> {
                    if (Random.bool(probability = 0.9)) {
                        "P  L   E   A   S   E"
                    } else if (Random.bool()) {
                        "P  L    E  A   S   E"
                    } else if (Random.bool(probability = 0.7)) {
                        "P   L   è    à §   é"
                    } else {
                        "Just let me me know you're ok, my friend"
                    }
                }
            }
            drawer.fill = ColorRGBa.WHITE_SMOKE.shade(Random.double(0.8,1.0))
            val textToShow = Text(drawer, text, "file:data/fonts/Cinzel_Regular.ttf", 100.0)
            textToShow.draw(
                drawer.bounds.center,
                HorizontalAlign.CENTER,
                VerticalAlign.CENTER
            )

            // SEGMENTS

            drawer.stroke = getColorFromInvertedFlag(invertColorFlag, Random.double(0.0,0.8))
            drawer.translate(width / 2.0, height / 2.0)
            drawer.rotate(seconds * 15.0)
            for (i in 0..2){
                drawer.strokeWeight = Random.double(0.0,1.0)
                drawer.segment(Segment(
                    start = Vector2(
                        x = 0.0 ,
                        y = sin(seconds) * height
                    ),
                    c0 = Vector2(
                        x = sin(seconds)*0.0+(i*10),
                        y = sin(seconds)*0.0+(i*10)
                    ),
                    c1 = Vector2(
                        x = sin(seconds)*Random.double(0.0+(i*10), width.toDouble()),
                        y = sin(seconds)*Random.double(0.0+(i*10), height.toDouble())
                    ),
                    end = Vector2(
                        x = width.toDouble(),
                        y = sin(-seconds) * height
                    )
                ))
            }
            for (i in 0..2){
                drawer.strokeWeight = Random.double(0.0,1.0)
                drawer.segment(Segment(
                    start = Vector2(
                        x = 0.0,
                        y = sin(seconds) * height
                    ),
                    c0 = Vector2(
                        x = sin(-seconds)*Random.double(0.0+(i*10), width.toDouble()),
                        y = sin(-seconds)*Random.double(0.0+(i*10), height.toDouble())
                    ),
                    c1 = Vector2(
                        x = sin(-seconds)*0.0+(i*10),
                        y = sin(-seconds)*0.0+(i*10)
                    ),
                    end = Vector2(
                        x = width.toDouble(),
                        y = sin(-seconds) * height
                    )
                ))
            }
            for (i in 0..2){
                drawer.strokeWeight = Random.double(0.0,1.0)
                drawer.segment(Segment(
                    start = Vector2(
                        x = sin(seconds) * width,
                        y = 0.0
                    ),
                    c0 = Vector2(
                        x = sin(seconds)*0.0+(i*10),
                        y = sin(seconds)*0.0+(i*10)
                    ),
                    c1 = Vector2(
                        x = sin(seconds)*Random.double(0.0+(i*10), width.toDouble()),
                        y = sin(seconds)*Random.double(0.0+(i*10), height.toDouble())
                    ),
                    end = Vector2(
                        x = sin(-seconds) * width,
                        y = height.toDouble()
                    )
                ))
            }
            for (i in 0..2){
                drawer.strokeWeight = Random.double(0.0,1.0)
                drawer.segment(Segment(
                    start = Vector2(
                        x = sin(seconds) * width,
                        y = 0.0
                    ),
                    c0 = Vector2(
                        x = sin(-seconds)*Random.double(0.0+(i*10), width.toDouble()),
                        y = sin(-seconds)*Random.double(0.0+(i*10), height.toDouble())
                    ),
                    c1 = Vector2(
                        x = sin(-seconds)*0.0+(i*10),
                        y = sin(-seconds)*0.0+(i*10)
                    ),
                    end = Vector2(
                        x = sin(-seconds) * width,
                        y = height.toDouble()
                    )
                ))
            }
        }
    }
}

private fun getColorFromInvertedFlag(flag: Boolean, shadeFactor: Double = 1.0): ColorRGBa{
    return if (flag) ColorRGBa.BLACK.shade(shadeFactor)
    else ColorRGBa.GREY.shade(shadeFactor)
}