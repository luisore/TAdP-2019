import MasterParser._

object PlayAudio extends App {
  //Silencios
  val silencio = char('_') <|> char('-') <|> char('~')

  //Sonidos
  val octava = digit()
  val notaIndividual = char('A') <|> char('B') <|> char('C') <|> char('D') <|> char('E') <|> char('F') <|> char('G')
  val modificador = char('#') <|> char('b')
  val nota = notaIndividual <> modificador.opt
  val tono = octava <> nota
  val figura = char('1') ~> char('/') ~> digit()
  val sonido = tono <> figura

  //Acordes
  //Todo


  val tema = sonido.sepBy(char(' '))

  val sariaSong = "F A B B F A B B F A B E D D B C B G E E D D E G E F A B B F A B B F A B E D D B C E B G G D E G E"
  val felizCumple = "4C1/4 4C1/4 4D1/2 4C1/4 4F1/2 4E1/2 4C1/8 4C1/4 4D1/2 4C1/2 4G1/2 4F1/2 4C1/8 4C1/4 5C1/2 4A1/2 4F1/8 4F1/4 4E1/2 4D1/2"
  val bonus = "4AM1/8 5C1/8 5C#1/8 5C#1/8 5D#1/8 5C1/8 4A#1/8 4G#1/2 - 4A#1/8 4A#1/8 5C1/4 5C#1/8 4A#1/4 4G#1/2 5G#1/4 5G#1/4 5D#1/2"
  println(tema(felizCumple))
  println(tema(bonus))

  //  Ahora convertir la partitura a la melodía y pasarle eso al AudioPlayer les toca hacerlo a ustedes.


  //AudioPlayer.reproducirNotas(sariaSong)
}
