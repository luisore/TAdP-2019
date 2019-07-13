import MasterParser._
import  Musica._

object PlayAudio extends App {
  //Silencios
  val silencio: ParserWrapper[Silencio] = (char('_') <|> char('-') <|> char('~')).map {
    case '_' => Silencio(Blanca)
    case '-' => Silencio(Negra)
    case '~' => Silencio(Corchea)
  }

  //Sonidos
  val octava = integer

  // De forma más corta:
  val charToNota = Map(
    'A' ->  A,
    'B' ->  B,
    'C' ->  C,
    'D' ->  D,
    'E' ->  E,
    'F' ->  F,
    'G' ->  G
  )
  val bemolChar = char('b')
  val sostenidoChar = char('#')
  def bemol(nota: ParserWrapper[Nota]) =
    (nota <~ bemolChar).map(_.bemol)
  def sostenido(nota: ParserWrapper[Nota]) =
    (nota <~ sostenidoChar).map(_.sostenido)
  // O es una nota bemol, o es un sostenido o es nota simple (importa el orden)
  def notaConModificador(nota: ParserWrapper[Nota]): ParserWrapper[Nota] =
    bemol(nota) <|> sostenido(nota) <|> nota
  // agarra el mapa de char a notas,
  //  lo transforma a una lista de parsers de notas con modificadores
  //  opera a todos contra todos usando "OR"
  val nota: ParserWrapper[Nota] = charToNota
    .map{ case (c, nota) => notaConModificador(char(c).const(nota)) }
    .reduce(_ <|> _)



  val tono = (octava <> nota).map {case (oct: Int, nta: Nota) => Tono(oct, nta)}
  val figura = (char('1') ~> char('/') ~> integer).map {
    case 1 => Redonda
    case 2 => Blanca
    case 4 => Negra
    case 8 => Corchea
    case 16 => SemiCorchea
  }
  val sonido: ParserWrapper[Sonido] = (tono <> figura).map {case (tn: Tono, fg: Figura) => Sonido(tn, fg)}

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


  val tema: ParserWrapper[List[Tocable]] = (silencio <|> sonido <|> acordeReducido <|> acordeExplicito).sepBy(char(' '))

  val felizCumple = "4C1/4 4C1/4 4D1/2 4C1/4 4F1/2 4E1/2 4C1/8 4C1/4 4D1/2 4C1/2 4G1/2 4F1/2 4C1/8 4C1/4 5C1/2 4A1/2 4F1/8 4F1/4 4E1/2 4D1/2"
  val bonus = "4AM1/8 5C1/8 5C#1/8 5C#1/8 5D#1/8 5C1/8 4A#1/8 4G#1/2 - 4A#1/8 4A#1/8 5C1/4 5C#1/8 4A#1/4 4G#1/2 5G#1/4 5G#1/4 5D#1/2"
  //println(tema(felizCumple))
  //println(tema(bonus))

  //  Ahora convertir la partitura a la melodía y pasarle eso al AudioPlayer les toca hacerlo a ustedes.

  tema(bonus) match {
    case ParserSuccess(lista: List[Tocable], _) => AudioPlayer.reproducir(lista)
    case _ => println("error")
  }

  println(integer("16"))
}
