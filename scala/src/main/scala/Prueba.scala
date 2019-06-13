class Parser(xc: String) {
  var x: String = xc
  def sacarRepetido = {
    var lista: String = ""
    for(nota <- x){
      if(nota == '(')
       for(i <- (x.indexOf('(') +1) to (x.indexOf(')')-1)){
         lista += x(i)
       }
         
    }
    lista
  }
  def cantidadARepetir = x.find(nota => (nota == '1') || (nota == '2')).get.toInt
  
  def insertar = {
    var listaAInsertar: String =""
    var repeticiones: Int = this.cantidadARepetir
    for (i <- 1 to repeticiones){
      listaAInsertar += this.sacarRepetido
    }
    listaAInsertar
  }
}

 
object Demo{
  def main(args: Array[String]){
    val point = new Parser("ASD2X(ABC)")
    var listaRespuesta: String = point.sacarRepetido
    print(point.cantidadARepetir)
 
    
  }
}