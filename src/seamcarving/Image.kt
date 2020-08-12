package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage

class Image {
    val height: Int
    val width: Int
    private val data: Array<Array<Color>>

    constructor(img: BufferedImage) {
        this.height = img.height
        this.width = img.width
        this.data = Array(height) { y -> Array(width) { x -> Color(img.getRGB(x, y)) } }
    }

    constructor(height: Int, width: Int) {
        this.height = height
        this.width = width
        this.data = Array(height) { Array(width) { Color.BLACK } }
    }

    constructor(source: Image) {
        this.height = source.height
        this.width = source.width
        this.data = Array(height) { y -> Array(width) { x -> source.getColor(x, y) } }
    }

    fun getColor(x: Int, y: Int) = data[y][x]

    fun setColor(x: Int, y: Int, c: Color) {
        data[y][x] = c
    }

    fun toBufferedImage() = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
        for (y in 0 until height) {
            for (x in 0 until width) {
                setRGB(x, y, getColor(x, y).rgb)
            }
        }
    }

    fun transposed() = Image(width, height).also { res ->
        for (y in 0 until height) {
            for (x in 0 until width) {
                res.setColor(y, x, getColor(x, y))
            }
        }
    }
}