package io.github.edadma

import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.collection.mutable
import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object libpng {

  private def copy(src: collection.Seq[Byte], dst: Ptr[Byte], count: UInt): Unit =
    for (i <- 0 until count.toInt)
      dst(i) = src(i)

  private def copy(src: Ptr[Byte], dst: mutable.Seq[Byte], count: Int): Unit =
    for (i <- 0 until count)
      dst(i) = src(i)

  private def bool(a: CInt): Boolean = if (a == 0) false else true

  implicit class PNG private[libpng] (val ptr: lib.png_structp) extends AnyVal {
    def setjmp: Boolean                       = bool(lib.png_setjmp(ptr))
    def set_sig_bytes(num_bytes: Int): Unit   = lib.png_set_sig_bytes(ptr, num_bytes)
    def create_info_struct: Option[Info]      = Option(lib.png_create_info_struct(ptr))
    def read_info(info: Info): Unit           = lib.png_read_info(ptr, info.ptr)
    def read_update_info(info: Info): Unit    = lib.png_read_update_info(ptr, info.ptr)
    def set_interlace_handling: Int           = lib.png_set_interlace_handling(ptr)
    def init_io(file: PNGFILE): Unit          = lib.png_init_io(ptr, file.fd)
    def get_channels(info: Info): Int         = lib.png_get_channels(ptr, info.ptr).toInt
    def get_image_width(info: Info): Int      = lib.png_get_image_width(ptr, info.ptr).toInt
    def get_image_height(info: Info): Int     = lib.png_get_image_height(ptr, info.ptr).toInt
    def get_bit_depth(info: Info): Int        = lib.png_get_bit_depth(ptr, info.ptr).toInt
    def get_color_type(info: Info): ColorType = lib.png_get_color_type(ptr, info.ptr)
  }

  implicit class Info private[libpng] (val ptr: lib.png_infop) extends AnyVal {
    //
  }

  implicit class PNGFILE private[libpng] (val fd: lib.png_FILE_p) extends AnyVal {
    def close(): Unit = fclose(fd)
  }

  implicit class RawImageBuffer private[libpng] (val ptr: Ptr[lib.png_byte]) {
    def getGray(x: Int, y: Int, width: Int, height: Int): Int = ptr(x + height * (y + 1) * width).toInt & 0xFF

    def getGA(x: Int, y: Int, width: Int, height: Int): Int = {
      val p = ptr + x * 2 + height * (y + 1) * width

      (!p << 8 | p(1)).toInt & 0xFFFF
    }

    def getRGB(x: Int, y: Int, width: Int, height: Int): Int = {
      val p = ptr + x * 3 + height * (y + 1) * width

      (!p << 16 | (p(1) << 8) | p(2)).toInt & 0xFFFFFF
    }
  }

  implicit class ColorType private[libpng] (val typ: lib.png_byte) extends AnyVal
  lazy val PNG_COLOR_TYPE_GRAY: ColorType       = ColorType(lib.PNG_COLOR_TYPE_GRAY)
  lazy val PNG_COLOR_TYPE_PALETTE: ColorType    = ColorType(lib.PNG_COLOR_TYPE_PALETTE)
  lazy val PNG_COLOR_TYPE_RGB: ColorType        = ColorType(lib.PNG_COLOR_TYPE_RGB)
  lazy val PNG_COLOR_TYPE_RGB_ALPHA: ColorType  = ColorType(lib.PNG_COLOR_TYPE_RGB_ALPHA)
  lazy val PNG_COLOR_TYPE_GRAY_ALPHA: ColorType = ColorType(lib.PNG_COLOR_TYPE_GRAY_ALPHA)
  lazy val PNG_COLOR_TYPE_RGBA: ColorType       = ColorType(lib.PNG_COLOR_TYPE_RGBA)
  lazy val PNG_COLOR_TYPE_GA: ColorType         = ColorType(lib.PNG_COLOR_TYPE_GA)

  def access_version_number: String = {
    val v = lib.png_access_version_number.toInt

    s"${v / 10000}.${(v % 10000) / 100}.${v % 100}"
  }

  def sig_cmp(sig: collection.Seq[Byte]): Boolean = {
    val count = sig.length min 8 toUInt
    val a     = stackalloc[Byte](count)

    copy(sig, a, count)
    bool(lib.png_sig_cmp(a, 0.toUInt, count.toUInt))
  }

  // todo: waiting for callbacks to be fixed
  def create_read_struct(user_png_ver: String): Option[PNG] =
    Zone(implicit z => Option(lib.png_create_read_struct(toCString(user_png_ver), null, null, null)))

  // header macros

  lazy val LIBPNG_VER_STRING: String = fromCString(lib.PNG_LIBPNG_VER_STRING)

  // convenience methods

  def open(path: String): Option[PNGFILE] = Zone { implicit z =>
    val file =
      fopen(toCString(path), c"r") match {
        case null =>
          Console.err.println(s"open: error opening file '$path'")
          return None
        case f => f
      }

    val header = stackalloc[Byte](8)

    fread(header, sizeof[Byte], 8.toUInt, file).toInt match {
      case 8 =>
        if (lib.png_sig_cmp(header, 0.toUInt, 8.toUInt) != 0) {
          Console.err.println(s"open: can't recognize PNG signature for file '$path'")
          return None
        }
      case _ =>
        Console.err.println(
          if (ferror(file) != 0) s"open: error reading file '$path'" else s"open: '$path' not a PNG file")
        return None
    }

    Some(file)
  }

}
