package br.ufpb.caixacerto;

import java.time.YearMonth;
import java.util.List;

/** Totais de um mês. O lucro real é a conta central do produto (US05). */
record Resumo(long entradasNegocio, long saidasNegocio, long entradasPessoal, long saidasPessoal) {

    static Resumo doMes(List<Movimento> movimentos, YearMonth mes) {
        long eN = 0, sN = 0, eP = 0, sP = 0;
        for (Movimento m : movimentos) {
            if (!YearMonth.from(m.data()).equals(mes)) {
                continue;
            }
            boolean entrada = m.natureza() == Movimento.Natureza.ENTRADA;
            if (m.tipo() == Movimento.Tipo.NEGOCIO) {
                if (entrada) eN += m.centavos(); else sN += m.centavos();
            } else {
                if (entrada) eP += m.centavos(); else sP += m.centavos();
            }
        }
        return new Resumo(eN, sN, eP, sP);
    }

    /** Entradas do negócio menos saídas do negócio. */
    long lucroReal() {
        return entradasNegocio - saidasNegocio;
    }

    /** Entradas pessoais menos gastos pessoais. */
    long saldoPessoal() {
        return entradasPessoal - saidasPessoal;
    }

    /** O que sobrou de verdade: lucro do negócio mais o saldo pessoal. */
    long sobraDoMes() {
        return lucroReal() + saldoPessoal();
    }
}
