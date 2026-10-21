package br.ufpb.caixacerto;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Senhas nunca são guardadas em texto aberto (UE01 e RNF de segurança). */
final class Seguranca {
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int ITERACOES = 120_000;

    private Seguranca() {}

    static String novoSal() {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    static String hashDaSenha(String senha, String salBase64) {
        try {
            KeySpec spec = new PBEKeySpec(senha.toCharArray(), Base64.getDecoder().decode(salBase64), ITERACOES, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível proteger a senha.", e);
        }
    }

    static boolean confere(String senha, String salBase64, String hashEsperado) {
        byte[] a = hashDaSenha(senha, salBase64).getBytes();
        byte[] b = hashEsperado.getBytes();
        return MessageDigest.isEqual(a, b);
    }

    /** Identificador aleatório e imprevisível para a sessão (cookie). */
    static String novoToken() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
