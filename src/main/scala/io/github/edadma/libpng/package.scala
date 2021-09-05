package io.github.edadma

import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.collection.mutable
import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object libpng {

  implicit class PNG private[libpng] (val png: lib.png_structp) extends AnyVal {
    //
  }

  implicit class Info private[libpng] (val info: lib.png_infop) extends AnyVal {
    //
  }

  implicit class PNGFILE private[libpng] (val file: lib.png_FILE_p) extends AnyVal {
    def close(): Unit = fclose(file)
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

  // header macros
  def LIBPNG_VER_STRING: String = fromCString(lib.PNG_LIBPNG_VER_STRING)

  // convenience methods

  def open(path: String): (PNG, Info, PNGFILE) = {
    null
  }

}
