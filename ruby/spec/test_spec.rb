

describe 'test de invariantes' do

  it 'cuando ejecuto un metedo que no cumple la invarianza tira error' do
    expect{TestClass2.new.aFailMethod}.to raise_error InvariantError
  end
  it 'cuando ejecuto un metodo que cumple el invariante este se ejecuta' do
    expect(TestClass2.new.aSuccessMethod).to eq(20)
  end
  it 'cuando se intancia una variable a partir de accessor pero no cumple la invariaza rompe' do
    expect{TestClass2.new.aFailMethod2}.to raise_error InvariantError
  end

end

describe 'test de condiciones' do

  it 'si ejecuto el metodo dividir con el divisor siendo 0 falla' do
    expect{Calculadora.new.dividir(10,0)}.to raise_error PreConditionError
  end

  it 'si el metodo ejecuto el metodo dividir con un divisor distinto de 0 devuelve el resultado' do
    expect(Calculadora.new.dividir(10,5)).to eq(2)
  end


end