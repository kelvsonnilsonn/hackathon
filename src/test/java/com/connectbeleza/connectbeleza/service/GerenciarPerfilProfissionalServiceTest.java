package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.AgendaRequest;
import com.connectbeleza.connectbeleza.dto.request.GerenciarPerfilProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.request.ServicoRequest;
import com.connectbeleza.connectbeleza.dto.response.AgendaResponse;
import com.connectbeleza.connectbeleza.dto.response.ProfissionalResponse;
import com.connectbeleza.connectbeleza.dto.response.ServicoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.*;
import com.connectbeleza.connectbeleza.util.PaginacaoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciarPerfilProfissionalService")
class GerenciarPerfilProfissionalServiceTest {

    @Mock ProfissionalRepository profissionalRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock AgendaRepository agendaRepository;
    @Mock AgendamentoRepository agendamentoRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock ParceriaRepository parceriaRepository;
    @Mock ProfissionalService profissionalService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks GerenciarPerfilProfissionalService service;

    private UUID usuarioId;
    private UUID profissionalId;
    private UUID servicoId;
    private Usuario usuario;
    private Profissional profissional;
    private Servico servico;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();
        servicoId = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Carlos Esteta")
                .email("carlos@esteta.com")
                .role(UserRole.PROFISSIONAL)
                .ativo(true)
                .build();

        profissional = Profissional.builder()
                .id(profissionalId)
                .usuario(usuario)
                .bio("Especialista em pele")
                .anosExperiencia(5)
                .notaMedia(BigDecimal.ZERO)
                .totalAvaliacoes(0)
                .verificado(false)
                .build();

