#El modulo Contracts va a agregar metodos de clase nuevos a la clase (o módulo) que lo incluya
module Contracts
  
  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module ContractsClassMethods
    
    #Estos dos diccionarios van a almacenar los procs que voy a querer ejecutar en los contratos
    #Son diccionarios que adentro tienen arrays/listas/como se llamen en ruby
    @@__beforeBlocks = Hash.new
    @@__afterBlocks = Hash.new
    
    #Esta variable la voy a utilizar mas abajo para evitar que mi código caiga en llamadas recursivas infinitas
    @@__lastMethodAdded = nil
    
    #Inicializa listas dentro de los diccionarios para esta clase/módulo
    def initLists
      if(!@@__beforeBlocks[self.name])
        @@__beforeBlocks[self.name] = []
        @@__afterBlocks[self.name] = []
      end
    end
    
    #Este método es el que voy a usar para definir los contratos en las clases que incluyan el modulo Contracts
    #Es un método de clase y recibe como parámetros dos procs
    def before_and_after_each_call(beforeBlock, afterBlock)
      initLists()
      #Simplemente agrega los bloques recibidos a las listas correspondientes
      @@__beforeBlocks[self.name].push(beforeBlock)
      @@__afterBlocks[self.name].push(afterBlock)
    end
    
    #Método que utilizo para heredar contratos de ancestros
    def inheritContracts
      initLists()
      
      myClass = self
      
      #Todo: cambiar este pseudo try catch por algo menos feo
      begin
        while (myClass.name != 'Class' && myClass.superclass)

          myClass = myClass.superclass
          if(@@__beforeBlocks[myClass.name])
            @@__beforeBlocks[myClass.name].each { |blck| @@__beforeBlocks[self.name].push(blck) }
          end

          if(@@__afterBlocks[myClass.name])
            @@__afterBlocks[myClass.name].each { |blck| @@__afterBlocks[self.name].push(blck) }
          end
        end
      rescue
        
      end
    end
    
    #Método que utilizo para incluir contratos de módulos
    def includeContracts
      initLists()
      self.included_modules.each {|modl| 
        if(@@__beforeBlocks[modl.name])
          @@__beforeBlocks[modl.name].each { |blck| @@__beforeBlocks[self.name].push(blck) }
        end

        if(@@__afterBlocks[modl.name])
          @@__afterBlocks[modl.name].each { |blck| @@__afterBlocks[self.name].push(blck) }
        end
      }
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
          if(@@__beforeBlocks[self.class.name])
            @@__beforeBlocks[self.class.name].each { |blck| blck.call(self) } #Ejecuto todos los bloques before que tenia almacenados
          end
          send original, *args, &block #Ejecuto el método original por su nombre alternativo
          if(@@__afterBlocks[self.class.name])
            @@__afterBlocks[self.class.name].each { |blck| blck.call(self) } #Ejecuto todos los bloques after que tenia almacenados
          end
        end
        
        #Defino los alias usando los identificadores previamente creados
        alias_method original, name #El identificador original va a servir para llamar al método original
        alias_method name, custom #El nombre original ahora va a llamar a mi método custom
      end
    end
  end
  
  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(ContractsClassMethods) #Agrego los métodos del módulo ClassMethods como métodos de clase al modulo/clase que me está incluyendo
    base.inheritContracts
    #base.includeContracts Esto por ahora acá no hace nada, hay que llamarlo al terminar de incluir los módulos
  end
end

#Módulos y clases de ejemplo para testear los contratos
module TestModule
  include Contracts
  
  before_and_after_each_call(proc{ puts 'Before from module' },  proc{ puts 'After from module' })
  
  def aModuleMethod
    'A Module Method'
  end
end

class ParentTestClass
  include Contracts
  
  before_and_after_each_call(proc{ puts 'Before from parent' },  proc{ puts 'After from parent' })
  
  def aParentMethod
    'A Parent Method'
  end
end

class TestClass < ParentTestClass
  include Contracts
  include TestModule
  
  includeContracts #Esto es opcional por si se quieren incluir los contratos presentes en módulos (sigo investigando como hacerlo automáticamente)
  
  before_and_after_each_call(proc{ puts 'Before 1' },  proc{ puts 'After 1' })
  before_and_after_each_call(proc{ puts 'Before 2' },  proc{ puts 'After 2' })
  
  def aMethod
    'A Class Method'
  end
  
  
end
