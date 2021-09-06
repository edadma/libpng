package io.github.edadma

import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.collection.mutable
import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object libpng {

  private def copy(src: collection.Seq[Byte], dst: Ptr[lib.png_byte], count: UInt): Unit =
    for (i <- 0 until count.toInt)
      dst(i) = src(i).toUByte

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

  implicit class RawImageBuffer private[libpng] (val ptr: lib.png_bytep) {
    @inline def px(x: Int, y: Int, w: Int, h: Int, format: Int): lib.png_bytep =
      ptr + x * format + h * (y + 1) * w
    @inline def pxget(x: Int, y: Int, w: Int, h: Int, format: Int, offset: Int = 0): Int =
      (!(px(x, y, w, h, format) + offset)).toInt & 0xFF
    @inline def pxset(x: Int, y: Int, w: Int, h: Int, format: Int, v: Int, offset: Int = 0): Unit =
      !(px(x, y, w, h, format) + offset) = v.toUByte

    def getGray(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 1)

    def setGray(x: Int, y: Int, w: Int, h: Int, v: Int): Unit = pxset(x, y, w, h, 1, v)

    def getGA(x: Int, y: Int, w: Int, h: Int): Int = {
      val p = px(x, y, w, h, 2)

      (!p << 8 | p(1)).toInt & 0xFFFF
    }

    def getGAGray(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 2)

    def getGAAlpha(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 2, 1)

    def getRGB(x: Int, y: Int, w: Int, h: Int): Int = {
      val p = px(x, y, w, h, 3)

      (!p << 16 | p(1) << 8 | p(2)).toInt & 0xFFFFFF
    }

    def getRGBRed(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 3)

    def getRGBGreen(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 3, 1)

    def getRGBBlue(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 3, 2)

    def getRGBA(x: Int, y: Int, w: Int, h: Int): Int = {
      val p = ptr + x * 4 + h * (y + 1) * w

      (!p << 24 | p(1) << 16 | p(2) << 8 | p(3)).toInt
    }

    def getRGBARed(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 4)

    def getRGBAGreen(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 4, 1)

    def getRGBABlue(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 4, 2)

    def getRGBAAlpha(x: Int, y: Int, w: Int, h: Int): Int = pxget(x, y, w, h, 4, 3)
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
    val a     = stackalloc[lib.png_byte](count)

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

    val header = stackalloc[lib.png_byte](8)

    fread(header.asInstanceOf[Ptr[Byte]], sizeof[Byte], 8.toUInt, file).toInt match {
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
