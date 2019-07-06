import MasterParser._
import org.scalatest.{FreeSpec, Matchers}


class MasterParserTest extends FreeSpec with Matchers {
  def assertParsesSucceededWithResult[T](actualResult: T, expectedResult: T): Unit = {
    actualResult shouldBe (expectedResult)
  }

  def assertParseFailed[T](actualResult: ⇒ T): Unit = {
    assertThrows[ParserException](actualResult)
  }


  val parseoUnaA = char('A')
  val parseoUnaB = char('B')
  val parseoUnaC = char('C')
  val parseoGuion = char('-')
  val parseoHola = string("Hola")
  val OrCharACharB = parseoUnaA <|> parseoUnaB
  val OrCharACharBCharC =  parseoUnaA <|> parseoUnaB <|>  parseoUnaC
  val RightCharACharB = parseoUnaA ~> parseoUnaB
  val LeftCharACharB = parseoUnaA <~ parseoUnaB
  val AndCharACharB = parseoUnaA <> parseoUnaB
  val AndCharACharBCharA =  AndCharACharB <> parseoUnaA

  "Probando los Parsers" - {
   "Combinator OR, de dos Char 'A' y 'B' respectivamente, retorna el resultado del primer parser" - {
      "Se le pasa 'ABCD' retorna ('A',  'BCD') " in {
        assertParsesSucceededWithResult( OrCharACharB ("ABCD") , ParserSuccess('A',"BCD"))
      }
    }
    "Combinator OR, de dos Char  'A' y 'B' respectivamente, retorna el resultado del segundo parser" - {
      "Se le pasa 'BCDE' retorna ('B',  'CDE') " in {
        assertParsesSucceededWithResult( OrCharACharB ("BCDE"), ParserSuccess('B',"CDE"))
      }
    }
    "Combinator OR, de dos Char  'A' y 'B' respectivamente,  Falla" - {
      "Se le pasa 'CDEF' y Falla" in {
        assertParsesSucceededWithResult( OrCharACharB ("CDEF") , ParserFailure)
      }
    }
    "Combinator OR, de tres Char 'A' , 'B' y  'C' respectivamente, , retorna el resultado del tercer parser" - {
      "Se le pasa 'CDEF' y retorna 'C',  'DEF'" in {
        assertParsesSucceededWithResult( OrCharACharBCharC ("CDEF") , ParserSuccess('C',"DEF"))
      }
    }
    "Combinator OR, de tres Char  'A' , 'B' y  'C' respectivamente, Falla" - {
      "Se le pasa 'XXX' y Falla" in {
        assertParsesSucceededWithResult( OrCharACharBCharC ("XXX") , ParserFailure)
      }
    }
    "Combinator OR, de tres Char  'A' , 'B' y  'C' respectivamente, con la operación opt" - {
      "Se le pasa 'XXX' y retorna  'XXX' " in {
        assertParsesSucceededWithResult( OrCharACharBCharC.opt() ("XXX") , ParserSuccess((),"XXX"))
      }
    }
    "Parser String que paresea 'Hola' "- {
      "Se le pasa 'Hola Mundo!' y retorna (Hola, Mundo)" in {
        assertParsesSucceededWithResult( parseoHola("Hola Mundo!"), ParserSuccess("Hola"," Mundo!"))
      }
    }
    "Parser String que parsea 'Hola', falla" - {
      "Se le pasa 'Mundo Hola!' y falla" in {
        assertParsesSucceededWithResult(parseoHola("Mundo Hola!"), ParserFailure)
      }
    }
    "Combinator RightMost que paresea 'A' y luego 'B' "- {
      "Se le pasa 'ABCD' y retorna (B,CD)" in {
        assertParsesSucceededWithResult(RightCharACharB("ABCD"), ParserSuccess('B',"CD"))
      }
    }
    "Combinator LeftMost que paresea 'A' y luego 'B' "- {
      "Se le pasa 'ABCD' y retorna (A,CD)" in {
        assertParsesSucceededWithResult(LeftCharACharB("ABCD"), ParserSuccess('A',"CD"))
      }
    }
    "Combinator And que paresea 'A' y luego 'B' "- {
      "Se le pasa 'ABCD' y retorna ((A,B),CD)" in {
        assertParsesSucceededWithResult(AndCharACharB("ABCD"), ParserSuccess(('A','B'),"CD"))
      }
    }
    "Combinator And que paresea 'A' y luego 'B' "- {
      "Se le pasa 'ACDE' y falla" in {
        assertParsesSucceededWithResult(AndCharACharB("ACDE"), ParserFailure)
      }
    }
    "Combinator And que paresea 'A' y luego 'B' "- {
      "Se le pasa 'BCDE' y falla" in {
        assertParsesSucceededWithResult(AndCharACharB("BCDE"), ParserFailure)
      }
    }
    "Combinator And que paresea 'A' && 'B' y luego 'A' "- {
      "Se le pasa 'ABABCD' y retorna (((A,B),A),CD)" in {
        assertParsesSucceededWithResult(AndCharACharBCharA("ABACDE"), ParserSuccess((('A','B'),'A'),"CDE"))
      }
    }
    "Combinator And que paresea 'A' && 'B' y luego 'A' "- {
      "Se le pasa 'ABBCD' y falla" in {
        assertParsesSucceededWithResult(AndCharACharBCharA("ABCDE"), ParserFailure)
      }
    }
    "Parser Char que parsea 'A' operado con clausura de Kleene"- {
      "Se le pasa 'ABCD' y retorna (List(A),BCD)" in {
        assertParsesSucceededWithResult(parseoUnaA.*() ("ABCD"), ParserSuccess(List('A'),"BCD"))
      }
    }
    "Parser Char que parsea 'A' operado con clausura de Kleene"- {
      "Se le pasa 'AAABCD' y retorna (List(A,A,A),BCD)" in {
        assertParsesSucceededWithResult(parseoUnaA.*() ("AAABCD"), ParserSuccess(List('A','A','A'),"BCD"))
      }
    }
    "Parser Char que parsea 'A' operado con clausura de Kleene"- {
      "Se le pasa 'BCDE' y retorna (List(),BCD)" in {
        assertParsesSucceededWithResult(parseoUnaA.*() ("BCDE"), ParserSuccess(List(),"BCDE"))
      }
    }
    "Parser Char que parsea 'A' operado con clausura de Kleene positiva"- {
      "Se le pasa 'ABCD' y retorna (List(A),BCD)" in {
        assertParsesSucceededWithResult(parseoUnaA.+() ("ABCD"), ParserSuccess(List('A'),"BCD"))
      }
    }
    "Parser Char que parsea 'A' operado con clausura de Kleene positiva"- {
      "Se le pasa 'BCDE' y falla" in {
        assertParsesSucceededWithResult(parseoUnaA.+() ("BCDE"), ParserFailure)
      }
    }
    "Parser Char que parsea 'A' operado con SepBy"- {
      "Se le pasa 'A-BCD' y retorna (List(A),BCD)" in {
        assertParsesSucceededWithResult(parseoUnaA.sepBy(parseoGuion)("A-A-BCD"), ParserSuccess(List('A', 'A'),"BCD"))
      }
    }
    "Parser Char que parsea 'A' operado con SepBy"- {
      "Se le pasa 'A BCD' y falla" in {
        assertParsesSucceededWithResult(parseoUnaA.sepBy(parseoGuion)("A A BCD"), ParserSuccess(List('A')," A BCD"))
      }
    }
  }
}
