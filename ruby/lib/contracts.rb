#El modulo Contracts va a agregar metodos de clase nuevos a la clase (o módulo) que lo incluya
module Contracts
  
  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module ClassMethods
    
    #Estas dos listas van a almacenar los procs/bloques que voy a querer ejecutar en los contratos
    @@__beforeBlocks = []  
    @@__afterBlocks = []
    
    #Esta variable la voy a utilizar mas abajo para evitar que mi código caiga en llamadas recursivas infinitas
    @@__lastMethodAdded = nil
    
    #Este método es el que voy a usar para definir los contratos en las clases que incluyan el modulo Contracts
    #Es un método de clase y recibe como parámetros dos procs/bloques
    def before_and_after_each_call(beforeBlock, afterBlock)
      
      #Simplemente agrega los bloques recibidos a las listas correspondientes
      @@__beforeBlocks.push(beforeBlock)
      @@__afterBlocks.push(afterBlock)
    end
    
    #Esto es un hook de ruby que se llama cada vez que un nuevo método es definido (en una clase o un módulo)
    #Tipicamente se ejecuta al hacer load de un archivo .rb o al definir un método o una clase "on the fly" con pry
    #Importante esto es llamado en la definición de la clase, no al instanciar un objeto de esta; tampoco al llamar a un método
    def method_added(name)
      
      #Esta condición evita que method_added se llame recursivamente hasta el infinito
      #Esto puede darse porque mas abajo estoy definiendo un nuevo método
      #@@__lastMethodAdded puede ser nill o una lista con el nombre original del método y los alternativos que defino mas abajo
      if(!@@__lastMethodAdded || !@@__lastMethodAdded.include?(name))
        
        #defino dos nuevos identificadores usando como base el nombre original del método
        custom = :"#{name}_custom" #este va a "apuntar" a un nuevo método que voy a definir a continuación
        original = :"#{name}_original" #Este va a apuntar al método original pero con un nombre alternativo
        @@__lastMethodAdded = [name, custom, original] #Asigno la lista que para la condición que mencioné antes
        
        #Defino un nuevo método que va a ser llamado en lugar del método original
        define_method custom do |*args, &block|
          @@__beforeBlocks.each { |blck| blck.call } #Ejecuto todos los bloques before que tenia almacenados
          send original, *args, &block #Ejecuto el método original por su nombre alternativo
          @@__afterBlocks.each { |blck| blck.call } #Ejecuto todos los bloques after que tenia almacenados
        end
        
        #Defino los alias usando los identificadores previamente creados
        alias_method original, name #El identificador original va a servir para llamar al método original
        alias_method name, custom #El nombre original ahora va a llamar a mi método custom
      end
    end
  end
  
  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(ClassMethods) #Agrego los métodos del módulo ClassMethods como métodos de clase al modulo/clase que me está incluyendo
  end
end

#Módulos y clases de ejemplo para testear los contratos
module TestModule
  include Contracts;
  
  before_and_after_each_call(proc{ puts 'Before from module' },  proc{ puts 'After from module' })
  
  def aModuleMethod
    'A Module Method'
  end
end

class ParentTestClass
  include Contracts;
  
  before_and_after_each_call(proc{ puts 'Before from parent' },  proc{ puts 'After from parent' })
  
  def aParentMethod
    'A Parent Method'
  end
end

class TestClass < ParentTestClass
  include Contracts
  include TestModule
  
  before_and_after_each_call(proc{ puts 'Before 1' },  proc{ puts 'After 1' })
  before_and_after_each_call(proc{ puts 'Before 2' },  proc{ puts 'After 2' })
  
  def aMethod
    'A Class Method'
  end
  
  
end
