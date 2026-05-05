package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.StatusAgendamento;
import com.connectbeleza.connectbeleza.domain.enums.StatusPagamento;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.AgendamentoRequest;
import com.connectbeleza.connectbeleza.dto.request.CancelarRequest;
import com.connectbeleza.connectbeleza.dto.request.RemarcarRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendamentoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.AgendamentoRepository;
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
@DisplayName("AgendamentoService")
class AgendamentoServiceTest {

    @Mock AgendamentoRepository agendamentoRepository;
    @Mock ServicoService servicoService;
    @Mock UsuarioService usuarioService;
    @Mock PagamentoService pagamentoService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks AgendamentoService agendamentoService;

    private UUID clienteId;
    private UUID agendamentoId;
    private UUID servicoId;
    private Usuario cliente;
    private Profissional profissional;
    private Servico servico;
    private Agendamento agendamento;
    private Pagamento pagamento;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        agendamentoId = UUID.randomUUID();
        servicoId = UUID.randomUUID();

        cliente = Usuario.builder()
                .id(clienteId)
                .nome("João Silva")
                .email("joao@email.com")
                .role(UserRole.CLIENTE)
                .ativo(true)
                .build();

        profissional = Profissional.builder()
                .id(UUID.randomUUID())
                .usuario(cliente)
                .build();

        servico = Servico.builder()
                .id(servicoId)
                .profissional(profissional)
                .nome("Corte de Cabelo")
                .preco(new BigDecimal("80.00"))
                .duracaoMinutos(60)
                .ativo(true)
                .build();

        pagamento = Pagamento.builder()
                .id(UUID.randomUUID())
                .valor(new BigDecimal("80.00"))
                .status(StatusPagamento.APROVADO)
                .metodoPagamento("PIX")
                .criadoEm(LocalDateTime.now())
                .build();

        agendamento = Agendamento.builder()
                .id(agendamentoId)
                .cliente(cliente)
                .servico(servico)
                .dataHoraAgendada(LocalDateTime.now().plusDays(3))
                .status(StatusAgendamento.CONFIRMADO)
                .pagamento(pagamento)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // ─── CONTRATAR SERVIÇO ────────────────────────────────────────────────────

    @Nested
    @DisplayName("contratarServico")
    class ContratarServico {

