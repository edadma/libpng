import io.github.edadma.libpng._

object Main extends App {
  val image = read("redish.png")

  println(image.width, image.height)
}

// http://zarb.org/~gc/html/libpng.html
// https://cpp.hotexamples.com/examples/-/-/png_destroy_read_struct/cpp-png_destroy_read_struct-function-examples.html
