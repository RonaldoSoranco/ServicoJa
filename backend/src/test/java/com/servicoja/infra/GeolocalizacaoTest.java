package com.servicoja.infra;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeolocalizacaoTest {

    @Test
    void umGrauDeLatitudeTemCercaDe111Km() {
        assertThat(Geolocalizacao.distanciaKm(-28.0, -52.0, -29.0, -52.0)).isCloseTo(111.2, within(0.1));
    }

    @Test
    void mesmoPontoTemDistanciaZero() {
        assertThat(Geolocalizacao.distanciaKm(-28.4489, -52.1992, -28.4489, -52.1992)).isZero();
    }

    @Test
    void distanciaEntreMarauEPassoFundo() {
        assertThat(Geolocalizacao.distanciaKm(-28.4489, -52.1992, -28.2620, -52.4064)).isCloseTo(28.6, within(1.0));
    }
}
