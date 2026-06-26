package br.unicamp.padroesestruturais.legacy.decorator;

public class ValorBase implements ValorCobranca {

    private final double valor;

    public ValorBase(double valor) {
        this.valor = valor;
    }

    @Override
    public double calcular() {
        return valor;
    }
}
