package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.RespostaRequest;
import com.connectbeleza.connectbeleza.dto.request.TopicoRequest;
import com.connectbeleza.connectbeleza.dto.response.ForumResponse;
import com.connectbeleza.connectbeleza.dto.response.RespostaResponse;
import com.connectbeleza.connectbeleza.dto.response.TopicoResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.ForumRepository;
import com.connectbeleza.connectbeleza.repository.RespostaRepository;
import com.connectbeleza.connectbeleza.repository.TopicoRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ─── FORUM SERVICE ────────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
@DisplayName("ForumService")
class ForumServiceTest {

    @Mock ForumRepository forumRepository;

    @InjectMocks ForumService forumService;

    private UUID forumId;
    private Forum forum;

    @BeforeEach
    void setUp() {
        forumId = UUID.randomUUID();
        forum = Forum.builder()
                .id(forumId)
                .nome("Fórum de Cabelo")
                .descricao("Dicas e tendências de cabelo")
                .categoria(CategoriaEstetica.CABELO)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("deve retornar apenas fóruns ativos")
        void deveRetornarForunsAtivos() {
            Forum forumInativo = Forum.builder().id(UUID.randomUUID())
                    .nome("Inativo").categoria(CategoriaEstetica.PELE).ativo(false).build();
            when(forumRepository.findAll()).thenReturn(List.of(forum, forumInativo));

            List<ForumResponse> result = forumService.listarTodos();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).categoria()).isEqualTo(CategoriaEstetica.CABELO);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há fóruns ativos")
        void deveRetornarListaVazia() {
            when(forumRepository.findAll()).thenReturn(List.of());

            assertThat(forumService.listarTodos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorCategoria")
    class BuscarPorCategoria {

        @Test
        @DisplayName("deve retornar fórum ativo pela categoria")
        void deveRetornarForumPorCategoria() {
            when(forumRepository.findByCategoria(CategoriaEstetica.CABELO)).thenReturn(Optional.of(forum));

            ForumResponse response = forumService.buscarPorCategoria(CategoriaEstetica.CABELO);

            assertThat(response.categoria()).isEqualTo(CategoriaEstetica.CABELO);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando fórum não encontrado")
        void deveLancarExcecaoForumNaoEncontrado() {
            when(forumRepository.findByCategoria(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> forumService.buscarPorCategoria(CategoriaEstetica.UNHAS))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando fórum está inativo")
        void deveLancarExcecaoForumInativo() {
            forum.setAtivo(false);
            when(forumRepository.findByCategoria(CategoriaEstetica.CABELO)).thenReturn(Optional.of(forum));

            assertThatThrownBy(() -> forumService.buscarPorCategoria(CategoriaEstetica.CABELO))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar fórum por id")
        void deveRetornarForum() {
            when(forumRepository.findById(forumId)).thenReturn(Optional.of(forum));

            ForumResponse response = forumService.buscarPorId(forumId);

            assertThat(response.id()).isEqualTo(forumId);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existe")
        void deveLancarExcecaoIdNaoExiste() {
            when(forumRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> forumService.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }
}


// ─── TOPICO SERVICE ───────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
@DisplayName("TopicoService")
class TopicoServiceTest {

    @Mock TopicoRepository topicoRepository;
    @Mock ForumService forumService;
    @Mock UsuarioService usuarioService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks TopicoService topicoService;

    private UUID autorId;
    private UUID forumId;
    private UUID topicoId;
    private Usuario autor;
    private Forum forum;
    private Topico topico;

    @BeforeEach
    void setUp() {
        autorId = UUID.randomUUID();
        forumId = UUID.randomUUID();
        topicoId = UUID.randomUUID();

        autor = Usuario.builder()
                .id(autorId).nome("Bruna Teixeira")
                .email("bruna@email.com").role(UserRole.CLIENTE).ativo(true).build();

        forum = Forum.builder()
                .id(forumId).nome("Fórum de Pele")
                .categoria(CategoriaEstetica.PELE).ativo(true).build();

        topico = Topico.builder()
                .id(topicoId).forum(forum).autor(autor)
                .titulo("Como tratar acne?")
                .conteudo("Preciso de dicas para tratar acne hormonal.")
                .fixado(false).fechado(false).totalRespostas(0)
                .criadoEm(LocalDateTime.now()).atualizadoEm(LocalDateTime.now()).build();
    }

    @Nested
    @DisplayName("criarTopico")
    class CriarTopico {

        @Test
        @DisplayName("deve criar tópico com sucesso em fórum ativo")
        void deveCriarTopicoComSucesso() {
            var request = new TopicoRequest(forumId, "Como tratar acne?", "Dicas para acne hormonal.");

            when(forumService.buscarEntidadePorId(forumId)).thenReturn(forum);
            when(usuarioService.buscarEntidadePorId(autorId)).thenReturn(autor);
            when(topicoRepository.save(any(Topico.class))).thenReturn(topico);

            TopicoResponse response = topicoService.criarTopico(autorId, request);

            assertThat(response).isNotNull();
            assertThat(response.titulo()).isEqualTo("Como tratar acne?");
            verify(topicoRepository).save(any(Topico.class));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando fórum está inativo")
        void deveLancarExcecaoForumInativo() {
            forum.setAtivo(false);
            when(forumService.buscarEntidadePorId(forumId)).thenReturn(forum);

            assertThatThrownBy(() -> topicoService.criarTopico(autorId,
                    new TopicoRequest(forumId, "titulo", "conteudo")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("fechado para novas publicações");
        }
    }

    @Nested
    @DisplayName("listarPorForum")
    class ListarPorForum {

        @Test
        @DisplayName("deve listar tópicos do fórum com paginação")
        void deveListarTopicosPaginados() {
            var pageable = PageRequest.of(0, 10);
            Page<Topico> page = new PageImpl<>(List.of(topico));

            when(forumService.buscarEntidadePorId(forumId)).thenReturn(forum);
            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(topicoRepository.findByForumIdOrderByFixadoDescCriadoEmDesc(forumId, pageable)).thenReturn(page);

            Page<TopicoResponse> result = topicoService.listarPorForum(forumId, null, 0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("deve usar busca por termo quando termo fornecido")
        void deveUsarBuscaPorTermoQuandoFornecido() {
            var pageable = PageRequest.of(0, 10);
            Page<Topico> page = new PageImpl<>(List.of(topico));

            when(forumService.buscarEntidadePorId(forumId)).thenReturn(forum);
            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(topicoRepository.buscarPorTermo(forumId, "acne", pageable)).thenReturn(page);

            topicoService.listarPorForum(forumId, "acne", 0, 10);

            verify(topicoRepository).buscarPorTermo(forumId, "acne", pageable);
            verify(topicoRepository, never()).findByForumIdOrderByFixadoDescCriadoEmDesc(any(), any());
        }
    }

    @Nested
    @DisplayName("incrementarRespostas")
    class IncrementarRespostas {

        @Test
        @DisplayName("deve incrementar contador de respostas do tópico")
        void deveIncrementarContador() {
            topico.setTotalRespostas(2);
            when(topicoRepository.findById(topicoId)).thenReturn(Optional.of(topico));

            topicoService.incrementarRespostas(topicoId);

            assertThat(topico.getTotalRespostas()).isEqualTo(3);
            verify(topicoRepository).save(topico);
        }
    }
}


// ─── RESPOSTA SERVICE ─────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
@DisplayName("RespostaService")
class RespostaServiceTest {

    @Mock RespostaRepository respostaRepository;
    @Mock TopicoService topicoService;
    @Mock UsuarioService usuarioService;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks RespostaService respostaService;

    private UUID autorId;
    private UUID topicoId;
    private Usuario autor;
    private Forum forum;
    private Topico topico;
    private Resposta resposta;

    @BeforeEach
    void setUp() {
        autorId = UUID.randomUUID();
        topicoId = UUID.randomUUID();

        autor = Usuario.builder()
                .id(autorId).nome("Diego Ferreira")
                .email("diego@email.com").role(UserRole.CLIENTE).ativo(true).build();

        forum = Forum.builder()
                .id(UUID.randomUUID()).nome("Fórum Cabelo")
                .categoria(CategoriaEstetica.CABELO).ativo(true).build();

        topico = Topico.builder()
                .id(topicoId).forum(forum).autor(autor)
                .titulo("Qual shampoo usar?").conteudo("Preciso de indicações.")
                .fechado(false).totalRespostas(0).criadoEm(LocalDateTime.now()).build();

        resposta = Resposta.builder()
                .id(UUID.randomUUID()).topico(topico).autor(autor)
                .conteudo("Use shampoo sem sulfato.")
                .totalCurtidas(0).criadoEm(LocalDateTime.now()).build();
    }

    @Nested
    @DisplayName("responder")
    class Responder {

        @Test
        @DisplayName("deve registrar resposta em tópico aberto")
        void deveResponderComSucesso() {
            var request = new RespostaRequest("Use shampoo sem sulfato.");

            when(topicoService.buscarEntidadePorId(topicoId)).thenReturn(topico);
            when(usuarioService.buscarEntidadePorId(autorId)).thenReturn(autor);
            when(respostaRepository.save(any(Resposta.class))).thenReturn(resposta);

            RespostaResponse response = respostaService.responder(autorId, topicoId, request);

            assertThat(response).isNotNull();
            assertThat(response.conteudo()).isEqualTo("Use shampoo sem sulfato.");
            verify(topicoService).incrementarRespostas(topicoId);
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando tópico está fechado")
        void deveLancarExcecaoTopicoFechado() {
            topico.setFechado(true);
            when(topicoService.buscarEntidadePorId(topicoId)).thenReturn(topico);

            assertThatThrownBy(() -> respostaService.responder(autorId, topicoId, new RespostaRequest("resposta")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("fechado para novas respostas");

            verify(respostaRepository, never()).save(any());
            verify(topicoService, never()).incrementarRespostas(any());
        }

        @Test
        @DisplayName("deve incrementar contador de respostas após salvar")
        void deveIncrementarContadorAposSalvar() {
            when(topicoService.buscarEntidadePorId(topicoId)).thenReturn(topico);
            when(usuarioService.buscarEntidadePorId(autorId)).thenReturn(autor);
            when(respostaRepository.save(any())).thenReturn(resposta);

            respostaService.responder(autorId, topicoId, new RespostaRequest("conteúdo válido"));

            verify(topicoService).incrementarRespostas(topicoId);
        }
    }

    @Nested
    @DisplayName("listarPorTopico")
    class ListarPorTopico {

        @Test
        @DisplayName("deve listar respostas em ordem cronológica ascendente")
        void deveListarRespostasOrdenadas() {
            var pageable = PageRequest.of(0, 20);
            Page<Resposta> page = new PageImpl<>(List.of(resposta));

            when(topicoService.buscarEntidadePorId(topicoId)).thenReturn(topico);
            when(paginacaoUtil.build(0, 20)).thenReturn(pageable);
            when(respostaRepository.findByTopicoIdOrderByCriadoEmAsc(topicoId, pageable)).thenReturn(page);

            Page<RespostaResponse> result = respostaService.listarPorTopico(topicoId, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).conteudo()).isEqualTo("Use shampoo sem sulfato.");
        }
    }
}
