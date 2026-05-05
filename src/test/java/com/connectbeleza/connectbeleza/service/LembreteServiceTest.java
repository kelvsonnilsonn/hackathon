package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Lembrete;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.TipoLembrete;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.LembreteRequest;
import com.connectbeleza.connectbeleza.dto.response.LembreteResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.LembreteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LembreteService")
class LembreteServiceTest {

    @Mock LembreteRepository lembreteRepository;
    @Mock UsuarioService usuarioService;

    @InjectMocks LembreteService lembreteService;

    private UUID usuarioId;
    private UUID lembreteId;
    private Usuario usuario;
    private Lembrete lembrete;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        lembreteId = UUID.randomUUID();

        usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Carla Santos")
                .email("carla@email.com")
                .role(UserRole.CLIENTE)
                .ativo(true)
                .build();

        lembrete = Lembrete.builder()
                .id(lembreteId)
                .usuario(usuario)
                .tipo(TipoLembrete.MANHA)
                .mensagem("Bom dia! Hidrate-se!")
                .horaEnvio("08:00")
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // ─── CONFIGURAR LEMBRETE ──────────────────────────────────────────────────

    @Nested
    @DisplayName("configurarLembrete")
    class ConfigurarLembrete {

        @Test
        @DisplayName("deve criar lembrete personalizado com sucesso")
        void deveCriarLembreteComSucesso() {
            var request = new LembreteRequest(TipoLembrete.PERSONALIZADO, "Fazer esfoliação!", "20:00");

            when(usuarioService.buscarEntidadePorId(usuarioId)).thenReturn(usuario);
            when(lembreteRepository.save(any(Lembrete.class))).thenReturn(lembrete);

            LembreteResponse response = lembreteService.configurarLembrete(usuarioId, request);

            assertThat(response).isNotNull();
            verify(lembreteRepository).save(any(Lembrete.class));
        }

        @Test
        @DisplayName("deve salvar com tipo e hora corretos")
        void deveSalvarComTipoEHoraCorretos() {
            var request = new LembreteRequest(TipoLembrete.NOITE, "Skincare noturno", "21:30");

            when(usuarioService.buscarEntidadePorId(usuarioId)).thenReturn(usuario);
            when(lembreteRepository.save(any())).thenReturn(lembrete);

            lembreteService.configurarLembrete(usuarioId, request);

            ArgumentCaptor<Lembrete> captor = ArgumentCaptor.forClass(Lembrete.class);
            verify(lembreteRepository).save(captor.capture());
            assertThat(captor.getValue().getTipo()).isEqualTo(TipoLembrete.NOITE);
            assertThat(captor.getValue().getHoraEnvio()).isEqualTo("21:30");
            assertThat(captor.getValue().getMensagem()).isEqualTo("Skincare noturno");
        }
    }

    // ─── LISTAR LEMBRETES ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarMeusLembretes")
    class ListarMeusLembretes {

        @Test
        @DisplayName("deve retornar apenas lembretes ativos do usuário")
        void deveRetornarLembretesAtivos() {
            when(lembreteRepository.findByUsuarioIdAndAtivoTrue(usuarioId))
                    .thenReturn(List.of(lembrete));

            List<LembreteResponse> result = lembreteService.listarMeusLembretes(usuarioId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).tipo()).isEqualTo(TipoLembrete.MANHA);
            assertThat(result.get(0).ativo()).isTrue();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há lembretes ativos")
        void deveRetornarListaVazia() {
            when(lembreteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(List.of());

            List<LembreteResponse> result = lembreteService.listarMeusLembretes(usuarioId);

            assertThat(result).isEmpty();
        }
    }

    // ─── DESATIVAR LEMBRETE ───────────────────────────────────────────────────

    @Nested
    @DisplayName("desativarLembrete")
    class DesativarLembrete {

        @Test
        @DisplayName("deve desativar lembrete quando pertence ao usuário")
        void deveDesativarLembrete() {
            when(lembreteRepository.findById(lembreteId)).thenReturn(Optional.of(lembrete));

            lembreteService.desativarLembrete(usuarioId, lembreteId);

            assertThat(lembrete.getAtivo()).isFalse();
            verify(lembreteRepository).save(lembrete);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando lembrete não encontrado")
        void deveLancarExcecaoLembreteNaoEncontrado() {
            when(lembreteRepository.findById(lembreteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lembreteService.desativarLembrete(usuarioId, lembreteId))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando lembrete pertence a outro usuário")
        void deveLancarExcecaoLembreteDeOutroUsuario() {
            UUID outroUsuario = UUID.randomUUID();
            when(lembreteRepository.findById(lembreteId)).thenReturn(Optional.of(lembrete));

            assertThatThrownBy(() -> lembreteService.desativarLembrete(outroUsuario, lembreteId))
                    .isInstanceOf(RecursoNaoEncontradoException.class)
                    .hasMessageContaining("não pertence a este usuário");
        }
    }

    // ─── CRIAR LEMBRETES DEFAULT ──────────────────────────────────────────────

    @Nested
    @DisplayName("criarLembretesDefault")
    class CriarLembretesDefault {

        @Test
        @DisplayName("deve criar 3 lembretes padrão (manhã, tarde, noite)")
        void deveCriarTresLembretesPadrao() {
            lembreteService.criarLembretesDefault(usuario);

            ArgumentCaptor<Lembrete> captor = ArgumentCaptor.forClass(Lembrete.class);
            verify(lembreteRepository, times(3)).save(captor.capture());

            List<Lembrete> salvos = captor.getAllValues();
            assertThat(salvos).extracting(Lembrete::getTipo)
                    .containsExactlyInAnyOrder(TipoLembrete.MANHA, TipoLembrete.TARDE, TipoLembrete.NOITE);
            assertThat(salvos).extracting(Lembrete::getHoraEnvio)
                    .containsExactlyInAnyOrder("08:00", "13:00", "21:00");
        }

        @Test
        @DisplayName("todos os lembretes padrão devem estar ativos")
        void todosDevemEstarAtivos() {
            when(lembreteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            lembreteService.criarLembretesDefault(usuario);

            ArgumentCaptor<Lembrete> captor = ArgumentCaptor.forClass(Lembrete.class);
            verify(lembreteRepository, times(3)).save(captor.capture());

            captor.getAllValues().forEach(l -> assertThat(l.getAtivo()).isTrue());
        }
    }

    // ─── DISPARAR LEMBRETES ───────────────────────────────────────────────────

    @Nested
    @DisplayName("dispararLembretes")
    class DispararLembretes {

        @Test
        @DisplayName("deve buscar lembretes ativos para o horário atual sem lançar exceção")
        void deveBuscarLembretesParaHorarioAtual() {
            when(lembreteRepository.findByAtivoTrueAndHoraEnvio(any())).thenReturn(List.of(lembrete));

            assertThatCode(() -> lembreteService.dispararLembretes()).doesNotThrowAnyException();

            verify(lembreteRepository).findByAtivoTrueAndHoraEnvio(any());
        }

        @Test
        @DisplayName("deve funcionar sem lembretes para disparar")
        void deveFuncionarSemLembretes() {
            when(lembreteRepository.findByAtivoTrueAndHoraEnvio(any())).thenReturn(List.of());

            assertThatCode(() -> lembreteService.dispararLembretes()).doesNotThrowAnyException();
        }
    }
}
