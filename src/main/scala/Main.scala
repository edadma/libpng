import io.github.edadma.libpng._

object Main extends App {
  val image = read("redish.png")

  if (image.format != ImageFormat.RGBA)
    sys.error("can only process RGBA type files")

  for (i <- 0 until image.width; j <- 0 until image.height) {
    image.setRGBARed(i, j, 0)
    image.setRGBAGreen(i, j, image.getRGBABlue(i, j))
  }

  write("new.png", image)
}

// http://zarb.org/~gc/html/libpng.html
// https://cpp.hotexamples.com/examples/-/-/png_destroy_read_struct/cpp-png_destroy_read_struct-function-examples.html
