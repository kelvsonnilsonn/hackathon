package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.StatusPagamento;
import com.connectbeleza.connectbeleza.dto.response.PagamentoResponse;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.PagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoService")
class PagamentoServiceTest {

    @Mock PagamentoRepository pagamentoRepository;

    @InjectMocks PagamentoService pagamentoService;

    private UUID agendamentoId;
    private Agendamento agendamento;
    private Pagamento pagamento;
    private Servico servico;

    @BeforeEach
    void setUp() {
        agendamentoId = UUID.randomUUID();

        servico = Servico.builder()
                .id(UUID.randomUUID())
                .nome("Corte")
                .preco(new BigDecimal("100.00"))
                .duracaoMinutos(60)
                .build();

        agendamento = Agendamento.builder()
                .id(agendamentoId)
                .servico(servico)
                .dataHoraAgendada(LocalDateTime.now().plusDays(1))
                .build();

        pagamento = Pagamento.builder()
                .id(UUID.randomUUID())
                .agendamento(agendamento)
                .valor(new BigDecimal("100.00"))
                .status(StatusPagamento.APROVADO)
                .metodoPagamento("CARTAO_CREDITO")
                .criadoEm(LocalDateTime.now())
                .pagoEm(LocalDateTime.now())
                .build();
    }

    // ─── REALIZAR PAGAMENTO ───────────────────────────────────────────────────

    @Nested
    @DisplayName("realizarPagamento")
    class RealizarPagamento {

        @Test
        @DisplayName("deve criar pagamento com status APROVADO quando gateway aprova")
        void deveCriarPagamentoAprovado() {
            when(pagamentoRepository.existsByAgendamentoIdAndStatus(agendamentoId, StatusPagamento.APROVADO))
                    .thenReturn(false);
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> {
                Pagamento p = inv.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            Pagamento resultado = pagamentoService.realizarPagamento(agendamento, "PIX");

            assertThat(resultado.getStatus()).isEqualTo(StatusPagamento.APROVADO);
            assertThat(resultado.getMetodoPagamento()).isEqualTo("PIX");
            assertThat(resultado.getValor()).isEqualTo(new BigDecimal("100.00"));
            assertThat(resultado.getPagoEm()).isNotNull();
            verify(pagamentoRepository, times(2)).save(any(Pagamento.class));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando pagamento já realizado")
        void deveLancarExcecaoPagamentoDuplicado() {
            when(pagamentoRepository.existsByAgendamentoIdAndStatus(agendamentoId, StatusPagamento.APROVADO))
                    .thenReturn(true);

            assertThatThrownBy(() -> pagamentoService.realizarPagamento(agendamento, "PIX"))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Pagamento já realizado");

            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve registrar o valor do serviço no pagamento")
        void deveRegistrarValorDoServico() {
            when(pagamentoRepository.existsByAgendamentoIdAndStatus(any(), any())).thenReturn(false);
            when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Pagamento resultado = pagamentoService.realizarPagamento(agendamento, "BOLETO");

            assertThat(resultado.getValor()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    // ─── ESTORNAR PAGAMENTO ───────────────────────────────────────────────────

    @Nested
    @DisplayName("estornarPagamento")
    class EstornarPagamento {

        @Test
        @DisplayName("deve mudar status para ESTORNADO quando pagamento está APROVADO")
        void deveEstornarComSucesso() {
            when(pagamentoRepository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(pagamento));

            pagamentoService.estornarPagamento(agendamentoId);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.ESTORNADO);
            verify(pagamentoRepository).save(pagamento);
        }

        @Test
        @DisplayName("deve ignorar estorno quando pagamento não está APROVADO")
        void deveIgnorarEstornoSeNaoAprovado() {
            pagamento.setStatus(StatusPagamento.RECUSADO);
            when(pagamentoRepository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.of(pagamento));

            pagamentoService.estornarPagamento(agendamentoId);

            assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.RECUSADO);
            verify(pagamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve não fazer nada quando não existe pagamento para o agendamento")
        void deveIgnorarQuandoPagamentoNaoEncontrado() {
            when(pagamentoRepository.findByAgendamentoId(agendamentoId)).thenReturn(Optional.empty());

            assertThatCode(() -> pagamentoService.estornarPagamento(agendamentoId))
                    .doesNotThrowAnyException();
        }
    }

    // ─── toResponse ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("deve retornar null quando pagamento é null")
        void deveRetornarNullParaNull() {
            assertThat(pagamentoService.toResponse(null)).isNull();
        }

        @Test
        @DisplayName("deve mapear corretamente os campos do pagamento")
        void deveMapearCampos() {
            PagamentoResponse response = pagamentoService.toResponse(pagamento);

            assertThat(response.valor()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(response.status()).isEqualTo(StatusPagamento.APROVADO);
            assertThat(response.metodoPagamento()).isEqualTo("CARTAO_CREDITO");
        }
    }
}
