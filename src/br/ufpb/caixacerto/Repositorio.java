package br.ufpb.caixacerto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Guarda usuários e movimentações em memória e grava em arquivos de texto na pasta "dados".
 * Para o MVP basta; depois pode ser trocado por um banco de dados (SQLite, PostgreSQL...)
 * mantendo os mesmos métodos públicos.
 */
final class Repositorio {

    private record Credencial(String nome, String sal, String hash) {}

    private final Path arquivoUsuarios;
    private final Path arquivoMovimentos;
    private final Map<String, Credencial> usuarios = new LinkedHashMap<>();
    private final List<Movimento> movimentos = new ArrayList<>();
    private long proximoId = 1;

    Repositorio(Path pasta) {
        try {
            Files.createDirectories(pasta);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.arquivoUsuarios = pasta.resolve("usuarios.tsv");
        this.arquivoMovimentos = pasta.resolve("movimentos.tsv");
        carregar();
    }

    static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    // ---------- usuários ----------

    synchronized boolean existeUsuario(String email) {
        return usuarios.containsKey(normalizarEmail(email));
    }

    /** Nome informado no cadastro (vazio em contas antigas). */
    synchronized String nomeDe(String email) {
        Credencial c = usuarios.get(normalizarEmail(email));
        return c == null ? "" : c.nome();
    }

    /** Retorna false se o e-mail já estiver cadastrado. */
    synchronized boolean criarUsuario(String nome, String email, String senha) {
        String chave = normalizarEmail(email);
        if (usuarios.containsKey(chave)) {
            return false;
        }
        String sal = Seguranca.novoSal();
        usuarios.put(chave, new Credencial(nome == null ? "" : nome.trim(), sal, Seguranca.hashDaSenha(senha, sal)));
        salvar();
        return true;
    }

    synchronized boolean autenticar(String email, String senha) {
        Credencial c = usuarios.get(normalizarEmail(email));
        if (c == null) {
            // gasta o mesmo tempo para não revelar se o e-mail existe
            Seguranca.hashDaSenha(senha, Seguranca.novoSal());
            return false;
        }
        return Seguranca.confere(senha, c.sal(), c.hash());
    }

    /** Apaga a conta e todas as movimentações dela (UE06). */
    synchronized void excluirUsuario(String email) {
        String chave = normalizarEmail(email);
        usuarios.remove(chave);
        movimentos.removeIf(m -> m.email().equals(chave));
        salvar();
    }

    // ---------- movimentações ----------

    synchronized Movimento adicionar(String email, LocalDate data, Movimento.Natureza natureza,
                                     Movimento.Tipo tipo, String categoria, long centavos, String descricao) {
        Movimento m = new Movimento(proximoId++, normalizarEmail(email), data, natureza, tipo,
                categoria, centavos, descricao);
        movimentos.add(m);
        salvar();
        return m;
    }

    /** Movimentações do usuário, da mais recente para a mais antiga. */
    synchronized List<Movimento> listar(String email) {
        String chave = normalizarEmail(email);
        return movimentos.stream()
                .filter(m -> m.email().equals(chave))
                .sorted(Comparator.comparing(Movimento::data).reversed().thenComparing(Comparator.comparingLong(Movimento::id).reversed()))
                .toList();
    }

    // ---------- arquivos ----------

    private static String cod(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String dec(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private void carregar() {
        try {
            if (Files.exists(arquivoUsuarios)) {
                for (String linha : Files.readAllLines(arquivoUsuarios, StandardCharsets.UTF_8)) {
                    String[] p = linha.split("\t", -1);
                    if (p.length == 4) {
                        usuarios.put(dec(p[0]), new Credencial(dec(p[1]), p[2], p[3]));
                    } else if (p.length == 3) { // formato antigo, sem nome
                        usuarios.put(dec(p[0]), new Credencial("", p[1], p[2]));
                    }
                }
            }
            if (Files.exists(arquivoMovimentos)) {
                for (String linha : Files.readAllLines(arquivoMovimentos, StandardCharsets.UTF_8)) {
                    String[] p = linha.split("\t", -1);
                    if (p.length == 8) {
                        long id = Long.parseLong(p[0]);
                        movimentos.add(new Movimento(id, dec(p[1]), LocalDate.parse(p[2]),
                                Movimento.Natureza.valueOf(p[3]), Movimento.Tipo.valueOf(p[4]),
                                dec(p[5]), Long.parseLong(p[6]), dec(p[7])));
                        proximoId = Math.max(proximoId, id + 1);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler a pasta de dados.", e);
        }
    }

    private void salvar() {
        List<String> u = new ArrayList<>();
        usuarios.forEach((email, c) -> u.add(cod(email) + "\t" + cod(c.nome()) + "\t" + c.sal() + "\t" + c.hash()));
        List<String> m = new ArrayList<>();
        for (Movimento x : movimentos) {
            m.add(x.id() + "\t" + cod(x.email()) + "\t" + x.data() + "\t" + x.natureza() + "\t" + x.tipo()
                    + "\t" + cod(x.categoria()) + "\t" + x.centavos() + "\t" + cod(x.descricao()));
        }
        escrever(arquivoUsuarios, u);
        escrever(arquivoMovimentos, m);
    }

    private static void escrever(Path destino, List<String> linhas) {
        try {
            Path temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar os dados.", e);
        }
    }
}
