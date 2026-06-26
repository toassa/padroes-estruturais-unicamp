package br.unicamp.padroesestruturais.legacy.decorator;

public class TaxaAntecipacaoRecebiveisDecorator extends AjusteValorDecorator {

    private static final double TAXA_ANTECIPACAO = 0.015;

    public TaxaAntecipacaoRecebiveisDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        double valor = valorCobranca.calcular();
        return valor + (valor * TAXA_ANTECIPACAO);
    }
}
