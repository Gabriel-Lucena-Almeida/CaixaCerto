# CaixaCerto (base do aplicativo)

Gestão financeira simples para jovens autônomos e microempreendedores: registra entradas e saídas, separa **negócio** e **pessoal** e mostra quanto **sobrou de verdade** no mês.

Projeto da disciplina de Engenharia de Software (UFPB, Campus IV). Esta é a base do MVP (histórias US01 a US05 do backlog), em **Java puro, sem frameworks e sem bibliotecas externas**.

## Como rodar

Você só precisa do **JDK 17 ou mais novo** (`java -version` e `javac -version` devem funcionar).

**Windows:** dê dois cliques em `rodar.bat` (ou rode no terminal).
**Linux / macOS:** `chmod +x rodar.sh` e depois `./rodar.sh`.

Depois abra **http://localhost:8080**.

### Conta de demonstração (para o pitch)

Rode com `--demo` para criar uma conta com dados **fictícios** (os mesmos números das telas do protótipo):

```
rodar.bat --demo        (Windows)
./rodar.sh --demo       (Linux/macOS)
```

Entre com `demo@caixacerto.local` e a senha `demo1234`. O resumo mostra lucro real de R$ 2.140,00 e sobra do mês de R$ 1.280,00.

Para outra porta: `--porta=9090`.

### Rodando manualmente

```
javac -encoding UTF-8 -d out src/br/ufpb/caixacerto/*.java
java -Dfile.encoding=UTF-8 -cp out br.ufpb.caixacerto.Main --demo
```

## As 4 telas

| Tela | Endereço | Histórias |
|---|---|---|
| Entrar | `/entrar` | US01 |
| Criar conta (nome, e-mail, senha, confirmação e consentimento) | `/cadastro` | US01 |
| Início (aba Negócio e Pessoal, últimas movimentações) | `/` | visão geral |
| Registrar movimentação | `/registrar` | US02, US03, US04 |
| Resumo do mês (lucro real) | `/resumo` | US05 |

Também existem **Sair** e **Excluir minha conta** (que apaga a conta e todas as movimentações).

## Como o código está organizado

Pasta `src/br/ufpb/caixacerto/`:

| Arquivo | O que faz |
|---|---|
| `Main.java` | Liga o servidor; opção `--demo` |
| `Servidor.java` | Rotas, sessão por cookie, validação dos formulários |
| `Paginas.java` | HTML e CSS das telas |
| `Repositorio.java` | Guarda usuários e movimentações (arquivos na pasta `dados/`) |
| `Resumo.java` | A conta do mês: lucro real = entradas do negócio − saídas do negócio |
| `Movimento.java` | Uma entrada ou saída (negócio ou pessoal) |
| `Dinheiro.java` | Converte "1.250,50" em centavos e formata "R$ 1.250,50" |
| `Seguranca.java` | Senha com hash PBKDF2 e token de sessão aleatório |

Os valores são guardados em **centavos** (`long`), para não ter erro de arredondamento.

## Dados pedidos no cadastro

Só o necessário: **nome, e-mail e senha** (com confirmação), e o aceite do uso dos dados. Não pedimos CPF nem outros documentos, de propósito: o app não precisa deles no MVP, e guardar menos dados reduz o risco para quem usa (histórias éticas de privacidade e consentimento).

## O que já está seguro

- O servidor escuta **só em localhost** (`127.0.0.1`); outros computadores da rede não acessam.
- Senhas guardadas com **PBKDF2 + sal** (nunca em texto aberto).
- Cada usuário só vê as próprias movimentações.
- Textos digitados são **escapados** no HTML (sem injeção de código).
- Cookie de sessão `HttpOnly` e `SameSite=Lax`; cabeçalhos de segurança nas respostas.
- Excluir a conta apaga os dados (história ética UE06).

## O que fica para as próximas sprints

- Trocar os arquivos de `dados/` por um banco (SQLite ou PostgreSQL), mantendo os métodos do `Repositorio`.
- Limite de tentativas de login, token anti-CSRF e HTTPS antes de publicar na internet.
- Histórias US06 a US12: categorias próprias, histórico por período, metas, lembretes, gráficos, dicas e relatório em PDF.
- Testes automatizados (JUnit) para `Dinheiro` e `Resumo`.

Contas criadas antes da página de cadastro (sem nome) continuam funcionando.

A pasta `dados/` é criada ao rodar e **não deve ir para o GitHub** (já está no `.gitignore`).
