package baseClass.text

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.Writer
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

class Text (val drawer: Drawer, val text: String, url: String, size: Double) {

    val font = FontImageMap.fromUrl(url, size)
    val writer = Writer(drawer)
    val height: Double
        get() {
            val isUpper = text.fold(true) { acc, char ->
                acc && (char.isUpperCase() || char.isDigit() || char.isWhitespace())
            }
            return if (isUpper) {
                font.height
            } else {
                font.height - font.descenderLength
            }
        }
    val width = font.let {
        drawer.fontMap = font
//        println("""
//            asc: ${font.ascenderLength}
//            height: ${font.height}
//        """.trimIndent())
        writer.textWidth(text)
    }

    private val fillRenderTarget by lazy {
        renderTarget(width.toInt(), height.toInt()) {
            colorBuffer()
        }
    }

    fun align(horizontalAlign: HorizontalAlign, verticalAlign: VerticalAlign
    ): Vector2 {
        val deltaX = when (horizontalAlign) {
            HorizontalAlign.LEFT -> 0.0
            HorizontalAlign.CENTER -> -width / 2.0
            HorizontalAlign.RIGHT -> -width
        }
        val deltaY = when (verticalAlign) {
            VerticalAlign.BASELINE -> 0.0
            VerticalAlign.ASCENDER -> font.height
            /// Not sure if we need all these vertical alignment options
            VerticalAlign.CENTER -> font.height / 2.0
            VerticalAlign.CENTER_FULL -> ((font.ascenderLength + font.descenderLength) / 2.0)
            VerticalAlign.DESCENDER -> font.descenderLength
        }
        return Vector2(deltaX, deltaY)
    }

    // draws text stretched to the width and height of a target rectangle
    fun stretch(target: Rectangle, fn: Drawer.() -> Unit) {
        drawer.isolatedWithTarget(fillRenderTarget) {
            drawer.clear(ColorRGBa.TRANSPARENT)
            drawer.ortho(fillRenderTarget)
            fn()
            draw(
                Vector2(0.0, 0.0),
                HorizontalAlign.LEFT,
                VerticalAlign.ASCENDER
            )
        }
        drawer.image(
            fillRenderTarget.colorBuffer(0),
            target.corner,
            target.width,
            target.height
        )
    }

    fun draw(
        position: Vector2,
        horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
        verticalAlign: VerticalAlign = VerticalAlign.BASELINE
    ) {
        drawer.fontMap = font
        val alignment = align(horizontalAlign, verticalAlign)
        drawer.text(text, position + alignment)
    }

    fun explain(
        position: Vector2,
        horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
        verticalAlign: VerticalAlign = VerticalAlign.BASELINE
    ) {

        drawer.fontMap = font
        val alignment = align(horizontalAlign, verticalAlign)
        val p = position + alignment

        // LEADING
        drawer.fill = ColorRGBa.PINK
        drawer.rectangle(p - Vector2(0.0, font.leading), width, font.leading)

        // ASC
        drawer.fill = ColorRGBa.BLUE.shade(0.5)
        drawer.rectangle(
            p - Vector2(0.0, font.ascenderLength),
            width,
            font.ascenderLength
        )

        // HEIGHT
        drawer.fill = ColorRGBa.GRAY
        drawer.rectangle(p - Vector2(0.0, font.height), width, font.height)

        // DESC
        drawer.fill = ColorRGBa.BLUE
        drawer.rectangle(p, width, -font.descenderLength)

        drawer.fill = ColorRGBa.BLACK
        drawer.text(text, p)
    }

}

enum class HorizontalAlign {
    LEFT,
    CENTER,
    RIGHT
}

enum class VerticalAlign {
    ASCENDER,
    //        XHEIGHT,
    BASELINE,
    DESCENDER,
    //        CENTER_XHEIGHT,
    CENTER,
    CENTER_FULL
}