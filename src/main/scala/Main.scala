import io.github.edadma.libpng._
import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.libc.stdio
import scala.scalanative.libc.stdlib

object Main extends App {

  def error(msg: String, file: PNGFile): Nothing = {
    Console.err.println(msg)
    file.close()
    sys.exit(1)
  }

  def readImage(path: String): PNGImage = {
    val file = open(path) getOrElse sys.exit(1)
    val png  = create_read_struct(LIBPNG_VER_STRING) getOrElse error("create_read_struct failed", file)
    val info = png.create_info_struct getOrElse error("create_info_struct failed", file)

    if (png.setjmp) error("error during init_io", file)

    png.init_io(file)
    png.set_sig_bytes(8)
    png.read_info(info)

    val color_type       = png.get_color_type(info)
    val bit_depth        = png.get_bit_depth(info)
    val number_of_passes = png.set_interlace_handling

    if (color_type == COLOR_TYPE_PALETTE)
      png.set_palette_to_rgb()

    if (color_type == COLOR_TYPE_GRAY && bit_depth < 8)
      png.set_expand_gray_1_2_4_to_8()

    if (png.get_valid(info, INFO_tRNS))
      png.set_tRNS_to_alpha()

    if (bit_depth == 16)
      png.set_strip_16()
    else if (bit_depth < 8)
      png.set_packing()

    png.read_update_info(info)

    val (width, height, b, c, _, _, _) = png.get_IHDR(info)
    val format =
      c match {
        case `COLOR_TYPE_GRAY`       => ImageFormat.GRAY
        case `COLOR_TYPE_GRAY_ALPHA` => ImageFormat.GRAY_ALPHA
        case `COLOR_TYPE_RGB`        => ImageFormat.RGB
        case `COLOR_TYPE_RGB_ALPHA`  => ImageFormat.RGB_ALPHA
      }

    val image        = stdlib.malloc((width * height * format.typ).toULong).asInstanceOf[lib.png_bytep]
    val row_pointers = stdlib.malloc(sizeof[lib.png_bytep] * height.toULong).asInstanceOf[lib.png_bytepp]

    for (i <- 0 until height)
      row_pointers(i) = image + ((height - (i + 1)) * width * format.typ)

    lib.png_read_image(png.ptr, row_pointers)
    file.close()

    new PNGImage(image, width, height, format)
  }

}

// http://zarb.org/~gc/html/libpng.html
// https://cpp.hotexamples.com/examples/-/-/png_destroy_read_struct/cpp-png_destroy_read_struct-function-examples.html
