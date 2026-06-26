package br.unicamp.padroesestruturais.legacy.decorator;

public class DescontoFidelidadeDecorator extends AjusteValorDecorator {

    private static final double TAXA_DESCONTO = 0.05;

    public DescontoFidelidadeDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        double valor = valorCobranca.calcular();
        return valor - (valor * TAXA_DESCONTO);
    }
}
