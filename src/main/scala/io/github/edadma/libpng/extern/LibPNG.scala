package io.github.edadma.libpng.extern

import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._

@link("png")
@extern
object LibPNG {

  type png_byte           = CUnsignedChar
  type png_bytep          = Ptr[Byte]
  type png_bytepp         = Ptr[png_bytep]
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
  type png_info           = CStruct0
  type png_infop          = Ptr[png_info]
  type png_const_inforp   = Ptr[png_info]
  type png_FILE_p         = Ptr[FILE]

  def png_access_version_number: CUnsignedLong                                   = extern
  def png_set_sig_bytes(png_ptr: png_structrp, num_bytes: CInt): Unit            = extern
  def png_sig_cmp(sig: png_const_bytep, start: CSize, num_to_check: CSize): CInt = extern
  def png_create_read_struct(user_png_ver: CString,
                             error_ptr: png_voidp,
                             error_fn: png_error_ptr,
                             warn_fn: png_error_ptr): png_structp = extern
  def png_create_write_struct(user_png_ver: CString,
                              error_ptr: png_voidp,
                              error_fn: png_error_ptr,
                              warn_fn: png_error_ptr): png_structp                               = extern
  def png_get_compression_buffer_size(png_ptr: png_const_structrp): CSize                        = extern
  def png_set_compression_buffer_size(png_ptr: png_const_structrp, size: CSize): Unit            = extern
  def png_create_info_struct(png_ptr: png_const_structrp): png_infop                             = extern
  def png_read_info(png_ptr: png_structrp, info_ptr: png_infop): Unit                            = extern
  def png_read_update_info(png_ptr: png_structrp, info_ptr: png_infop): Unit                     = extern
  def png_set_interlace_handling(png_ptr: png_structrp): CInt                                    = extern
  def png_read_image(png_ptr: png_structrp, image: png_bytepp): Unit                             = extern
  def png_init_io(png_ptr: png_structrp, fp: png_FILE_p): Unit                                   = extern
  def png_get_channels(png_ptr: png_const_structrp, info_ptr: png_const_inforp): png_byte        = extern
  def png_get_image_width(png_ptr: png_const_structrp, info_ptr: png_const_inforp): png_uint_32  = extern
  def png_get_image_height(png_ptr: png_const_structrp, info_ptr: png_const_inforp): png_uint_32 = extern
  def png_get_bit_depth(png_ptr: png_const_structrp, info_ptr: png_const_inforp): png_byte       = extern
  def png_get_color_type(png_ptr: png_const_structrp, info_ptr: png_const_inforp): png_byte      = extern

  // header macros

  @name("png_PNG_LIBPNG_VER_STRING")
  def PNG_LIBPNG_VER_STRING: CString = extern

}
