package br.unicamp.padroesestruturais.legacy.service;

import br.unicamp.padroesestruturais.legacy.adapter.GatewayCobranca;
import br.unicamp.padroesestruturais.legacy.adapter.GatewayPagamentoInternoAdapter;
import br.unicamp.padroesestruturais.legacy.adapter.PaySecureGatewayAdapter;
import br.unicamp.padroesestruturais.legacy.adapter.WalletPayGatewayAdapter;
import br.unicamp.padroesestruturais.legacy.decorator.*;
import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.PaySecureGateway;
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamentoInterno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CobrancaService {

    private final Map<FormaPagamento, GatewayCobranca> gateways = new HashMap<>();

    public CobrancaService() {
        GatewayCobranca gatewayInterno =
                new GatewayPagamentoInternoAdapter(new GatewayPagamentoInterno());

        gateways.put(FormaPagamento.BOLETO, gatewayInterno);
        gateways.put(FormaPagamento.PIX, gatewayInterno);
        gateways.put(FormaPagamento.CARTAO_CREDITO,
                new PaySecureGatewayAdapter(new PaySecureGateway()));
        gateways.put(FormaPagamento.CARTEIRA_DIGITAL,
                new WalletPayGatewayAdapter(new WalletPaySDK()));
    }

    public ResultadoCobranca cobrar(Pedido pedido, FormaPagamento forma,
                                     boolean aplicarDescontoFidelidade,
                                     boolean aplicarJurosParcelamento,
                                     boolean aplicarTaxaInternacional,
                                     boolean aplicarSeguro) {

        ValorCobranca valorCobranca = criarValorComDecorators(
                pedido.getValorBase(),
                aplicarDescontoFidelidade,
                aplicarJurosParcelamento,
                aplicarTaxaInternacional,
                aplicarSeguro
        );

        return cobrar(pedido, forma, valorCobranca);
    }

    public ResultadoCobranca cobrar(Pedido pedido, FormaPagamento forma, ValorCobranca valorCobranca) {
        if (forma == null || !gateways.containsKey(forma)) {
            throw new IllegalArgumentException("Forma de pagamento nao suportada: " + forma);
        }

        double valorFinal = valorCobranca.calcular();
        GatewayCobranca gateway = gateways.get(forma);

        return gateway.cobrar(pedido, valorFinal, forma);
    }

    public List<ResultadoCobranca> cobrarEmLote(List<Pedido> pedidos, FormaPagamento forma,
                                                  boolean aplicarDescontoFidelidade,
                                                  boolean aplicarJurosParcelamento,
                                                  boolean aplicarTaxaInternacional,
                                                  boolean aplicarSeguro) {

        List<ResultadoCobranca> resultados = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            resultados.add(cobrar(
                    pedido,
                    forma,
                    aplicarDescontoFidelidade,
                    aplicarJurosParcelamento,
                    aplicarTaxaInternacional,
                    aplicarSeguro
            ));
        }

        return resultados;
    }

    public double calcularValorFinal(double valorBase,
                                      boolean aplicarDescontoFidelidade,
                                      boolean aplicarJurosParcelamento,
                                      boolean aplicarTaxaInternacional,
                                      boolean aplicarSeguro) {

        ValorCobranca valorCobranca = criarValorComDecorators(
                valorBase,
                aplicarDescontoFidelidade,
                aplicarJurosParcelamento,
                aplicarTaxaInternacional,
                aplicarSeguro
        );

        return valorCobranca.calcular();
    }

    private ValorCobranca criarValorComDecorators(double valorBase,
                                                  boolean aplicarDescontoFidelidade,
                                                  boolean aplicarJurosParcelamento,
                                                  boolean aplicarTaxaInternacional,
                                                  boolean aplicarSeguro) {

        ValorCobranca valor = new ValorBase(valorBase);

        if (aplicarDescontoFidelidade) {
            valor = new DescontoFidelidadeDecorator(valor);
        }

        if (aplicarJurosParcelamento) {
            valor = new JurosParcelamentoDecorator(valor);
        }

        if (aplicarTaxaInternacional) {
            valor = new TaxaInternacionalDecorator(valor);
        }

        if (aplicarSeguro) {
            valor = new SeguroTransacaoDecorator(valor);
        }

        return valor;
    }
}
