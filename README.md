# 📋 Sistema de Reserva de Salas

Sistema de gerenciamento de reserva de salas (estudo individual, trabalho em grupo, laboratórios) com suporte a múltiplas políticas de alocação e notificações em tempo real focado no ambiente acadêmico.

**Projeto Prático - Padrões de Projeto Orientados a Objetos** | UNIFESP

---
## Funcionalidades

- ✅ **Consultar Disponibilidade**: Listar salas disponíveis em um intervalo de tempo;
- ✅ **Criar Reservas**: Reservar salas com validação de conflitos;
- ✅ **Modificar Reservas**: Alterar data/hora de reservas existentes;
- ✅ **Cancelar Reservas**: Cancelar reservas com notificação de usuários;
- 🆕 **Reservas Recorrentes**: Gerar automaticamente reservas diárias, semanais ou mensais;
- ✅ **Políticas de Reserva**: Suporta "Primeiro a Reservar" e "Prioridade Docente" (intercambíveis em tempo de execução);
- ✅ **Relatórios**: Gerar relatórios diários e histórico de salas;
- ✅ **Serviços Adicionais**: Decorar reservas com limpeza, multimídia e quadro negro;
- ✅ **Notificações**: Usuários recebem notificações via padrão Observer;
- ✅ **Persistência**: Suporte a carregamento lazy de salas via arquivo;

---
## Arquitetura & Padrões de Projeto

O projeto implementa **8 Design Patterns**:

### 1. **Singleton** - `RepositorioSalas`
Garante única instância do repositório central (salas, reservas, usuários), necessária para consistência de dados e thread-safety.

### 2. **Factory Method** - `FabricaDeSalas`
Centraliza criação de diferentes tipos de sala (`SalaEstudoIndividual`, `SalaTrabalhoGrupo`, `Laboratorio`), reduzindo acoplamento.

### 3. **Strategy** - `PoliticaDeReserva`
Permite trocar políticas de alocação em tempo de execução sem modificar código existente.

- `PrimeiroAReservar`: Primeira solicitação válida ganha
- `PrioridadeDocente`: Docentes têm prioridade sobre alunos

### 4. **Observer** - `Observador`
Notifica automaticamente usuários e serviço de relatório quando reservas são criadas/modificadas/canceladas.

- **Push Mode**: Reserva passa evento completo
- **Pull Mode**: Observador consulta repositório para detalhes

### 5. **Decorator** - `ReservaDecorator`
Adiciona funcionalidades extras às reservas dinamicamente:

- `ReservaComMultimidia`: Adiciona equipamento multimídia
- `ReservaComLimpeza`: Adiciona serviço de limpeza
- `ReservaComQuadroNegro`: Adiciona quadro negro

### 6. **Visitor** - `VisitorRelatorio`
Gera diferentes tipos de relatório sem acoplar lógica à classe `Reserva`:

- `VisitorRelatorioDiario`: Relatório do dia
- `VisitorHistoricoSala`: Histórico completo de uma sala

### 7. **Facade** - `SistemaDeReservas`
Interface única de alto nível para o sistema, orquestrando internamente todos os subsistemas.

### 8. **Proxy** - `SalaProxy`
Implementa lazy loading de salas do arquivo, mantendo cache em memória.

### 9. [NOVO] **Template Method** - `ReservaRecorrente`
Define o fluxo de geração de reservas recorrentes na classe abstrata,
delegando apenas o cálculo do intervalo às subclasses:

- `ReservaRecorrenteDiaria`: Recorrência diária
- `ReservaRecorrenteSemanal`: Recorrência semanal
- `ReservaRecorrenteMensal`: Recorrência mensal

---
## Como Usar

### Compilar
```bash
javac -d bin src/main/br/unifesp/reservasalas/**/*.java
```

### Executar
```bash
java -cp bin main.br.unifesp.reservasalas.cli.Main
```

### Menu Principal da CLI
```
=== Sistema de Reserva de Salas ===
1.  Listar salas disponíveis
2.  Listar salas disponíveis por intervalo
3.  Criar reserva
4.  Modificar reserva
5.  Cancelar reserva
6.  Listar reservas por sala
7.  Listar reservas por usuário
8.  Gerar relatório diário
9.  Gerar histórico de sala
10. Criar reserva recorrente <--- NOVIDADE
11. Trocar política de reserva
12. Sair
```

---

## Fluxos Principais

### Criar Reserva
1. Usuário fornece dados (usuário, sala, intervalo)
2. Sistema valida conflitos usando a política ativa
3. Se válido, cria `Reserva` com observadores registrados
4. Observadores notificados via evento `CRIACAO`
5. Reserva armazenada no repositório

### Criar Reserva Recorrente - NOVIDADE
1. Usuário fornece dados (usuário, sala, horário, intervalo de datas e tipo)
2. Sistema instancia a subclasse correta de `ReservaRecorrente`
3. Template Method itera pelas datas gerando uma reserva por ocorrência
4. Conflitos em ocorrências individuais são ignorados sem interromper o fluxo
5. Retorna lista de reservas criadas com sucesso

### Consultar Salas Disponíveis
1. Usuário especifica intervalo de tempo
2. Sistema busca todas as salas
3. Filtra aquelas sem conflitos no intervalo
4. Retorna lista de salas disponíveis

### Gerar Relatório Diário
1. Usuário especifica data
2. Sistema cria `VisitorRelatorioDiario`
3. Itera sobre reservas do dia aplicando visitor
4. Visitor acumula dados e formata relatório
5. Relatório exibido na CLI

---

## Dados de Teste (Padrão)

**Salas:**
- `S01` - Sala 01 (Estudo Individual, cap. 1)
- `S02` - Sala 02 (Trabalho em Grupo, cap. 10)
- `L01` - Lab 01 (Laboratório, cap. 20)

**Usuários:**
- `A01` - Davi Santos (Aluno, Matrícula: 176001)
- `D01` - Prof. Maria (Docente, Disciplina: Computação)

**Política Padrão:** Primeiro a Reservar

---
## Requisitos Funcionais Atendidos

- **RF-01**: Consultar salas disponíveis por intervalo
- **RF-02**: Criar, modificar e cancelar reservas
- **RF-03**: Validar conflitos com política ativa
- **RF-04**: Notificar usuários sobre alterações
- **RF-05**: Gerar relatórios e históricos

---

## Documentação Adicional

Veja a pasta `docs/` para:
- `decisoes_design.md` - Justificativas de cada padrão
- `logica_negocio.md` - Fluxos detalhados de cada funcionalidade
- `diagrams/` - Diagramas UML de cada padrão

---

## Autor

**Davi Santos**   - Arquitetura, documentação e implementação base
**João Vitor Mancio** - Implementação de reservas recorrentes
Projeto Prático - Disciplina de Padrões de Projeto Orientados a Objetos  
UNIFESP