        @Test
        @DisplayName("deve criar agendamento com status CONFIRMADO quando tudo válido")
        void deveCriarAgendamentoComSucesso() {
            var dataHora = LocalDateTime.now().plusDays(3);
            var request = new AgendamentoRequest(servicoId, dataHora, "PIX", "Sem observações");

            when(usuarioService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
            when(servicoService.buscarEntidadePorId(servicoId)).thenReturn(servico);
            when(agendamentoRepository.existeConflitoDeHorario(any(), any(), any())).thenReturn(false);
            when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);
            when(pagamentoService.realizarPagamento(any(), any())).thenReturn(pagamento);
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            AgendamentoResponse response = agendamentoService.contratarServico(clienteId, request);

            assertThat(response).isNotNull();
            verify(agendamentoRepository, times(2)).save(any(Agendamento.class));
            verify(pagamentoService).realizarPagamento(any(), eq("PIX"));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando serviço inativo")
        void deveLancarExcecaoServicoInativo() {
            servico.setAtivo(false);
            var request = new AgendamentoRequest(servicoId, LocalDateTime.now().plusDays(1), "PIX", null);

            when(usuarioService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
            when(servicoService.buscarEntidadePorId(servicoId)).thenReturn(servico);

            assertThatThrownBy(() -> agendamentoService.contratarServico(clienteId, request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não está disponível");

            verify(agendamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando há conflito de horário")
        void deveLancarExcecaoConflitoDeHorario() {
            var request = new AgendamentoRequest(servicoId, LocalDateTime.now().plusDays(1), "PIX", null);

            when(usuarioService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
            when(servicoService.buscarEntidadePorId(servicoId)).thenReturn(servico);
            when(agendamentoRepository.existeConflitoDeHorario(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> agendamentoService.contratarServico(clienteId, request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Horário indisponível");

            verify(agendamentoRepository, never()).save(any());
        }
    }

    // ─── CANCELAR SERVIÇO ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancelarServico")
    class CancelarServico {

        @Test
        @DisplayName("deve cancelar agendamento com antecedência maior que 24h")
        void deveCancelarComSucesso() {
            agendamento.setDataHoraAgendada(LocalDateTime.now().plusDays(3));
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            var request = new CancelarRequest("Motivo válido");

            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.save(any())).thenReturn(agendamento);
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            AgendamentoResponse response = agendamentoService.cancelarServico(clienteId, agendamentoId, request);

            assertThat(response).isNotNull();
            verify(pagamentoService).estornarPagamento(agendamentoId);
            verify(agendamentoRepository).save(argThat(a ->
                    a.getStatus() == StatusAgendamento.CANCELADO &&
                    a.getMotivoCancelamento().equals("Motivo válido")));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando já está cancelado")
        void deveLancarExcecaoJaCancelado() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelarServico(clienteId, agendamentoId, new CancelarRequest("motivo")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("já está cancelado");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando agendamento já concluído")
        void deveLancarExcecaoJaConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelarServico(clienteId, agendamentoId, new CancelarRequest("motivo")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não é possível cancelar");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando cancelamento dentro de 24h")
        void deveLancarExcecaoCancelamentoDentro24h() {
            agendamento.setDataHoraAgendada(LocalDateTime.now().plusHours(10));
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelarServico(clienteId, agendamentoId, new CancelarRequest("motivo")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("24h de antecedência");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando agendamento não pertence ao cliente")
        void deveLancarExcecaoProprietarioErrado() {
            UUID outroCliente = UUID.randomUUID();
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.cancelarServico(outroCliente, agendamentoId, new CancelarRequest("m")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não pertence a este cliente");
        }
    }

    // ─── REAGENDAR SERVIÇO ────────────────────────────────────────────────────

    @Nested
    @DisplayName("reagendarServico")
    class RemarcarServico {

        @Test
        @DisplayName("deve reagendar com sucesso quando novo horário disponível")
        void deveRemarcarComSucesso() {
            var novaData = LocalDateTime.now().plusDays(5);
            var request = new RemarcarRequest(novaData);

            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.existeConflitoDeHorario(any(), any(), any())).thenReturn(false);
            when(agendamentoRepository.save(any())).thenReturn(agendamento);
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            AgendamentoResponse response = agendamentoService.reagendarServico(clienteId, agendamentoId, request);

            assertThat(response).isNotNull();
            verify(agendamentoRepository).save(argThat(a ->
                    a.getStatus() == StatusAgendamento.REAGENDADO));
        }

        @Test
        @DisplayName("deve salvar a data anterior ao reagendar")
        void deveSalvarDataAnterior() {
            var dataOriginal = agendamento.getDataHoraAgendada();
            var novaData = LocalDateTime.now().plusDays(5);

            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.existeConflitoDeHorario(any(), any(), any())).thenReturn(false);
            when(agendamentoRepository.save(any())).thenReturn(agendamento);
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            agendamentoService.reagendarServico(clienteId, agendamentoId, new RemarcarRequest(novaData));

            verify(agendamentoRepository).save(argThat(a ->
                    dataOriginal.equals(a.getDataHoraAnterior())));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando novo horário indisponível")
        void deveLancarExcecaoConflitoNovoHorario() {
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
            when(agendamentoRepository.existeConflitoDeHorario(any(), any(), any())).thenReturn(true);

            assertThatThrownBy(() -> agendamentoService.reagendarServico(
                    clienteId, agendamentoId, new RemarcarRequest(LocalDateTime.now().plusDays(5))))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Novo horário indisponível");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException ao tentar reagendar agendamento cancelado")
        void deveLancarExcecaoReagendarCancelado() {
            agendamento.setStatus(StatusAgendamento.CANCELADO);
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.reagendarServico(
                    clienteId, agendamentoId, new RemarcarRequest(LocalDateTime.now().plusDays(5))))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("reagendar um agendamento cancelado");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException ao tentar reagendar agendamento concluído")
        void deveLancarExcecaoReagendarConcluido() {
            agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

            assertThatThrownBy(() -> agendamentoService.reagendarServico(
                    clienteId, agendamentoId, new RemarcarRequest(LocalDateTime.now().plusDays(5))))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("reagendar um serviço já concluído");
        }
    }

    // ─── BUSCAR/LISTAR ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar agendamento quando id e cliente conferem")
        void deveRetornarAgendamento() {
            when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            AgendamentoResponse response = agendamentoService.buscarPorId(agendamentoId, clienteId);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existe")
        void deveLancarExcecaoNaoEncontrado() {
            when(agendamentoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> agendamentoService.buscarPorId(UUID.randomUUID(), clienteId))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("listarDoCliente")
    class ListarDoCliente {

        @Test
        @DisplayName("deve retornar página de agendamentos do cliente")
        void deveRetornarPagina() {
            var pageable = PageRequest.of(0, 10);
            Page<Agendamento> page = new PageImpl<>(List.of(agendamento));
            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(agendamentoRepository.findByClienteIdOrderByDataHoraAgendadaDesc(clienteId, pageable)).thenReturn(page);
            when(servicoService.toResponse(any())).thenReturn(null);
            when(pagamentoService.toResponse(any())).thenReturn(null);

            Page<AgendamentoResponse> result = agendamentoService.listarDoCliente(clienteId, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }
}
