PODE IGNORAR ESSE ARQUIVO. PASSE PARA O PRÓXIMO

# Documentação da API - Connect Beleza

## Visão Geral

### Autenticação:

- Cadastro (/auth/cadastro) :

```json
    {
        "nome": "Ana Silva",
        "email": "ana@email.com",
        "senha": "senha123456",
        "telefone": "(11) 99999-8888",
        "role": "CLIENTE"
    }
```

- Login (/auth/login) :
```json
    {
      "email": "ana@email.com",
      "senha": "senha123456"
    }
```
Responde: 

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "usuario": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "nome": "Ana Silva",
    "email": "ana@email.com",
    "role": "CLIENTE"
  }
}
```

Agendamentos:

- Contratar Serviço (POST /agendamentos) :
```json
  {
      "servicoId": "b6a7c8d9-0123-4567-89ab-cdef01234567",
      "dataHoraAgendada": "2024-12-25T10:00:00Z",
      "metodoPagamento": "CREDITO",
      "observacoes": "Chegar com 10 minutos de antecedência"
  } 
```
- Listar Agendamentos (GET /agendamentos)
- Detalhes do Agendamento (GET /agendamentos/{id})
- Cancelar Agendamento (PATCH /agendamentos/{id}/cancelar) : 

```json
    {
      "motivoCancelamento": "Conflito de horário"
    }
```

- Reagendar Serviço (PATCH /agendamentos/{id}/reagendar) :

```json
    {
      "novaDataHora": "2024-12-26T14:00:00Z"
    }
```

Avaliações:

- Avaliar Profissional (POST /avaliacoes) : 

```json
    {
      "agendamentoId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "nota": 5,
      "comentario": "Atendimento excelente, profissional muito atencioso!"
    }
```

Busca de Profissionais:

- Buscar Profissionais (GET /profissionais/buscar) : Query Params

```bash
    GET /profissionais/buscar?categoria=CABELO&latitude=-23.5505&longitude=-46.6333&raioKm=10
```

Empresa:

- Gerenciar Produtos (CRUD: GET, POST, PUT, DELETE em /empresa/produtos):

CRIAÇÃO:
```json
    {
      "nome": "Shampoo Revitalizador",
      "descricao": "Fortalece os fios e reduz queda",
      "categoria": "CABELO",
      "preco": 49.90,
      "urlImagem": "https://...",
      "urlCompra": "https://...",
      "patrocinado": true
    }
```

UPDATE:
```bash
    PUT /empresa/produtos/{produtoId}
```

DELEÇÃO:
```bash
    DELETE /empresa/produtos/{produtoId}
```

- Gerenciar Parcerias com Profissionais (CRUD: GET, POST, PATCH em /empresa/parcerias) :

```json
    {
      "profissionalId": "b6a7c8d9-0123-4567-89ab-cdef01234567",
      "descricao": "Parceria para divulgação de produtos"
    }
```

Validação: descricao máximo 600 caracteres

Fóruns e Comunidade:

- Acessar Fóruns (GET /forums)
- Acessar Fórum por Categoria (GET /forums/categoria/{categoria})
- Gerenciar Tópicos (Criar, Listar, Detalhar em /forums/{forumId}/topicos)

```bash
  GET /forums/{forumId}/topicos?termo=duvida&page=0&size=20
```

CRIAR TÓPICO
```json
    {
      "titulo": "Dúvida sobre progressiva",
      "conteudo": "Qual a melhor marca para cabelos quimicamente tratados?"
    }
```

DETALHA TOPICO
```bash
GET /forums/topicos/{topicoId}
```

- Gerenciar Respostas (POST Criar, GET Listar em /topicos/{topicoId}/respostas)

```json
    {
      "conteudo": "Recomendo a marca X, uso há anos!"
    }
```

Lembretes:

- Listar Lembretes (GET /lembretes)
- Configurar Lembrete (POST /lembretes)
- 
```json
    {
      "tipo": "HIDRATACAO",
      "horaEnvio": "20:00",
      "mensagem": "Hora do skincare noturno! 🧴"
    }
```

- Desativar Lembrete (DELETE /lembretes/{id})

Perfil do Profissional:

- Atualizar Perfil (PUT / profissional/perfil)

```json
    {
      "bio": "Especialista em coloração e mechas",
      "anosExperiencia": 8,
      "especialidades": ["CABELO", "MAQUIAGEM"],
      "certificacoes": ["Botox Capilar", "Alongamento de Cílios"],
      "urlPortfolio": "https://instagram.com/profissional",
      "localizacao": "Salão Beleza Pura - Av. Paulista",
      "latitude": -23.5505,
      "longitude": -46.6333
    }
```

- Ver Métricas (GET /profissional/perfil/metricas) :

RESPOSTA:
```json
    {
      "totalAtendimentos": 150,
      "mediaAvaliacoes": 4.8,
      "totalClientes": 45,
      "taxaOcupacao": 75.5
    }
```
- Gerenciar Serviços Oferecidos (CRUD em /profissional/perfil/servicos) : 

CRIAR E ATUALIZAR SERVICO (POST, PUT)
```json
    {
      "nome": "Corte e Finalização",
      "descricao": "Corte com tesoura e finalização com produtos de qualidade",
      "categoria": "CABELO",
      "preco": 120.00,
      "duracaoMinutos": 90
    }
```

PATCH

```bash
PATCH /profissional/perfil/servicos/{servicoId}/preco?valor=130.00
```

- Definir Agenda (CRUD em /profissional/perfil/agenda) :

RESPOSTA:
```json
[
  {
    "id": "...",
    "diaSemana": "SEGUNDA",
    "horaInicio": "09:00",
    "horaFim": "18:00"
  }
]
```

CRIAR: 

```json
{
  "diaSemana": "SEGUNDA",
  "horaInicio": "09:00",
  "horaFim": "18:00"
}
```
- Ver Agendamentos Recebidos (GET /profissional/perfil/agendamentos)
- Gerenciar Parcerias (Listar e Responder em /profissional/perfil/parcerias)

```bash
PATCH /profissional/perfil/parcerias/{parceriaId}?status=ACEITA
```

## Enums Importantes :
### UserRole
CLIENTE
PROFISSIONAL
EMPRESA

### CategoriaEstetica
CABELO
PELE
MAQUIAGEM
UNHAS
SOBRANCELHAS
DEPILACAO
MASSAGEM
ESTETICA_CORPORAL

### TipoLembrete: 
HIDRATACAO
MEDICAMENTO
CUIDADO
PERSONALIZADO

### Dia Da Semana:
SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO



	