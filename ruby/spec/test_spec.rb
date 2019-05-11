describe 'test de Antes y Después' do
  it 'Se imprimen por consola los mensajes del bloque before, del método en si y del bloque after' do
    expect{TestClass3.new.aMessage}.to output('Entro a un mensajeMENSAJESalgo de un mensaje').to_stdout
  end

  it 'Invoco un método que recibe contratos desde su clase (incluyendo algunos exclusivos para este método), 
  desde su superclase y desde un módulo y controlo que esten todos los cartelitos' do
    expect{TestClass.new.aMethod}.to output('Before 1Before 2Before from moduleBefore from parentAfter 1After 2After from moduleAfter from parent').to_stdout
  end
  
  it 'Misma cosa pero sin bloques exclusivos' do
    expect{TestClass.new.aMethod2}.to output('Before 1Before from moduleBefore from parentAfter 1After from moduleAfter from parent').to_stdout
  end
end

describe 'test de invariantes' do

  it 'cuando ejecuto un metedo que no cumple la invarianza tira error' do
    expect{TestClass2.new.aFailMethod}.to raise_error InvariantError
  end
  it 'cuando ejecuto un metodo que cumple el invariante este se ejecuta' do
    expect(TestClass2.new.aSuccessMethod).to eq(20)
  end
  it 'cuando accedo a una variable a partir de accessor pero no cumple la invariaza rompe' do
    expect{TestClass2.new.aFailMethod2}.to raise_error InvariantError
  end

end

describe 'test de condiciones' do

  it 'si ejecuto el método dividir con el divisor siendo 0 falla' do
    expect{Calculadora.new.dividir(10,0)}.to raise_error PreConditionError
  end

  it 'si se invoca al método dividir con un divisor distinto de 0 devuelve el resultado' do
    expect(Calculadora.new.dividir(10,5)).to eq(2)
  end

  it 'si se invoca al método sumar (que utiliza super) se ejecuta tambien la precondición del supermétodo' do
    expect{Calculadora.new.sumar(2,4)}.to output('Hijo1Padre1').to_stdout
  end
  
  it 'La postcondición del método restar espera que su resultado sea menor que el resultado del método suma con los mismos parámetros' do
    expect(Calculadora.new.restar(4,2)).to eq(2)
  end
  
  it 'La postcondición del método restar espera que su resultado sea menor que el resultado del método suma con los mismos parámetros. 
      Así que la rompo restando un negativo' do
    expect{Calculadora.new.restar(4,-2)}.to raise_error PostConditionError
  end
end