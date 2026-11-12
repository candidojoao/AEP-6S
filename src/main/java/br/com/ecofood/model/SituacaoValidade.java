package br.com.ecofood.model;

public enum SituacaoValidade {
    VENCIDO,
    VENCE_HOJE,
    PERTO_DO_VENCIMENTO;

    public static SituacaoValidade para(long diasParaVencer) {
        if (diasParaVencer < 0) {
            return VENCIDO;
        }
        if (diasParaVencer == 0) {
            return VENCE_HOJE;
        }
        return PERTO_DO_VENCIMENTO;
    }
}
