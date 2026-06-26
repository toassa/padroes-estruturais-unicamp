package br.unicamp.padroesestruturais.legacy.decorator;

public class TaxaInternacionalDecorator extends AjusteValorDecorator {

    private static final double TAXA_INTERNACIONAL = 0.05;

    public TaxaInternacionalDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        double valor = valorCobranca.calcular();
        return valor + (valor * TAXA_INTERNACIONAL);
    }
}
