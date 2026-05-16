package main.br.unifesp.reservasalas.patterns.facade;

// Pacotes usados
import main.br.unifesp.reservasalas.domain.Reserva;
import main.br.unifesp.reservasalas.domain.Sala;
import main.br.unifesp.reservasalas.domain.Usuario;
import main.br.unifesp.reservasalas.enums.TipoSala;
import main.br.unifesp.reservasalas.patterns.factory.FabricaDeSalas;
import main.br.unifesp.reservasalas.patterns.observer.Observador;
import main.br.unifesp.reservasalas.patterns.observer.ServicoRelatorio;
import main.br.unifesp.reservasalas.patterns.observer.UsuarioObservador;
import main.br.unifesp.reservasalas.patterns.singleton.RepositorioSalas;
import main.br.unifesp.reservasalas.patterns.strategy.PoliticaDeReserva;
import main.br.unifesp.reservasalas.patterns.templatemethod.ReservaRecorrente;
import main.br.unifesp.reservasalas.patterns.visitor.VisitorHistoricoSala;
import main.br.unifesp.reservasalas.patterns.visitor.VisitorRelatorioDiario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SistemaDeReservas {

    private RepositorioSalas    repositorio;
    private PoliticaDeReserva   politica;
    private boolean             silenciarConflitos = false;

    public SistemaDeReservas(PoliticaDeReserva politica) {
        this.repositorio    = RepositorioSalas.getInstance();
        this.politica       = politica;
    }

    // Configuração
    public void setPolitica(PoliticaDeReserva politica) {
        this.politica = politica;
    }

    // Usuários
    public void cadastrarUsuario(Usuario usuario) {
        repositorio.adicionarUsuario(usuario);
    }

    public Usuario buscarUsuario(String id) {
        return repositorio.buscarUsuarioPorId(id);
    }

    // Salas
    public void cadastrarSala(TipoSala tipo, String id, String nome, int capacidade) {
        Sala sala = FabricaDeSalas.criarSala(tipo, id, nome, capacidade);
        repositorio.adicionarSala(sala);
    }

    public List<Sala> listarSalasDisponiveis() {
        return repositorio.listarSalasDisponiveis();
    }

    public List<Sala> listarSalasPorIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        List<Sala> disponiveis = new ArrayList<>();
        for (Sala sala : repositorio.listarSalas()) {
            if (salaDisponiveNoIntervalo(sala, inicio, fim)) {
                disponiveis.add(sala);
            }
        }
        return disponiveis;
    }

    private boolean salaDisponiveNoIntervalo(Sala sala, LocalDateTime inicio, LocalDateTime fim) {
        for (Reserva reserva : repositorio.listarReservasPorSala(sala.getId())) {
            if (reserva.getInicio().isBefore(fim) && reserva.getFim().isAfter(inicio)) {
                return false;
            }
        }
        return true;
    }

    // Reservas
    public Reserva criarReserva(Usuario usuario, Sala sala,
                                LocalDateTime inicio, LocalDateTime fim) {

        List<Reserva> existentes = repositorio.listarReservasPorSala(sala.getId());

        List<Observador> observadores = new ArrayList<>();
        observadores.add(new UsuarioObservador(usuario));
        observadores.add(new ServicoRelatorio());

        Reserva reserva = new Reserva(
                UUID.randomUUID().toString(),
                usuario, sala, inicio, fim, observadores
        );

        if (!politica.validar(reserva, existentes)) {
            if (!silenciarConflitos) {
                System.out.println("Conflito detectado! Reserva não criada.");
            }
            return null;
        }

        reserva.confirmar();
        repositorio.adicionarReserva(reserva);
        return reserva;
    }

    public List<Reserva> criarReservaRecorrente(ReservaRecorrente recorrencia) {
        boolean silenciarAnterior = silenciarConflitos;
        silenciarConflitos = true;
        try {
            return recorrencia.gerarReservas(this);
        } finally {
            silenciarConflitos = silenciarAnterior;
        }
    }

    public void modificarReserva(String id, LocalDateTime novoInicio, LocalDateTime novoFim) {
        Reserva reserva = repositorio.buscarReservaPorId(id);
        if (reserva == null) {
            System.out.println("Reserva não encontrada.");
            return;
        }

        List<Reserva> existentes = repositorio.listarReservasPorSala(reserva.getSala().getId());
        existentes.remove(reserva);

        if (!politica.validar(reserva, existentes)) {
            System.out.println("Conflito detectado! Reserva não modificada.");
            return;
        }

        reserva.modificar(novoInicio, novoFim);
    }

    public void cancelarReserva(String id) {
        Reserva reserva = repositorio.buscarReservaPorId(id);
        if (reserva == null) {
            System.out.println("Reserva não encontrada.");
            return;
        }
        reserva.cancelar();
        repositorio.removerReserva(id);
    }

    public List<Reserva> listarReservasPorSala(String salaId) {
        return repositorio.listarReservasPorSala(salaId);
    }

    public List<Reserva> listarReservasPorUsuario(String usuarioId) {
        return repositorio.listarReservasPorUsuario(usuarioId);
    }

    // Relatórios
    public String gerarRelatorioDiario(LocalDate data) {
        VisitorRelatorioDiario visitor = new VisitorRelatorioDiario(data);
        for (Reserva reserva : repositorio.listarReservasPorData(data)) {
            reserva.aceitar(visitor);
        }
        return visitor.gerarRelatorio();
    }

    public String gerarHistoricoSala(String salaId) {
        VisitorHistoricoSala visitor = new VisitorHistoricoSala(salaId);
        for (Reserva reserva : repositorio.listarReservasPorSala(salaId)) {
            reserva.aceitar(visitor);
        }
        return visitor.gerarRelatorio();
    }
}