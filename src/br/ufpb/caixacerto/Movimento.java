package br.ufpb.caixacerto;

import java.time.LocalDate;

/** Uma entrada ou saída de dinheiro, do negócio ou pessoal (US02, US03 e US04). */
record Movimento(
        long id,
        String email,
        LocalDate data,
        Natureza natureza,
        Tipo tipo,
        String categoria,
        long centavos,
        String descricao) {

    enum Natureza { ENTRADA, SAIDA }

    enum Tipo {
        NEGOCIO("Negócio"),
        PESSOAL("Pessoal");

        final String rotulo;

        Tipo(String rotulo) {
            this.rotulo = rotulo;
        }
    }

    /** Valor com sinal: entradas positivas, saídas negativas. */
    long assinado() {
        return natureza == Natureza.ENTRADA ? centavos : -centavos;
    }
}
