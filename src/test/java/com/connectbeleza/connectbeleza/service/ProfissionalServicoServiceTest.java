package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Profissional;
import com.connectbeleza.connectbeleza.domain.entity.Servico;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.BuscarProfissionalRequest;
import com.connectbeleza.connectbeleza.dto.response.ProfissionalResponse;
import com.connectbeleza.connectbeleza.dto.response.ServicoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.ProfissionalRepository;
import com.connectbeleza.connectbeleza.repository.ServicoRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


// ─── PROFISSIONAL SERVICE ─────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfissionalService")
class ProfissionalServiceTest {

    @Mock ProfissionalRepository profissionalRepository;
    @Mock UsuarioService usuarioService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks ProfissionalService profissionalService;

    private UUID profissionalId;
    private UUID usuarioId;
    private Usuario usuario;
    private Profissional profissional;

    @BeforeEach
    void setUp() {
        profissionalId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(usuarioId).nome("Fernanda Melo").email("fernanda@email.com")
                .role(UserRole.PROFISSIONAL).ativo(true).build();

        profissional = Profissional.builder()
                .id(profissionalId).usuario(usuario)
                .bio("Especialista em cabelos").anosExperiencia(8)
                .notaMedia(new BigDecimal("4.80")).totalAvaliacoes(50)
                .verificado(true).localizacao("São Paulo, SP").build();
    }

    @Nested
    @DisplayName("buscar")
    class Buscar {

