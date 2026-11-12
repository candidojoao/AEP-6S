package br.com.ecofood.config;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockConfigTest {

    @Test
    void deveUsarOHorarioDeBrasilia() {
        assertEquals(ZoneId.of("America/Sao_Paulo"), new ClockConfig().relogio().getZone());
    }
}
