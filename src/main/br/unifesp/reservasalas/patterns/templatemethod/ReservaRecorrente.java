package main.br.unifesp.reservasalas.patterns.templatemethod;

import main.br.unifesp.reservasalas.domain.Reserva;
import main.br.unifesp.reservasalas.domain.Sala;
import main.br.unifesp.reservasalas.domain.Usuario;
import main.br.unifesp.reservasalas.patterns.facade.SistemaDeReservas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class ReservaRecorrente {

    protected Usuario usuario;
    protected Sala sala;
    protected LocalTime horaInicio;
    protected LocalTime horaFim;
    protected LocalDate dataInicio;
    protected LocalDate dataFim;
    protected List<Reserva> reservasGeradas;

    protected ReservaRecorrente(Usuario usuario, Sala sala,
                                LocalTime horaInicio, LocalTime horaFim,
                                LocalDate dataInicio, LocalDate dataFim) {
        this.usuario = usuario;
        this.sala = sala;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.reservasGeradas = new ArrayList<>();
    }

    public final List<Reserva> gerarReservas(SistemaDeReservas sistema) {
        LocalDate dataAtual = dataInicio;
        while (!dataAtual.isAfter(dataFim)) {
            LocalDateTime inicio = dataAtual.atTime(horaInicio);
            LocalDateTime fim = dataAtual.atTime(horaFim);
            Reserva reserva = sistema.criarReserva(usuario, sala, inicio, fim);
            if (reserva != null) {
                reservasGeradas.add(reserva);
            }
            dataAtual = calcularProximaData(dataAtual);
        }
        return reservasGeradas;
    }

    public abstract LocalDate calcularProximaData(LocalDate dataAtual);

    public Usuario getUsuario() { return usuario; }
    public Sala getSala() { return sala; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public List<Reserva> getReservasGeradas() { return reservasGeradas; }
}
