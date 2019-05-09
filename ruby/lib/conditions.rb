require_relative  'contracts.rb'

#Error custom para cuando la invarianza da false
class PreConditionError < StandardError
  def initialize(msg = "Pre Condition Error")
    super
  end
end

#Error custom para cuando la invarianza da false
class PostConditionError < StandardError
  def initialize(msg = "Post Condition Error")
    super
  end
end

module Conditions
  include Contracts
  
  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module ConditionsClassMethods
    
    def pre(&block)
      
      #bloque de la precondición que se supone devuelve un boolean y 
      #espera recibír el valor de return de la función como argumento
      myBlock = block 
      
      #Creo un contrato nuevo, solo me interesa el block before (para la precondicion)
      before_and_after_each_call(proc{|myInstance, paramsNames, values| 

        #Ejecuto en la instancia del objeto que contiene el método sobre el que se va a aplicar la postcondición          
        if(!myInstance.instance_exec *values, &myBlock)
          raise PreConditionError
        end

      }, false,
      true) #El tercer parámetro está en true porque solo quiero que aplique al siguiente método en ser definido
    end
    
    def post(&block)
      
      #bloque de la postcondición que se supone devuelve un boolean y 
      #espera recibír el valor de return de la función como argumento
      myBlock = block 
      
      #Creo un contrato nuevo, solo me interesa el block after (para la postcondicion)
      before_and_after_each_call(false, proc{|myInstance, paramsNames, values, result| 

        #Ejecuto en la instancia del objeto que contiene el método sobre el que se va a aplicar la postcondición          
        if(!myInstance.instance_exec result, *values, &myBlock)
          raise PostConditionError
        end

      }, true) #El tercer parámetro está en true porque solo quiero que aplique al siguiente método en ser definido
    end
    
  end
  
  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(ConditionsClassMethods)
  end
end


#Clases para testeo
class Abaco
  include Contracts
  include Conditions
  
  pre {
    puts 'Padre1'
    true
  }
  def sumar(numero1, numero2)
    numero1 + numero2
  end
  
  pre {
    puts 'Padre2'
    true
  }
  def restar(numero1, numero2)
    numero1 - numero2
  end
end

class Calculadora < Abaco
  include Contracts
  include Conditions
  
  pre { |dividendo, divisor|  divisor > 0  }
  post { |result, dividendo, divisor| result == dividendo / divisor  }
  def dividir(numero1, numero2)
    numero1 / numero2
  end
  
  pre {
    puts 'Hijo1'
    true
  }
  def sumar(numero1, numero2) #Este método ejecuta tambien la precondición del padre porque llama a super
    super
  end
  
  
  post {|result, numero1, numero2| result < sumar(numero1, numero2)} #Esta precondición llama al método sumar
  def restar(numero1, numero2) #Este método solo ejecuta su precondición porque no llama a super
    numero1 - numero2
  end
end