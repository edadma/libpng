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

  private def bool(a: CUnsignedInt): Boolean = if (a == 0.toUInt) false else true

  implicit class PNG private[libpng] (val ptr: lib.png_structp) extends AnyVal {
    def setjmp: Boolean                           = bool(lib.png_setjmp(ptr))
    def set_sig_bytes(num_bytes: Int): Unit       = lib.png_set_sig_bytes(ptr, num_bytes)
    def create_info_struct: Option[Info]          = Option(lib.png_create_info_struct(ptr))
    def read_info(info: Info): Unit               = lib.png_read_info(ptr, info.ptr)
    def set_expand_gray_1_2_4_to_8(): Unit        = lib.png_set_expand_gray_1_2_4_to_8(ptr)
    def set_palette_to_rgb(): Unit                = lib.png_set_palette_to_rgb(ptr)
    def set_tRNS_to_alpha(): Unit                 = lib.png_set_tRNS_to_alpha(ptr)
    def set_strip_16(): Unit                      = lib.png_set_strip_16(ptr)
    def read_update_info(info: Info): Unit        = lib.png_read_update_info(ptr, info.ptr)
    def set_packing(): Unit                       = lib.png_set_packing(ptr)
    def set_interlace_handling: Int               = lib.png_set_interlace_handling(ptr)
    def init_io(file: PNGFile): Unit              = lib.png_init_io(ptr, file.fd)
    def get_valid(info: Info, flag: Int): Boolean = bool(lib.png_get_valid(ptr, info.ptr, flag.toUInt))

    def get_channels(info: Info): Int         = lib.png_get_channels(ptr, info.ptr).toInt
    def get_image_width(info: Info): Int      = lib.png_get_image_width(ptr, info.ptr).toInt
    def get_image_height(info: Info): Int     = lib.png_get_image_height(ptr, info.ptr).toInt
    def get_bit_depth(info: Info): Int        = lib.png_get_bit_depth(ptr, info.ptr).toInt
    def get_color_type(info: Info): ColorType = lib.png_get_color_type(ptr, info.ptr)
    def get_IHDR(info: Info): (Int, Int, Int, ColorType, Int, Int, Int) = {
      val width              = stackalloc[CUnsignedInt]
      val height             = stackalloc[CUnsignedInt]
      val bit_depth          = stackalloc[CInt]
      val color_type         = stackalloc[CInt]
      val interlace_method   = stackalloc[CInt]
      val compression_method = stackalloc[CInt]
      val filter_method      = stackalloc[CInt]

      lib.png_get_IHDR(ptr,
                       info.ptr,
                       width,
                       height,
                       bit_depth,
                       color_type,
                       interlace_method,
                       compression_method,
                       filter_method)

      ((!width).toInt,
       (!height).toInt,
       !bit_depth,
       ColorType(!color_type),
       !interlace_method,
       !compression_method,
       !filter_method)
    }
  }

  implicit class Info private[libpng] (val ptr: lib.png_infop) extends AnyVal {
    //
  }

  implicit class PNGFile private[libpng] (val fd: lib.png_FILE_p) extends AnyVal {
    def close(): Unit = fclose(fd)
  }

  implicit class ColorType private[libpng] (val typ: lib.png_byte) extends AnyVal
  lazy val COLOR_TYPE_GRAY: ColorType       = ColorType(lib.PNG_COLOR_TYPE_GRAY)
  lazy val COLOR_TYPE_PALETTE: ColorType    = ColorType(lib.PNG_COLOR_TYPE_PALETTE)
  lazy val COLOR_TYPE_RGB: ColorType        = ColorType(lib.PNG_COLOR_TYPE_RGB)
  lazy val COLOR_TYPE_RGB_ALPHA: ColorType  = ColorType(lib.PNG_COLOR_TYPE_RGB_ALPHA)
  lazy val COLOR_TYPE_GRAY_ALPHA: ColorType = ColorType(lib.PNG_COLOR_TYPE_GRAY_ALPHA)
  lazy val COLOR_TYPE_RGBA: ColorType       = ColorType(lib.PNG_COLOR_TYPE_RGBA)
  lazy val COLOR_TYPE_GA: ColorType         = ColorType(lib.PNG_COLOR_TYPE_GA)

  implicit class ImageFormat private[libpng] (val typ: Int) extends AnyVal
  object ImageFormat {
    val GRAY: ImageFormat       = ImageFormat(0)
    val GRAY_ALPHA: ImageFormat = ImageFormat(1)
    val RGB: ImageFormat        = ImageFormat(2)
    val RGB_ALPHA: ImageFormat  = ImageFormat(3)
  }

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

  lazy val INFO_tRNS: Int = lib.PNG_INFO_tRNS

  // convenience methods

  def open(path: String): Option[PNGFile] = Zone { implicit z =>
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
          fclose(file)
          return None
        }
      case _ =>
        Console.err.println(
          if (ferror(file) != 0) s"open: error reading file '$path'" else s"open: '$path' not a PNG file")
        fclose(file)
        return None
    }

    Some(file)
  }

}
