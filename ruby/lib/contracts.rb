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
