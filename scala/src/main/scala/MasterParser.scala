sealed trait ParserResult
case class ParserSuccess[a](head: a, tail: String) extends ParserResult
case object ParserFailure extends ParserResult


//Objeto que wrappea la lógica de los parsers básicos y me permite implementar los avanzados
class ParserWrapper(callback: (String) => ParserResult) {

  //Esto se llama al ejecutar el wrapper como función
  def apply(input: String): ParserResult = {
    callback(input)
  }

  //Combinators

  //OrCombinator
  def <|>(after: ParserWrapper): ParserWrapper = {
    new ParserWrapper((input: String) => {
      this.apply(input) match {
        case ParserFailure => after(input)
        case a => a
      }
    })
  }

  //ConcatCombinator
  def <>(after: ParserWrapper): ParserWrapper = {
    new ParserWrapper((input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(h,t) => after(t) match {
          case ParserSuccess(h2,t2) => ParserSuccess((h, h2), t2)
          case _ => ParserFailure
        }
        case _ => res
      }
    })
  }

  //RightmostCombinator
  def ~>(after: ParserWrapper): ParserWrapper = {
    new ParserWrapper((input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(_, t) => after(t)
        case ParserFailure => res
      }
    })
  }

  //LeftmostCombinator
  def <~(after: ParserWrapper): ParserWrapper = {
    new ParserWrapper((input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(h,t) => after(t) match {
          case ParserSuccess(_,t2) => ParserSuccess(h, t2)
          case _ => ParserFailure
        }
        case _ => res
      }
    })
  }

  //Métodos auxiliares
  private def recursiveParsing[a](prevList: List[a], input: String): ParserResult = {
    this.apply(input) match {
      case ParserSuccess(h,t) => this.recursiveParsing(prevList.::(h), t)
      case _ => ParserSuccess(prevList, input)
    }
  }

  //Parsers avanzados
  def *(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      this.recursiveParsing(List.empty[Any], input)
    })
  }

  def +(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      this.apply(input) match {
        case ParserSuccess(_,_) => this.recursiveParsing(List.empty[Any], input)
        case _ => ParserFailure
      }
    })
  }

  def opt(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(_, _) => res
        case _ => ParserSuccess[Unit]((), input)
      }
    })
  }

  def satisfies(condition: Any => Boolean): ParserWrapper = {
    new ParserWrapper((input: String) => {
      val res = this.apply(input)
      res match {
        case ParserSuccess(h, _) if condition(h) => res
        case _ => ParserFailure
      }
    })
  }

  def sepBy(sep: ParserWrapper): ParserWrapper = {
    (this <~ sep.opt).+
  }

  def const[a](value: a): ParserWrapper = {
    new ParserWrapper((input: String) => {
      this.apply(input) match {
        case ParserSuccess(_, t) => ParserSuccess(value, t)
        case _ => ParserFailure
      }
    })
  }
}

object MasterParser extends App {

  //Parsers básicos
  def digit(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) if a.isDigit => ParserSuccess[Char](a, input.drop(1))
        case _ => ParserFailure
      }
    })
  }

  def char(aChar: Char): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) if a == aChar => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    })
  }
  
  def anychar(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) => ParserSuccess[Char](a, input.drop(1))
        case _ => ParserFailure
      }
    })
  }
  
  def letter(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) if a.isLetter => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    })
  }
  
  def alphaNum(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) if a.isLetterOrDigit => ParserSuccess[Char](input.head, input.drop(1))
        case _ => ParserFailure
      }
    })
  }
  def void(): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(_)  => ParserSuccess[Unit]((),input.drop(1))
        case _ => ParserFailure
      }
    })
  }

  def string(aString: String): ParserWrapper = {
    new ParserWrapper((input: String) => {
      input.substring(0, Math.min(input.length, aString.length)) match {
        case a if a == aString => ParserSuccess[String](a,input.drop(aString.length))
        case _ => ParserFailure
      }
    })
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
  val parserJkleene = parserJ.*
  val parserJmas = parserJ.+
  val parserJmasConCondicion = parserJmas.satisfies((aList: Any) => {
    aList.isInstanceOf[List[_]] && aList.asInstanceOf[List[_]].length > 1
  })
  val saludoSep = saludo.sepBy(char('-'))
  val saludoSepMinion = saludoSep.const("BANANA")
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
  println(parserJkleene("xxxkkk")) //ParserSuccess(List(),xxxkkk)
  println(parserJkleene("jjjkkk")) //ParserSuccess(List(j, j, j),kkk)
  println(parserJmas("xxxkkk")) //ParserFailure
  println(parserJmas("jjjkkk")) //ParserSuccess(List(j, j, j),kkk)
  println(parserJmasConCondicion("jjjkkk")) //ParserSuccess(List(j, j, j),kkk)
  println(parserJmasConCondicion("jkkk")) //ParserFailure
  println(saludoSep("hola")) //ParserSuccess(List(hola),)
  println(saludoSep("hola-hola-hola-chau")) //ParserSuccess(List(hola, hola, hola),chau)
  println(saludoSep("hola hola hola chau")) //ParserSuccess(List(hola), hola hola chau)
  println(saludoSep("chau-hola")) //ParserFailure
  println(saludoSepMinion("hola chau")) //ParserSuccess(BANANA, chau)
}