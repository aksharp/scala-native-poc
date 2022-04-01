import scala.scalanative.build._

scalaVersion := "3.1.1"

// sbt nativeLink  for .out file

// Set to false or remove if you want to show stubs as linking errors
nativeLinkStubs := true

enablePlugins(ScalaNativePlugin)

//    .withTargetTriple("x86_64-apple-macosx10.14.0")
nativeConfig ~= {
  _.withLTO(LTO.thin)
    .withMode(Mode.releaseFast)
    .withGC(GC.commix)
}