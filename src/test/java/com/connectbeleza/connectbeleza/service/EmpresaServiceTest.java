package com.connectbeleza.connectbeleza.service;

import com.connectbeleza.connectbeleza.domain.entity.*;
import com.connectbeleza.connectbeleza.domain.enums.CategoriaEstetica;
import com.connectbeleza.connectbeleza.domain.enums.StatusParceria;
import com.connectbeleza.connectbeleza.domain.enums.UserRole;
import com.connectbeleza.connectbeleza.dto.request.ParceriaRequest;
import com.connectbeleza.connectbeleza.dto.request.ProdutoRequest;
import com.connectbeleza.connectbeleza.dto.response.ParceriaResponse;
import com.connectbeleza.connectbeleza.dto.response.ProdutoResponse;
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
@DisplayName("EmpresaService")
class EmpresaServiceTest {

    @Mock EmpresaRepository empresaRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock ParceriaRepository parceriaRepository;
    @Mock ProfissionalRepository profissionalRepository;
    @Mock PaginacaoUtil paginacaoUtil;

    @InjectMocks EmpresaService empresaService;

    private UUID usuarioEmpresaId;
    private UUID empresaId;
    private UUID profissionalId;
    private UUID produtoId;
    private Usuario usuarioEmpresa;
    private Empresa empresa;
    private Profissional profissional;
    private Produto produto;
    private Parceria parceria;

