package object MasterParser {
  // IMPORTANTE: El ParseResult necesita tener un tipo paramétrico (sino todos los resultados son iguales)
  // MUY Importante: Si no hago que A sea Covariante, ParserFailure no va a poder ser retornado para cualquier tipo de ParserResult
  sealed trait ParserResult[+A]
  // Generalmente se usan letras mayúsculas para definir tipos
  case class ParserSuccess[A](head: A, tail: String) extends ParserResult[A]
  case object ParserFailure extends ParserResult[Nothing]


  // Objeto que wrappea la lógica de los parsers básicos y me permite implementar los avanzados
  // IMPORTANTE: Dado que el resultado de parseo tiene un tipo paramétrico, ustedes tienen que definirlo también en el parser
  //  (porque a esta altura aún no saben que tipo de resultado va a parsear, eso depende de la función "callback")
  // IMPORTANTE Nº2: Necesito que A sea covariante para que pueda asignar parsers de tipos "más chicos" cuando espero parsers de tipos padres
  // ej:
  //  val sonido: Parser[Sonido] = ???
  //  val tocable: Parser[Tocable] = sonido
  // si A no fuera covariante, no puedo asignar "sonido" en "tocable"
  class ParserWrapper[+A](callback: (String) => ParserResult[A]) {

    //Esto se llama al ejecutar el wrapper como función
    def apply(input: String): ParserResult[A] = {
      callback(input)
    }

    //Combinators

    //OrCombinator
    // IMPORTANTE: Necesitan definir cual va a ser el tipo del resultado
    //  - El parser actual (this) tiene un tipo A
    //  - El parser "after" tiene un tipo B
    //  - No puedo retornar simplemente A o B porque el resultado tiene que ser el tipo común entre A y B (hacia arriba de la jerarquía de tipos)
    //  - Tengo que relacionar ambos tipos, entonces B tiene que ser un tipo "superior" / padre de A, entonces B es supertipo de A (B >: A)
    // Pueden ver un ejemplo de lo mismo en getOrElse de Option (ej. Some(1).getOrElse(123))
    def <|>[B >: A](after: ParserWrapper[B]): ParserWrapper[B] = {
      new ParserWrapper[B]((input: String) => {
        this.apply(input) match {
          case ParserFailure => after(input)
          case a => a
        }
      })
    }

    //ConcatCombinator
    // IMPORTANTE: Ahora este combinator tiene que retornar el tipo tupla de A y B
    def <>[B](after: ParserWrapper[B]): ParserWrapper[(A, B)] = {
      new ParserWrapper((input: String) => {
        this(input) match {
          case ParserSuccess(h,t) => after(t) match {
            // IMPORTANTE: No les parece que esta transformación es como un flatMap?
            // Piensen cómo implementar flatMap para los parsers
            case ParserSuccess(h2,t2) => ParserSuccess((h, h2), t2)
            case _ => ParserFailure
          }
          // ERROR: este último case no puede retornar "res" porque no es una tupla (aca pueden ver como los tipos te ayudan a prevenir errores de lógica)
          // Si uno de los dos parsers falla, debe fallar => si el primer parseo falla, debe retornar una falla
          case _ => ParserFailure
        }
      })
    }

    //RightmostCombinator
    // Este combinator descarta lo que consume "this" y retorna B
    def ~>[B](after: ParserWrapper[B]): ParserWrapper[B] = {
      new ParserWrapper((input: String) => {
        val res = this.apply(input)
        res match {
          case ParserSuccess(_, t) => after(t)
          // Nota: acá no pueden retornar "res" porque es de tipo "A", por más que ya sepan que fué un parser failure
          case ParserFailure => ParserFailure
        }
      })
    }

    //LeftmostCombinator
    def <~[B](after: ParserWrapper[B]): ParserWrapper[A] = {
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

    //Parsers avanzados
    // Retorna una lista de A
    def * : ParserWrapper[List[A]] = {

      // como A es covariante, hago que "recursiveParsing" sea una función interna al operador
      // Retorna lista de A
      def recursiveParsing(prevList: List[A], input: String): ParserResult[List[A]] = {
        this.apply(input) match {
          //Este primer case lo agrego para que la lista quede con el tipo de h y no any, pero no se si funca
          // case ParserSuccess(h,t) if prevList.isEmpty => this.recursiveParsing(List(h), t)

          // No es necesario, Nil o List.empty puede ser usado como "terminador" de listas:
          case ParserSuccess(h, t) => recursiveParsing(prevList :+ h, t)
          case _ => ParserSuccess(prevList, input)
        }
      }

      new ParserWrapper(input =>
        recursiveParsing(List.empty, input)
      )
    }

    def + : ParserWrapper[List[A]] = {
      new ParserWrapper((input: String) => {
        *(input) match {
          case ParserSuccess(Nil, _) => ParserFailure
          case res => res
        }
      })
    }

    // Mejora: Si usamos "Option" como resultado, podemos usar el valor final con tipo A o None si el parser falla
    def opt: ParserWrapper[Option[A]] = {
      new ParserWrapper((input: String) => {
        val res = this.apply(input)
        res match {
          case ParserSuccess(a, tail) => ParserSuccess(Some(a), tail)
          case _ => ParserSuccess(None, input)
        }
      })
    }

    // Nota: A es el valor que (potencialmente) va a parsear este parser, luego si "condition" da true para el valor, va a retornar un Success
    def satisfies(condition: A => Boolean): ParserWrapper[A] = {
      new ParserWrapper((input: String) => {
        val res = this.apply(input)
        res match {
          case ParserSuccess(h, _) if condition(h) => res
          case _ => ParserFailure
        }
      })
    }

    // Error de lógica, el sepBy no funciona como está definido aca, revisar la info del TP (y el test corregido)
    def sepBy[B](sep: ParserWrapper[B]): ParserWrapper[List[A]] = {
      (this <~ sep.opt).+
    }

    def const[B](value: B): ParserWrapper[B] = {
      this.map {case a => value}
    }

    // Nota: Por lo general, "map" está definido para usarse con funciones completas
    def map[B](tFunction: A => B): ParserWrapper[B] = {
      new ParserWrapper((input: String) => {
        this.apply(input) match {
          case ParserSuccess(h, t) => ParserSuccess(tFunction(h), t)
          case _ => ParserFailure
        }
      })
    }
  }



  //Parsers básicos
  val anychar: ParserWrapper[Char] = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(a) => ParserSuccess[Char](a, input.drop(1))
        case _ => ParserFailure
      }
    })
  }

  val digit: ParserWrapper[Char] = {
    anychar.satisfies((inputChar: Char) => {inputChar.isDigit})
  }

  //Modifico la definición para que soporte integers de más de un dígito
  val integer = digit.+.map(_.map(_.toString).mkString("").toInt)

  def char(aChar: Char): ParserWrapper[Char] = {
    anychar.satisfies((inputChar: Char) => {inputChar == aChar})
  }
  
  val letter: ParserWrapper[Char] = {
    anychar.satisfies((inputChar: Char) => {inputChar.isLetter})
  }

  val alphaNum = letter <|> digit

  val void: ParserWrapper[Unit] = {
    new ParserWrapper((input: String) => {
      input.headOption match {
        case Some(_)  => ParserSuccess[Unit]((),input.drop(1))
        case _ => ParserFailure
      }
    })
  }

  def string(aString: String): ParserWrapper[String] = {
    new ParserWrapper((input: String) => {
      input.substring(0, Math.min(input.length, aString.length)) match {
        case a if a == aString => ParserSuccess(a, input.drop(aString.length))
        case _ => ParserFailure
      }
    })
  }
}