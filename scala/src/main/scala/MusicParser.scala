import java.io.{PushbackReader, StringReader}
import Musica._
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

case class Note(name: String)

object RegexParser {
  def parse(input: String): String = {
    val pattern = """(\d{1,})x\Q(\E((\w|\s)+)\Q)\E""".r
    val output = pattern.replaceAllIn(input, m => (m.group(2) + " ") * m.group(1).toInt) //Esta expresión hace los reemplazos
                 .replaceAll("( )+", " ") //Esta simplemente remueve los espacios dobles
    pattern.findFirstIn(output) match { //Si es necesario sigo llamando recursivamente a la función
      case Some(i) => return this.parse(output)
      case None => return output.trim
    }
  }
}

class MusicParser(input: String) {
  protected val inputStream = new PushbackReader(new StringReader(RegexParser.parse(input)))

  protected def parseChar(): Char = {
    val parsed = inputStream.read()
    if (parsed == -1) throw new EOIParserException
    return parsed.toChar
  }

  protected def parseNote(): Nota = {
    var next: Char = ' '
    do next = parseChar() while (next == ' ')
    Nota.notas.find(_.toString == next.toString()).getOrElse(throw new NotANoteException(next))
  }

  def parse(): List[Nota] = {
    val result: ListBuffer[Nota] = ListBuffer()
    try while (true)
      result += parseNote()
    catch {
      case _: EOIParserException =>
    }
    return result.toList
  }
}

class ParserException(reason: String) extends Exception(reason)
class EOIParserException extends ParserException("reached end of input")
class NotANoteException(val read: Char) extends ParserException(s"Expected [A|B|C|D|E|F|G] but got $read")