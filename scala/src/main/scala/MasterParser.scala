sealed trait ParseResult
case class ParserSuccess[a](head: a, tail: String) extends ParseResult
case object ParserFailure extends ParseResult


//Objeto que wrappea la lógica de los parsers básicos y me permite implementar los avanzados
class ParserWrapper(callback: (String) => ParseResult) {

  //Esto se llama al ejecutar el wrapper como función
  def apply(input: String): ParseResult = {
    callback(input)
  }

  //Combinators

  //OrCombinator
  def <|>(after: ParserWrapper): ParserWrapper = {
    val logic = (input: String) => {
      this.apply(input) match {
        case ParserFailure => after(input)
        case a => a
      }
    }
    new ParserWrapper(logic)
  }

  //ConcatCombinator
  def <>(after: ParserWrapper): ParserWrapper = {
    val logic = (input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(h,t) => after(t) match {
          case ParserSuccess(h2,t2) => ParserSuccess((h, h2), t2)
          case _ => ParserFailure
        }
        case _ => res
      }
    }
    new ParserWrapper(logic)
  }

  //RightmostCombinator
  def ~>(after: ParserWrapper): ParserWrapper = {
    val logic = (input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(_, t) => after(t)
        case ParserFailure => res
      }
    }
    new ParserWrapper(logic)
  }

  //LeftmostCombinator
  def <~(after: ParserWrapper): ParserWrapper = {
    val logic = (input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(h,t) => after(t) match {
          case ParserSuccess(_,t2) => ParserSuccess(h, t2)
          case _ => ParserFailure
        }
        case _ => res
      }
    }
    new ParserWrapper(logic)
  }

  //Parsers avanzados
  def opt(): ParserWrapper = {
    val logic = (input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(_, _) => res
        case _ => ParserSuccess[Unit]((), input.drop(1))
      }
    }
    new ParserWrapper(logic)
  }
}

object MasterParser extends App {

  //Parsers básicos
  def digit(): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(a) if a.isDigit => ParserSuccess[Char](a, input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }

  def char(aChar: Char): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(a) if a == aChar => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }
  
  def anychar(): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(a) => ParserSuccess[Char](a, input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }
  
  def letter(): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(a) if a.isLetter => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }
  
  def alphaNum(): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(a) if a.isLetterOrDigit => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }
  def void(): ParserWrapper = {
    val logic = (input: String) => {
      input.headOption match {
        case Some(_)  => ParserSuccess[Unit]((),input.drop(1))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }

  def string(aString: String): ParserWrapper = {
    val logic = (input: String) => {
      input.substring(0, Math.min(input.length, aString.length)) match {
        case a if a == aString => ParserSuccess[String](a,input.drop(aString.length))
        case _ => ParserFailure
      }
    }
    new ParserWrapper(logic)
  }
  

  val parserJ = char('j')
  val parserI = char('i')
  val parserA = char('a')
  val saludo = string("hola")
  val JoI = parserJ <|> parserI
  val JoIoA = parserJ <|> parserI <|> parserA
  val JoIoAOPT = JoIoA.opt
  val JyI = parserJ <> parserI
  val JyIyA = JyI <> parserA
  val JrightI = parserJ ~> parserI
  val JleftI = parserJ <~ parserI
  println(JoI("ijaaah"))  //ParserSuccess(i,jaaah)
  println(JoI("jijaaah")) //ParserSuccess(j,ijaaah)
  println(JoI("aaah"))    //ParserFailure
  println(JoIoA("aaah"))  //ParserSuccess(a,aah)
  println(JoIoA("xxx"))   //ParserFailure
  println(JoIoAOPT("xxx"))//ParserSuccess((),xxx)
  println(saludo("!hola mundo")) //ParserFailure
  println(saludo("hola mundo")) //ParserSuccess(hola, mundo)
  println(JrightI("jijaaah")) //ParserSuccess(i,jaaah)
  println(JleftI("jijaaah")) //ParserSuccess(j,jaaah)
  println(JyI("jojo")) //ParserFailure
  println(JyI("jiji")) //ParserSuccess((j,i),ji)
  println(JyIyA("jio")) //ParserFailure
  println(JyIyA("jia")) //ParserSuccess(((j,i),a),)
}