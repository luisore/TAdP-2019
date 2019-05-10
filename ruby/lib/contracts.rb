class BlocksManager
  def initialize
    #Estos dos diccionarios van a almacenar los procs que voy a querer ejecutar en los contratos
    #Son diccionarios que adentro tienen arrays/listas/como se llamen en ruby
    @beforeBlocks = Hash.new
    @afterBlocks = Hash.new
    
    @unassignedBeforeBlocks = Hash.new
    @unassignedAfterBlocks = Hash.new
    
    @singleBeforeBlocks = Hash.new
    @singleAfterBlocks = Hash.new
  end
  
  def beforeBlockPush(beforeBlock, key)
    if(!@beforeBlocks[key])
      @beforeBlocks[key] = []
    end
    @beforeBlocks[key].push(beforeBlock)
  end
  
  def afterBlockPush(afterBlock, key)
    if(!@afterBlocks[key])
      @afterBlocks[key] = []
    end
    @afterBlocks[key].push(afterBlock)
  end
  
  def unassignedBeforeBlockPush(beforeBlock, key)
    if(!@unassignedBeforeBlocks[key])
      @unassignedBeforeBlocks[key] = []
    end
    @unassignedBeforeBlocks[key].push(beforeBlock)
  end
  
  def unassignedAfterBlockPush(afterBlock, key)
    if(!@unassignedAfterBlocks[key])
      @unassignedAfterBlocks[key] = []
    end
    @unassignedAfterBlocks[key].push(afterBlock)
  end
  
  def singleBeforeBlockAssign(ownerName, methodName)
    if(@unassignedBeforeBlocks[ownerName] && @unassignedBeforeBlocks[ownerName].length > 0)
      if(!@singleBeforeBlocks[ownerName])
        @singleBeforeBlocks[ownerName] = Hash.new
      end
      if(!@singleBeforeBlocks[ownerName][methodName])
        @singleBeforeBlocks[ownerName][methodName] = []
      end
      @singleBeforeBlocks[ownerName][methodName] += @unassignedBeforeBlocks[ownerName]
      @unassignedBeforeBlocks[ownerName] = []
    end
  end
  
  def singleAfterBlockAssign(ownerName, methodName)
    if(@unassignedAfterBlocks[ownerName] && @unassignedAfterBlocks[ownerName].length > 0)
      if(!@singleAfterBlocks[ownerName])
        @singleAfterBlocks[ownerName] = Hash.new
      end
      if(!@singleAfterBlocks[ownerName][methodName])
        @singleAfterBlocks[ownerName][methodName] = []
      end
      @singleAfterBlocks[ownerName][methodName] += @unassignedAfterBlocks[ownerName]
      @unassignedAfterBlocks[ownerName] = []
    end
  end
  
  def getBeforeBlocks(obj, ownerName, methodName)
    result = []
    if(@beforeBlocks[obj.class.name])
      result += @beforeBlocks[obj.class.name]
    end
    if(@singleBeforeBlocks[ownerName] && @singleBeforeBlocks[ownerName][methodName])
      result += @singleBeforeBlocks[ownerName][methodName]
    end
    obj.class.ancestors.each {
      |anc|
      if(anc.name != ownerName && @beforeBlocks[anc.name])
        result += @beforeBlocks[anc.name]
      end
    }
    result
  end
  
  def getAfterBlocks(obj, ownerName, methodName)
    result = []
    if(@afterBlocks[obj.class.name])
      result = result + @afterBlocks[obj.class.name]
    end
    if(@singleAfterBlocks[ownerName] && @singleAfterBlocks[ownerName][methodName])
      result += @singleAfterBlocks[ownerName][methodName]
    end
    obj.class.ancestors.each {
      |anc|
      if(anc.name != ownerName && @afterBlocks[anc.name])
        result += @afterBlocks[anc.name]
      end
    }
    result
  end
end

