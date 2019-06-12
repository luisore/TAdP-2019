import Musica._
import javax.sound.midi.{MidiChannel, MidiSystem}

object AudioPlayer {
  private val VOLUME = 80
  require(VOLUME <= 127)
  require(VOLUME >= 0)

  def reproducir(partitura: String): Unit = {
    val melodia = new MusicParser(partitura).parse()

    reproducir(melodia)
  }

  def reproducir(melodia: List[Nota]): Unit = {
    val synth = MidiSystem.getSynthesizer

    synth.open()

    val channel = synth.getChannels().head

    tocarMelodia(channel, melodia)

    synth.close()
  }

  def tocarMelodia(channel: MidiChannel, melodia: List[Nota], duracionNota: Int = 300): Unit = {
    melodia match {
      case Nil ⇒ Unit
      case nota :: otraNota :: notas if nota == otraNota ⇒ {
        tocarMelodia(channel, otraNota :: notas, duracionNota + 300)
      }
      case nota :: notas ⇒ {
        tocarNota(channel, nota, duracionNota)
        tocarMelodia(channel, notas)
      }
    }
  }

  def tocarNota(channel: MidiChannel, nota: Nota, duracionNota: Int): Unit = {
    prenderNota(channel)(nota)

    descansar(duracionNota)

    apagarNota(channel)(nota)
  }

  def descansar(duration: Int): Unit = {
    Thread.sleep(duration)
  }

  def prenderNota(channel: MidiChannel)(nota: Nota): Unit = {
    channel.noteOn(midiId(nota), VOLUME)
  }

  def apagarNota(channel: MidiChannel)(nota: Nota): Unit = {
    channel.noteOff(midiId(nota))
  }

  private def midiId(nota: Nota) = {
    val cantidadSemitonosEnOctava = 12

    val octava = 5

    val idNota = nota match {
      case C ⇒ 0
      case D ⇒ 2
      case E ⇒ 4
      case F ⇒ 5
      case G ⇒ 7
      case A ⇒ 9
      case B ⇒ 11
    }

    idNota + cantidadSemitonosEnOctava * octava
  }
}
