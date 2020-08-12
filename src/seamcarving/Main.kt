package seamcarving

import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun getEnergy(img: Image, x: Int, y: Int): Double {
    fun gradient(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val c1 = img.getColor(x1, y1)
        val c2 = img.getColor(x2, y2)
        val r = c1.red - c2.red
        val g = c1.green - c2.green
        val b = c1.blue - c2.blue
        return r * r + g * g + b * b
    }

    val x1 = min(img.width - 2, max(1, x))
    val y1 = min(img.height - 2, max(1, y))
    val g1 = gradient(x1 - 1, y, x1 + 1, y)
    val g2 = gradient(x, y1 - 1, x, y1 + 1)
    return sqrt((g1 + g2).toDouble())
}

fun removeSeam(img: Image): Image {
    val dp = Array(img.height) { DoubleArray(img.width) }
    val parent = Array(img.height) { IntArray(img.width) }
    for (x in 0 until img.width) dp[0][x] = getEnergy(img, x, 0)
    for (y in 1 until img.height) {
        for (x in 0 until img.width) {
            parent[y][x] = (x - 1..x + 1).filter { it in 0 until img.width }.minBy { dp[y - 1][it] }!!
            dp[y][x] = getEnergy(img, x, y) + dp[y - 1][parent[y][x]]
        }
    }
    val removed = IntArray(img.height)
    var x = (0 until img.width).minBy { dp[img.height - 1][it] }!!
    for (y in img.height - 1 downTo 0) {
        removed[y] = x
        x = parent[y][x]
    }
    return Image(img.height, img.width - 1).apply {
        for (y in 0 until img.height) {
            for ((xNew, xOld) in (0 until img.width).filter { it != removed[y] }.withIndex()) {
                setColor(xNew, y, img.getColor(xOld, y))
            }
        }
    }
}

fun Image.compressed(removeWidth: Int, removeHeight: Int, compress: (Image) -> Image): Image {
    var img = this
    repeat(removeWidth) { img = compress(img) }
    img = img.transposed()
    repeat(removeHeight) { img = compress(img) }
    img = img.transposed()
    return img
}

@ExperimentalTime
fun main(args: Array<String>) {
    val arguments = HashMap<String, String>().apply {
        for (i in args.indices step 2) put(args[i].drop(1), args[i + 1])
    }

    val img = Image(ImageIO.read(File(arguments["in"]!!)))

    val (result, duration) = measureTimedValue {
        img.compressed(arguments["width"]!!.toInt(), arguments["height"]!!.toInt(), ::removeSeam)
    }
    println(duration)

    ImageIO.write(result.toBufferedImage(), "PNG", File(arguments["out"]!!))
}
