module Contracts
  module ClassMethods
    
    @@__beforeBlocks = []
    @@__afterBlocks = []
    @__lastMethodAdded = nil
    
    def before_and_after_each_call(beforeBlock, afterBlock)
      @@__beforeBlocks.push(beforeBlock)
      @@__afterBlocks.push(afterBlock)
    end
    
    def method_added(name)
      if(!@__lastMethodAdded || !@__lastMethodAdded.include?(name))
        custom = :"#{name}_custom"
        original = :"#{name}_original"
        @__lastMethodAdded = [name, custom, original]
        define_method custom do |*args, &block|
          @@__beforeBlocks.each { |blck| blck.call }
          send original, *args, &block
          @@__afterBlocks.each { |blck| blck.call }
        end
        alias_method original, name
        alias_method name, custom
      end
    end
  end
  
  def self.included(base)
    base.extend(ClassMethods)
  end
end

module TestModule
  def aModuleMethod
    'Module Method'
  end
end

class ParentTestClass
  def aParentMethod
    'a parent method'
  end
end

class TestClass < ParentTestClass
  include Contracts
  include TestModule
  
  before_and_after_each_call(proc{ puts 'Entré a un mensaje' },  proc{ puts 'Salí de un mensaje' })
  before_and_after_each_call(proc{ puts 'Entré a un mensaje2' },  proc{ puts 'Salí de un mensaje2' })
  
  def aMethod
    'Class Method'
  end
end