        servico = Servico.builder()
                .id(servicoId)
                .profissional(profissional)
                .nome("Limpeza de Pele")
                .descricao("Limpeza profunda")
                .categoria(CategoriaEstetica.PELE)
                .preco(new BigDecimal("150.00"))
                .duracaoMinutos(90)
                .ativo(true)
                .build();
    }

    // ─── ATUALIZAR PERFIL ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("atualizarPerfil")
    class AtualizarPerfil {

        @Test
        @DisplayName("deve atualizar perfil com sucesso")
        void deveAtualizarPerfilComSucesso() {
            var request = new GerenciarPerfilProfissionalRequest(
                    "Nova bio", 7, List.of(CategoriaEstetica.PELE), List.of("Cert A"),
                    "https://portfolio.com", "São Paulo, SP", -23.5, -46.6);
            var mockResponse = mock(ProfissionalResponse.class);

            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(profissionalRepository.save(any())).thenReturn(profissional);
            when(profissionalService.toResponse(any())).thenReturn(mockResponse);

            ProfissionalResponse response = service.atualizarPerfil(usuarioId, request);

            assertThat(response).isNotNull();
            assertThat(profissional.getBio()).isEqualTo("Nova bio");
            assertThat(profissional.getAnosExperiencia()).isEqualTo(7);
            assertThat(profissional.getLocalizacao()).isEqualTo("São Paulo, SP");
            assertThat(profissional.getLatitude()).isEqualByComparingTo(new BigDecimal("-23.5"));
        }

        @Test
        @DisplayName("não deve alterar coordenadas quando são null no request")
        void naoDeveAlterarCoordenadasQuandoNull() {
            var request = new GerenciarPerfilProfissionalRequest(
                    "Bio", 3, null, null, null, "SP", null, null);

            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(profissionalRepository.save(any())).thenReturn(profissional);
            when(profissionalService.toResponse(any())).thenReturn(mock(ProfissionalResponse.class));

            service.atualizarPerfil(usuarioId, request);

            assertThat(profissional.getLatitude()).isNull();
            assertThat(profissional.getLongitude()).isNull();
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando perfil não encontrado")
        void deveLancarExcecaoPerfilNaoEncontrado() {
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarPerfil(usuarioId,
                    new GerenciarPerfilProfissionalRequest("b", 1, null, null, null, "SP", null, null)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    // ─── CRIAR SERVIÇO ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("criarServico")
    class CriarServico {

        @Test
        @DisplayName("deve criar serviço com preço (include DEFINIR PREÇO)")
        void deveCriarServicoComPreco() {
            var request = new ServicoRequest("Limpeza", "desc", CategoriaEstetica.PELE,
                    new BigDecimal("150.00"), 90);

            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

            ServicoResponse response = service.criarServico(usuarioId, request);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Limpeza de Pele");
            assertThat(response.preco()).isEqualByComparingTo(new BigDecimal("150.00"));
            verify(servicoRepository).save(argThat(s -> s.getPreco().compareTo(new BigDecimal("150.00")) == 0));
        }

        @Test
        @DisplayName("deve lançar exceção quando profissional não encontrado")
        void deveLancarExcecaoProfissionalNaoEncontrado() {
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarServico(usuarioId,
                    new ServicoRequest("Corte", "d", CategoriaEstetica.CABELO, BigDecimal.TEN, 30)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    // ─── ATUALIZAR PREÇO ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("atualizarPreco")
    class AtualizarPreco {

        @Test
        @DisplayName("deve atualizar preço com sucesso")
        void deveAtualizarPrecoComSucesso() {
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(servicoRepository.save(any())).thenReturn(servico);

            ServicoResponse response = service.atualizarPreco(usuarioId, servicoId, new BigDecimal("200.00"));

            assertThat(servico.getPreco()).isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando preço é zero ou negativo")
        void deveLancarExcecaoPrecoInvalido() {
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> service.atualizarPreco(usuarioId, servicoId, BigDecimal.ZERO))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("maior que zero");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando preço é null")
        void deveLancarExcecaoPrecoNull() {
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> service.atualizarPreco(usuarioId, servicoId, null))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("maior que zero");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando serviço pertence a outro profissional")
        void deveLancarExcecaoServicoDeOutroProfissional() {
            Profissional outroProfissional = Profissional.builder().id(UUID.randomUUID()).usuario(usuario).build();
            servico.setProfissional(outroProfissional);

            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> service.atualizarPreco(usuarioId, servicoId, new BigDecimal("100.00")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não pertence ao seu perfil");
        }
    }

    // ─── DEFINIR AGENDA ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("definirAgenda")
    class DefinirAgenda {

        @Test
        @DisplayName("deve criar nova entrada de agenda para o dia")
        void deveCriarAgendaComSucesso() {
            var request = new AgendaRequest(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
            var agenda = Agenda.builder().id(UUID.randomUUID()).profissional(profissional)
                    .diaSemana(DayOfWeek.MONDAY).horaInicio(LocalTime.of(9, 0))
                    .horaFim(LocalTime.of(18, 0)).ativo(true).build();

            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(agendaRepository.findByProfissionalIdAndDiaSemana(profissionalId, DayOfWeek.MONDAY))
                    .thenReturn(Optional.empty());
            when(agendaRepository.save(any(Agenda.class))).thenReturn(agenda);

            AgendaResponse response = service.definirAgenda(usuarioId, request);

            assertThat(response).isNotNull();
            assertThat(response.diaSemana()).isEqualTo(DayOfWeek.MONDAY);
            verify(agendaRepository).save(any(Agenda.class));
        }

        @Test
        @DisplayName("deve fazer upsert quando já existe agenda para o dia")
        void deveFazerUpsertQuandoExisteAgendaParaODia() {
            var agendaExistente = Agenda.builder().id(UUID.randomUUID()).profissional(profissional)
                    .diaSemana(DayOfWeek.TUESDAY).horaInicio(LocalTime.of(8, 0))
                    .horaFim(LocalTime.of(17, 0)).ativo(true).build();
            var request = new AgendaRequest(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(19, 0));

            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));
            when(agendaRepository.findByProfissionalIdAndDiaSemana(profissionalId, DayOfWeek.TUESDAY))
                    .thenReturn(Optional.of(agendaExistente));
            when(agendaRepository.save(any())).thenReturn(agendaExistente);

            service.definirAgenda(usuarioId, request);

            verify(agendaRepository).save(argThat(a ->
                    a.getHoraInicio().equals(LocalTime.of(10, 0)) &&
                    a.getHoraFim().equals(LocalTime.of(19, 0))));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando hora início >= hora fim")
        void deveLancarExcecaoHoraInicioMaiorQueFim() {
            var request = new AgendaRequest(DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(9, 0));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> service.definirAgenda(usuarioId, request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Hora de início deve ser anterior");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando hora início igual hora fim")
        void deveLancarExcecaoHoraInicioIgualFim() {
            var request = new AgendaRequest(DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(10, 0));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            assertThatThrownBy(() -> service.definirAgenda(usuarioId, request))
                    .isInstanceOf(RegraDeNegocioException.class);
        }
    }

    // ─── DESATIVAR SERVIÇO ────────────────────────────────────────────────────

    @Nested
    @DisplayName("desativarServico")
    class DesativarServico {

        @Test
        @DisplayName("deve desativar serviço (soft delete)")
        void deveDesativarServico() {
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            service.desativarServico(usuarioId, servicoId);

            assertThat(servico.getAtivo()).isFalse();
            verify(servicoRepository).save(servico);
        }
    }
}
