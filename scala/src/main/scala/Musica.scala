package object Musica {
  object Nota {
    def notas = List(C, D, E, F, G, A, B)
  }
  trait Nota

  case object C extends Nota
  case object D extends Nota
  case object E extends Nota
  case object F extends Nota
  case object G extends Nota
  case object A extends Nota
  case object B extends Nota

  type Melodia = List[Nota]
}
