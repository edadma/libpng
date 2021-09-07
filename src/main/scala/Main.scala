import io.github.edadma.libpng._

object Main extends App {
  val image = read("redish.png")

  write("new.png", image)
}

// http://zarb.org/~gc/html/libpng.html
// https://cpp.hotexamples.com/examples/-/-/png_destroy_read_struct/cpp-png_destroy_read_struct-function-examples.html