        @Test
        @DisplayName("deve buscar por localização quando lat/lng/raio fornecidos")
        void deveBuscarPorLocalizacao() {
            var filtro = new BuscarProfissionalRequest(null, CategoriaEstetica.CABELO, -23.5, -46.6, 10.0);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Profissional> page = new PageImpl<>(List.of(profissional));

            when(paginacaoUtil.build(0, 10, "notaMedia", "desc")).thenReturn(pageable);
            when(profissionalRepository.buscarPorLocalizacao(-23.5, -46.6, 10.0, CategoriaEstetica.CABELO, pageable))
                    .thenReturn(page);

            Page<ProfissionalResponse> result = profissionalService.buscar(filtro, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(profissionalRepository).buscarPorLocalizacao(any(), any(), any(), any(), any());
            verify(profissionalRepository, never()).buscarPorFiltros(any(), any(), any());
        }

        @Test
        @DisplayName("deve buscar por filtros quando sem geo")
        void deveBuscarPorFiltros() {
            var filtro = new BuscarProfissionalRequest("Fernanda", CategoriaEstetica.CABELO, null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Profissional> page = new PageImpl<>(List.of(profissional));

            when(paginacaoUtil.build(0, 10, "notaMedia", "desc")).thenReturn(pageable);
            when(profissionalRepository.buscarPorFiltros(CategoriaEstetica.CABELO, "Fernanda", pageable)).thenReturn(page);

            profissionalService.buscar(filtro, 0, 10);

            verify(profissionalRepository).buscarPorFiltros(CategoriaEstetica.CABELO, "Fernanda", pageable);
            verify(profissionalRepository, never()).buscarPorLocalizacao(any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar ProfissionalResponse por id")
        void deveRetornarProfissional() {
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));

            ProfissionalResponse response = profissionalService.buscarPorId(profissionalId);

            assertThat(response.id()).isEqualTo(profissionalId);
            assertThat(response.nome()).isEqualTo("Fernanda Melo");
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando não encontrado")
        void deveLancarExcecao() {
            when(profissionalRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profissionalService.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("buscarEntidadePorUsuarioId")
    class BuscarEntidadePorUsuarioId {

        @Test
        @DisplayName("deve retornar profissional pelo usuarioId")
        void deveRetornarProfissional() {
            when(profissionalRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(profissional));

            Profissional resultado = profissionalService.buscarEntidadePorUsuarioId(usuarioId);

            assertThat(resultado.getId()).isEqualTo(profissionalId);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando usuário não tem perfil")
        void deveLancarExcecaoPerfilNaoEncontrado() {
            when(profissionalRepository.findByUsuarioId(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> profissionalService.buscarEntidadePorUsuarioId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("Perfil profissional não encontrado");
        }
    }

    @Nested
    @DisplayName("atualizarNotaMedia")
    class AtualizarNotaMedia {

        @Test
        @DisplayName("deve atualizar nota média e total de avaliações")
        void deveAtualizarNotaMedia() {
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));

            profissionalService.atualizarNotaMedia(profissionalId, 4.75, 10L);

            assertThat(profissional.getNotaMedia()).isEqualByComparingTo(new BigDecimal("4.75"));
            assertThat(profissional.getTotalAvaliacoes()).isEqualTo(10);
            verify(profissionalRepository).save(profissional);
        }

        @Test
        @DisplayName("deve arredondar nota média para 2 casas decimais")
        void deveArredondarNotaMedia() {
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));

            profissionalService.atualizarNotaMedia(profissionalId, 4.666666, 3L);

            assertThat(profissional.getNotaMedia().scale()).isEqualTo(2);
            assertThat(profissional.getNotaMedia()).isEqualByComparingTo(new BigDecimal("4.67"));
        }
    }
}


// ─── SERVICO SERVICE ──────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoService")
class ServicoServiceTest {

    @Mock ServicoRepository servicoRepository;
    @Mock ProfissionalService profissionalService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks ServicoService servicoService;

    private UUID profissionalId;
    private UUID servicoId;
    private Profissional profissional;
    private Servico servico;

    @BeforeEach
    void setUp() {
        profissionalId = UUID.randomUUID();
        servicoId = UUID.randomUUID();

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID()).nome("Gabi Nunes").build();

        profissional = Profissional.builder()
                .id(profissionalId).usuario(usuario).build();

        servico = Servico.builder()
                .id(servicoId).profissional(profissional)
                .nome("Manicure").descricao("Esmaltação em gel")
                .categoria(CategoriaEstetica.UNHAS)
                .preco(new BigDecimal("70.00")).duracaoMinutos(60)
                .ativo(true).criadoEm(LocalDateTime.now()).build();
    }

    @Nested
    @DisplayName("listarPorProfissional")
    class ListarPorProfissional {

        @Test
        @DisplayName("deve retornar serviços ativos do profissional")
        void deveRetornarServicosAtivos() {
            var pageable = PageRequest.of(0, 10);
            Page<Servico> page = new PageImpl<>(List.of(servico));

            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(servicoRepository.findByProfissionalIdAndAtivoTrue(profissionalId, pageable)).thenReturn(page);

            Page<ServicoResponse> result = servicoService.listarPorProfissional(profissionalId, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).nome()).isEqualTo("Manicure");
            verify(profissionalService).buscarEntidadePorId(profissionalId);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar ServicoResponse quando id existe")
        void deveRetornarServico() {
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));

            ServicoResponse response = servicoService.buscarPorId(servicoId);

            assertThat(response.id()).isEqualTo(servicoId);
            assertThat(response.categoria()).isEqualTo(CategoriaEstetica.UNHAS);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existe")
        void deveLancarExcecao() {
            when(servicoRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> servicoService.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("deve mapear todos os campos corretamente")
        void deveMapearCampos() {
            ServicoResponse response = servicoService.toResponse(servico);

            assertThat(response.id()).isEqualTo(servicoId);
            assertThat(response.profissionalId()).isEqualTo(profissionalId);
            assertThat(response.nomeProfissional()).isEqualTo("Gabi Nunes");
            assertThat(response.nome()).isEqualTo("Manicure");
            assertThat(response.preco()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(response.duracaoMinutos()).isEqualTo(60);
            assertThat(response.ativo()).isTrue();
        }
    }
}
