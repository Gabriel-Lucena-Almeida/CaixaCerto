package br.ufpb.caixacerto;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * CaixaCerto — gestão financeira simples para quem empreende.
 *
 * Uso:  java -cp out br.ufpb.caixacerto.Main [--demo] [--porta=8080]
 *   --demo   cria a conta de demonstração com movimentações de exemplo (dados fictícios).
 */
public final class Main {
    private static final String DEMO_EMAIL = "demo@caixacerto.local";
    private static final String DEMO_SENHA = "demo1234";

    public static void main(String[] args) throws Exception {
        int porta = 8080;
        boolean demo = false;
        for (String a : args) {
            if (a.equals("--demo")) demo = true;
            if (a.startsWith("--porta=")) porta = Integer.parseInt(a.substring("--porta=".length()));
        }

        Repositorio repo = new Repositorio(Path.of("dados"));
        if (demo) carregarDemo(repo);

        new Servidor(repo).iniciar(porta);
        System.out.println("CaixaCerto no ar: http://localhost:" + porta);
        if (demo) System.out.println("Conta de demonstração: " + DEMO_EMAIL + " / " + DEMO_SENHA);
        System.out.println("Para encerrar, aperte Ctrl+C.");
    }

    /** Dados fictícios, só para apresentar o app: entradas de 4.300, saídas do negócio de 2.160 e gastos pessoais de 860. */
    private static void carregarDemo(Repositorio repo) {
        if (!repo.criarUsuario("Demo", DEMO_EMAIL, DEMO_SENHA)) {
            return; // já existe: não repete os dados de exemplo
        }
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        var e = Movimento.Natureza.ENTRADA;
        var s = Movimento.Natureza.SAIDA;
        var n = Movimento.Tipo.NEGOCIO;
        var p = Movimento.Tipo.PESSOAL;
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(2)), e, n, "Receita", 142000, "Atendimentos da primeira semana");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(9)), e, n, "Receita", 150000, "Atendimentos da segunda semana");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(16)), e, n, "Receita", 120000, "Atendimentos da terceira semana");
        repo.adicionar(DEMO_EMAIL, hoje, e, n, "Receita", 18000, "Atendimento de cliente");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(1)), s, n, "Aluguel", 120000, "Aluguel do espaço");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(5)), s, n, "Divulgação", 45000, "Anúncios nas redes sociais");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(7)), s, n, "Taxas", 41500, "Taxas da maquininha");
        repo.adicionar(DEMO_EMAIL, hoje.minusDays(1).isBefore(inicio) ? inicio : hoje.minusDays(1), s, n, "Material", 9500, "Compra de material");
        repo.adicionar(DEMO_EMAIL, min(hoje, inicio.plusDays(3)), s, p, "Outros", 74000, "Contas de casa");
        repo.adicionar(DEMO_EMAIL, hoje.minusDays(1).isBefore(inicio) ? inicio : hoje.minusDays(1), s, p, "Outros", 12000, "Mercado");
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
