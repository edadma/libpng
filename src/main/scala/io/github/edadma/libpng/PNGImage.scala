package io.github.edadma.libpng

import io.github.edadma.libpng.extern.{LibPNG => lib}

import scala.scalanative.unsigned._

class PNGImage /*private[libpng]*/ (private[libpng] val ptr: lib.png_bytep,
                                    val width: Int,
                                    val height: Int,
                                    val format: ImageFormat) {
  @inline def px(x: Int, y: Int): lib.png_bytep                = ptr + x * format.typ + height * (y + 1) * width
  @inline def pxget(x: Int, y: Int, offset: Int): Int          = (!(px(x, y) + offset)).toInt & 0xFF
  @inline def pxset(x: Int, y: Int, offset: Int, v: Int): Unit = !(px(x, y) + offset) = v.toUByte

  def getGray(x: Int, y: Int): Int = pxget(x, y, 0)

  def setGray(x: Int, y: Int, v: Int): Unit = pxset(x, y, 0, v)

  def getGA(x: Int, y: Int): Int = {
    val p = px(x, y)

    (!p << 8 | p(1)).toInt & 0xFFFF
  }

  def getGAGray(x: Int, y: Int): Int = pxget(x, y, 0)

  def getGAAlpha(x: Int, y: Int): Int = pxget(x, y, 1)

  def getRGB(x: Int, y: Int): Int = {
    val p = px(x, y)

    (!p << 16 | p(1) << 8 | p(2)).toInt & 0xFFFFFF
  }

  def getRGBRed(x: Int, y: Int): Int = pxget(x, y, 0)

  def getRGBGreen(x: Int, y: Int): Int = pxget(x, y, 1)

  def getRGBBlue(x: Int, y: Int): Int = pxget(x, y, 2)

  def getRGBA(x: Int, y: Int): Int = {
    val p = px(x, y)

    (!p << 24 | p(1) << 16 | p(2) << 8 | p(3)).toInt
  }

  def getRGBARed(x: Int, y: Int): Int = pxget(x, y, 0)

  def setRGBARed(x: Int, y: Int, v: Int): Unit = pxset(x, y, 0, v)

  def getRGBAGreen(x: Int, y: Int): Int = pxget(x, y, 1)

  def setRGBAGreen(x: Int, y: Int, v: Int): Unit = pxset(x, y, 1, v)

  def getRGBABlue(x: Int, y: Int): Int = pxget(x, y, 2)

  def setRGBABlue(x: Int, y: Int, v: Int): Unit = pxset(x, y, 2, v)

  def getRGBAAlpha(x: Int, y: Int): Int = pxget(x, y, 3)

  def setRGBAAlpha(x: Int, y: Int, v: Int): Unit = pxset(x, y, 3, v)

}
