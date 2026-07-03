package br.unicamp.padroesestruturais.legacy;

import br.unicamp.padroesestruturais.legacy.adapter.PaySecureGatewayAdapter;
import br.unicamp.padroesestruturais.legacy.adapter.WalletPayGatewayAdapter;
import br.unicamp.padroesestruturais.legacy.decorator.DescontoFidelidadeDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.JurosParcelamentoDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.SeguroTransacaoDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaAntecipacaoRecebiveisDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaEmissaoNotaFiscalDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.TaxaInternacionalDecorator;
import br.unicamp.padroesestruturais.legacy.decorator.ValorBase;
import br.unicamp.padroesestruturais.legacy.decorator.ValorCobranca;
import br.unicamp.padroesestruturais.legacy.domain.FormaPagamento;
import br.unicamp.padroesestruturais.legacy.domain.Pedido;
import br.unicamp.padroesestruturais.legacy.domain.ResultadoCobranca;
import br.unicamp.padroesestruturais.legacy.externo.ChargeRequest;
import br.unicamp.padroesestruturais.legacy.externo.ChargeResponse;
import br.unicamp.padroesestruturais.legacy.externo.ChargeStatus;
import br.unicamp.padroesestruturais.legacy.externo.GatewayIndisponivelException;
import br.unicamp.padroesestruturais.legacy.externo.PaySecureGateway;
import br.unicamp.padroesestruturais.legacy.externo.TransacaoExterna;
import br.unicamp.padroesestruturais.legacy.externo.WalletPaySDK;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdapterDecoratorTest {

    @Test
    void deveAdaptarPaySecureParaCargaAprovada() {
        CapturingPaySecureGateway gateway = new CapturingPaySecureGateway(
                new TransacaoExterna("PSEC-TEST", 200, 1234.56, "BRL")
        );
        PaySecureGatewayAdapter adapter = new PaySecureGatewayAdapter(gateway);
        Pedido pedido = new Pedido("PED-001", "Joao Silva", "Notebook", 1234.56);

        ResultadoCobranca resultado = adapter.cobrar(pedido, 1234.56, FormaPagamento.CARTAO_CREDITO);

        assertEquals("APROVADA", resultado.getStatus());
        assertEquals("PSEC-TEST", resultado.getReferencia());
        assertEquals(1234.56, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTAO_CREDITO, resultado.getFormaPagamento());
        assertEquals("PED-001", gateway.dadosCapturados.get("orderId"));
        assertEquals("Joao Silva", gateway.dadosCapturados.get("customerName"));
        assertEquals(1234.56, ((Number) gateway.dadosCapturados.get("amount")).doubleValue(), 0.001);
        assertEquals("BRL", gateway.dadosCapturados.get("currency"));
    }

    @Test
    void deveAdaptarPaySecureParaCargaRecusadaQuandoGatewayRetornaErro() {
        CapturingPaySecureGateway gateway = new CapturingPaySecureGateway(
                new GatewayIndisponivelException("indisponivel")
        );
        PaySecureGatewayAdapter adapter = new PaySecureGatewayAdapter(gateway);
        Pedido pedido = new Pedido("PED-002", "Maria Santos", "Servidor", 15000.0);

        ResultadoCobranca resultado = adapter.cobrar(pedido, 15000.0, FormaPagamento.CARTAO_CREDITO);

        assertEquals("RECUSADA", resultado.getStatus());
        assertNull(resultado.getReferencia());
        assertEquals(15000.0, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTAO_CREDITO, resultado.getFormaPagamento());
    }

    @Test
    void deveAdaptarWalletPayConvertendoValorParaCentavos() {
        CapturingWalletPaySDK sdk = new CapturingWalletPaySDK(
                new ChargeResponse(ChargeStatus.CONFIRMED, "WPAY-TEST")
        );
        WalletPayGatewayAdapter adapter = new WalletPayGatewayAdapter(sdk);
        Pedido pedido = new Pedido("PED-003", "Carlos Lima", "Headset", 12.345);

        ResultadoCobranca resultado = adapter.cobrar(pedido, 12.345, FormaPagamento.CARTEIRA_DIGITAL);

        assertEquals("APROVADA", resultado.getStatus());
        assertEquals("WPAY-TEST", resultado.getReferencia());
        assertEquals(12.345, resultado.getValorCobrado(), 0.001);
        assertEquals(FormaPagamento.CARTEIRA_DIGITAL, resultado.getFormaPagamento());
        assertNotNull(sdk.ultimaRequisicao);
        assertEquals("PED-003", sdk.ultimaRequisicao.getMerchantOrderId());
        assertEquals("Carlos Lima", sdk.ultimaRequisicao.getPayerName());
        assertEquals(1235L, sdk.ultimaRequisicao.getAmountInCents());
    }

    @Test
    void deveRecusarWalletPayQuandoSdkNaoConfirma() {
        CapturingWalletPaySDK sdk = new CapturingWalletPaySDK(
                new ChargeResponse(ChargeStatus.DECLINED, "WPAY-DECLINED")
        );
        WalletPayGatewayAdapter adapter = new WalletPayGatewayAdapter(sdk);
        Pedido pedido = new Pedido("PED-004", "Construtora ABC Ltda", "Servidor", 50000.0);

        ResultadoCobranca resultado = adapter.cobrar(pedido, 50000.0, FormaPagamento.CARTEIRA_DIGITAL);

        assertEquals("RECUSADA", resultado.getStatus());
        assertEquals("WPAY-DECLINED", resultado.getReferencia());
    }

    @Test
    void deveAplicarCadaDecoratorIndividualmente() {
        assertEquals(950.0, new DescontoFidelidadeDecorator(new ValorBase(1000.0)).calcular(), 0.001);
        assertEquals(1029.9, new JurosParcelamentoDecorator(new ValorBase(1000.0)).calcular(), 0.001);
        assertEquals(1050.0, new TaxaInternacionalDecorator(new ValorBase(1000.0)).calcular(), 0.001);
        assertEquals(1004.9, new SeguroTransacaoDecorator(new ValorBase(1000.0)).calcular(), 0.001);
        assertEquals(1015.0, new TaxaAntecipacaoRecebiveisDecorator(new ValorBase(1000.0)).calcular(), 0.001);
        assertEquals(1002.5, new TaxaEmissaoNotaFiscalDecorator(new ValorBase(1000.0)).calcular(), 0.001);
    }

    @Test
    void deveComporDecoratorsNaOrdemTaxaAntesDaEmissao() {
        ValorCobranca valor = new TaxaEmissaoNotaFiscalDecorator(
                new TaxaAntecipacaoRecebiveisDecorator(new ValorBase(1000.0))
        );

        assertEquals(1017.5, valor.calcular(), 0.001);
    }

    @Test
    void deveGerarValorDiferenteQuandoAOrdemDosDecoratorsEhInvertida() {
        ValorCobranca valor = new TaxaAntecipacaoRecebiveisDecorator(
                new TaxaEmissaoNotaFiscalDecorator(new ValorBase(1000.0))
        );

        assertEquals(1017.5375, valor.calcular(), 0.001);
    }

    @Test
    void deveManterOComportamentoQuandoDecoratorsPrincipaisSaoCombinadosEmSequencia() {
        ValorCobranca valor = new SeguroTransacaoDecorator(
                new TaxaInternacionalDecorator(
                        new JurosParcelamentoDecorator(
                                new DescontoFidelidadeDecorator(new ValorBase(1000.0))
                        )
                )
        );

        assertEquals(1032.22525, valor.calcular(), 0.001);
    }

    private static class CapturingPaySecureGateway extends PaySecureGateway {

        private final TransacaoExterna retorno;
        private final GatewayIndisponivelException excecao;
        private Map<String, Object> dadosCapturados;

        private CapturingPaySecureGateway(TransacaoExterna retorno) {
            this.retorno = retorno;
            this.excecao = null;
        }

        private CapturingPaySecureGateway(GatewayIndisponivelException excecao) {
            this.retorno = null;
            this.excecao = excecao;
        }

        @Override
        public TransacaoExterna processarTransacao(Map<String, Object> dadosTransacao) throws GatewayIndisponivelException {
            this.dadosCapturados = dadosTransacao;

            if (excecao != null) {
                throw excecao;
            }

            return retorno;
        }
    }

    private static class CapturingWalletPaySDK extends WalletPaySDK {

        private final ChargeResponse retorno;
        private ChargeRequest ultimaRequisicao;

        private CapturingWalletPaySDK(ChargeResponse retorno) {
            this.retorno = retorno;
        }

        @Override
        public ChargeResponse charge(ChargeRequest request) {
            this.ultimaRequisicao = request;
            return retorno;
        }
    }
}