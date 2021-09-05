package io.github.edadma.libpng

import scala.scalanative.unsafe._

@link("png")
@extern
object LibPNG {

  type png_const_bytep    = Ptr[Byte]
  type png_uint_32        = CUnsignedInt
  type png_struct         = CStruct0
  type png_structp        = Ptr[png_struct]
  type png_structpp       = Ptr[png_structp]
  type png_structrp       = Ptr[png_struct]
  type png_const_structrp = Ptr[png_struct]
  type png_voidp          = Ptr[Unit]
  type png_const_charp    = CString
  type png_error_ptr      = CFuncPtr2[png_structp, png_const_charp, Unit]

  def png_access_version_number: CUnsignedLong = extern

  def png_sig_cmp(sig: png_const_bytep, start: CSize, num_to_check: CSize): CInt = extern

  def png_create_read_struct(user_png_ver: CString,
                             error_ptr: png_voidp,
                             error_fn: png_error_ptr,
                             warn_fn: png_error_ptr): png_structp = extern

  def png_create_write_struct(user_png_ver: CString,
                              error_ptr: png_voidp,
                              error_fn: png_error_ptr,
                              warn_fn: png_error_ptr): png_structp = extern

  def png_get_compression_buffer_size(png_ptr: png_const_structrp): CSize = extern

  def png_set_compression_buffer_size(png_ptr: png_const_structrp, size: CSize): Unit = extern

  // header macros

  @name("png_PNG_LIBPNG_VER_STRING")
  def PNG_LIBPNG_VER_STRING: CString = extern

}
