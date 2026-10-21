package br.ufpb.caixacerto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Conversão e formatação de valores em reais. Internamente tudo é guardado em centavos (long). */
final class Dinheiro {
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final BigDecimal MAXIMO = new BigDecimal("9999999.99");

    private Dinheiro() {}

    /** Aceita "95", "95,5", "1.250,50", "R$ 95,00". Lança IllegalArgumentException com mensagem amigável. */
    static long paraCentavos(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Informe o valor.");
        }
        String t = texto.replace("R$", "").replace(" ", "").replace(" ", "").trim();
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", ""); // 1.250 = mil duzentos e cinquenta
        }
        BigDecimal v;
        try {
            v = new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido. Use números, como 95,00.");
        }
        v = v.setScale(2, RoundingMode.HALF_UP);
        if (v.signum() <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }
        if (v.compareTo(MAXIMO) > 0) {
            throw new IllegalArgumentException("Valor muito alto para o MVP.");
        }
        return v.movePointRight(2).longValue();
    }

    /** Ex.: 214000 vira "R$ 2.140,00"; valores negativos viram "− R$ 10,00". */
    static String formatar(long centavos) {
        String sinal = centavos < 0 ? "− " : "";
        return sinal + "R$ " + numero(Math.abs(centavos));
    }

    /** Ex.: 9500 vira "95,00" (sem o R$). */
    static String numero(long centavos) {
        return String.format(PT_BR, "%,.2f", BigDecimal.valueOf(centavos, 2));
    }

    /** Ex.: 430000 vira "R$ 4.300" (sem centavos, para cartões compactos). */
    static String formatarInteiro(long centavos) {
        String sinal = centavos < 0 ? "− " : "";
        return sinal + "R$ " + String.format(PT_BR, "%,d", Math.abs(centavos) / 100);
    }
}
