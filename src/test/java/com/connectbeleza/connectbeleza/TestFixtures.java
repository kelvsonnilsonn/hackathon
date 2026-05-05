package com.connectbeleza.connectbeleza;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.*;
import com.connectbeleza.connectbeleza.dto.request.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * Fábrica centralizada de objetos de teste.
 * Evita duplicação de builders espalhados por todos os testes.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ─── USUARIOS ──────────────────────────────────────────────────────────────

    public static Usuario umCliente() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Ana Silva")
                .email("ana@teste.com")
                .senha("$2a$10$hash")
                .role(UserRole.CLIENTE)
                .ativo(true)
                .emailVerificado(true)
                .build();
    }

    public static Usuario umProfissionalUsuario() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Carla Estética")
                .email("carla@teste.com")
                .senha("$2a$10$hash")
                .role(UserRole.PROFISSIONAL)
                .ativo(true)
                .emailVerificado(true)
                .build();
    }

    public static Usuario umaEmpresaUsuario() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Beleza SA")
                .email("empresa@teste.com")
                .senha("$2a$10$hash")
                .role(UserRole.EMPRESA)
                .ativo(true)
                .build();
    }

    // ─── PROFISSIONAL ──────────────────────────────────────────────────────────

    public static Profissional umProfissional() {
        return umProfissional(umProfissionalUsuario());
    }

    public static Profissional umProfissional(Usuario usuario) {
        return Profissional.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .bio("Especialista em cuidados capilares com 5 anos de experiência.")
                .anosExperiencia(5)
                .especialidades(List.of(CategoriaEstetica.CABELO, CategoriaEstetica.PELE))
                .certificacoes(List.of("SENAC 2020", "Curso Avançado ABIHPEC"))
                .urlPortfolio("https://portfolio.carla.com")
                .notaMedia(BigDecimal.valueOf(4.50))
                .totalAvaliacoes(10)
                .verificado(true)
                .localizacao("Recife - PE")
                .latitude(BigDecimal.valueOf(-8.0539))
                .longitude(BigDecimal.valueOf(-34.8811))
                .build();
    }

    // ─── EMPRESA ───────────────────────────────────────────────────────────────

    public static Empresa umaEmpresa() {
        return Empresa.builder()
                .id(UUID.randomUUID())
                .usuario(umaEmpresaUsuario())
                .razaoSocial("Beleza SA Cosméticos")
                .cnpj("12.345.678/0001-99")
                .descricao("Distribuidora de cosméticos profissionais.")
                .urlSite("https://belezasa.com.br")
                .verificada(true)
                .build();
    }

    // ─── SERVICO ───────────────────────────────────────────────────────────────

    public static Servico umServico(Profissional profissional) {
        return Servico.builder()
                .id(UUID.randomUUID())
                .profissional(profissional)
                .nome("Corte Feminino")
                .descricao("Corte e escova para cabelos longos.")
                .categoria(CategoriaEstetica.CABELO)
                .preco(BigDecimal.valueOf(80.00))
                .duracaoMinutos(60)
                .ativo(true)
                .build();
    }

    // ─── AGENDAMENTO ───────────────────────────────────────────────────────────

    public static Agendamento umAgendamento(Usuario cliente, Servico servico) {
        return Agendamento.builder()
                .id(UUID.randomUUID())
                .cliente(cliente)
                .servico(servico)
                .dataHoraAgendada(LocalDateTime.now().plusDays(3))
                .status(StatusAgendamento.CONFIRMADO)
                .build();
    }

    public static Agendamento umAgendamentoConcluido(Usuario cliente, Servico servico) {
        return Agendamento.builder()
                .id(UUID.randomUUID())
                .cliente(cliente)
                .servico(servico)
                .dataHoraAgendada(LocalDateTime.now().minusDays(1))
                .status(StatusAgendamento.CONCLUIDO)
                .build();
    }

    // ─── PAGAMENTO ─────────────────────────────────────────────────────────────

    public static Pagamento umPagamentoAprovado(Agendamento agendamento) {
        return Pagamento.builder()
                .id(UUID.randomUUID())
                .agendamento(agendamento)
                .valor(agendamento.getServico().getPreco())
                .status(StatusPagamento.APROVADO)
                .metodoPagamento("PIX")
                .build();
    }

    // ─── FORUM / TOPICO / RESPOSTA ─────────────────────────────────────────────

    public static Forum umForum() {
        return Forum.builder()
                .id(UUID.randomUUID())
                .nome("Fórum de Cabelos")
                .descricao("Dúvidas e dicas sobre cabelos.")
                .categoria(CategoriaEstetica.CABELO)
                .ativo(true)
                .build();
    }

    public static Topico umTopico(Forum forum, Usuario autor) {
        return Topico.builder()
                .id(UUID.randomUUID())
                .forum(forum)
                .autor(autor)
                .titulo("Como hidratar cabelo seco?")
                .conteudo("Tenho o cabelo muito seco e gostaria de dicas de hidratação.")
                .fixado(false)
                .fechado(false)
                .totalRespostas(0)
                .build();
    }

    // ─── AVALIACAO ─────────────────────────────────────────────────────────────

    public static Avaliacao umaAvaliacao(Agendamento agendamento, Profissional profissional) {
        return Avaliacao.builder()
                .id(UUID.randomUUID())
                .agendamento(agendamento)
                .avaliador(agendamento.getCliente())
                .profissional(profissional)
                .nota(5)
                .comentario("Excelente profissional! Super recomendo.")
                .build();
    }

    // ─── LEMBRETE ──────────────────────────────────────────────────────────────

    public static Lembrete umLembrete(Usuario usuario) {
        return Lembrete.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .tipo(TipoLembrete.MANHA)
                .mensagem("Beba água ao acordar!")
                .horaEnvio("08:00")
                .ativo(true)
                .build();
    }

    // ─── REQUESTS ──────────────────────────────────────────────────────────────

    public static CriarContaRequest umCriarContaRequest() {
        return new CriarContaRequest(
                "Ana Silva", "ana@teste.com", "Senha@1234",
                "(81) 99999-0001", UserRole.CLIENTE);
    }

    public static AgendamentoRequest umAgendamentoRequest(UUID servicoId) {
        return new AgendamentoRequest(
                servicoId,
                LocalDateTime.now().plusDays(5),
                "PIX",
                "Sem observações");
    }

    public static AvaliacaoRequest umaAvaliacaoRequest(UUID agendamentoId) {
        return new AvaliacaoRequest(agendamentoId, 5, "Serviço impecável!");
    }

    public static TopicoRequest umTopicoRequest(UUID forumId) {
        return new TopicoRequest(forumId,
                "Como cuidar de cabelo oleoso?",
                "Preciso de dicas para controlar a oleosidade do couro cabeludo.");
    }

    public static AgendaRequest umaAgendaRequest() {
        return new AgendaRequest(DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(18, 0));
    }

    public static ServicoRequest umServicoRequest() {
        return new ServicoRequest(
                "Hidratação Profunda",
                "Hidratação intensiva para cabelos ressecados.",
                CategoriaEstetica.CABELO,
                BigDecimal.valueOf(120.00),
                90);
    }

    public static ParceriaRequest umaParceriaRequest(UUID profissionalId) {
        return new ParceriaRequest(profissionalId,
                "Parceria para promover nossa linha de produtos capilares.");
    }

    public static ProdutoRequest umProdutoRequest() {
        return new ProdutoRequest(
                "Shampoo Hidratante XL",
                "Shampoo para cabelos secos e danificados.",
                CategoriaEstetica.CABELO,
                BigDecimal.valueOf(45.90),
                "https://imagens.belezasa.com/shampoo.jpg",
                "https://loja.belezasa.com/shampoo",
                false);
    }
}