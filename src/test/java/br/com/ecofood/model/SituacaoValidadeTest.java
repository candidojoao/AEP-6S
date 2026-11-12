package br.com.ecofood.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoValidadeTest {

    @Test
    void deveClassificarPelosDiasQueFaltam() {
        assertEquals(SituacaoValidade.VENCIDO, SituacaoValidade.para(-1));
        assertEquals(SituacaoValidade.VENCE_HOJE, SituacaoValidade.para(0));
        assertEquals(SituacaoValidade.PERTO_DO_VENCIMENTO, SituacaoValidade.para(1));
    }
}
