import scala.scalanative.libc.stdio.FILE
import scala.scalanative.unsafe.{CInt, CLongLong, CString, Ptr, extern}

@extern
object ExternApi {
  def add3(in: CLongLong): CLongLong = extern
  def fgets(str: CString, count: CInt, stream: Ptr[FILE]): CString = extern
}