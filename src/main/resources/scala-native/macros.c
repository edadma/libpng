#include <png.h>
#include <setjmp.h>

char* PNG_LIBPNG_VER_STRING() { return PNG_LIBPNG_VER_STRING; }

int png_setjmp(png_structp png_ptr) { return setjmp(png_jmpbuf(png_ptr); }

int PNG_COLOR_TYPE_GRAY() { return PNG_COLOR_TYPE_GRAY; }
int PNG_COLOR_TYPE_PALETTE() { return PNG_COLOR_TYPE_PALETTE; }
int PNG_COLOR_TYPE_RGB() { return PNG_COLOR_TYPE_RGB; }
int PNG_COLOR_TYPE_RGB_ALPHA() { return PNG_COLOR_TYPE_RGB_ALPHA; }
int PNG_COLOR_TYPE_GRAY_ALPHA() { return PNG_COLOR_TYPE_GRAY_ALPHA; }
int PNG_COLOR_TYPE_RGBA() { return PNG_COLOR_TYPE_RGBA; }
int PNG_COLOR_TYPE_GA() { return PNG_COLOR_TYPE_GA; }

int PNG_INFO_tRNS() { return PNG_INFO_tRNS; }
