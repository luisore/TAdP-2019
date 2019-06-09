#Este objeto se encarga del manejo y organizaci'on de bloques y procs
#También se encarga de controlar la asignación de procs en la herencia
class BlocksManager
  def initialize(owner)
    @myOwner = owner #Esto esta para testeo despues sacarlo

    #Estos dos diccionarios van a almacenar los procs que voy a querer ejecutar en los contratos
    #Son diccionarios que adentro tienen arrays/listas/como se llamen en ruby
    @beforeBlocks = []
    @afterBlocks = []

    #Misma cosa, pero estos contienen los bloques sin asignar que van a ser asignados al próximo metodo en definirse 
    #(en este módulo o clase, no en otro)
    @unassignedBeforeBlocks = []
    @unassignedAfterBlocks = []

    #Acá se almacenan los bloques ya asignados a métodos específicos
    @singleBeforeBlocks = Hash.new
    @singleAfterBlocks = Hash.new
  end

  def getOwner
    @myOwner
  end

  def beforeBlockPush(beforeBlock)
    @beforeBlocks.push(beforeBlock)
  end

  def afterBlockPush(afterBlock)
    @afterBlocks.push(afterBlock)
  end

  def unassignedBeforeBlockPush(beforeBlock)
    @unassignedBeforeBlocks.push(beforeBlock)
  end

  def unassignedAfterBlockPush(afterBlock)
    @unassignedAfterBlocks.push(afterBlock)
  end

  def singleBeforeBlockAssign(methodName)
    if (@unassignedBeforeBlocks.length > 0)
      if (!@singleBeforeBlocks[methodName])
        @singleBeforeBlocks[methodName] = []
      end
      @singleBeforeBlocks[methodName] += @unassignedBeforeBlocks
      @unassignedBeforeBlocks = []
    end
  end

  def singleAfterBlockAssign(methodName)
    if (@unassignedAfterBlocks.length > 0)
      if (!@singleAfterBlocks[methodName])
        @singleAfterBlocks[methodName] = []
      end
      @singleAfterBlocks[methodName] += @unassignedAfterBlocks
      @unassignedAfterBlocks = []
    end
  end

  def getBeforeBlocks(myClass, methodName, includeAncestors = true)
    result = []

    if (@beforeBlocks)
      result += @beforeBlocks
    end

    if (methodName && @singleBeforeBlocks[methodName])
      result += @singleBeforeBlocks[methodName]
    end

    if (includeAncestors)
      myClass.ancestors.each {
          |anc|
        if (anc != myClass && anc.respond_to?("__blocksManager"))
          result += anc.__blocksManager.getBeforeBlocks(false, methodName, false)
        end
      }
    end

    result
  end

  def getAfterBlocks(myClass, methodName, includeAncestors = true)
    result = []

    if (@afterBlocks)
      result += @afterBlocks
    end

    if (methodName && @singleAfterBlocks[methodName])
      result += @singleAfterBlocks[methodName]
    end

    if (includeAncestors)
      myClass.ancestors.each {
          |anc|
        if (anc != myClass && anc.respond_to?("__blocksManager"))
          result += anc.__blocksManager.getAfterBlocks(false, methodName, false)
        end
      }
    end
    result
  end
end

#El modulo Contracts va a agregar metodos de clase nuevos a la clase (o módulo) que lo incluya
module Contracts

  #El módulo ClassMethods incluye en si los métodos de clase que van a agregarse a la clase objetivo
  module ContractsClassMethods

    #Objeto que se encarga de la gestion de las listas y diccionarios de bloques
    def __blocksManager
      @__blocksManager ||= BlocksManager.new(self)
    end


    #Esta variable la voy a utilizar mas abajo para evitar que mi código caiga en llamadas recursivas infinitas
    # def __lastMethodAdded
    #   @__lastMethodAdded ||= Hash.new
    # end
    # @__lastMethodAdded = Hash.new


    #Este método es el que voy a usar para definir los contratos en las clases que incluyan el modulo Contracts
    #Es un método de clase y recibe como parámetros dos procs y un flag para determinar si los bloques se agregan para
    #toda la clase/modulo o solo para el siguiente metodo en definirse
    def before_and_after_each_call(beforeBlock = false, afterBlock = false, singleMethod = false)
      if (!singleMethod)
        if (beforeBlock)
          __blocksManager.beforeBlockPush(beforeBlock)
        end
        if (afterBlock)
          __blocksManager.afterBlockPush(afterBlock)
        end
      else
        if (beforeBlock)
          __blocksManager.unassignedBeforeBlockPush(beforeBlock)
        end
        if (afterBlock)
          __blocksManager.unassignedAfterBlockPush(afterBlock)
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
      if (!@__defining_contract)
        #defino dos nuevos identificadores usando como base el nombre original del método
        @__defining_contract = true

        begin
          original_method = instance_method(name)
          paramNames = original_method.parameters.map(&:last).map(&:to_s)

          #Defino un nuevo método que va a ser llamado en lugar del método original
          define_method name do |*args, &block|
            #Si no es un accesor
            if (!instance_variables.include?('@'.concat(name.to_s.sub('=', '')).to_sym))
              #Ejecuto todos los bloques before
              self.class.__blocksManager.getBeforeBlocks(self.class, name).each {
                  |blck| blck.call(self, paramNames, args)
              }
            end
            #Ejecuto el método original por su nombre alternativo
            result = original_method.bind(self).call(*args, &block)

            #Si no es un accesor
            if (!instance_variables.include?('@'.concat(name.to_s.sub('=', '')).to_sym))
              #Ejecuto todos los bloques after que tenia almacenados
              self.class.__blocksManager.getAfterBlocks(self.class, name).each {
                  |blck| blck.call(self, paramNames, args, result)
              }
            end
            result
          end

          #Defino los alias usando los identificadores previamente creados
          # alias_method original, name #El identificador original va a servir para llamar al método original
          # alias_method name, custom #El nombre original ahora va a llamar a mi método custom

          #Le asigno los bloques single si existen
          __blocksManager.singleBeforeBlockAssign(name)
          __blocksManager.singleAfterBlockAssign(name)
        ensure
          @__defining_contract = false
        end
      end

    end
  end

  #Cuando el módulo es incluido
  def self.included(base)
    base.extend(ContractsClassMethods) #Agrego los métodos del módulo ClassMethods como métodos de clase al modulo/clase que me está incluyendo
  end
end