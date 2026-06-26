package br.unicamp.padroesestruturais.legacy.decorator;

public abstract class AjusteValorDecorator implements ValorCobranca {

    protected final ValorCobranca valorCobranca;

    public AjusteValorDecorator(ValorCobranca valorCobranca) {
        this.valorCobranca = valorCobranca;
    }
}
