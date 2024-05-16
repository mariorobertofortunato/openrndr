import baseClass.text.HorizontalAlign
import baseClass.text.Text
import baseClass.text.VerticalAlign
import org.openrndr.Fullscreen
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.loadFont
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.drawImage
import org.openrndr.extra.color.presets.FLORAL_WHITE
import org.openrndr.extra.color.presets.LINEN
import org.openrndr.extra.color.presets.MISTY_ROSE
import org.openrndr.extra.fx.blur.BoxBlur
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        title = "Sphere"
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        multisample = WindowMultisample.SampleCount(4)
    }

    program {

        var time = 0.0
        val gradientBackground = drawImage(width, height) {
            drawer.shadeStyle = RadialGradient(ColorRGBa.LINEN, ColorRGBa.BLACK)
            val r = Rectangle.fromCenter(
                center = drawer.bounds.center,
                width = width.toDouble() * 1.25,
                height = height.toDouble() * 1.25
            )
            drawer.rectangle(r)
        }

        val offscreen = renderTarget(width, height) {
            colorBuffer()
            depthBuffer()
        }
        val blur = BoxBlur()
        val blurred = colorBuffer(width, height)

        val style = shadeStyle {
            fragmentPreamble = """
                    #define linearstep(edge0, edge1, x) clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0)
                """.trimIndent()
            fragmentTransform = """
                    float t = c_contourPosition/p_length;
                    float alpha = fract(t - p_time -p_offset);
                    alpha = linearstep(0.92, 1.0, alpha);
                    vec3 col = mix(vec3(1.0, 1.0, 1.0), vec3(1.0, 1.0, 1.0), 1.0 - alpha * alpha);
                    x_stroke.rgb = mix(col, vec3(1.0), 1.0 - alpha * alpha);
                    x_stroke.a = alpha;
                """.trimIndent()
        }

        extend {

            val invertColorFlag = Random.bool(probability = 0.025)

            /** DRAWING */

            // FLASHING BLURRED BACKGROUND
            if (invertColorFlag) {
                drawer.isolatedWithTarget(offscreen) {
                    // GRADIENT BACKGROUND
                    drawer.image(gradientBackground)
                    // CIRCLE
                    drawer.fill = getColorFromInvertedFlag(true, Random.double(0.4, 0.8))
                    drawer.circle(width / 2.0, height / 2.0, 200.0)
                }
                blur.window = Random.int(30, 90)
                blur.apply(offscreen.colorBuffer(0), blurred)
                drawer.image(blurred)
            }

            // "STARS" BACKGROUND
            drawer.points {
                repeat(20000) {
                    fill = rgb(gray = (it * 0.1 - seconds) % 1, a = 0.2)
                    point((it * it * 0.011) % width, (it * 4.011) % height)
                }
            }

            // CENTER PIECE SEGMENTS
            for (i in -20..20) {
                val segment = Segment(
                    start = Vector2(
                        x = width / 2.0,
                        y = height / 2.0,
                    ),
                    c0 = Vector2(
                        x = cos(seconds/5 + i) * width / 2.0 + (width / 2),
                        y = sin(seconds/5 + i) * height / 3.0 + (height / 3),
                    ),
                    c1 = Vector2(
                        x = cos(seconds/5 + i) * -width / 2.0 + (width / 2),
                        y = sin(seconds/5 + i) * -height / 3.0 + (height / 3),
                    ),
                    end = Vector2(
                        x = width / 2.0,
                        y = height / 2.0,
                    )
                )
                drawer.stroke = ColorRGBa.LINEN.shade(Random.double(0.4, 0.8))
                drawer.segment(segment)
            }

            // FUNNEL SEGMENTS
            for (i in -20..20) {
                val segment = Segment(
                    start = Vector2(
                        x = cos(seconds + i) * width / 2.0 + (width / 2),
                        y = sin(seconds + i) * height / 2.0 + (height / 3) + i,
                    ),
                    c0 = Vector2(
                        x = width / 2.0,
                        y = height / 2.0
                    ),
                    end = Vector2(
                        x = (width / 2.0) + i,
                        y = height.toDouble() + 200.0,
                    )
                )
                drawer.stroke = ColorRGBa.LINEN.shade(Random.double(0.4, 0.8))
                drawer.segment(segment)
            }

            // FUNNEL & CENTER PIECE PARTICLES
            for (i in -100..100) {
                val funnelParticles = Segment(
                    start = Vector2(
                        x = cos(seconds + i) * width / 2.0 + (width / 2) + (i / 10),
                        y = sin(seconds + i) * height / 2.0 + (height / 3),
                    ),
                    c0 = Vector2(
                        x = width / 2.0,
                        y = height / 2.0
                    ),
                    end = Vector2(
                        x = (width / 2.0) + (i / 10),
                        y = height.toDouble() + 200.0,
                    )
                )
                val centerPieceParticles = Segment(
                    start = Vector2(
                        x = width / 2.0,
                        y = height / 2.0,
                    ),
                    c0 = Vector2(
                        x = cos(seconds/5 + i) * width / 2.0 + (width / 2),
                        y = sin(seconds/5 + i) * height / 3.0 + (height / 3),
                    ),
                    c1 = Vector2(
                        x = cos(seconds/5 + i) * -width / 2.0 + (width / 2),
                        y = sin(seconds/5 + i) * -height / 3.0 + (height / 3),
                    ),
                    end = Vector2(
                        x = width / 2.0,
                        y = height / 2.0,
                    )
                )
                style.parameter("time", time)
                style.parameter("length", centerPieceParticles.length * 2)
                style.parameter("offset", Random.perlin(i * 0.303, i * 0.808))
                drawer.shadeStyle = style
                drawer.segments(listOf(centerPieceParticles, funnelParticles))
            }


            // TEXT
            drawer.fill = ColorRGBa.FLORAL_WHITE

            val textFreeFall = Text(
                drawer,
                "FREE FALL",
                "file:data/fonts/Cinzel_Regular.ttf",
                99.0
            )
            textFreeFall.draw(
                Vector2(0.0 + 25.0, height - textFreeFall.height + sin(seconds/10) * 5),
                HorizontalAlign.LEFT,
                VerticalAlign.BASELINE
            )

            val textDoomedTo = Text(
                drawer,
                "DOOMED TO",
                "file:data/fonts/Cinzel_Regular.ttf",
                99.0
            )
            textDoomedTo.draw(
                Vector2(0.0 + 20.0, (height - textDoomedTo.height - textFreeFall.height - sin(seconds/10) * 5)),
                HorizontalAlign.LEFT,
                VerticalAlign.BASELINE
            )

            val textBottomlessPit = Text(
                drawer,
                "BOTTOMLESS PIT",
                "file:data/fonts/Cinzel_Regular.ttf",
                99.0
            )
            textBottomlessPit.draw(
                Vector2(width.toDouble() - 20.0, height - textBottomlessPit.height - sin(seconds/10) * 5),
                HorizontalAlign.RIGHT,
                VerticalAlign.BASELINE
            )

            val textInThis= Text(
                drawer,
                "IN THIS",
                "file:data/fonts/Cinzel_Regular.ttf",
                99.0
            )
            textInThis.draw(
                Vector2(width.toDouble() - 25.0, (height - textInThis.height - textBottomlessPit.height + sin(seconds/10) * 5)),
                HorizontalAlign.RIGHT,
                VerticalAlign.BASELINE
            )


/*            val si = compound {
                intersection {

                }
            }
            drawer.fill = ColorRGBa.BLACK
            drawer.shapes(si)*/

            // TODO vedere se si riesce a fare un funzione helper per trasformare le stringhe in shape IN MODO DA POTER FARE LE INTERSEZIONI TRA DI LORO
/*            val face = loadFace("data/fonts/default.otf")
            val charArray = charArrayOf('F','R','E','E')

            val shapeList = mutableListOf<Shape>()
            charArray.forEach { char ->
                shapeList.add(face.glyphForCharacter(char).shape(75.0))
            }
            shapeList.forEachIndexed { i, shape ->
                drawer.translate(Vector2(0.0 + (i * shape.bounds.width), 0.0))
                drawer.shape(shape)
            }*/




            time += 0.005
        }
    }
}

private fun getColorFromInvertedFlag(flag: Boolean, shadeFactor: Double = 1.0): ColorRGBa {
    return if (flag) ColorRGBa.BLACK.shade(shadeFactor)
    else ColorRGBa.LINEN.shade(shadeFactor)
}
