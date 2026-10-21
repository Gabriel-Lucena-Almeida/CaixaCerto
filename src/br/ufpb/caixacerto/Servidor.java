package br.ufpb.caixacerto;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/** Servidor web do CaixaCerto. Escuta somente em localhost (127.0.0.1). */
final class Servidor {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int TAMANHO_MAXIMO_FORMULARIO = 10_000;

    private final Repositorio repo;
    private final Map<String, String> sessoes = new ConcurrentHashMap<>(); // token -> e-mail

    Servidor(Repositorio repo) {
        this.repo = repo;
    }

    void iniciar(int porta) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), porta), 0);
        servidor.createContext("/", this::roteador);
        servidor.setExecutor(Executors.newFixedThreadPool(8));
        servidor.start();
    }

    // ---------- rotas ----------

    private void roteador(HttpExchange ex) throws IOException {
        try {
            String rota = ex.getRequestMethod() + " " + ex.getRequestURI().getPath();
            switch (rota) {
                case "GET /" -> inicio(ex);
                case "GET /entrar" -> responder(ex, 200, Paginas.entrar(null, ""));
                case "GET /cadastro" -> responder(ex, 200, Paginas.cadastro(null, "", ""));
                case "POST /entrar" -> entrar(ex);
                case "POST /cadastrar" -> cadastrar(ex);
                case "GET /registrar" -> registrarForm(ex);
                case "POST /registrar" -> registrarSalvar(ex);
                case "GET /resumo" -> resumo(ex);
                case "POST /sair" -> sair(ex);
                case "GET /conta/excluir" -> excluirForm(ex);
                case "POST /conta/excluir" -> excluirConfirmar(ex);
                default -> responder(ex, 404, Paginas.erro(404, "Página não encontrada."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            responder(ex, 500, Paginas.erro(500, "Algo deu errado. Tente de novo."));
        } finally {
            ex.close();
        }
    }

    private void inicio(HttpExchange ex) throws IOException {
        String email = usuarioLogado(ex);
        if (email == null) { redirecionar(ex, "/entrar", null); return; }
        boolean abaNegocio = !"pessoal".equals(parametros(ex.getRequestURI().getRawQuery()).get("aba"));
        Movimento.Tipo tipo = abaNegocio ? Movimento.Tipo.NEGOCIO : Movimento.Tipo.PESSOAL;
        List<Movimento> todas = repo.listar(email);
        List<Movimento> ultimas = todas.stream().filter(m -> m.tipo() == tipo).limit(3).toList();
        YearMonth mes = YearMonth.now();
        responder(ex, 200, Paginas.inicio(repo.nomeDe(email), abaNegocio, Resumo.doMes(todas, mes), ultimas, mes));
    }

    private void entrar(HttpExchange ex) throws IOException {
        Map<String, String> f = formulario(ex);
        String email = f.getOrDefault("email", "").trim();
        if (repo.autenticar(email, f.getOrDefault("senha", ""))) {
            iniciarSessao(ex, Repositorio.normalizarEmail(email));
            redirecionar(ex, "/", null);
        } else {
            responder(ex, 401, Paginas.entrar("E-mail ou senha incorretos.", email));
        }
    }

    private void cadastrar(HttpExchange ex) throws IOException {
        Map<String, String> f = formulario(ex);
        String nome = f.getOrDefault("nome", "").trim().replaceAll("\\s+", " ");
        String email = f.getOrDefault("email", "").trim();
        String senha = f.getOrDefault("senha", "");
        String confirmar = f.getOrDefault("confirmar", "");

        String erro = null;
        if (nome.length() < 2 || nome.length() > 80) {
            erro = "Informe seu nome (de 2 a 80 caracteres).";
        } else if (!EMAIL.matcher(email).matches() || email.length() > 120) {
            erro = "Informe um e-mail válido.";
        } else if (senha.length() < 6 || senha.length() > 100) {
            erro = "A senha precisa ter pelo menos 6 caracteres.";
        } else if (!senha.equals(confirmar)) {
            erro = "As duas senhas precisam ser iguais.";
        } else if (!"sim".equals(f.get("consentimento"))) {
            erro = "Para criar a conta, marque que você concorda com o uso dos seus dados.";
        }
        if (erro != null) {
            responder(ex, 400, Paginas.cadastro(erro, nome, email));
        } else if (!repo.criarUsuario(nome, email, senha)) {
            responder(ex, 409, Paginas.cadastro("Já existe uma conta com esse e-mail. Volte e toque em Entrar.", nome, email));
        } else {
            iniciarSessao(ex, Repositorio.normalizarEmail(email));
            redirecionar(ex, "/", null);
        }
    }

    private void registrarForm(HttpExchange ex) throws IOException {
        if (usuarioLogado(ex) == null) { redirecionar(ex, "/entrar", null); return; }
        responder(ex, 200, Paginas.registrar(null, Map.of()));
    }

    private void registrarSalvar(HttpExchange ex) throws IOException {
        String email = usuarioLogado(ex);
        if (email == null) { redirecionar(ex, "/entrar", null); return; }
        Map<String, String> f = formulario(ex);
        try {
            Movimento.Natureza natureza = Movimento.Natureza.valueOf(f.getOrDefault("natureza", ""));
            Movimento.Tipo tipo = Movimento.Tipo.valueOf(f.getOrDefault("tipo", ""));
            long centavos = Dinheiro.paraCentavos(f.get("valor"));
            String descricao = f.getOrDefault("descricao", "").trim();
            if (descricao.isEmpty()) {
                throw new IllegalArgumentException("Escreva uma descrição.");
            }
            if (descricao.length() > 80) {
                throw new IllegalArgumentException("A descrição pode ter até 80 caracteres.");
            }
            LocalDate data = LocalDate.parse(f.getOrDefault("data", ""));
            if (data.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("A data não pode ser no futuro.");
            }
            String categoria = f.getOrDefault("categoria", Paginas.CATEGORIA_PADRAO);
            if (natureza == Movimento.Natureza.ENTRADA) {
                categoria = "Receita";
            } else if (!Paginas.CATEGORIAS.contains(categoria)) {
                categoria = Paginas.CATEGORIA_PADRAO;
            }
            repo.adicionar(email, data, natureza, tipo, categoria, centavos, descricao);
            redirecionar(ex, "/?aba=" + (tipo == Movimento.Tipo.NEGOCIO ? "negocio" : "pessoal"), null);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            String msg = e instanceof DateTimeParseException ? "Informe uma data válida." : e.getMessage();
            if (msg == null || msg.startsWith("No enum constant")) {
                msg = "Escolha entrada ou saída e se é do negócio ou pessoal.";
            }
            responder(ex, 400, Paginas.registrar(msg, f));
        }
    }

    private void resumo(HttpExchange ex) throws IOException {
        String email = usuarioLogado(ex);
        if (email == null) { redirecionar(ex, "/entrar", null); return; }
        YearMonth mes = YearMonth.now();
        responder(ex, 200, Paginas.resumo(Resumo.doMes(repo.listar(email), mes), mes));
    }

    private void sair(HttpExchange ex) throws IOException {
        String token = cookie(ex, "sid");
        if (token != null) sessoes.remove(token);
        redirecionar(ex, "/entrar", "sid=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }

    private void excluirForm(HttpExchange ex) throws IOException {
        if (usuarioLogado(ex) == null) { redirecionar(ex, "/entrar", null); return; }
        responder(ex, 200, Paginas.confirmarExclusao());
    }

    private void excluirConfirmar(HttpExchange ex) throws IOException {
        String email = usuarioLogado(ex);
        if (email == null) { redirecionar(ex, "/entrar", null); return; }
        repo.excluirUsuario(email);
        sessoes.values().removeIf(e -> e.equals(email));
        redirecionar(ex, "/entrar", "sid=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
    }

    // ---------- sessão ----------

    private void iniciarSessao(HttpExchange ex, String email) {
        String token = Seguranca.novoToken();
        sessoes.put(token, email);
        ex.getResponseHeaders().add("Set-Cookie", "sid=" + token + "; Path=/; HttpOnly; SameSite=Lax");
    }

    private String usuarioLogado(HttpExchange ex) {
        String token = cookie(ex, "sid");
        return token == null ? null : sessoes.get(token);
    }

    private static String cookie(HttpExchange ex, String nome) {
        List<String> cabecalhos = ex.getRequestHeaders().get("Cookie");
        if (cabecalhos == null) return null;
        for (String linha : cabecalhos) {
            for (String par : linha.split(";")) {
                String[] kv = par.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(nome)) return kv[1];
            }
        }
        return null;
    }

    // ---------- HTTP ----------

    private static Map<String, String> formulario(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] bytes = in.readNBytes(TAMANHO_MAXIMO_FORMULARIO + 1);
            if (bytes.length > TAMANHO_MAXIMO_FORMULARIO) {
                throw new IOException("Formulário muito grande.");
            }
            return parametros(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> parametros(String texto) {
        Map<String, String> mapa = new HashMap<>();
        if (texto == null || texto.isEmpty()) return mapa;
        for (String par : texto.split("&")) {
            String[] kv = par.split("=", 2);
            String chave = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String valor = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            mapa.putIfAbsent(chave, valor);
        }
        return mapa;
    }

    private static void responder(HttpExchange ex, int codigo, String html) throws IOException {
        byte[] corpo = html.getBytes(StandardCharsets.UTF_8);
        var h = ex.getResponseHeaders();
        h.set("Content-Type", "text/html; charset=utf-8");
        h.set("Cache-Control", "no-store");
        h.set("X-Content-Type-Options", "nosniff");
        h.set("Content-Security-Policy", "default-src 'self'; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
                + "font-src https://fonts.gstatic.com; img-src 'self' data:; form-action 'self'; frame-ancestors 'none'");
        ex.sendResponseHeaders(codigo, corpo.length);
        ex.getResponseBody().write(corpo);
    }

    private static void redirecionar(HttpExchange ex, String destino, String setCookie) throws IOException {
        if (setCookie != null) ex.getResponseHeaders().add("Set-Cookie", setCookie);
        ex.getResponseHeaders().set("Location", destino);
        ex.sendResponseHeaders(303, -1);
    }
}
