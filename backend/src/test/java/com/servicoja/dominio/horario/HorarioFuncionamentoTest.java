package com.servicoja.dominio.horario;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorarioFuncionamentoTest {

    private final HorarioFuncionamento manhaDeQuarta =
            new HorarioFuncionamento(null, DayOfWeek.WEDNESDAY.getValue(), LocalTime.of(8, 0), LocalTime.of(12, 0));

    @Test
    void abertoDaAberturaAteAntesDoFechamento() {
        assertThat(manhaDeQuarta.cobre(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0))).isTrue();
        assertThat(manhaDeQuarta.cobre(DayOfWeek.WEDNESDAY, LocalTime.of(11, 59))).isTrue();
        assertThat(manhaDeQuarta.cobre(DayOfWeek.WEDNESDAY, LocalTime.of(12, 0))).isFalse();
        assertThat(manhaDeQuarta.cobre(DayOfWeek.WEDNESDAY, LocalTime.of(7, 59))).isFalse();
    }

    @Test
    void naoCobreOutroDiaDaSemana() {
        assertThat(manhaDeQuarta.cobre(DayOfWeek.THURSDAY, LocalTime.of(9, 0))).isFalse();
    }

    @Test
    void consideraTodosOsIntervalosDoDia() {
        HorarioFuncionamento tardeDeQuarta =
                new HorarioFuncionamento(null, DayOfWeek.WEDNESDAY.getValue(), LocalTime.of(13, 30), LocalTime.of(18, 0));
        List<HorarioFuncionamento> horarios = List.of(manhaDeQuarta, tardeDeQuarta);
        LocalDateTime quarta = LocalDateTime.of(2026, 10, 7, 0, 0);

        assertThat(HorarioFuncionamento.algumCobre(horarios, quarta.withHour(15))).isTrue();
        assertThat(HorarioFuncionamento.algumCobre(horarios, quarta.withHour(12).withMinute(45))).isFalse();
        assertThat(HorarioFuncionamento.algumCobre(List.of(), quarta.withHour(10))).isFalse();
    }
}
