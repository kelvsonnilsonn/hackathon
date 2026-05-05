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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UsuarioResponse criarConta(CriarContaRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraDeNegocioException("Email já cadastrado: " + request.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .telefone(request.telefone())
                .role(request.role() != null ? request.role() : UserRole.CLIENTE)
                .build();

        usuario = usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RegraDeNegocioException("Email ou senha incorretos"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new RegraDeNegocioException("Email ou senha incorretos");
        }

        if (!usuario.getAtivo()) {
            throw new RegraDeNegocioException("Conta desativada. Entre em contato com o suporte.");
        }

        String token = jwtUtil.gerarToken(usuario.getEmail(), usuario.getRole().name());
        return AuthResponse.of(token, toResponse(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarEntidadePorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário com email " + email + " não encontrado"));
    }

    public UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(),
                u.getTelefone(), u.getRole(), u.getAtivo(), u.getCriadoEm()
        );
    }
}