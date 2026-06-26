package br.unicamp.padroesestruturais.legacy.adapter;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.gateway.GatewayPagamentoInterno;

public class GatewayPagamentoInternoAdapter implements GatewayCobranca {

    private final GatewayPagamentoInterno gateway;

    public GatewayPagamentoInternoAdapter(GatewayPagamentoInterno gateway) {
        this.gateway = gateway;
    }

    @Override
    public ResultadoCobranca cobrar(Pedido pedido, double valor, FormaPagamento forma) {
        return gateway.cobrar(pedido.getId(), pedido.getCliente(), valor, forma);
    }
}
