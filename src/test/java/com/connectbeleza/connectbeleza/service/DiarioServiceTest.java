package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Diario;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.dto.response.DiarioResponse;
import com.connectbeleza.connectbeleza.repository.DiarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiarioServiceTest {

    @Mock
    private DiarioRepository diarioRepository;

    @InjectMocks
    private DiarioService diarioService;

    private UUID id;
    private Usuario autor;
    private Diario diario;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        autor = new Usuario();
        autor.setId(UUID.randomUUID());

        diario = new Diario();
        diario.setId(id);
        diario.setAutor(autor);
        diario.setConteudo("Conteúdo de teste");
        diario.setCriadoEm(LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("deve salvar e retornar DiarioResponse corretamente")
        void deveSalvarERetornarResponse() {
            when(diarioRepository.save(any(Diario.class))).thenReturn(diario);

            DiarioResponse response = diarioService.criar(autor, "Conteúdo de teste");

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.conteudo()).isEqualTo("Conteúdo de teste");
            verify(diarioRepository, times(1)).save(any(Diario.class));
        }
    }

    // -------------------------------------------------------------------------
    // READ (todos)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve retornar lista de DiarioResponse quando existem registros")
        void deveRetornarListaQuandoExistemRegistros() {
            when(diarioRepository.findAll()).thenReturn(List.of(diario));

            List<DiarioResponse> lista = diarioService.listar();

            assertThat(lista).hasSize(1);
            assertThat(lista.get(0).id()).isEqualTo(id);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não existem registros")
        void deveRetornarListaVaziaQuandoSemRegistros() {
            when(diarioRepository.findAll()).thenReturn(List.of());

            List<DiarioResponse> lista = diarioService.listar();

            assertThat(lista).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // READ (por id)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar DiarioResponse quando id existe")
        void deveRetornarResponseQuandoIdExiste() {
            when(diarioRepository.findById(id)).thenReturn(Optional.of(diario));

            DiarioResponse response = diarioService.buscarPorId(id);

            assertThat(response.id()).isEqualTo(id);
            assertThat(response.conteudo()).isEqualTo("Conteúdo de teste");
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando id não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(diarioRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> diarioService.buscarPorId(id))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar conteúdo e retornar response atualizado")
        void deveAtualizarConteudoERetornarResponse() {
            when(diarioRepository.findById(id)).thenReturn(Optional.of(diario));
            when(diarioRepository.save(diario)).thenReturn(diario);

            DiarioResponse response = diarioService.atualizar(id, "Novo conteúdo");

            assertThat(response.conteudo()).isEqualTo("Novo conteúdo");
            verify(diarioRepository).save(diario);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando id não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(diarioRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> diarioService.atualizar(id, "Qualquer coisa"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve deletar quando id existe")
        void deveDeletarQuandoIdExiste() {
            when(diarioRepository.existsById(id)).thenReturn(true);

            diarioService.deletar(id);

            verify(diarioRepository).deleteById(id);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando id não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(diarioRepository.existsById(id)).thenReturn(false);

            assertThatThrownBy(() -> diarioService.deletar(id))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(diarioRepository, never()).deleteById(any());
        }
    }

    // -------------------------------------------------------------------------
    // PRIVACIDADE
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("alternarPrivacidade()")
    class AlternarPrivacidade {

        @Test
        @DisplayName("deve chamar mudarPrivacidade() e salvar")
        void deveChamarMudarPrivacidadeESalvar() {
            Diario spy = spy(diario);
            when(diarioRepository.findById(id)).thenReturn(Optional.of(spy));

            diarioService.alternarPrivacidade(id);

            verify(spy).mudarPrivacidade();
            verify(diarioRepository).save(spy);
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando id não existe")
        void deveLancarExcecaoQuandoIdNaoExiste() {
            when(diarioRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> diarioService.alternarPrivacidade(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}