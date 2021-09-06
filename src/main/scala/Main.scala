import io.github.edadma.libpng._
import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._
import scala.scalanative.libc.stdio._

object Main extends App {

  val file =
    fopen(c"build.sbt", c"r") match {
      case null =>
        println("error opening file")
        sys.exit(1)
      case f => f
    }

  val header = stackalloc[Byte](8)

  fread(header, sizeof[Byte], 8.toUInt, file) match {
    case 8 =>
      if (lib.png_sig_cmp(header, 0.toUInt, 8) != 0) {
        println("bad PNG signature")
        sys.exit(1)
      }
    case _ =>
      if (ferror(file) != 0)
        println("error reading file")
      else
        println("not a PNG file")

      sys.exit(1)
  }

  val png = create_read_struct(LIBPNG_VER_STRING)

  png.init_io(file)

  val info_ptr = png.create_info_struct

  fclose(file)

}