#El modulo Contracts va a agregar metodos de clase nuevos a la clase (o módulo) que lo incluya
module Contracts
  
  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module ContractsClassMethods
    
    #Objeto que se encarga de la gestion de las listas y diccionarios de bloques
    @@__blocksManager = BlocksManager.new
    
    #Esta variable la voy a utilizar mas abajo para evitar que mi código caiga en llamadas recursivas infinitas
    @@__lastMethodAdded = Hash.new
    
    #Este método es el que voy a usar para definir los contratos en las clases que incluyan el modulo Contracts
    #Es un método de clase y recibe como parámetros dos procs y un flag para determinar si los bloques se agregan para
    #toda la clase/modulo o solo para el siguiente metodo en definirse
    def before_and_after_each_call(beforeBlock = false, afterBlock = false, singleMethod = false)
      if(!singleMethod)
        if(beforeBlock)
          @@__blocksManager.beforeBlockPush(beforeBlock, self.name)
        end
        if(afterBlock)
          @@__blocksManager.afterBlockPush(afterBlock, self.name)
        end
      else
        if(beforeBlock)
          @@__blocksManager.unassignedBeforeBlockPush(beforeBlock, self.name)
        end
        if(afterBlock)
          @@__blocksManager.unassignedAfterBlockPush(afterBlock, self.name)
        end
      end
    end
    
    #Esto es un hook de ruby que se llama cada vez que un nuevo método es definido (en una clase o un módulo)
    #Tipicamente se ejecuta al hacer load de un archivo .rb o al definir un método o una clase "on the fly" con pry
    #Importante esto es llamado en la definición de la clase, no al instanciar un objeto de esta; tampoco al llamar a un método
    def method_added(name)
      
      #Esta condición evita que method_added se llame recursivamente hasta el infinito
      #Esto puede darse porque mas abajo estoy definiendo un nuevo método
      #@@__lastMethodAdded puede ser nill o una lista con el nombre original del método y los alternativos que defino mas abajo
      if(!@@__lastMethodAdded[self.name] || !@@__lastMethodAdded[self.name].include?(name))
        
        #defino dos nuevos identificadores usando como base el nombre original del método
        custom = :"#{self.name}_#{name}_custom" #este va a "apuntar" a un nuevo método que voy a definir a continuación
        original = :"#{self.name}_#{name}_original" #Este va a apuntar al método original pero con un nombre alternativo
        @@__lastMethodAdded[self.name] = [name, custom, original] #Asigno la lista que para la condición que mencioné antes
        
        #Defino un nuevo método que va a ser llamado en lugar del método original
        define_method custom do |*args, &block|
          
          paramNames = method(original).parameters.map(&:last).map(&:to_s)
          
          #Si no es un accesor
          if(!instance_variables.include?('@'.concat(name.to_s.sub('=', '')).to_sym))
            #Ejecuto todos los bloques before
            @@__blocksManager.getBeforeBlocks(self, self.method(__method__.to_sym).owner.name, name).each { 
              |blck| blck.call(self, paramNames, args) 
            }
          end
          #Ejecuto el método original por su nombre alternativo
          result = send original, *args, &block 
          
          #Si no es un accesor
          if(!instance_variables.include?('@'.concat(name.to_s.sub('=', '')).to_sym))
            #Ejecuto todos los bloques after que tenia almacenados
            @@__blocksManager.getAfterBlocks(self, self.method(__method__.to_sym).owner.name, name).each { 
              |blck| blck.call(self, paramNames, args, result) 
            }
          end
          result
        end
        
        #Defino los alias usando los identificadores previamente creados
        alias_method original, name #El identificador original va a servir para llamar al método original
        alias_method name, custom #El nombre original ahora va a llamar a mi método custom
        
        #Le asigno los bloques single si existen
        @@__blocksManager.singleBeforeBlockAssign(self.name, name)
        @@__blocksManager.singleAfterBlockAssign(self.name, name)
      end
    end
  end
  
  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(ContractsClassMethods) #Agrego los métodos del módulo ClassMethods como métodos de clase al modulo/clase que me está incluyendo
    #base.inheritContracts
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
  
  before_and_after_each_call(proc{ puts 'Before 1' },  proc{ puts 'After 1' })
  before_and_after_each_call(proc{ puts 'Before 2' },  proc{ puts 'After 2' }, true)
  
  def aMethod(param1, param2)
    'A Class Method'
  end
  
  def aMethod2(param1, param2)
    'A Class Method'
  end
end
