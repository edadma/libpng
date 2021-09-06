import io.github.edadma.libpng._
import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.libc.stdio
import scala.scalanative.libc.stdlib

object Main extends App {

  val file = open("") getOrElse sys.exit(1)
  val png  = create_read_struct(LIBPNG_VER_STRING) getOrElse error("create_read_struct failed")
  val info = png.create_info_struct getOrElse error("create_info_struct failed")

  if (png.setjmp) error("error during init_io")
  png.init_io(file)

  png.set_sig_bytes(8)
  png.read_info(info)

  val width            = png.get_image_width(info)
  val height           = png.get_image_height(info)
  val color_type       = png.get_color_type(info)
  val bit_depth        = png.get_bit_depth(info)
  val number_of_passes = png.set_interlace_handling

  png.read_update_info(info)

  val row_pointers = stdlib.malloc(sizeof[lib.png_bytep] * height).asInstanceOf[lib.png_bytepp]

  file.close()

  def error(msg: String): Nothing = {
    Console.err.println(msg)
    file.close()
    sys.exit(1)
  }

}
