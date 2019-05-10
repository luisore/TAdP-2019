

describe 'golondrina' do

  it 'cuando un galo ataca a otro la resistencia del otro galo de disminuir' do
    expect{TestClass2.new.aFailMethod}.to raise_error InvariantError
  end
  it 'cuando ejecuto un metodo que cumple el invariante este se ejecuta' do
    expect(TestClass2.new.aSuccessMethod).to eq(40)
  end




end