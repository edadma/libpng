libpng
======

![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/edadma/libpng?include_prereleases) ![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/edadma/libpng) ![GitHub last commit](https://img.shields.io/github/last-commit/edadma/libpng) ![GitHub](https://img.shields.io/github/license/edadma/libpng)

*libpng* provides Scala Native bindings for the [libpng](http://www.libpng.org/) C library for reading and writing PNGs.

Overview
--------

The goal of this project is to provide an easy-to-use Scala Native facade for the majority *libpng*, the official PNG reference library.  Currently, many of the functions needed to read and write PNGs are covered.  Also, convenience methods are provided to read into and write from an image buffer.  The simplified API offered by the C library has not yet been covered, but is planned.

The more "programmer friendly" part of this library is found in the `io.github.edadma.libpng` package.  That's the only package you need to import from, as seen in the example below.  The other package in the library is `io.github.edadma.libpng.extern` which provides for interaction with the libpng C library using Scala Native interoperability elements from the so-call `unsafe` namespace.  There are no public declarations in the `io.github.edadma.libpng` package that use `unsafe` types in their parameter or return types, making it a pure Scala facade.  Consequently, you never have to worry about memory allocation or type conversions.

Usage
-----

To use this library, `libpng-dev` needs to be installed:

```shell
sudo apt install libpng-dev
```

Include the following in your `project/plugins.sbt`:

```sbt
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")
```

Include the following in your `build.sbt`:

```sbt
resolvers += Resolver.githubPackages("edadma")

libraryDependencies += "io.github.edadma" %%% "libpng" % "0.1.0"
```

Use the following `import` in your code:

```scala
import io.github.edadma.libpng._
```

Example
-------

The following example shows the use of convenience methods to read and write a PNG file to and from an image buffer.  The example reads a PNG image file `image.png`, and modifies it, pixel by pixel, to remove all the red from it and to make the amount of green at each pixel equal to the amount of blue.  The image is then written to a new PNG file `new.png`.

```scala
import io.github.edadma.libpng._

object Main extends App {

  val image = read("image.png")

  if (image.format != ImageFormat.RGBA)
    sys.error("can only process RGBA type files")

  for (i <- 0 until image.width; j <- 0 until image.height) {
    image.setRGBARed(i, j, 0)
    image.setRGBAGreen(i, j, image.getRGBABlue(i, j))
  }

  write("new.png", image)

}
```

As to performance, if you use `nativeMode := "release-fast"` in your `build.sbt`, then this example runs as fast as the corresponding C program would.

Documentation
-------------

API documentation is forthcoming, however documentation for the C library is found [here](http://www.libpng.org/pub/png/libpng-manual.txt).

License
-------

[ISC](https://github.com/edadma/libpng/blob/main/LICENSE)
