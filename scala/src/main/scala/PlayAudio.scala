import MasterParser._
import  Musica._

object PlayAudio extends App {
  //Silencios
  val silencio = (char('_') <|> char('-') <|> char('~')).map {
    case '_' => Silencio(Blanca)
    case '-' => Silencio(Negra)
    case '~' => Silencio(Corchea)
  }

  //Sonidos
  val octava = digit()
  val notaIndividual = char('A') <|> char('B') <|> char('C') <|> char('D') <|> char('E') <|> char('F') <|> char('G')
  val modificador = char('#') <|> char('b')
  //Esto no es muy elegante pero es la 1:30 de la mañana y no se como invocar un objeto a partir de un char
  val nota = (notaIndividual <> modificador.opt).map {
    case (n, '#') => n match {
      case 'A' => A.sostenido
      case 'B' => B.sostenido
      case 'C' => C.sostenido
      case 'D' => D.sostenido
      case 'E' => E.sostenido
      case 'F' => F.sostenido
      case 'G' => G.sostenido
    }
    case (n, 'b') => n match {
      case 'A' => A.bemol
      case 'B' => B.bemol
      case 'C' => C.bemol
      case 'D' => D.bemol
      case 'E' => E.bemol
      case 'F' => F.bemol
      case 'G' => G.bemol
    }
    case (n, ()) => n match {
      case 'A' => A
      case 'B' => B
      case 'C' => C
      case 'D' => D
      case 'E' => E
      case 'F' => F
      case 'G' => G
    }
  }
  val tono = (octava <> nota).map {case (oct: Int, nta: Nota) => Tono(oct, nta)}
  val figura = (char('1') ~> char('/') ~> digit()).map {
    case 1 => Redonda
    case 2 => Blanca
    case 4 => Negra
    case 8 => Corchea
    case 16 => SemiCorchea
  }
  val sonido = (tono <> figura).map {case (tn: Tono, fg: Figura) => Sonido(tn, fg)}

  //Acordes
  val modificadorDeAcorde = char('m') <|> char('M')
  val acordeExplicito = (tono.sepBy(char('+')) <> figura).map {
    case (tonos: List[Tono], fig: Figura) => Acorde(tonos, fig)
  }
  val acordeReducido = (tono <> modificadorDeAcorde <> figura).map {
    case ((ton: Tono, 'm'), fig: Figura) => ton match {
      case Tono(oct, not) => not.acordeMenor(oct, fig)
    }
    case ((ton: Tono, 'M'), fig: Figura) => ton match {
      case Tono(oct, not) => not.acordeMayor(oct, fig)
    }
  }


  val tema = (silencio <|> sonido <|> acordeReducido <|> acordeExplicito).sepBy(char(' '))

  val felizCumple = "4C1/4 4C1/4 4D1/2 4C1/4 4F1/2 4E1/2 4C1/8 4C1/4 4D1/2 4C1/2 4G1/2 4F1/2 4C1/8 4C1/4 5C1/2 4A1/2 4F1/8 4F1/4 4E1/2 4D1/2"
  val bonus = "4AM1/8 5C1/8 5C#1/8 5C#1/8 5D#1/8 5C1/8 4A#1/8 4G#1/2 - 4A#1/8 4A#1/8 5C1/4 5C#1/8 4A#1/4 4G#1/2 5G#1/4 5G#1/4 5D#1/2"
  //println(tema(felizCumple))
  println(tema(bonus))

  //  Ahora convertir la partitura a la melodía y pasarle eso al AudioPlayer les toca hacerlo a ustedes.

  tema(bonus) match {
    case ParserSuccess(lista: List[Tocable], _) => AudioPlayer.reproducir(lista)
    case _ => println("error")
  }

}
