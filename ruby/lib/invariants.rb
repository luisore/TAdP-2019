require_relative  'contracts.rb'

#Error custom para cuando la invarianza da false
class InvariantError < StandardError
  def initialize(msg = "Invariant Error")
    super
  end
end

#El modulo Invariants va a agregar metodos de clase nuevos a la clase (o módulo) que lo incluya
module Invariants
  include Contracts
  
  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module InvariantsClassMethods
    
    #Este método va a crear un contrato, a partir de un bloque recibido
    #Dicho contrato va a constar de un proc after que va a ejecutar el bloque recibido en el contexto del objeto 
    #instanciado a partir de la clase que implementa la invarianza (me expliqué para el tujes no?)
    def invariant(&block)
      myBlock = block
      #Creo un contrato nuevo, solo me interesa el block after
      before_and_after_each_call(false, proc{|myInstance| 
        #Ejecuto el bloque en el contexto/scope del objeto que me llega por parametro
        #Evaluo el resultado como booleano
        #Si da false tiro una excepción
        if(!myInstance.instance_eval &myBlock)
          raise InvariantError
        end
      })
    end
  end
  
  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(InvariantsClassMethods)
  end
end

#Clase de ejemplo para testear, deberia fallar al llamar al método "aMethod"
class TestClass2
  include Contracts
  include Invariants
  
  invariant {@life > 0}
  
  def initialize()
    @life = 100
  end
  
  def aFailMethod
    @life = -2
  end
  
  def aSuccessMethod
    @life = 40
  end
end