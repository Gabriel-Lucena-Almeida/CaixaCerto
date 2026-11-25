# CaixaCerto

Gestão financeira simples para autônomos e microempreendedores: registra entradas e saídas, separa **negócio** e **pessoal** e mostra quanto **sobrou de verdade** no mês.

---

## Stack Tecnológica

### Backend
- **Java 21** + **Spring Boot 3.3**
- **Spring Data JPA** + **Hibernate**
- **Spring Security** + **JWT** (access + refresh tokens)
- **SQLite** (arquivo único, zero config)
- **Flyway** (migrations)
- **MapStruct** (DTO ↔ Entity)
- **Lombok** (boilerplate reduction)
- **Springdoc OpenAPI** (Swagger UI)
- **Maven** (build)

### Frontend
- **React 18** + **TypeScript** + **Vite**
- **Tailwind CSS** (design system)
- **React Router v6** (rotas)
- **TanStack Query v5** (cache, loading, mutation)
- **React Hook Form** + **Zod** (formulários + validação)
- **Axios** (HTTP client com interceptors)
- **Recharts** (gráficos)
- **Lucide React** (ícones)
- **date-fns** (datas)
- **Vitest** + **Testing Library** (testes)

---

## Estrutura do Projeto

```
caixacerto/
├── docker-compose.yml          # Sobe tudo (opcional)
├── backend/                    # Spring Boot API
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/br/ufpb/caixacerto/
│       │   ├── CaixacertoApplication.java
│       │   ├── config/         # Security, OpenAPI, DemoDataLoader
│       │   ├── auth/           # JWT, Login, Register
│       │   ├── user/           # User entity, repository, service
│       │   ├── movement/       # Movement entity, repository, service, controller
│       │   └── common/         # Enums, utils (MoneyUtils), exceptions
│       └── resources/
│           ├── application.yml
│           └── db/migration/   # Flyway SQL migrations
└── frontend/                   # React + Vite
    ├── package.json
    ├── vite.config.ts
    ├── tailwind.config.js
    ├── tsconfig.json
    ├── index.html
    ├── Dockerfile.dev
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── routes.tsx
        ├── auth/               # AuthContext, hooks
        ├── api/                # Axios client, types, React Query hooks
        ├── pages/              # Login, Register, Home, MovementForm, Summary, Settings
        ├── ui/                 # Button, Input, Card, RadioGroup, Chip, Modal, etc.
        ├── utils/              # money.ts (port do Dinheiro.java)
        └── styles/             # globals.css (Tailwind)
```

---

## Como Rodar

### Pré-requisitos
- **Java 21** (JDK)
- **Node.js 20+**
- **Maven 3.9+** (ou usar wrapper)
- **Docker** (opcional, para docker-compose)

### Opção 1: Docker Compose (recomendado)

```bash
docker-compose up --build
```

- Backend: http://localhost:8080/api
- Frontend: http://localhost:5173
- Swagger UI: http://localhost:8080/api/swagger-ui.html

### Opção 2: Desenvolvimento Local

#### Backend
```bash
cd backend
./mvnw spring-boot:run
# Ou com demo data:
./mvnw spring-boot:run -Dspring-boot.run.arguments=--demo
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## Variáveis de Ambiente

### Backend (`backend/src/main/resources/application.yml` ou env vars)
```yaml
app:
  jwt:
    secret: "base64-encoded-secret-min-32-chars"  # Gere com: openssl rand -base64 32
    expiration: 86400000        # 24h
    refresh-expiration: 604800000  # 7 dias
  cors:
    allowed-origins: "http://localhost:5173,http://localhost:3000"
```

### Frontend (`frontend/.env`)
```env
VITE_API_URL=http://localhost:8080/api
```

---

## Endpoints da API

### Autenticação
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login (retorna access + refresh token) |
| POST | `/api/auth/register` | Cadastro |
| POST | `/api/auth/refresh` | Renovar access token |

### Movimentações
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/movements` | Lista com filtros (type, nature, date range, pagination) |
| GET | `/api/movements/recent?type=NEGOCIO` | Últimas 3 por tipo |
| GET | `/api/movements/{id}` | Detalhe |
| POST | `/api/movements` | Criar |
| PUT | `/api/movements/{id}` | Atualizar |
| DELETE | `/api/movements/{id}` | Excluir |
| GET | `/api/movements/summary?month=2024-01` | Resumo do mês |

### Health Check
| Método | Rota |
|--------|------|
| GET | `/api/actuator/health` |

---

## Funcionalidades (MVP - US01 a US05)

- ✅ **Login / Cadastro** com consentimento LGPD
- ✅ **Separação Negócio vs Pessoal** em todas as telas
- ✅ **Registrar movimentação**: entrada/saída, tipo, categoria, valor, descrição, data
- ✅ **Início** com abas Negócio/Pessoal + últimas 3 movimentações
- ✅ **Resumo do mês**: lucro real (entradas - saídas do negócio) + sobra total
- ✅ **Excluir conta** (apaga tudo - UE06)
- ✅ **Segurança**: JWT HttpOnly, BCrypt, CORS, CSP, validação server-side

---

## Próximas Sprints (US06-US12)

- [ ] Categorias personalizadas
- [ ] Histórico por período / filtros avançados
- [ ] Metas / orçamentos
- [ ] Lembretes / recorrência
- [ ] Gráficos avançados (evolução temporal, comparação)
- [ ] Dicas / insights baseados nos dados
- [ ] Exportar PDF
- [ ] Testes automatizados (JUnit + Testcontainers / Vitest)

---

## Licença

MIT