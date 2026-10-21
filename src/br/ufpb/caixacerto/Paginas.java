package br.ufpb.caixacerto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Gera o HTML das telas do MVP (Entrar, Criar conta, Início, Registrar e Resumo do mês). */
final class Paginas {
    static final List<String> CATEGORIAS = List.of("Material", "Aluguel", "Divulgação", "Taxas", "Outros");
    static final String CATEGORIA_PADRAO = "Outros";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private Paginas() {}

    // ---------- utilidades ----------

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    static String nomeDoMes(YearMonth mes) {
        String nome = mes.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1) + " de " + mes.getYear();
    }

    private static String quando(LocalDate data) {
        LocalDate hoje = LocalDate.now();
        if (data.equals(hoje)) return "hoje";
        if (data.equals(hoje.minusDays(1))) return "ontem";
        return DIA_MES.format(data);
    }

    private static final String LOGO =
            "<svg class=\"logo\" width=\"44\" height=\"44\" viewBox=\"0 0 44 44\" role=\"img\" aria-label=\"CaixaCerto\">"
            + "<rect width=\"44\" height=\"44\" rx=\"12\" fill=\"#14263B\"/>"
            + "<path d=\"M12 23.5l7 7L32 15\" fill=\"none\" stroke=\"#5FD3A5\" stroke-width=\"4\" "
            + "stroke-linecap=\"round\" stroke-linejoin=\"round\"/></svg>";

    /** Valor de uma saída, com o sinal de menos separado do R$. */
    private static String negativo(long centavos) {
        return centavos == 0 ? Dinheiro.formatar(0) : "− " + Dinheiro.formatar(centavos);
    }

    private static String alerta(String erro) {
        return erro == null || erro.isBlank() ? "" : "<p class=\"alerta\" role=\"alert\">" + esc(erro) + "</p>";
    }

    // ---------- estrutura comum ----------

    static String layout(String titulo, String corpo) {
        return "<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + esc(titulo) + " · CaixaCerto</title>"
                + "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">"
                + "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>"
                + "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&family=Sora:wght@600;700&display=swap\">"
                + "<style>" + CSS + "</style></head><body><main class=\"app\">" + corpo + "</main></body></html>";
    }

    // ---------- 1. Entrar ----------

    static String entrar(String erro, String email) {
        return layout("Entrar", """
                <div class="marca">""" + LOGO + """
                <span class="nome">CaixaCerto</span></div>
                <div class="bloco">
                <h1 class="grande">Saiba quanto sobrou de verdade.</h1>
                <p class="suave">Separe o dinheiro do seu negócio do seu dinheiro pessoal em poucos toques.</p>
                </div>
                """ + alerta(erro) + """
                <form method="post" action="/entrar" class="form">
                <label for="email">E-mail</label>
                <input id="email" name="email" type="email" autocomplete="email" placeholder="voce@email.com" required value="
                """.strip() + esc(email) + """
                ">
                <label for="senha">Senha</label>
                <input id="senha" name="senha" type="password" autocomplete="current-password" placeholder="Sua senha" required>
                <button class="btn escuro" type="submit">Entrar</button>
                <p class="centro suave">Ainda não tem conta?
                <a href="/cadastro">Criar conta</a></p>
                </form>
                <p class="centro pequeno suave">Seus dados financeiros ficam visíveis apenas para você
                e você pode excluir a conta quando quiser.</p>
                """);
    }

    // ---------- 1b. Criar conta ----------

    static String cadastro(String erro, String nome, String email) {
        return layout("Criar conta",
                "<header class=\"topo-voltar\"><a class=\"voltar\" href=\"/entrar\" aria-label=\"Voltar\">←</a>"
                + "<h1>Criar conta</h1></header>"
                + "<p class=\"suave\">Preencha os dados abaixo para começar a organizar o seu caixa.</p>"
                + alerta(erro)
                + "<form method=\"post\" action=\"/cadastrar\" class=\"form\">"
                + "<label for=\"nome\">Nome</label>"
                + "<input id=\"nome\" name=\"nome\" type=\"text\" autocomplete=\"name\" placeholder=\"Como você se chama\" "
                + "required maxlength=\"80\" value=\"" + esc(nome) + "\">"
                + "<label for=\"email\">E-mail</label>"
                + "<input id=\"email\" name=\"email\" type=\"email\" autocomplete=\"email\" placeholder=\"voce@email.com\" "
                + "required maxlength=\"120\" value=\"" + esc(email) + "\">"
                + "<label for=\"senha\">Senha</label>"
                + "<input id=\"senha\" name=\"senha\" type=\"password\" autocomplete=\"new-password\" "
                + "placeholder=\"Pelo menos 6 caracteres\" required minlength=\"6\" maxlength=\"100\">"
                + "<label for=\"confirmar\">Confirmar senha</label>"
                + "<input id=\"confirmar\" name=\"confirmar\" type=\"password\" autocomplete=\"new-password\" "
                + "placeholder=\"Repita a senha\" required minlength=\"6\" maxlength=\"100\">"
                + "<label class=\"aceite\" for=\"consentimento\"><input id=\"consentimento\" name=\"consentimento\" "
                + "type=\"checkbox\" value=\"sim\" required><span>Concordo que o CaixaCerto guarde meu nome, meu e-mail e "
                + "minhas movimentações para funcionar. Posso excluir minha conta e meus dados quando quiser.</span></label>"
                + "<button class=\"btn verde\" type=\"submit\">Criar conta</button>"
                + "<p class=\"centro suave\">Já tem conta? <a href=\"/entrar\">Entrar</a></p>"
                + "</form>"
                + "<p class=\"centro pequeno suave\">Pedimos só o necessário: nome, e-mail e senha. "
                + "Seus dados financeiros ficam visíveis apenas para você.</p>");
    }

    // ---------- 2. Início ----------

    static String inicio(String nome, boolean abaNegocio, Resumo r, List<Movimento> ultimas, YearMonth mes) {
        StringBuilder lista = new StringBuilder();
        if (ultimas.isEmpty()) {
            lista.append("<p class=\"vazio\">Nenhuma movimentação ainda. Registre a primeira.</p>");
        } else {
            lista.append("<ul class=\"lista\">");
            for (Movimento m : ultimas) {
                boolean entrada = m.natureza() == Movimento.Natureza.ENTRADA;
                lista.append("<li><div><span class=\"titulo-item\">").append(esc(m.descricao())).append("</span>")
                        .append("<span class=\"detalhe\">").append(m.tipo().rotulo).append(" · ").append(quando(m.data()))
                        .append("</span></div><b class=\"").append(entrada ? "verde" : "vermelho").append("\">")
                        .append(entrada ? "+ " : "− ").append(Dinheiro.formatar(m.centavos())).append("</b></li>");
            }
            lista.append("</ul>");
        }

        String primeiroNome = nome == null ? "" : nome.trim().split(" ")[0];
        String saudacao = primeiroNome.isEmpty() ? "" : "Olá, " + primeiroNome + " · ";

        String rotulo = abaNegocio ? "Sobrou de verdade este mês" : "Saldo pessoal do mês";
        long destaque = abaNegocio ? r.lucroReal() : r.saldoPessoal();
        long entradas = abaNegocio ? r.entradasNegocio() : r.entradasPessoal();
        long saidas = abaNegocio ? r.saidasNegocio() : r.saidasPessoal();

        return layout("Início", """
                <header class="topo"><div><span class="mes">""" + esc(saudacao + nomeDoMes(mes)) + """
                </span><h1>Seu caixa</h1></div>""" + LOGO + """
                </header>
                <nav class="abas" aria-label="Tipo de caixa">
                <a href="/?aba=negocio"\s""" + (abaNegocio ? "class=\"ativa\" aria-current=\"page\"" : "") + """
                >Negócio</a>
                <a href="/?aba=pessoal"\s""" + (abaNegocio ? "" : "class=\"ativa\" aria-current=\"page\"") + """
                >Pessoal</a></nav>
                <section class="cartao-escuro"><span class="rotulo">""" + rotulo + """
                </span><strong class="destaque">""" + Dinheiro.formatar(destaque) + """
                </strong><div class="duas"><div><span>Entradas</span><b class="menta">""" + Dinheiro.formatarInteiro(entradas) + """
                </b></div><div><span>Saídas</span><b class="salmao">""" + Dinheiro.formatarInteiro(saidas) + """
                </b></div></div></section>
                <div class="linha"><h2>Últimas movimentações</h2><a href="/resumo">Ver resumo</a></div>
                """ + lista + """
                <a class="btn verde empurra" href="/registrar">+ Registrar movimentação</a>
                <form method="post" action="/sair" class="rodape">
                <button class="link cinza" type="submit">Sair</button>
                <span aria-hidden="true">·</span>
                <a href="/conta/excluir" class="cinza">Excluir minha conta</a></form>
                """);
    }

    // ---------- 3. Registrar movimentação ----------

    static String registrar(String erro, Map<String, String> v) {
        String natureza = v.getOrDefault("natureza", "SAIDA");
        String tipo = v.getOrDefault("tipo", "NEGOCIO");
        String categoria = v.getOrDefault("categoria", CATEGORIA_PADRAO);
        String data = v.getOrDefault("data", LocalDate.now().toString());

        StringBuilder cats = new StringBuilder();
        for (String c : CATEGORIAS) {
            cats.append("<label class=\"chip\"><input type=\"radio\" name=\"categoria\" value=\"").append(esc(c)).append("\"")
                    .append(c.equals(categoria) ? " checked" : "").append("><span>").append(esc(c)).append("</span></label>");
        }

        return layout("Nova movimentação", """
                <header class="topo-voltar"><a class="voltar" href="/" aria-label="Voltar">←</a>
                <h1>Nova movimentação</h1></header>
                """ + alerta(erro) + """
                <form method="post" action="/registrar" class="form">
                <div class="seg" role="radiogroup" aria-label="Entrada ou saída">
                <label class="saida"><input type="radio" name="natureza" value="SAIDA"\s""" + ("SAIDA".equals(natureza) ? "checked" : "") + """
                ><span>Saída</span></label>
                <label class="entrada"><input type="radio" name="natureza" id="nat-entrada" value="ENTRADA"\s""" + ("ENTRADA".equals(natureza) ? "checked" : "") + """
                ><span>Entrada</span></label></div>
                <label for="valor">Valor</label>
                <input id="valor" name="valor" class="valor" inputmode="decimal" autocomplete="off" placeholder="R$ 0,00" required value="
                """.strip() + esc(v.getOrDefault("valor", "")) + """
                ">
                <label for="descricao">Descrição</label>
                <input id="descricao" name="descricao" maxlength="80" autocomplete="off" placeholder="Ex.: Compra de material" required value="
                """.strip() + esc(v.getOrDefault("descricao", "")) + """
                ">
                <fieldset><legend>Essa movimentação é do…</legend><div class="duas-opcoes">
                <label class="caixa"><input type="radio" name="tipo" value="NEGOCIO"\s""" + ("NEGOCIO".equals(tipo) ? "checked" : "") + """
                ><span>Negócio</span></label>
                <label class="caixa"><input type="radio" name="tipo" value="PESSOAL"\s""" + ("PESSOAL".equals(tipo) ? "checked" : "") + """
                ><span>Pessoal</span></label></div></fieldset>
                <fieldset class="cats"><legend>Categoria</legend><div class="chips">""" + cats + """
                </div></fieldset>
                <label for="data">Data</label>
                <input id="data" name="data" type="date" required max="
                """.strip() + LocalDate.now() + "\" value=\"" + esc(data) + """
                ">
                <button class="btn escuro empurra" type="submit">Salvar</button>
                </form>
                """);
    }

    // ---------- 4. Resumo do mês ----------

    static String resumo(Resumo r, YearMonth mes) {
        long custos = r.saidasNegocio();
        long pessoal = r.saidasPessoal();
        long sobra = Math.max(0, r.entradasNegocio() - custos - pessoal);
        long total = custos + pessoal + sobra;

        String grafico;
        if (total <= 0) {
            grafico = "<p class=\"vazio\">Registre entradas e saídas para ver para onde foi o dinheiro.</p>";
        } else {
            double pc = custos * 100.0 / total;
            double pp = pessoal * 100.0 / total;
            double ps = 100.0 - pc - pp;
            grafico = "<div class=\"barra\" role=\"img\" aria-label=\"Custos do negócio " + Math.round(pc)
                    + "%, gastos pessoais " + Math.round(pp) + "%, sobra " + Math.round(ps) + "%\">"
                    + "<div style=\"width:" + String.format(Locale.ROOT, "%.1f", ps) + "%;background:#12805C\"></div>"
                    + "<div style=\"width:" + String.format(Locale.ROOT, "%.1f", pc) + "%;background:#14263B\"></div>"
                    + "<div style=\"width:" + String.format(Locale.ROOT, "%.1f", pp) + "%;background:#B8402C\"></div></div>"
                    + "<div class=\"legenda\"><span><b style=\"color:#12805C\">●</b> Sobra " + Math.round(ps) + "%</span>"
                    + "<span><b style=\"color:#14263B\">●</b> Custos do negócio " + Math.round(pc) + "%</span>"
                    + "<span><b style=\"color:#B8402C\">●</b> Gastos pessoais " + Math.round(pp) + "%</span></div>";
        }

        return layout("Resumo do mês", """
                <header class="topo-voltar"><a class="voltar" href="/" aria-label="Voltar">←</a>
                <div><span class="mes">""" + esc(nomeDoMes(mes)) + """
                </span><h1>Resumo do mês</h1></div></header>
                <section class="cartao-claro"><span class="rotulo-verde">Lucro real do negócio</span>
                <strong class="destaque escuro-texto">""" + Dinheiro.formatar(r.lucroReal()) + """
                </strong><span class="pequeno">Entradas do negócio menos saídas do negócio.
                Vale só para o que foi registrado.</span></section>
                <h2>Para onde foi o dinheiro</h2>
                """ + grafico + """
                <dl class="tabela">
                <div><dt>Entradas do negócio</dt><dd class="verde">""" + Dinheiro.formatar(r.entradasNegocio()) + """
                </dd></div>
                <div><dt>Saídas do negócio</dt><dd class="vermelho">""" + negativo(r.saidasNegocio()) + """
                </dd></div>
                <div><dt>Entradas pessoais</dt><dd class="verde">""" + Dinheiro.formatar(r.entradasPessoal()) + """
                </dd></div>
                <div><dt>Gastos pessoais</dt><dd class="vermelho">""" + negativo(r.saidasPessoal()) + """
                </dd></div>
                <div class="total"><dt>Sobra do mês</dt><dd>""" + Dinheiro.formatar(r.sobraDoMes()) + """
                </dd></div></dl>
                <p class="pequeno suave empurra">Dica: separe uma parte do lucro para a sua reserva antes de gastar.</p>
                """);
    }

    // ---------- outras ----------

    static String confirmarExclusao() {
        return layout("Excluir conta", """
                <header class="topo-voltar"><a class="voltar" href="/" aria-label="Voltar">←</a>
                <h1>Excluir minha conta</h1></header>
                <p>Isso apaga a sua conta e <b>todas</b> as suas movimentações. Não dá para desfazer.</p>
                <form method="post" action="/conta/excluir" class="form">
                <button class="btn perigo" type="submit">Sim, excluir tudo</button>
                <a class="btn contorno" href="/">Cancelar</a></form>
                """);
    }

    static String erro(int codigo, String mensagem) {
        return layout("Erro " + codigo, "<h1>" + codigo + "</h1><p>" + esc(mensagem)
                + "</p><a class=\"btn escuro\" href=\"/\">Voltar ao início</a>");
    }

    // ---------- estilos (mesma identidade das telas do protótipo) ----------

    private static final String CSS = """
            *{box-sizing:border-box}
            body{margin:0;background:#E7EEEC;color:#14263B;font-family:'DM Sans',system-ui,-apple-system,'Segoe UI',Arial,sans-serif;font-size:16px;line-height:1.4}
            .app{max-width:430px;min-height:100vh;margin:0 auto;padding:44px 20px 24px;background:#F3F6F5;display:flex;flex-direction:column;gap:20px}
            h1,h2,.nome,.destaque,.valor,.grande{font-family:'Sora','DM Sans',system-ui,sans-serif;margin:0}
            h1{font-size:22px;font-weight:700}
            h2{font-size:18px;font-weight:600}
            p{margin:0}
            a{color:#12805C;font-weight:700}
            a:hover{color:#0c5e43}
            :focus-visible{outline:3px solid #14263B;outline-offset:2px}
            .marca{display:flex;align-items:center;gap:12px}
            .nome{font-size:24px;font-weight:700}
            .grande{font-size:32px;font-weight:700;line-height:1.15}
            .bloco{display:flex;flex-direction:column;gap:12px}
            .suave{color:#52636B}
            .pequeno{font-size:14px}
            .centro{text-align:center}
            .empurra{margin-top:auto}
            .topo{display:flex;align-items:center;justify-content:space-between}
            .topo-voltar{display:flex;align-items:center;gap:12px}
            .voltar{display:flex;align-items:center;justify-content:center;width:44px;height:44px;border-radius:12px;background:#fff;border:1px solid #DCE4E1;color:#14263B;text-decoration:none;font-size:20px}
            .mes{display:block;font-size:14px;color:#52636B;font-weight:400}
            .form{display:flex;flex-direction:column;gap:18px;flex:1}
            label,legend{font-size:14px;font-weight:500}
            fieldset{border:0;margin:0;padding:0;display:flex;flex-direction:column;gap:8px}
            legend{padding:0;margin-bottom:8px}
            input[type=text],input[type=email],input[type=password],input[type=date],input:not([type]){height:52px;padding:0 16px;border:1px solid #C9D4D0;border-radius:12px;font:inherit;font-size:16px;background:#fff;color:#14263B;width:100%}
            .valor{height:64px!important;font-size:28px!important;font-weight:700}
            .btn{display:flex;align-items:center;justify-content:center;height:56px;border-radius:16px;border:0;font:inherit;font-weight:700;font-size:17px;text-decoration:none;cursor:pointer;width:100%}
            .escuro{background:#14263B;color:#fff}.escuro:hover{color:#fff;background:#1d3550}
            .verde.btn{background:#12805C;color:#fff}.verde.btn:hover{color:#fff;background:#0c5e43}
            .perigo{background:#B8402C;color:#fff}.contorno{background:#fff;border:1px solid #C9D4D0;color:#14263B}
            .link{background:none;border:0;padding:0;font:inherit;font-weight:700;color:#12805C;text-decoration:underline;cursor:pointer}
            .link.cinza,.cinza{color:#52636B;font-weight:500}
            .alerta{background:#FBE6E1;color:#7A2415;border-radius:12px;padding:12px 16px;font-weight:500}
            .aceite{display:flex;gap:12px;align-items:flex-start;font-size:14px;font-weight:400;color:#52636B}
            .aceite input{width:22px;height:22px;margin:2px 0 0;flex:none;accent-color:#12805C}
            .abas{display:flex;gap:6px;padding:4px;background:#E3EAE7;border-radius:14px}
            .abas a{flex:1;text-align:center;padding:12px 0;border-radius:10px;color:#52636B;font-weight:500;text-decoration:none}
            .abas a.ativa{background:#fff;color:#14263B;font-weight:700}
            .cartao-escuro{background:#14263B;color:#fff;border-radius:20px;padding:22px;display:flex;flex-direction:column;gap:14px}
            .rotulo{color:#C5D3DC}
            .destaque{font-size:38px;font-weight:700;line-height:1.1}
            .duas{display:flex;gap:12px}
            .duas>div{flex:1;background:#1F3A55;border-radius:12px;padding:12px;display:flex;flex-direction:column;gap:2px}
            .duas span{font-size:13px;color:#C5D3DC}
            .duas b{font-size:17px}
            .menta{color:#5FD3A5}.salmao{color:#FFB4A3}
            .linha{display:flex;align-items:center;justify-content:space-between}
            .lista{list-style:none;margin:0;padding:0;background:#fff;border-radius:16px;border:1px solid #DCE4E1}
            .lista li{display:flex;align-items:center;gap:12px;padding:14px 16px;border-bottom:1px solid #DCE4E1}
            .lista li:last-child{border-bottom:0}
            .lista li>div{flex:1;display:flex;flex-direction:column;gap:2px;min-width:0}
            .titulo-item{font-weight:500;overflow-wrap:anywhere}
            .detalhe{font-size:13px;color:#52636B}
            .verde{color:#12805C}.vermelho{color:#B8402C}
            .vazio{background:#fff;border:1px dashed #C9D4D0;border-radius:16px;padding:20px;color:#52636B;text-align:center}
            .rodape{display:flex;justify-content:center;gap:12px;font-size:14px}
            .seg{display:flex;gap:6px;padding:4px;background:#E3EAE7;border-radius:14px}
            .seg label{flex:1;position:relative}
            .seg input{position:absolute;opacity:0;inset:0}
            .seg span{display:block;text-align:center;padding:12px 0;border-radius:10px;color:#52636B;font-weight:500}
            .seg input:checked+span{background:#fff;font-weight:700}
            .seg .saida input:checked+span{color:#B8402C}
            .seg .entrada input:checked+span{color:#12805C}
            .seg input:focus-visible+span{outline:3px solid #14263B}
            .duas-opcoes{display:flex;gap:10px}
            .caixa{flex:1;position:relative}
            .caixa input,.chip input{position:absolute;opacity:0;inset:0}
            .caixa span{display:flex;align-items:center;justify-content:center;height:52px;border:1px solid #C9D4D0;border-radius:12px;background:#fff;color:#52636B;font-size:16px}
            .caixa input:checked+span{border:2px solid #12805C;background:#E3F3EC;color:#0c5e43;font-weight:700}
            .chips{display:flex;flex-wrap:wrap;gap:8px}
            .chip{position:relative}
            .chip span{display:flex;align-items:center;height:44px;padding:0 16px;border:1px solid #C9D4D0;border-radius:22px;background:#fff;color:#52636B}
            .chip input:checked+span{border:2px solid #14263B;color:#14263B;font-weight:700}
            .caixa input:focus-visible+span,.chip input:focus-visible+span{outline:3px solid #14263B;outline-offset:2px}
            form:has(#nat-entrada:checked) .cats{display:none}
            .cartao-claro{background:#E3F3EC;border-radius:20px;padding:22px;display:flex;flex-direction:column;gap:6px}
            .rotulo-verde{color:#0c5e43;font-weight:500}
            .escuro-texto{color:#14263B}
            .barra{display:flex;height:18px;border-radius:9px;overflow:hidden;background:#DCE4E1}
            .legenda{display:flex;flex-wrap:wrap;gap:6px 16px;font-size:14px;color:#52636B}
            .tabela{margin:0;display:flex;flex-direction:column;background:#fff;border-radius:16px;border:1px solid #DCE4E1}
            .tabela>div{display:flex;align-items:center;padding:14px 16px;border-bottom:1px solid #DCE4E1}
            .tabela>div:last-child{border-bottom:0}
            .tabela dt{flex:1}.tabela dd{margin:0;font-weight:700}
            .tabela .total dt,.tabela .total dd{font-weight:700;color:#14263B}
            """;
}
