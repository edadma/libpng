package io.github.edadma

import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.collection.mutable
import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object libpng {

  implicit class PNG private[libpng] (val png: lib.png_structp) extends AnyVal {
    def set_sig_bytes(num_bytes: Int): Unit = lib.png_set_sig_bytes(png, num_bytes)
    def create_info_struct: Info            = lib.png_create_info_struct(png)
    def init_io(file: PNGFILE): Unit        = lib.png_init_io(png, file.fd)
  }

  implicit class Info private[libpng] (val info: lib.png_infop) extends AnyVal {
    //
  }

  implicit class PNGFILE private[libpng] (val fd: lib.png_FILE_p) extends AnyVal {
    def close(): Unit = fclose(fd)
  }

  private def copy(src: collection.Seq[Byte], dst: Ptr[Byte], count: UInt): Unit =
    for (i <- 0 until count.toInt)
      dst(i) = src(i)

  private def copy(src: Ptr[Byte], dst: mutable.Seq[Byte], count: Int): Unit =
    for (i <- 0 until count)
      dst(i) = src(i)

  private def bool(a: CInt): Boolean = if (a == 0) false else true

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
  def create_read_struct(user_png_ver: String): PNG =
    Zone(implicit z => lib.png_create_read_struct(toCString(user_png_ver), null, null, null))

  // header macros
  def LIBPNG_VER_STRING: String = fromCString(lib.PNG_LIBPNG_VER_STRING)

  // convenience methods

  def open(path: String): (PNG, Info, PNGFILE) = {
    null
  }

}
