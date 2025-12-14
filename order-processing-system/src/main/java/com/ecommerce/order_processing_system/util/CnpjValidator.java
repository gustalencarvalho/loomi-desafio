package com.ecommerce.order_processing_system.util;

public final class CnpjValidator {

    private CnpjValidator() {

    }

    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        // Remove tudo que não for dígito
        String digits = cnpj.replaceAll("\\D", "");

        // Tem que ter exatamente 14 dígitos
        if (digits.length() != 14) {
            return false;
        }

        // Rejeita sequências óbvias inválidas (000..., 111..., etc.)
        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        // Calcula primeiro dígito verificador
        int firstCheckDigit = calculateCheckDigit(digits.substring(0, 12));
        // Calcula segundo dígito verificador
        int secondCheckDigit = calculateCheckDigit(digits.substring(0, 12) + firstCheckDigit);

        // Compara com os dígitos informados
        return digits.charAt(12) == (char) ('0' + firstCheckDigit)
                && digits.charAt(13) == (char) ('0' + secondCheckDigit);
    }

    private static int calculateCheckDigit(String base) {
        int[] weights = (base.length() == 12)
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int num = base.charAt(i) - '0';
            sum += num * weights[i];
        }

        int mod = sum % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }
}
