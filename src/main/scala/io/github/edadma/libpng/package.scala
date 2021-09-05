package io.github.edadma

import io.github.edadma.libpng.{LibPNG => lib}

import scala.collection.mutable
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

package object libpng {

  private def copy(src: collection.Seq[Byte], dst: Ptr[Byte], count: UInt): Unit = {
    for (i <- 0 until count.toInt)
      dst(i) = src(i)
  }

  private def copy(src: Ptr[Byte], dst: mutable.Seq[Byte], count: Int): Unit = {
    for (i <- 0 until count)
      dst(i) = src(i)
  }

  private def bool(a: CInt): Boolean = if (a == 0) false else true

  def png_access_version_number: String = {
    val v = lib.png_access_version_number.toInt

    s"${v / 10000}.${(v % 10000) / 100}.${v % 100}"
  }

  def png_sig_cmp(sig: collection.Seq[Byte]): Boolean = {
    val count = sig.length min 8 toUInt
    val a     = stackalloc[Byte](count)

    copy(sig, a, count)
    bool(lib.png_sig_cmp(a, 0.toUInt, count.toUInt))
  }

  def PNG_LIBPNG_VER_STRING: String = fromCString(lib.PNG_LIBPNG_VER_STRING)

}
