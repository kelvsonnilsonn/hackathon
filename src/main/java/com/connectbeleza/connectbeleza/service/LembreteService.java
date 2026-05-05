package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.Lembrete;
import com.connectbeleza.connectbeleza.domain.entity.Usuario;
import com.connectbeleza.connectbeleza.domain.enums.TipoLembrete;
import com.connectbeleza.connectbeleza.dto.request.LembreteRequest;
import com.connectbeleza.connectbeleza.dto.response.LembreteResponse;
import com.connectbeleza.connectbeleza.exception.RecursoNaoEncontradoException;
import com.connectbeleza.connectbeleza.repository.LembreteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: RECEBER LEMBRETE (ator Sistema)
 * O sistema dispara lembretes inteligentes de manhã, tarde e noite.
 * O usuário pode também configurar lembretes personalizados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LembreteService {

    private final LembreteRepository lembreteRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public LembreteResponse configurarLembrete(UUID usuarioId, LembreteRequest request) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);

        Lembrete lembrete = Lembrete.builder()
                .usuario(usuario)
                .tipo(request.tipo())
                .mensagem(request.mensagem())
                .horaEnvio(request.horaEnvio())
                .build();

        return toResponse(lembreteRepository.save(lembrete));
    }

    @Transactional(readOnly = true)
    public List<LembreteResponse> listarMeusLembretes(UUID usuarioId) {
        return lembreteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void desativarLembrete(UUID usuarioId, UUID lembreteId) {
        Lembrete lembrete = lembreteRepository.findById(lembreteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lembrete", lembreteId));
        if (!lembrete.getUsuario().getId().equals(usuarioId)) {
            throw new RecursoNaoEncontradoException("Lembrete não pertence a este usuário.");
        }
        lembrete.setAtivo(false);
        lembreteRepository.save(lembrete);
    }

    /**
     * Scheduled job: Sistema dispara lembretes a cada minuto,
     * verificando quais usuários têm lembrete para o horário atual.
     * Em produção, substituir por push notification / e-mail / SMS.
     */
    @Scheduled(cron = "0 * * * * *") // todo minuto
    public void dispararLembretes() {
        String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        List<Lembrete> lembretes = lembreteRepository.findByAtivoTrueAndHoraEnvio(horaAtual);

        for (Lembrete l : lembretes) {
            log.info("[LEMBRETE] Usuário {} — {} — {}",
                    l.getUsuario().getNome(), l.getTipo(), l.getMensagem());
            // TODO: integrar com serviço de push notification (Firebase, OneSignal etc.)
        }
    }

    /**
     * Lembretes padrão criados automaticamente para novos usuários:
     * manhã (08:00), tarde (13:00), noite (21:00) — beber água.
     */
    @Transactional
    public void criarLembretesDefault(Usuario usuario) {
        List.of(
                buildDefault(usuario, TipoLembrete.MANHA, "08:00",
                        "Bom dia! Que tal começar o dia se hidratando? 💧"),
                buildDefault(usuario, TipoLembrete.TARDE, "13:00",
                        "Hora do check-in: você bebeu água hoje?"),
                buildDefault(usuario, TipoLembrete.NOITE, "21:00",
                        "Lembre-se da sua rotina noturna de skincare! 🌙")
        ).forEach(lembreteRepository::save);
    }

    private Lembrete buildDefault(Usuario u, TipoLembrete tipo, String hora, String msg) {
        return Lembrete.builder()
                .usuario(u).tipo(tipo).mensagem(msg).horaEnvio(hora).build();
    }

    public LembreteResponse toResponse(Lembrete l) {
        return new LembreteResponse(l.getId(), l.getTipo(),
                l.getMensagem(), l.getHoraEnvio(), l.getAtivo(), l.getCriadoEm());
    }
}