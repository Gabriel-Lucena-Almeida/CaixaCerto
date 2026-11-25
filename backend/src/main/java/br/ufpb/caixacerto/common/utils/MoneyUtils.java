package br.ufpb.caixacerto.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitários para manipulação de valores monetários.
 * Internamente tudo é guardado em centavos (long) para evitar erros de ponto flutuante.
 * Portado do Dinheiro.java original.
 */
public final class MoneyUtils {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat INTEGER_FORMATTER = NumberFormat.getNumberInstance(PT_BR);
    private static final BigDecimal MAX_VALUE = new BigDecimal("9999999.99");

    static {
        INTEGER_FORMATTER.setMinimumFractionDigits(0);
        INTEGER_FORMATTER.setMaximumFractionDigits(0);
    }

    private MoneyUtils() {}

    /**
     * Converte string para centavos.
     * Aceita: "95", "95,5", "1.250,50", "R$ 95,00", "95.00"
     *
     * @throws IllegalArgumentException se valor inválido
     */
    public static long toCents(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Informe o valor.");
        }

        String t = text.replace("R$", "")
                .replace(" ", "") // non-breaking space
                .replace(" ", "")
                .trim();

        // Formato brasileiro: 1.250,50 -> 1250.50
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            // 1.250 = mil duzentos e cinquenta (sem centavos)
            t = t.replace(".", "");
        }

        BigDecimal value;
        try {
            value = new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido. Use números, como 95,00.");
        }

        value = value.setScale(2, RoundingMode.HALF_UP);

        if (value.signum() <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }

        if (value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Valor muito alto (máximo R$ 9.999.999,99).");
        }

        return value.movePointRight(2).longValueExact();
    }

    /**
     * Formata centavos para "R$ 1.250,00".
     * Valores negativos: "− R$ 10,00".
     */
    public static String format(long cents) {
        String sign = cents < 0 ? "− " : "";
        return sign + CURRENCY_FORMATTER.format(BigDecimal.valueOf(Math.abs(cents), 2));
    }

    /**
     * Formata centavos para "1.250,00" (sem R$).
     */
    public static String formatNumber(long cents) {
        return CURRENCY_FORMATTER.format(BigDecimal.valueOf(Math.abs(cents), 2)).replace("R$", "").trim();
    }

    /**
     * Formata centavos para "R$ 1.250" (sem centavos, para cards compactos).
     */
    public static String formatInteger(long cents) {
        String sign = cents < 0 ? "− " : "";
        long reais = Math.abs(cents) / 100;
        return sign + "R$ " + INTEGER_FORMATTER.format(reais);
    }

    /**
     * Formata apenas o número inteiro (sem R$, sem centavos).
     */
    public static String formatIntegerNumber(long cents) {
        long reais = Math.abs(cents) / 100;
        return INTEGER_FORMATTER.format(reais);
    }

    /**
     * Converte centavos para BigDecimal com 2 casas decimais.
     */
    public static BigDecimal toBigDecimal(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }

    /**
     * Soma segura de centavos (evita overflow).
     */
    public static long add(long a, long b) {
        return Math.addExact(a, b);
    }

    /**
     * Subtração segura de centavos.
     */
    public static long subtract(long a, long b) {
        return Math.subtractExact(a, b);
    }
}