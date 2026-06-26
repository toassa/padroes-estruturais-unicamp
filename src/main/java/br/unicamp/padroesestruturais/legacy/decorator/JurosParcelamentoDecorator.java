package br.unicamp.padroesestruturais.legacy.decorator;

public class JurosParcelamentoDecorator extends AjusteValorDecorator {

    private static final double TAXA_JUROS = 0.0299;

    public JurosParcelamentoDecorator(ValorCobranca valorCobranca) {
        super(valorCobranca);
    }

    @Override
    public double calcular() {
        double valor = valorCobranca.calcular();
        return valor + (valor * TAXA_JUROS);
    }
}
