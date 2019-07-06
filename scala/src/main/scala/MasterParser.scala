package object MasterParser {
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
    private def recursiveParsing[A](prevList: List[A], input: String): ParserResult = {
      this.apply(input) match {
        //Este primer case lo agrego para que la lista quede con el tipo de h y no any, pero no se si funca
        case ParserSuccess(h,t) if prevList.isEmpty => this.recursiveParsing(List(h), t)
        case ParserSuccess(h,t) => this.recursiveParsing(prevList :+ h, t)
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
          case ParserSuccess(h,_) => this.recursiveParsing(List.empty[Any], input)
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

    def satisfies[A](condition: A => Boolean): ParserWrapper = {
      new ParserWrapper((input: String) => {
        val res = this.apply(input)
        res match {
          case ParserSuccess(h: A, _) if condition(h) => res
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

    def map (tFunction: PartialFunction[Any, Any]): ParserWrapper = {
      new ParserWrapper((input: String) => {
        this.apply(input) match {
          case ParserSuccess(h, t) if tFunction.isDefinedAt(h) => ParserSuccess(tFunction(h), t)
          case _ => ParserFailure
        }
      })
    }
  }



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
}