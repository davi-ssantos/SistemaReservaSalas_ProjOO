package main.br.unifesp.reservasalas.patterns.templatemethod;

import main.br.unifesp.reservasalas.domain.Sala;
import main.br.unifesp.reservasalas.domain.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaRecorrenteMensal extends ReservaRecorrente {

    public ReservaRecorrenteMensal(Usuario usuario, Sala sala,
                                   LocalTime horaInicio, LocalTime horaFim,
                                   LocalDate dataInicio, LocalDate dataFim) {
        super(usuario, sala, horaInicio, horaFim, dataInicio, dataFim);
    }

    @Override
    public LocalDate calcularProximaData(LocalDate dataAtual) {
        return dataAtual.plusMonths(1);
    }
}
