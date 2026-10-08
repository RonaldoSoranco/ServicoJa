package com.servicoja.dominio.horario;

import com.servicoja.dominio.empresa.Empresa;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

/**
 * Um intervalo em que a empresa atende, em um dia da semana (ISO-8601: 1 = segunda ... 7 = domingo).
 * Um mesmo dia pode ter mais de um intervalo (ex.: pausa para o almoco).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "horarios_funcionamento")
public class HorarioFuncionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    @Column(nullable = false)
    private LocalTime abre;

    @Column(nullable = false)
    private LocalTime fecha;

    public HorarioFuncionamento(Empresa empresa, Integer diaSemana, LocalTime abre, LocalTime fecha) {
        this.empresa = empresa;
        this.diaSemana = diaSemana;
        this.abre = abre;
        this.fecha = fecha;
    }

    /** O horario de abertura conta como aberto; o de fechamento, como fechado. */
    public boolean cobre(DayOfWeek dia, LocalTime hora) {
        return diaSemana == dia.getValue() && !hora.isBefore(abre) && hora.isBefore(fecha);
    }

    public static boolean algumCobre(Collection<HorarioFuncionamento> horarios, LocalDateTime momento) {
        return horarios.stream().anyMatch(h -> h.cobre(momento.getDayOfWeek(), momento.toLocalTime()));
    }
}
