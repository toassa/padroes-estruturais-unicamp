package br.unicamp.padroesestruturais.legacy.decorator;

public class TaxaEmissaoNotaFiscalDecorator extends AjusteValorDecorator {

    private static final double VALOR_TAXA_NOTA_FISCAL = 2.50;

    public TaxaEmissaoNotaFiscalDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        return valorCobranca.calcular() + VALOR_TAXA_NOTA_FISCAL;
    }
}
