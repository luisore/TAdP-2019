require_relative  'contracts.rb'

#Error custom para cuando la precondición da false
class PreConditionError < StandardError
  def initialize(msg = "Pre Condition Error")
    super
  end
end

#Error custom para cuando la postcondición da false
class PostConditionError < StandardError
  def initialize(msg = "Post Condition Error")
    super
  end
end

#Esta clase la uso para wrapear la instancia de un objeto y simular 'variables locales' para un método
class ObjectWrapper
  def initialize(originalObject, paramNames, values)
    @__params = Hash.new
    paramNames.each_with_index { |val,index| 
      @__params[val.to_s] = values[index]
    }
    @__originalObject = originalObject
    
    #Copio las variables de instancia del objeto original
    originalObject.instance_variables.each {|name| instance_variable_set(name, originalObject.instance_variable_get(name)) }
  end
  
  def method_missing(mName, *args, &block)
    if(@__params.key?(mName.to_s)) #si tengo el parametro en mi lista, devuelvo su valor
      @__params[mName.to_s]
    else #Sino llamo al método del objeto original
      @__originalObject.send mName, *args, &block
    end
  end
  
  def respond_to_missing?(mName, includePrivate = false)
    @__params.key?(mName.to_s) || @__originalObject.respond_to_missing?(mName, includePrivate)
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

        #wrappeo el objeto original en mi clase especial que hace que el bloque "vea" los argumentos pasados al método original
        instanceWrapper = ObjectWrapper.new(myInstance, paramsNames, values)
          
        #Ejecuto el bloque en el wrap de la instancia del objeto que contenia el método de la condición
        if(!instanceWrapper.instance_exec &myBlock)
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

        #wrappeo el objeto original en mi clase especial que hace que el bloque "vea" los argumentos pasados al método original
        instanceWrapper = ObjectWrapper.new(myInstance, paramsNames, values) 
          
        #Ejecuto el bloque en el wrap de la instancia del objeto que contenia el método de la condición (además le paso el result del método)
        if(!instanceWrapper.instance_exec(result, &myBlock))
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
