# ConnectBeleza — Backend Spring Boot

> Plataforma de comunidade e marketplace de profissionais de beleza.

---

## Tecnologias

| Tecnologia         | Versão  |
|--------------------|---------|
| Java               | 21      |
| Spring Boot        | 3.2.4   |
| Spring Security    | 6       |
| Spring Data JPA    | 3.2     |
| PostgreSQL         | 16+     |
| JWT (jjwt)         | 0.12.5  |
| Lombok             | 1.18.32 |
| MapStruct          | 1.5.5   |
| SpringDoc OpenAPI  | 2.4.0   |

---

## Estrutura de pacotes

```
com.connectbeleza
├── config/          # SecurityConfig + JwtFilter
├── controller/      # Camada HTTP — um controller por ator/contexto
├── service/         # Regras de negócio — um service por caso de uso agrupado
├── repository/      # Spring Data JPA repositories
├── domain/
│   ├── entity/      # Entidades JPA
│   └── enums/       # Enums de domínio
├── dto/
│   ├── request/     # Payloads de entrada (records com @Valid)
│   └── response/    # Payloads de saída (records)
├── exception/       # Exceções customizadas + GlobalExceptionHandler
└── util/            # JwtUtil, PaginacaoUtil
```

---

## Casos de uso implementados

### Cliente
| Caso de Uso              | Relação      | Endpoint                                    |
|--------------------------|-------------|---------------------------------------------|
| Criar conta              | —            | `POST /auth/cadastro`                       |
| Login                    | —            | `POST /auth/login`                          |
| Buscar profissional      | —            | `GET  /profissionais?categoria=&nome=&lat=` |
| Filtrar por categoria    | include ↑    | (param `categoria` no endpoint acima)       |
| Acessar fórum            | —            | `GET  /forums`                              |
| Criar tópico             | include fórum| `POST /forums/{forumId}/topicos`            |
| Participar de tópico     | include fórum| `POST /forums/topicos/{id}/respostas`       |
| Contratar serviço        | —            | `POST /agendamentos`                        |
| Realizar pagamento       | include ↑    | (automático dentro de contratar serviço)    |
| Cancelar serviço         | extend ↑     | `PATCH /agendamentos/{id}/cancelar`         |
| Reagendar serviço        | extend ↑     | `PATCH /agendamentos/{id}/reagendar`        |
| Avaliar profissional     | include ↑    | `POST /avaliacoes`                          |
| Configurar lembrete      | —            | `POST /lembretes`                           |

### Profissional
| Caso de Uso              | Relação       | Endpoint                                       |
|--------------------------|--------------|------------------------------------------------|
| Gerenciar perfil         | —             | `PUT  /profissional/perfil`                    |
| Oferecer serviços        | —             | `POST /profissional/perfil/servicos`           |
| Definir preço            | include ↑     | `PATCH /profissional/perfil/servicos/{id}/preco` |
| Definir agenda           | include ↑     | `POST /profissional/perfil/agenda`             |
| Participar da comunidade | —             | `POST /forums/{forumId}/topicos` / respostas   |
| Gerenciar parcerias      | —             | `GET/PATCH /profissional/perfil/parcerias`     |
| Visualizar métricas      | —             | `GET /profissional/perfil/metricas`            |

### Empresa
| Caso de Uso                      | Relação | Endpoint                   |
|----------------------------------|---------|----------------------------|
| Promover produtos                | —       | `POST /empresa/produtos`   |
| Realizar parceria com profissional| —      | `POST /empresa/parcerias`  |

### Sistema
| Caso de Uso       | Mecanismo                                                   |
|-------------------|-------------------------------------------------------------|
| Receber lembrete  | `@Scheduled` (cron a cada minuto) + `GET /lembretes`        |

---

## Como rodar

### Pré-requisitos
- Java 21
- PostgreSQL 16+ rodando localmente
- Maven 3.9+

### 1. Banco de dados
```sql
CREATE DATABASE connectbeleza;
```

### 2. Variáveis de ambiente (ou edite `application.yml`)
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=connectbeleza-secret-key-must-be-at-least-256-bits-long
```

### 3. Rodar
```bash
cd connectbeleza
mvn spring-boot:run
```

### 4. Swagger UI
```
http://localhost:8080/api/v1/swagger-ui.html
```

---

## Autenticação

Todos os endpoints (exceto `/auth/**`, `GET /profissionais/**`, `GET /forums/**`) exigem:

```
Authorization: Bearer <token_jwt>
```

O token é obtido via `POST /auth/login`.

---

## Regras de negócio principais

- **Cancelamento**: apenas com ≥ 24h de antecedência; gera estorno automático.
- **Avaliação**: somente após agendamento com status `CONCLUIDO`; uma avaliação por agendamento.
- **Conflito de horário**: bloqueia contratação se o profissional já tiver agendamento no período.
- **Parceria**: empresa não pode enviar duas solicitações pendentes para o mesmo profissional.
- **Lembretes default**: criados automaticamente para cada novo usuário (manhã/tarde/noite).