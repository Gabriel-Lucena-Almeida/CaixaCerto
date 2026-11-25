package br.ufpb.caixacerto.movement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

public class SummaryDTOs {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private YearMonth month;
        private Long entradasNegocio;
        private Long saidasNegocio;
        private Long entradasPessoal;
        private Long saidasPessoal;
        private Long lucroReal;
        private Long saldoPessoal;
        private Long sobraDoMes;

        // Formatted versions for frontend
        private String formattedEntradasNegocio;
        private String formattedSaidasNegocio;
        private String formattedEntradasPessoal;
        private String formattedSaidasPessoal;
        private String formattedLucroReal;
        private String formattedSaldoPessoal;
        private String formattedSobraDoMes;

        // Chart data
        public ChartData getChartData() {
            long custos = saidasNegocio;
            long pessoal = saidasPessoal;
            long sobra = Math.max(0, entradasNegocio - custos - pessoal);
            long total = custos + pessoal + sobra;

            if (total <= 0) {
                return ChartData.builder()
                        .hasData(false)
                        .message("Registre entradas e saídas para ver para onde foi o dinheiro.")
                        .build();
            }

            double pc = custos * 100.0 / total;
            double pp = pessoal * 100.0 / total;
            double ps = 100.0 - pc - pp;

            return ChartData.builder()
                    .hasData(true)
                    .sobraPercent(Math.round(ps))
                    .custosPercent(Math.round(pc))
                    .pessoalPercent(Math.round(pp))
                    .build();
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChartData {
            private boolean hasData;
            private String message;
            private long sobraPercent;
            private long custosPercent;
            private long pessoalPercent;
        }
    }
}