package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.CriarContaRequest;
import com.connectbeleza.connectbeleza.dto.request.LoginRequest;
import com.connectbeleza.connectbeleza.dto.response.AuthResponse;
import com.connectbeleza.connectbeleza.dto.response.UsuarioResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.exception.RegraDeNegocioException;
import com.connectbeleza.connectbeleza.repository.UsuarioRepository;
import com.connectbeleza.connectbeleza.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService")
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;

    @InjectMocks UsuarioService usuarioService;

    private UUID usuarioId;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Ana Lima")
                .email("ana@email.com")
                .senha("$2a$encoded")
                .telefone("11999999999")
                .role(UserRole.CLIENTE)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // ─── CRIAR CONTA ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("criarConta")
    class CriarConta {

        @Test
        @DisplayName("deve criar conta com sucesso quando email não existe")
        void deveCriarContaComSucesso() {
            var request = new CriarContaRequest("Ana Lima", "ana@email.com", "senha123", "11999999999", UserRole.CLIENTE);
            when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponse response = usuarioService.criarConta(request);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Ana Lima");
            assertThat(response.email()).isEqualTo("ana@email.com");
            assertThat(response.role()).isEqualTo(UserRole.CLIENTE);
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("deve usar role CLIENTE como padrão quando role não informado")
        void deveUsarRoleClientePadrao() {
            var request = new CriarContaRequest("Ana Lima", "ana@email.com", "senha123", "11999999999", null);
            when(usuarioRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                assertThat(u.getRole()).isEqualTo(UserRole.CLIENTE);
                return usuario;
            });

            usuarioService.criarConta(request);

            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando email já cadastrado")
        void deveLancarExcecaoEmailDuplicado() {
            var request = new CriarContaRequest("Ana Lima", "ana@email.com", "senha123", "11999999999", UserRole.CLIENTE);
            when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.criarConta(request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Email já cadastrado");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve codificar a senha antes de salvar")
        void deveCodificarSenha() {
            var request = new CriarContaRequest("Ana Lima", "ana@email.com", "senha123", "11999999999", UserRole.CLIENTE);
            when(usuarioRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("$2a$encoded");
            when(usuarioRepository.save(any())).thenReturn(usuario);

            usuarioService.criarConta(request);

            verify(passwordEncoder).encode("senha123");
        }
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("deve retornar token JWT quando credenciais válidas")
        void deveRetornarTokenJwtComSucesso() {
            var request = new LoginRequest("ana@email.com", "senha123");
            when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("senha123", "$2a$encoded")).thenReturn(true);
            when(jwtUtil.gerarToken("ana@email.com", "CLIENTE")).thenReturn("jwt-token-abc");

            AuthResponse response = usuarioService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token-abc");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando email não encontrado")
        void deveLancarExcecaoEmailNaoEncontrado() {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.login(new LoginRequest("nao@existe.com", "abc")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Email ou senha incorretos");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando senha incorreta")
        void deveLancarExcecaoSenhaIncorreta() {
            when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.login(new LoginRequest("ana@email.com", "errada")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Email ou senha incorretos");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando conta desativada")
        void deveLancarExcecaoContaDesativada() {
            usuario.setAtivo(false);
            when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.login(new LoginRequest("ana@email.com", "senha123")))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Conta desativada");
        }
    }

    // ─── BUSCAR ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar UsuarioResponse quando id existe")
        void deveRetornarUsuario() {
            when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

            UsuarioResponse response = usuarioService.buscarPorId(usuarioId);

            assertThat(response.id()).isEqualTo(usuarioId);
            assertThat(response.nome()).isEqualTo("Ana Lima");
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando id não existe")
        void deveLancarExcecaoIdNaoEncontrado() {
            when(usuarioRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarPorId(UUID.randomUUID()))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("buscarPorEmail")
    class BuscarPorEmail {

        @Test
        @DisplayName("deve retornar entidade quando email existe")
        void deveRetornarEntidade() {
            when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));

            Usuario resultado = usuarioService.buscarPorEmail("ana@email.com");

            assertThat(resultado.getEmail()).isEqualTo("ana@email.com");
        }

        @Test
        @DisplayName("deve lançar exceção quando email não encontrado")
        void deveLancarExcecao() {
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.buscarPorEmail("x@x.com"))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }
}