    @BeforeEach
    void setUp() {
        usuarioEmpresaId = UUID.randomUUID();
        empresaId = UUID.randomUUID();
        profissionalId = UUID.randomUUID();
        produtoId = UUID.randomUUID();

        usuarioEmpresa = Usuario.builder()
                .id(usuarioEmpresaId)
                .nome("Beleza Corp")
                .email("corp@beleza.com")
                .role(UserRole.EMPRESA)
                .ativo(true)
                .build();

        empresa = Empresa.builder()
                .id(empresaId)
                .usuario(usuarioEmpresa)
                .razaoSocial("Beleza Corp LTDA")
                .cnpj("12.345.678/0001-99")
                .verificada(true)
                .criadoEm(LocalDateTime.now())
                .build();

        Usuario usuarioProfissional = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Carlos Profissional")
                .build();

        profissional = Profissional.builder()
                .id(profissionalId)
                .usuario(usuarioProfissional)
                .build();

        produto = Produto.builder()
                .id(produtoId)
                .empresa(empresa)
                .nome("Creme Hidratante")
                .descricao("Creme para pele seca")
                .categoria(CategoriaEstetica.PELE)
                .preco(new BigDecimal("59.90"))
                .ativo(true)
                .patrocinado(false)
                .criadoEm(LocalDateTime.now())
                .build();

        parceria = Parceria.builder()
                .id(UUID.randomUUID())
                .empresa(empresa)
                .profissional(profissional)
                .descricao("Parceria estratégica")
                .status(StatusParceria.PENDENTE)
                .criadoEm(LocalDateTime.now())
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    // ─── PROMOVER PRODUTOS ────────────────────────────────────────────────────

    @Nested
    @DisplayName("criarProduto")
    class CriarProduto {

        @Test
        @DisplayName("deve criar produto com sucesso para empresa existente")
        void deveCriarProdutoComSucesso() {
            var request = new ProdutoRequest("Creme Hidratante", "Creme para pele seca",
                    CategoriaEstetica.PELE, new BigDecimal("59.90"), "url-img", "url-compra", false);

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

            ProdutoResponse response = empresaService.criarProduto(usuarioEmpresaId, request);

            assertThat(response).isNotNull();
            assertThat(response.nome()).isEqualTo("Creme Hidratante");
            assertThat(response.preco()).isEqualByComparingTo(new BigDecimal("59.90"));
            verify(produtoRepository).save(any(Produto.class));
        }

        @Test
        @DisplayName("deve marcar produto como não-patrocinado por padrão quando patrocinado é null")
        void deveMarcarNaoPatrocinadoPorPadrao() {
            var request = new ProdutoRequest("Produto", "desc", CategoriaEstetica.CABELO,
                    new BigDecimal("30.00"), null, null, null);

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> {
                Produto p = inv.getArgument(0);
                assertThat(p.getPatrocinado()).isFalse();
                return produto;
            });

            empresaService.criarProduto(usuarioEmpresaId, request);
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando empresa não encontrada")
        void deveLancarExcecaoEmpresaNaoEncontrada() {
            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empresaService.criarProduto(usuarioEmpresaId,
                    new ProdutoRequest("p", "d", CategoriaEstetica.PELE, BigDecimal.TEN, null, null, false)))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("atualizarProduto")
    class AtualizarProduto {

        @Test
        @DisplayName("deve atualizar produto quando empresa é proprietária")
        void deveAtualizarProduto() {
            var request = new ProdutoRequest("Creme Premium", "Nova desc",
                    CategoriaEstetica.PELE, new BigDecimal("79.90"), null, null, true);

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
            when(produtoRepository.save(any())).thenReturn(produto);

            ProdutoResponse response = empresaService.atualizarProduto(usuarioEmpresaId, produtoId, request);

            assertThat(response).isNotNull();
            verify(produtoRepository).save(produto);
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando produto pertence a outra empresa")
        void deveLancarExcecaoProdutoDeOutraEmpresa() {
            Empresa outraEmpresa = Empresa.builder().id(UUID.randomUUID()).usuario(usuarioEmpresa)
                    .razaoSocial("Outra").cnpj("99").build();
            produto.setEmpresa(outraEmpresa);

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            assertThatThrownBy(() -> empresaService.atualizarProduto(usuarioEmpresaId, produtoId,
                    new ProdutoRequest("p", "d", CategoriaEstetica.PELE, BigDecimal.ONE, null, null, false)))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não pertence à sua empresa");
        }
    }

    @Nested
    @DisplayName("removerProduto")
    class RemoverProduto {

        @Test
        @DisplayName("deve desativar produto (soft delete)")
        void deveDesativarProduto() {
            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

            empresaService.removerProduto(usuarioEmpresaId, produtoId);

            assertThat(produto.getAtivo()).isFalse();
            verify(produtoRepository).save(produto);
        }
    }

    @Nested
    @DisplayName("listarPatrocinados")
    class ListarPatrocinados {

        @Test
        @DisplayName("deve listar apenas produtos patrocinados e ativos")
        void deveListarProdutosPatrocinados() {
            var pageable = PageRequest.of(0, 10);
            produto.setPatrocinado(true);
            when(paginacaoUtil.build(0, 10)).thenReturn(pageable);
            when(produtoRepository.findByAtivoTrueAndPatrocinadoTrue(pageable))
                    .thenReturn(new PageImpl<>(List.of(produto)));

            Page<ProdutoResponse> result = empresaService.listarPatrocinados(0, 10);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    // ─── PARCERIAS ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("solicitarParceria")
    class SolicitarParceria {

        @Test
        @DisplayName("deve solicitar parceria com sucesso")
        void deveSolicitarParceriaComSucesso() {
            var request = new ParceriaRequest(profissionalId, "Parceria estratégica");

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.existsByEmpresaIdAndProfissionalIdAndStatus(
                    empresaId, profissionalId, StatusParceria.PENDENTE)).thenReturn(false);
            when(parceriaRepository.save(any(Parceria.class))).thenReturn(parceria);

            ParceriaResponse response = empresaService.solicitarParceria(usuarioEmpresaId, request);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(StatusParceria.PENDENTE);
            verify(parceriaRepository).save(any(Parceria.class));
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando já existe parceria pendente")
        void deveLancarExcecaoParceriaDuplicada() {
            var request = new ParceriaRequest(profissionalId, "Parceria");

            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.existsByEmpresaIdAndProfissionalIdAndStatus(
                    any(), any(), eq(StatusParceria.PENDENTE))).thenReturn(true);

            assertThatThrownBy(() -> empresaService.solicitarParceria(usuarioEmpresaId, request))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("parceria pendente");
        }

        @Test
        @DisplayName("deve lançar RecursoNaoEncontradoException quando profissional não encontrado")
        void deveLancarExcecaoProfissionalNaoEncontrado() {
            when(empresaRepository.findByUsuarioId(usuarioEmpresaId)).thenReturn(Optional.of(empresa));
            when(profissionalRepository.findById(profissionalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empresaService.solicitarParceria(usuarioEmpresaId,
                    new ParceriaRequest(profissionalId, "desc")))
                    .isInstanceOf(RecursoNaoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("responderParceria")
    class ResponderParceria {

        @Test
        @DisplayName("deve aceitar parceria com sucesso")
        void deveAceitarParceria() {
            UUID usuarioProfId = profissional.getUsuario().getId();
            parceria.setStatus(StatusParceria.PENDENTE);

            when(profissionalRepository.findByUsuarioId(usuarioProfId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.findById(parceria.getId())).thenReturn(Optional.of(parceria));
            when(parceriaRepository.save(any())).thenReturn(parceria);

            ParceriaResponse response = empresaService.responderParceria(usuarioProfId, parceria.getId(), StatusParceria.ACEITA);

            assertThat(response.status()).isEqualTo(StatusParceria.ACEITA);
        }

        @Test
        @DisplayName("deve recusar parceria com sucesso")
        void deveRecusarParceria() {
            UUID usuarioProfId = profissional.getUsuario().getId();
            parceria.setStatus(StatusParceria.PENDENTE);

            when(profissionalRepository.findByUsuarioId(usuarioProfId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.findById(parceria.getId())).thenReturn(Optional.of(parceria));
            when(parceriaRepository.save(any())).thenReturn(parceria);

            ParceriaResponse response = empresaService.responderParceria(usuarioProfId, parceria.getId(), StatusParceria.RECUSADA);

            assertThat(response.status()).isEqualTo(StatusParceria.RECUSADA);
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando parceria não está PENDENTE")
        void deveLancarExcecaoParceriaNaoPendente() {
            UUID usuarioProfId = profissional.getUsuario().getId();
            parceria.setStatus(StatusParceria.ACEITA);

            when(profissionalRepository.findByUsuarioId(usuarioProfId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.findById(parceria.getId())).thenReturn(Optional.of(parceria));

            assertThatThrownBy(() -> empresaService.responderParceria(usuarioProfId, parceria.getId(), StatusParceria.RECUSADA))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("pendentes");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando status inválido (PENDENTE ou ENCERRADA)")
        void deveLancarExcecaoStatusInvalido() {
            UUID usuarioProfId = profissional.getUsuario().getId();
            parceria.setStatus(StatusParceria.PENDENTE);

            when(profissionalRepository.findByUsuarioId(usuarioProfId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.findById(parceria.getId())).thenReturn(Optional.of(parceria));

            assertThatThrownBy(() -> empresaService.responderParceria(usuarioProfId, parceria.getId(), StatusParceria.PENDENTE))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("Status inválido");
        }

        @Test
        @DisplayName("deve lançar RegraDeNegocioException quando parceria é de outro profissional")
        void deveLancarExcecaoParceiaDeOutroProfissional() {
            UUID usuarioProfId = profissional.getUsuario().getId();
            Profissional outroProfissional = Profissional.builder().id(UUID.randomUUID())
                    .usuario(profissional.getUsuario()).build();
            parceria.setProfissional(outroProfissional);

            when(profissionalRepository.findByUsuarioId(usuarioProfId)).thenReturn(Optional.of(profissional));
            when(parceriaRepository.findById(parceria.getId())).thenReturn(Optional.of(parceria));

            assertThatThrownBy(() -> empresaService.responderParceria(usuarioProfId, parceria.getId(), StatusParceria.ACEITA))
                    .isInstanceOf(RegraDeNegocioException.class)
                    .hasMessageContaining("não é direcionada a você");
        }
    }
}
