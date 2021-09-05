package io.github.edadma.libpng

import scala.scalanative.unsafe._

@link("png")
@extern
object LibPNG {

  type png_const_bytep = Ptr[Byte]
  type png_uint_32     = CUnsignedInt

  def png_access_version_number: CUnsignedLong = extern

  def png_sig_cmp(sig: png_const_bytep, start: CSize, num_to_check: CSize): CInt = extern

  @name("png_PNG_LIBPNG_VER_STRING")
  def PNG_LIBPNG_VER_STRING: CString = extern

}
