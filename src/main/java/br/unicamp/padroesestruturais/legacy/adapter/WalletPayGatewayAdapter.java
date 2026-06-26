package br.unicamp.padroesestruturais.legacy.adapter;

import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.ChargeRequest;
import br.unicamp.padroesestruturais.legacy.externo.ChargeResponse;
import br.unicamp.padroesestruturais.legacy.externo.ChargeStatus;
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK;

public class WalletPayGatewayAdapter implements GatewayCobranca {

    private final WalletPaySDK sdk;

    public WalletPayGatewayAdapter(WalletPaySDK sdk) {
        this.sdk = sdk;
    }

    @Override
    public ResultadoCobranca cobrar(Pedido pedido, double valor, FormaPagamento forma) {
        long valorEmCentavos = Math.round(valor * 100);

        ChargeRequest request = new ChargeRequest(
                pedido.getId(),
                pedido.getCliente(),
                valorEmCentavos
        );

        ChargeResponse response = sdk.charge(request);

        String status = response.getStatus() == ChargeStatus.CONFIRMED
                ? "APROVADA"
                : "RECUSADA";

        return new ResultadoCobranca(
                pedido.getId(),
                valor,
                status,
                response.getWalletTransactionId(),
                forma
        );
    }
}
