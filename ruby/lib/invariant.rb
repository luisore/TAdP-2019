class M
  attr_accessor :invariant
  def invariante (&block)
    self.invariant = block
  end

  def self.define_attr(attr)
    define_method(attr) do
      instance_variable_get("@#{attr}")
    end

    define_method("#{attr}=") do |val|
      instance_variable_set("@#{attr}", val)
      if(!instance_eval(&self.invariant))
        raise 'no puede ser'
      end
    end
  end

end