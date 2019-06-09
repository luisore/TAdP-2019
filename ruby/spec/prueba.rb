require_relative '../lib/invariants'
require_relative '../lib/contracts'
require_relative '../lib/conditions'

class Abaco
  include Contracts
  include Conditions

  pre {
    numero2 > -100
  }
  def sumar(numero1, numero2)
    # puts self.class.__blocksManager.getOwner #Esto deberia mostrar Abaco pero muestra Calculadora
    #
    # NO! self.class para TODOS los métodos del method lookup va a ser Calculadora, porque
    # self referencia al objeto que recibió el mensaje y está ejecutando este método (sino, por ejemplo,
    # las variables de instancia de las clases padre no tendrían sentido)
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
  
  def initialize()
    @limitaContador = 2
  end
  
  pre { @limitaContador > valor }
  def chequearContador(valor)
    valor + 1
  end

  pre { divisor > 0 }
  post { |result| result == dividendo / divisor }
  def dividir(dividendo, divisor)
    @algoMas = 1
    dividendo / divisor
  end

  pre {
    numero1 > numero2
  }
  def sumar(numero1, numero2) #Este método ejecuta tambien la precondición del padre porque llama a super
    super
  end


  post {|result| result < sumar(numero1, numero2)} #Esta precondición llama al método sumar
  def restar(numero1, numero2) #Este método solo ejecuta su precondición porque no llama a super
    numero1 - numero2
  end
end

class TestClass2
  include Contracts
  include Invariants

  attr_accessor :energy

  invariant {@life > 0}
  invariant {energy > 0}

  def initialize()
    @life = 100
    self.energy = 100
  end

  def aFailMethod
    @life = -2
  end

  def aFailMethod2
    self.energy = -4
  end

  def aSuccessMethod
    @life = 40
    self.energy = 20
  end
end

class ParentTestClass
  include Contracts

  before_and_after_each_call(proc{ print 'Before from parent' },  proc{ print 'After from parent' })

  def aParentMethod
    'A Parent Method'
  end
end

#Módulos y clases de ejemplo para testear los contratos
module TestModule
  include Contracts
  
  before_and_after_each_call(proc{ print 'Before from module' },  proc{ print 'After from module' })
  
  def aModuleMethod
    'A Module Method'
  end
end

class TestClass < ParentTestClass
  include Contracts
  include TestModule

  before_and_after_each_call(proc{ print 'Before 1' },  proc{ print 'After 1' })
  before_and_after_each_call(proc{ print 'Before 2' },  proc{ print 'After 2' }, true)

  def aMethod()
    'A Class Method'
  end

  def aMethod2()
    'A Class Method'
  end
end

class TestClass3
  include Contracts
  before_and_after_each_call(proc{print"Entro a un mensaje"},proc{print"Salgo de un mensaje"})
  def aMessage
    print "MENSAJE"
    return 5
  end
end