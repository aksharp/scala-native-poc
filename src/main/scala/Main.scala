import scala.scalanative.libc.stdio.{FILE, stdin}
import scalanative.unsafe._
import scalanative.libc._
import scala.scalanative.unsigned._
import ExternApi._

object Main {

  def parseNumbers(): Unit = {
    val line_in_buffer = stackalloc[Byte](1024)
    while (stdio.fgets(line_in_buffer, 1023, stdio.stdin) != null) {
      parse_int_line(line_in_buffer)
    }

    def parse_int_line(line: CString): Int = {
      val int_pointer: Ptr[Int] = stackalloc[Int]()
      val scan_result = stdio.sscanf(line, c"%d\n", int_pointer)
      if (scan_result == 0) {
        throw new Exception("parse error in sscanf")
      }
      stdio.printf(c"read value %d into address %p\n", !int_pointer, int_pointer)
      val int_value: Int = !int_pointer
      int_value
    }

    println("done")
  }

  def helloWorld() = {
    val res = add3(-3L)
    assert(res == 0L)
    println(s"Add3 to -3 = $res")
    stdio.printf(c"hello native %s!\n", c"world")



    //    val str:CString = c"hello, world"
    //    val str_len: CSize = string.strlen(str)
    //    stdio.printf(c"the string '%s' at address %p is %d bytes long\n", str, str, str_len)
    //    stdio.printf(c"the CString value 'str' itself is %d bytes long\n", sizeof[CString])


    val str: Ptr[Byte] = c"hello, world"
    val str_len = string.strlen(str)
    stdio.printf(c"the string '%s' at address %p is %d bytes long\n", str, str, str_len)
    stdio.printf(c"the Ptr[Byte] value 'str' itself is %d bytes long\n", sizeof[CString])

    for (offset <- 0L to str_len.toLong) {
      val chr_addr: Ptr[Byte] = str + offset
      val chr: CChar = !chr_addr // str(offset)
      //      stdio.printf(c"""the character '%c' is %d bytes long and has binary value %d\n""", chr, sizeof[CChar], chr)
      stdio.printf(c"'%c'\t(%d) at address %p is %d bytes long\n", chr, chr, chr_addr, sizeof[CChar])
    }
  }

  def parseText() = {
    val line_in_buffer = stackalloc[Byte](1024)
    val word_out_buffer = stackalloc[Byte](32)
    while (stdio.fgets(line_in_buffer, 1023, stdio.stdin) != null) {
      parseLine(line_in_buffer, word_out_buffer, 32)
      stdio.printf(c"read word: '%s'\n", word_out_buffer)
    }

    def parseLine(line: CString, word_out: CString, buffer_size: Int): Unit = {
      val temp_buffer = stackalloc[Byte](1024)
      val max_word_length: CInt = buffer_size - 1
      val scanResult = stdio.sscanf(line, c"%1023s\n", temp_buffer)
      if (scanResult < 1) {
        throw new Exception(s"bad scanf result: $scanResult")
      }
      val word_length: CSize = string.strlen(temp_buffer)
      if (word_length.toInt >= max_word_length) {
        throw new Exception(
          s"word length $word_length exceeds max buffer size $buffer_size")
      }
      string.strncpy(word_out, temp_buffer, word_length)
    }
  }

  def main(args: Array[String]): Unit = {
    parse_a_native()
  }

  /*
   max count: and, 2008; 470825580 occurrences
  829739 ms elapsed total.
  [success] Total time: 832 s (13:52), completed Jan 28, 2022, 3:33:09 PM
  * */
  def parse_a() = {
    var max = 0
    var max_word = ""
    var max_year = 0
    println("reading from STDIN")
    val read_start = System.currentTimeMillis()
    var lines_read = 0
    for (line <- scala.io.Source.stdin.getLines) {
      val split_fields = line.split("\\s+")
      if (split_fields.size != 4) {
        throw new Exception("Parse Error")
      }

      val word = split_fields(0)
      val year = split_fields(1).toInt
      val count = split_fields(2).toInt
      if (count > max) {
        println(s"found new max: $word $count $year")
        max = count
        max_word = word
        max_year = year
      }
      lines_read += 1
      if (lines_read % 5000000 == 0) {
        val elapsed_now = System.currentTimeMillis() - read_start
        println(s"read $lines_read lines in $elapsed_now ms")
      }
    }
    val read_done = System.currentTimeMillis() - read_start
    println(s"max count: ${max_word}, ${max_year}; ${max} occurrences")
    println(s"$read_done ms elapsed total.")
  }

  /*
  done. read 86618505 lines
  maximum word count: 470825580 for 'and' @ 2008
  ./native3-out < googlebooks-eng-all-1gram-20120701-a  49.02s user 1.28s system 100% cpu 50.125 total
  */
  def parse_a_native() = {
    var max_word: Ptr[Byte] = stackalloc[Byte](1024)
    val max_count = stackalloc[Int]()
    val max_year = stackalloc[Int]()
    val line_buffer = stackalloc[Byte](1024)
    val temp_word = stackalloc[Byte](1024)
    val temp_count = stackalloc[Int]()
    val temp_year = stackalloc[Int]()
    val temp_doc_count = stackalloc[Int]()
    var lines_read = 0
    !max_count = 0
    !max_year = 0
    val max_word_buffer_size: Int = 1024
    val max_word_buffer_size_csize: CSize = max_word_buffer_size.toULong

    def parse_and_compare(line_buffer: CString, max_word: CString, temp_word: CString, max_word_buffer_size: CSize,
                          max_count: Ptr[Int], temp_count: Ptr[Int], max_year: Ptr[Int],
                          temp_year: Ptr[Int], temp_doc_count: Ptr[Int]
                         ): Unit = {
      val scan_result = stdio.sscanf(line_buffer, c"%1023s %d %d %d\n", temp_word, temp_year, temp_count, temp_doc_count)
      if (scan_result < 4) {
        throw new Exception("bad input")
      }
      if (!temp_count <= !max_count) {
        return
      } else {
        stdio.printf(c"saw new max: %s %d occurences at year %d\n", temp_word, !temp_count, !temp_year)
        val word_length: CSize = string.strlen(temp_word)
        if (word_length >= (max_word_buffer_size)) {
          throw new Exception(
            s"length $word_length exceeded buffer size $max_word_buffer_size")
        }
        string.strncpy(max_word, temp_word, max_word_buffer_size)
        !max_count = !temp_count
        !max_year = !temp_year
      }

//      if (!temp_count > !max_count) {
//        stdio.printf(c"saw new max: %s %d occurences at year %d\n", temp_word, !temp_count, !temp_year)
//        val word_length: CSize = string.strlen(temp_word)
//        if (word_length >= (max_word_buffer_size)) {
//          throw new Exception(
//            s"length $word_length exceeded buffer size $max_word_buffer_size")
//        }
//        string.strncpy(max_word, temp_word, max_word_buffer_size)
//        !max_count = !temp_count
//        !max_year = !temp_year
//      }
    }

    while (stdio.fgets(line_buffer, 1024, stdio.stdin) != null) {
      lines_read += 1
      parse_and_compare(line_buffer, max_word, temp_word, max_word_buffer_size_csize, max_count, temp_count, max_year, temp_year, temp_doc_count)
    }
    stdio.printf(c"done. read %d lines\n", lines_read)
    stdio.printf(c"maximum word count: %d for '%s' @ %d\n", !max_count, max_word, !max_year)
  }


}