sealed trait ParseResult
case class ParseSuccess[a](head: a, tail: String) extends ParseResult
case object ParseFail extends ParseResult


//Objeto que wrappea la lógica de los parsers básicos y me permite implementar los avanzados
class ParserWrapper(callback: (String) => ParseResult) {

  //Esto se llama al ejecutar el wrapper como función
  def apply(input: String): ParseResult = {
    callback(input)
  }

  //Parsers avanzados
  def opt(): ParserWrapper = {
    val logic = (input: String) => {
      val res = callback(input)
      res match {
        case ParseSuccess(h, t) => res
        case _ => ParseSuccess(' ', input)
      }
    }
    new ParserWrapper(logic)
  }
}

object MasterParser extends App {

  //Parsers básicos
  def digit(): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case a if a.isDigit => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
    new ParserWrapper(logic)
  }

  def char(aChar: Char): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case a if a == aChar => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
    new ParserWrapper(logic)
  }
  
  def anychar(): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case "" => ParseFail
        case _  => ParseSuccess(input.head, input.tail) 
      }
    }
    new ParserWrapper(logic)
  }
  
  def letter(): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case a if a.isLetter => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
    new ParserWrapper(logic)
  }
  
  def alphaNum(): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case a if a.isLetterOrDigit => ParseSuccess(input.head, input.tail)
        case _ => ParseFail
      }
    }
    new ParserWrapper(logic)
  }
  def void(): ParserWrapper = {
    val logic = (input: String) => {
      input.head match {
        case "" => ParseFail
        case _  => ParseSuccess((),input.tail) 
      }
    }
    new ParserWrapper(logic)
  }


  //Combinators
  implicit class ORCombinator(val before: ParserWrapper) extends AnyVal {
    def <|>(after: ParserWrapper): ParserWrapper = {
      val logic = (input: String) => {
        before(input) match {
          case ParseFail => after(input)
          case _ => before(input)
        }
      }
      new ParserWrapper(logic)
    }
  }

  implicit class RightmostCombinator(val before: ParserWrapper) extends AnyVal {
    def ~>(after: ParserWrapper): ParserWrapper = {
      val logic = (input: String) => {
        val res = before(input)
        res match {
          case ParseSuccess(h, t) => after(t)
          case ParseFail => res
        }
      }
      new ParserWrapper(logic)
    }
  }
  
  

  val parserJ = char('j')
  val parserI = char('i')
  val parserA = char('a')
  val JoI = parserJ <|> parserI
  val JoIoA = parserJ <|> parserI <|> parserA
  var JoIoAOPT = JoIoA.opt
  println(JoI("ijaaah"))  //ParseSuccess(i,jaaah)
  println(JoI("jijaaah")) //ParseSuccess(j,ijaaah)
  println(JoI("aaah"))    //ParseFail
  println(JoIoA("aaah"))  //ParseSuccess(a,aah)
  println(JoIoA("xxx"))   //ParseFail
  println(JoIoAOPT("xxx"))//ParseSuccess( ,xxx)

  val JrightI = parserJ ~> parserI
  println(JrightI("jijaaah")) //ParseSuccess(i,jaaah)
}