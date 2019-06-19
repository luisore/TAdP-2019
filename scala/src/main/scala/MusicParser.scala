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

object Parser{
  def anychar(palabra: String) = {
    if(palabra != ""){
    ( palabra.head , palabra.tail )
    }else{
      throw new RuntimeException("error de parseo")
    }
    
  }
  def char(palabra: String , letra: Char) = {
    if(palabra.head == letra)
      this.anychar(palabra)
  }
  def digit(palabra: String) = {
    palabra.head match{
      case '0' => ( palabra.head , palabra.tail )
      case '1' => ( palabra.head , palabra.tail )
      case '2' => ( palabra.head , palabra.tail )
      case '3' => ( palabra.head , palabra.tail )
      case '4' => ( palabra.head , palabra.tail )
      case '5' => ( palabra.head , palabra.tail )
      case '6' => ( palabra.head , palabra.tail )
      case '7' => ( palabra.head , palabra.tail )
      case '8' => ( palabra.head , palabra.tail )
      case '9' => ( palabra.head , palabra.tail )
      case _ => throw new RuntimeException("error de parseo")
    }
  }
  def void(palabra: String) = ("",palabra.tail)
  def letter(palabra: String) ={
    if(palabra.head.isLetter){
      (palabra.head , palabra.tail)
    }else{
       throw new RuntimeException("error de parseo")
    }  
  }
  def alphaNum(palabra: String) ={
    if(palabra.head.isLetterOrDigit){
      (palabra.head , palabra.tail)
    }else{
       throw new RuntimeException("error de parseo")
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