package main.br.unifesp.reservasalas.patterns.templatemethod;

import main.br.unifesp.reservasalas.domain.Sala;
import main.br.unifesp.reservasalas.domain.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaRecorrenteDiaria extends ReservaRecorrente {

    public ReservaRecorrenteDiaria(Usuario usuario, Sala sala,
                                   LocalTime horaInicio, LocalTime horaFim,
                                   LocalDate dataInicio, LocalDate dataFim) {
        super(usuario, sala, horaInicio, horaFim, dataInicio, dataFim);
    }

    @Override
    public LocalDate calcularProximaData(LocalDate dataAtual) {
        return dataAtual.plusDays(1);
    }
}
