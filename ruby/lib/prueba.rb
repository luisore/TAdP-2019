require_relative '../lib/invariants'
require_relative '../lib/contracts'
require_relative '../lib/conditions'

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

  pre { divisor > 0 }
  post { |result| result == dividendo / divisor  }
  def dividir(dividendo, divisor)
    dividendo / divisor
  end

  pre {
    puts 'Hijo1'
    true
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

class TestClass3
  include Contracts
  before_and_after_each_call(proc{puts"Entro a un mensaje"},proc{puts"Salgo de un mensaje"})
  def aMessage
    puts "MENSAJE"
    return 5
  end
end