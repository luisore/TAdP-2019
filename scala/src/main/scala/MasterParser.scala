sealed trait ParseResult

case class ParseSuccess(head: Char, tail: String) extends ParseResult
case object ParseFail extends ParseResult

object MasterParser extends App {

  //Parsers básicos
  def digit(): (String) => ParseResult = {
    (input: String) => {
      input.head match {
        case a if a.isDigit => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
  }

  def char(aChar: Char): (String) => ParseResult = {
    (input: String) => {
      input.head match {
        case a if a == aChar => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
  }


  //Combinators
  implicit class ORCombinator(val before: String => ParseResult) extends AnyVal {
    def <|>(after: String => ParseResult): String => ParseResult = {
      (input: String) => {
        before(input) match {
          case ParseFail => after(input)
          case _ => before(input)
        }
      }
    }
  }

  implicit class RightmostCombinator(val before: String => ParseResult) extends AnyVal {
    def ~>(after: String => ParseResult): String => ParseResult = {
      (input: String) => {
        val res = before(input)
        res match {
          case ParseSuccess(h, t) => after(t)
          case ParseFail => res
        }
      }
    }
  }

  val parserJ = char('j')
  val parserI = char('i')
  val parserA = char('a')
  val JoI = parserJ <|> parserI
  val JoIoA = parserJ <|> parserI <|> parserA
  println(JoI("ijaaah"))  //ParseSuccess(i,jaaah)
  println(JoI("jijaaah")) //ParseSuccess(j,ijaaah)
  println(JoI("aaah"))    //ParseFail
  println(JoIoA("aaah"))  //ParseSuccess(a,aah)

  val JrightI = parserJ ~> parserI
  println(JrightI("jijaaah")) //ParseSuccess(i,jaaah)
}