package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.AvaliacaoRequest;
import com.connectbeleza.connectbeleza.dto.response.AvaliacaoResponse;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.AvaliacaoRepository;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvaliacaoService")
class AvaliacaoServiceTest {

    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock AgendamentoService agendamentoService;
    @Mock UsuarioService usuarioService;
    @Mock ProfissionalService profissionalService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks AvaliacaoService avaliacaoService;

    private UUID clienteId;
    private UUID agendamentoId;
    private UUID profissionalId;
    private Usuario cliente;
    private Profissional profissional;
    private Servico servico;
    private Agendamento agendamento;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        agendamentoId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();

        cliente = Usuario.builder()
                .id(clienteId)
                .nome("Maria Costa")
                .email("maria@email.com")
                .role(UserRole.CLIENTE)
                .ativo(true)
                .build();

        Usuario usuarioProfissional = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Carlos Esteta")
                .build();

        profissional = Profissional.builder()
                .id(profissionalId)
                .usuario(usuarioProfissional)
                .notaMedia(BigDecimal.ZERO)
                .totalAvaliacoes(0)
                .build();

        servico = Servico.builder()
                .id(UUID.randomUUID())
                .profissional(profissional)
                .nome("Limpeza de Pele")
                .preco(new BigDecimal("150.00"))
                .duracaoMinutos(90)
                .categoria(CategoriaEstetica.PELE)
                .ativo(true)
                .build();

        agendamento = Agendamento.builder()
                .id(agendamentoId)
                .cliente(cliente)
                .servico(servico)
                .dataHoraAgendada(LocalDateTime.now().minusDays(2))
                .status(StatusAgendamento.CONCLUIDO)
                .criadoEm(LocalDateTime.now().minusDays(2))
                .build();

        avaliacao = Avaliacao.builder()
                .id(UUID.randomUUID())
                .agendamento(agendamento)
                .avaliador(cliente)
                .profissional(profissional)
                .nota(5)
                .comentario("Excelente serviço!")
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // ─── AVALIAR PROFISSIONAL ─────────────────────────────────────────────────

    @Nested
    @DisplayName("avaliarProfissional")
    class AvaliarProfissional {

        @Test
        @DisplayName("deve registrar avaliação com sucesso para agendamento CONCLUIDO")
        void deveAvaliarComSucesso() {
            var request = new AvaliacaoRequest(agendamentoId, 5, "Excelente serviço!");

            when(agendamentoService.buscarEntidadePorId(agendamentoId)).thenReturn(agendamento);
            when(avaliacaoRepository.existsByAgendamentoId(agendamentoId)).thenReturn(false);
            when(usuarioService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
            when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacao);
            when(avaliacaoRepository.calcularMediaPorProfissional(profissionalId)).thenReturn(Optional.of(5.0));
            when(avaliacaoRepository.countByProfissionalId(profissionalId)).thenReturn(1L);

            AvaliacaoResponse response = avaliacaoService.avaliarProfissional(clienteId, request);

            assertThat(response).isNotNull();
            assertThat(response.nota()).isEqualTo(5);
            assertThat(response.comentario()).isEqualTo("Excelente serviço!");
            verify(profissionalService).atualizarNotaMedia(profissionalId, 5.0, 1L);
        }

        @Test
        @DisplayName("deve usar a nota como média quando não há avaliações anteriores")
        void deveUsarNotaComoMediaQuandoSemAvaliacoesAnteriores() {
            var request = new AvaliacaoRequest(agendamentoId, 4, "Muito bom.");

            when(agendamentoService.buscarEntidadePorId(agendamentoId)).thenReturn(agendamento);
            when(avaliacaoRepository.existsByAgendamentoId(agendamentoId)).thenReturn(false);
            when(usuarioService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
            when(avaliacaoRepository.save(any())).thenReturn(avaliacao);
            when(avaliacaoRepository.calcularMediaPorProfissional(profissionalId)).thenReturn(Optional.empty());
            when(avaliacaoRepository.countByProfissionalId(profissionalId)).thenReturn(1L);

            avaliacaoService.avaliarProfissional(clienteId, request);

            verify(profissionalService).atualizarNotaMedia(eq(profissionalId), eq(4.0), eq(1L));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando cliente não é dono do agendamento")
        void deveLancarExcecaoClienteNaoEhDono() {
            UUID outroCliente = UUID.randomUUID();
            when(agendamentoService.buscarEntidadePorId(agendamentoId)).thenReturn(agendamento);

            assertThatThrownBy(() -> avaliacaoService.avaliarProfissional(outroCliente, new AvaliacaoRequest(agendamentoId, 3, "ok")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Somente o cliente do agendamento pode avaliar");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando agendamento não está CONCLUIDO")
        void deveLancarExcecaoAgendamentoNaoConcluido() {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoService.buscarEntidadePorId(agendamentoId)).thenReturn(agendamento);

            assertThatThrownBy(() -> avaliacaoService.avaliarProfissional(clienteId, new AvaliacaoRequest(agendamentoId, 5, "ok")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("CONCLUIDO");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando agendamento já foi avaliado")
        void deveLancarExcecaoAgendamentoJaAvaliado() {
            when(agendamentoService.buscarEntidadePorId(agendamentoId)).thenReturn(agendamento);
            when(avaliacaoRepository.existsByAgendamentoId(agendamentoId)).thenReturn(true);

            assertThatThrownBy(() -> avaliacaoService.avaliarProfissional(clienteId, new AvaliacaoRequest(agendamentoId, 5, "ótimo")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já foi avaliado");
        }
    }

    // ─── LISTAR POR PROFISSIONAL ──────────────────────────────────────────────

    @Nested
    @DisplayName("listarPorProfissional")
    class ListarPorProfissional {

        @Test
        @DisplayName("deve retornar página de avaliações do profissional")
        void deveRetornarPaginaDeAvaliacoes() {
            var pageable = PageRequest.of(0, 10);
            Page<Avaliacao> page = new PageImpl<>(List.of(avaliacao));

            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(avaliacaoRepository.findByProfissionalIdOrderByCriadoEmDesc(profissionalId, pageable)).thenReturn(page);

            Page<AvaliacaoResponse> result = avaliacaoService.listarPorProfissional(profissionalId, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).nota()).isEqualTo(5);
            verify(profissionalService).buscarEntidadePorId(profissionalId);
        }

        @Test
        @DisplayName("deve validar existência do profissional antes de listar")
        void deveValidarExistenciaDoProfissional() {
            var pageable = PageRequest.of(0, 10);
            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(avaliacaoRepository.findByProfissionalIdOrderByCriadoEmDesc(any(), any()))
                    .thenReturn(Page.empty());

            avaliacaoService.listarPorProfissional(profissionalId, 0, 10);

            verify(profissionalService).buscarEntidadePorId(profissionalId);
        }
    }
}
