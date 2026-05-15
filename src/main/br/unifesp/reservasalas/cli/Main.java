package main.br.unifesp.reservasalas.cli;

import main.br.unifesp.reservasalas.domain.Aluno;
import main.br.unifesp.reservasalas.domain.Docente;
import main.br.unifesp.reservasalas.domain.Reserva;
import main.br.unifesp.reservasalas.domain.Sala;
import main.br.unifesp.reservasalas.domain.Usuario;
import main.br.unifesp.reservasalas.enums.TipoSala;
import main.br.unifesp.reservasalas.patterns.facade.SistemaDeReservas;
import main.br.unifesp.reservasalas.patterns.templatemethod.ReservaRecorrente;
import main.br.unifesp.reservasalas.patterns.templatemethod.ReservaRecorrenteDiaria;
import main.br.unifesp.reservasalas.patterns.templatemethod.ReservaRecorrenteMensal;
import main.br.unifesp.reservasalas.patterns.templatemethod.ReservaRecorrenteSemanal;
import main.br.unifesp.reservasalas.patterns.strategy.PrimeiroAReservar;
import main.br.unifesp.reservasalas.patterns.strategy.PrioridadeDocente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static SistemaDeReservas sistema    = new SistemaDeReservas(new PrimeiroAReservar());
    private static Scanner scanner              = new Scanner(System.in);
    private static DateTimeFormatter formatter      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static DateTimeFormatter formatterData  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static DateTimeFormatter formatterHora  = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) {

        popularSistema();       // Adiciona dados para teste
        configuracoesPadrao();  // Mostra as configurações padrão ao usuário

        int opcao = -1;
        while (opcao != 0) {    // Interface
            exibirMenu();
            opcao = lerInt();
            switch (opcao) {
                case 1:     listarSalasDisponiveis(); break;
                case 2:     listarSalasPorIntervalo(); break;
                case 3:     criarReserva(); break;
                case 4:     modificarReserva(); break;
                case 5:     cancelarReserva(); break;
                case 6:     listarReservasPorSala(); break;
                case 7:     listarReservasPorUsuario(); break;
                case 8:     gerarRelatorioDiario(); break;
                case 9:     gerarHistoricoSala(); break;
                case 10:    trocarPolitica(); break;
                case 11:    criarReservaRecorrente(); break;
                case 0:     System.out.println("Encerrando sistema..."); break;
                default:    System.out.println("Opção inválida.");
            }
        }
    }

    private static void exibirMenu() {
        System.out.println("\n=== Sistema de Reserva de Salas ===");
        System.out.println("\t1.\tListar salas disponíveis");
        System.out.println("\t2.\tListar salas disponíveis por intervalo");
        System.out.println("\t3.\tCriar reserva");
        System.out.println("\t4.\tModificar reserva");
        System.out.println("\t5.\tCancelar reserva");
        System.out.println("\t6.\tListar reservas por sala");
        System.out.println("\t7.\tListar reservas por usuário");
        System.out.println("\t8.\tGerar relatório diário");
        System.out.println("\t9.\tGerar histórico de sala");
        System.out.println("\t10.\tTrocar política de reserva");
        System.out.println("\t11.\tCriar reserva recorrente");
        System.out.println("\t0.\tSair");
        System.out.print("Opção: ");
    }

    private static void listarSalasDisponiveis() {
        List<Sala> salas = sistema.listarSalasDisponiveis();
        if (salas.isEmpty()) {
            System.out.println("Nenhuma sala disponível.");
            return;
        }
        System.out.println("\n=== Salas Disponíveis ===");
        for (Sala sala : salas) {
            System.out.println("ID: " + sala.getId() +
                    "\t|\t" + sala.getDescricao());
        }
    }

    private static void listarSalasPorIntervalo() {
        System.out.print("Início (dd/MM/yyyy HH:mm): ");
        LocalDateTime inicio = lerDateTime();
        System.out.print("Fim (dd/MM/yyyy HH:mm): ");
        LocalDateTime fim = lerDateTime();
        if (inicio == null || fim == null) return;

        List<Sala> salas = sistema.listarSalasPorIntervalo(inicio, fim);
        if (salas.isEmpty()) {
            System.out.println("Nenhuma sala disponível nesse intervalo.");
            return;
        }
        System.out.println("\n=== Salas Disponíveis no Intervalo ===");
        for (Sala sala : salas) {
            System.out.println("ID: " + sala.getId() +
                    "\t|\t" + sala.getDescricao());
        }
    }

    private static void criarReserva() {
        System.out.print("ID do usuário: ");
        String usuarioId = scanner.nextLine();
        System.out.print("ID da sala: ");
        String salaId = scanner.nextLine();
        System.out.print("Início (dd/MM/yyyy HH:mm): ");
        LocalDateTime inicio = lerDateTime();
        System.out.print("Fim (dd/MM/yyyy HH:mm): ");
        LocalDateTime fim = lerDateTime();
        if (inicio == null || fim == null) return;

        Usuario usuario = sistema.buscarUsuario(usuarioId);
        if (usuario == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }

        Sala sala = sistema.listarSalasDisponiveis().stream()
                .filter(s -> s.getId().equals(salaId))
                .findFirst().orElse(null);

        if (sala == null) {
            System.out.println("Sala não encontrada.");
            return;
        }

        Reserva reserva = sistema.criarReserva(usuario, sala, inicio, fim);
        if (reserva != null) {
            System.out.println("Reserva criada com sucesso! ID: " + reserva.getId());
        }
    }

    private static void criarReservaRecorrente() {
        System.out.print("ID do usuário: ");
        String usuarioId = scanner.nextLine();
        System.out.print("ID da sala: ");
        String salaId = scanner.nextLine();

        Usuario usuario = sistema.buscarUsuario(usuarioId);
        if (usuario == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }

        Sala sala = sistema.listarSalasDisponiveis().stream()
                .filter(s -> s.getId().equals(salaId))
                .findFirst().orElse(null);

        if (sala == null) {
            System.out.println("Sala não encontrada.");
            return;
        }

        System.out.print("Horário de início (HH:mm): ");
        LocalTime horaInicio = lerHora();
        System.out.print("Horário de fim (HH:mm): ");
        LocalTime horaFim = lerHora();
        System.out.print("Data de início (dd/MM/yyyy): ");
        LocalDate dataInicio = lerData();
        System.out.print("Data de fim (dd/MM/yyyy): ");
        LocalDate dataFim = lerData();
        if (horaInicio == null || horaFim == null || dataInicio == null || dataFim == null) return;

        System.out.println("Tipo de recorrência:");
        System.out.println("1. Diária");
        System.out.println("2. Semanal");
        System.out.println("3. Mensal");
        System.out.print("Opção: ");
        int opcao = lerInt();

        ReservaRecorrente recorrencia;
        switch (opcao) {
            case 1:
                recorrencia = new ReservaRecorrenteDiaria(usuario, sala, horaInicio, horaFim, dataInicio, dataFim);
                break;
            case 2:
                recorrencia = new ReservaRecorrenteSemanal(usuario, sala, horaInicio, horaFim, dataInicio, dataFim);
                break;
            case 3:
                recorrencia = new ReservaRecorrenteMensal(usuario, sala, horaInicio, horaFim, dataInicio, dataFim);
                break;
            default:
                System.out.println("Opção inválida.");
                return;
        }

        List<Reserva> reservas = sistema.criarReservaRecorrente(recorrencia);
        System.out.println("Reservas criadas com sucesso: " + reservas.size());
    }

    private static void modificarReserva() {
        System.out.print("ID da reserva: ");
        String id = scanner.nextLine();
        System.out.print("Novo início (dd/MM/yyyy HH:mm): ");
        LocalDateTime novoInicio = lerDateTime();
        System.out.print("Novo fim (dd/MM/yyyy HH:mm): ");
        LocalDateTime novoFim = lerDateTime();
        if (novoInicio == null || novoFim == null) return;

        sistema.modificarReserva(id, novoInicio, novoFim);
        System.out.println("Reserva modificada com sucesso!");
    }

    private static void cancelarReserva() {
        System.out.print("ID da reserva: ");
        String id = scanner.nextLine();
        sistema.cancelarReserva(id);
        System.out.println("Reserva cancelada com sucesso!");
    }

    private static void listarReservasPorSala() {
        System.out.print("ID da sala: ");
        String salaId = scanner.nextLine();
        List<Reserva> reservas = sistema.listarReservasPorSala(salaId);
        if (reservas.isEmpty()) {
            System.out.println("Nenhuma reserva encontrada.");
            return;
        }
        System.out.println("\n=== Reservas da Sala ===");
        for (Reserva reserva : reservas) {
            exibirReserva(reserva);
        }
    }

    private static void listarReservasPorUsuario() {
        System.out.print("ID do usuário: ");
        String usuarioId = scanner.nextLine();
        List<Reserva> reservas = sistema.listarReservasPorUsuario(usuarioId);
        if (reservas.isEmpty()) {
            System.out.println("Nenhuma reserva encontrada.");
            return;
        }
        System.out.println("\n=== Reservas do Usuário ===");
        for (Reserva reserva : reservas) {
            exibirReserva(reserva);
        }
    }

    private static void gerarRelatorioDiario() {
        System.out.print("Data (dd/MM/yyyy): ");
        String dataStr = scanner.nextLine();
        try {
            LocalDate data = LocalDate.parse(dataStr,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            System.out.println(sistema.gerarRelatorioDiario(data));
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida.");
        }
    }

    private static void gerarHistoricoSala() {
        System.out.print("ID da sala: ");
        String salaId = scanner.nextLine();
        System.out.println(sistema.gerarHistoricoSala(salaId));
    }

    private static void trocarPolitica() {
        System.out.println("Escolha a política:");
        System.out.println("1. Primeiro a reservar");
        System.out.println("2. Prioridade para docente");
        System.out.print("Opção: ");
        int opcao = lerInt();
        switch (opcao) {
            case 1:
                sistema.setPolitica(new PrimeiroAReservar());
                System.out.println("Política alterada para: Primeiro a reservar.");
                break;
            case 2:
                sistema.setPolitica(new PrioridadeDocente());
                System.out.println("Política alterada para: Prioridade para docente.");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private static void exibirReserva(Reserva reserva) {
        System.out.println("ID: " + reserva.getId() +
                " | Sala: " + reserva.getSala().getNome() +
                " | Usuário: " + reserva.getUsuario().getNome() +
                " | Início: " + reserva.getInicio().format(formatter) +
                " | Fim: " + reserva.getFim().format(formatter) +
                " | Status: " + reserva.getStatus());
    }

    private static LocalDateTime lerDateTime() {
        try {
            return LocalDateTime.parse(scanner.nextLine(), formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use dd/MM/yyyy HH:mm");
            return null;
        }
    }

    private static LocalDate lerData() {
        try {
            return LocalDate.parse(scanner.nextLine(), formatterData);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use dd/MM/yyyy");
            return null;
        }
    }

    private static LocalTime lerHora() {
        try {
            return LocalTime.parse(scanner.nextLine(), formatterHora);
        } catch (DateTimeParseException e) {
            System.out.println("Formato inválido. Use HH:mm");
            return null;
        }
    }

    private static int lerInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
            return -1;
        }
    }

    private static void popularSistema() {
        sistema.cadastrarSala(TipoSala.INDIVIDUAL, "S01", "Sala 01", 1);
        sistema.cadastrarSala(TipoSala.GRUPO, "S02", "Sala 02", 10);
        sistema.cadastrarSala(TipoSala.LABORATORIO, "L01", "Lab 01", 20);

        sistema.cadastrarUsuario(new Aluno("A01", "Davi Santos",
                "davi@unifesp.br", "123", "176001"));
        sistema.cadastrarUsuario(new Docente("D01", "Prof. Maria",
                "maria@unifesp.br", "456", "Computação"));
    }

    private static void configuracoesPadrao() {
    System.out.println("\n=== Configurações Padrão ===");
    System.out.println("Política de reserva padrão: Primeiro a reservar");

    System.out.println("\nSalas cadastradas:");
    System.out.println("- ID: S01, Tipo: INDIVIDUAL, Nome: Sala 01, Capacidade: 1");
    System.out.println("- ID: S02, Tipo: GRUPO, Nome: Sala 02, Capacidade: 10");
    System.out.println("- ID: L01, Tipo: LABORATORIO, Nome: Lab 01, Capacidade: 20");

    System.out.println("\nUsuários cadastrados:");
    System.out.println("- ID: A01, Nome: Davi Santos, Email: davi@unifesp.br, Tipo: Aluno, Matrícula: 176001");
    System.out.println("- ID: D01, Nome: Prof. Maria, Email: maria@unifesp.br, Tipo: Docente, Disciplina: Computação");
    }
}