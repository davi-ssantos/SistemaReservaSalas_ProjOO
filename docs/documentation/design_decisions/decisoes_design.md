# Decisões de Design

---
## Singleton - `RepositorioSalas`

### Problema
O sistema precisa de uma única fonte de verdade para salas, reservas e usuários.
Múltiplas instâncias do repositório e seu arquivo causariam inconsistências - uma reserva criada em uma instância não seria visível em outra.

### Solução
`RepositorioSalas` implementa o padrão Singleton com construtor privado e método `getInstance()` sincronizado, o que nos garante *thread-safety* conforme exigido no enunciado.

### Outras alternativas
Poderíamos usar variáveis estáticas diretamente nas classes, mas isso aumentaria o acoplamento ruim e viola o SRP.

---
## Factory Method - `FabricaDeSalas`

### Problema
O sistema precisa instanciar diferentes tipos de sala (`SalaEstudoIndividual`, `SalaTrabalhoGrupo`, `Laboratorio`) em vários pontos, e instanciar diretamente com `new` intensificaria o acoplamento às classes concretas de salas por todo o código.

### Solução
`FabricaDeSalas` centraliza a criação de salas através de um método estático `criarSala()` que recebe um `TipoSala` e retorna a subclasse correta. O restante do sistema conhece apenas a abstração `Sala`, nunca as classes concretas.

### Outras alternativas
Poderíamos ter instanciado as salas diretamente no `SistemaDeReservas` (violando o SRP e deixando maior uma das maiores classes do projeto). Descartamos também por violar o OCP - adicionar um novo tipo de sala exigiria modificar `SistemaDeReservas`. Com Factory, apenas criamos uma nova subclasse e atualizamos a classe fábrica.

--- 
## Strategy - `PoliticaDeReserva`

### Problema
O sistema precisa suportar diferentes políticas de detecção de conflitos de reserva, como "primeiro a reservar" ou "prioridade para docente", e também permitir a troca entre elas em tempo de execução, sem modificar o código existente.

### Solução
`PoliticaDeReserva` é uma interface com o método  `validar()`, e cada política a implementa de forma independente: `PrimeiroAReservar` e `PrioridadeDocente` fazem esse função no projeto. O `SistemaDeReservas` mantém uma referência à interface e possui `setPolitica()` para troca em tempo de execução.

### Outras alternativas
Usar condicionais `if/else` dentro de `SistemasDeReservas` para decidir a política, mas isso claramente viola OCP - adicionar uma nova política significa modificar a classe adicionando uma cadeia de `if/else`'s, dificultando manutenção e teste do código.

---
## Observer - `Observador`

### Problema
Quando uma reserva é alterada ou cancelada, todos os envolvidos precisam ser notificados imediatamente - o usuário responsável pela reserva e o serviço de relatório. Notificar diretamente dentro de `Reserva` criaria acoplamento forte ruim com cada destinatário.

### Solução
`Observador` é a interface com o método `notificar()`, implementada por `UsuarioObservador` e `ServicoRelatorio`. A classe `Reserva` mantém uma lista de observadores registrados no construtor e os notifica via `notificarObservadores()` sempre que seu estado muda - em `confirmar()`, `modificar()` e `cancelar()`.

O padrão foi implementado em duas modalidades, conforme enunciado:
- **Push:** a `Reserva` passa o `Evento` completo ao notificar, com tipo, timestamp e referência à reserva;
- **Pull:** o observador pode buscar detalhes adicionais no `RepositorioSalas` a partir da referência recebida.

### Outras alternativas
Notificar os destinatários diretamente dentro dos métodos de `Reserva`. Descartado, pois viola o OCP - adicionar um novo destinatário significa modificar `Reserva`, além do desnecessário acoplamento entre a classe de domínio e os serviços de notificação.

---
## Decorator - `ReservaDecorator`

### Problema
Reservas podem ter funcionalidades extras opcionais - equipamento multimídia, serviço de limpeza, quadro negro - e combinações destas. Modelar isso com herança criaria uma explosão de subclasses para cada combinação possível.

### Solução
`ReservaDecorator` é uma classe abstrata que recebe em seu construtor uma instância de `Reserva` e define os métodos `getDescricaoExtras()` e `getCustoExtra()`. Cada decorator concreto - `ReservaComMultimidia`, `ReservaComLimpeza`, `ResevaComQuadroNegro` - adiciona sua própria descrição e custo. Os decorators podem ser empilhados livremente em tempo de execução:

```java
Reserva reserva = new Reserva(...);
reserva = new ReservaComMultimidia(reserva);
reserva = new ReservaComLimpeza(reserva);
```

### Outras alternativas
Criar subclasses para cada combinação de funcionalidades extras, como `ReservaComMultimidiaELimpeza`. Descartado pois com três extras já seriam sete subclasses possíveis - inviável de manter e escalar.

---
## Visitor — `VisitorRelatorio`

### Problema
O sistema precisa gerar diferentes tipos de relatório sobre o histórico de reservas - relatório diário e histórico por sala - sem acoplar essa lógica à  `Reserva`. Adicionar métodos de relatório diretamente em `Reserva` violaria o SRP e dificultaria a adição de novos tipos de relatório, violando também OCP.

### Solução
`VisitorRelatorio` define a interface com o método `visitar()`, implementada por `VisitorRelatorioDiario` e `VisitorHistoricoSala`. A classe `Reserva` possui o método `aceitar()`, que recebe um visitor e o deixa operar sobre seus dados. Cada visitor acumula as reservas relevantes e gera o relatório via `gerarRelatorio()`.

### Alternativas consideradas
Implementar a lógica de relatório diretamente no `RepositorioSalas` ou no `SistemaDeReservas`. Descartado pois violaria o SRP — essas classes já têm responsabilidades bem definidas, e adicionar geração de relatório as sobrecarregaria.

---
## Facade — `SistemaDeReservas`

### Problema
O projeto envolve múltiplos subsistemas - `RepositorioSalas`, `FabricaDeSalas`, `PoliticaDeReserva`, observers e visitors - que precisam se comunicar entre si de maneira organizada. Expor toda essa complexidade de forma direta à interface do terminal tornaria o código do `cli` difícil de manter e fortemente acoplado aos detalhes internos do sistema, violando o princípio de Ocultamento de Informação.

### Solução
`SistemaDeReservas` atua como interface central, oferecendo métodos de alto nível como `criarReserva()`, `cancelarReserva()` e `gerarRelatorioDiario()`. O terminal fala exclusivamente com essa classe, sem conhecer os subsistemas internos. Internamente, `SistemaDeReservas` orquestra o repositório, a fábrica, a política e os observers de forma
transparente.

### Alternativas consideradas
Deixar o `cli` interagir diretamente com cada subsistema. Descartado pois criaria forte acoplamento entre a interface do terminal e os detalhes internos do sistema, dificultando manutenção e tornando o código do `cli` excessivamente complexo.

---
## Proxy - `SalaProxy`

### Problema
As salas são persistidas em um arquivo. Carregar todos os dados do arquivo a cada inicialização do sistema ou busca de dados seria ineficiente, especialmente se o número de salas for grande. O restante do sistema não deveria precisar saber se uma sala vem da memória ou do arquivo.

### Solução
`SalaProxy` intermedia as buscas de sala feitas pelo `RepositorioSalas`. Se a sala já está em memória no cache `salasCarregadas`, retorna diretamente. Caso contrário carrega do arquivo via `carregarDoArquivo()`, armazena no cache e retorna. Isto é lazy load transparente para o restante do sistema.

### Outras alternativas
Todas as salas no arquivo são carregadas quando o sistema é inicializado. Descartado pois carrega dados que não necessariamente serão usados, aumentando o tempo de inicialização e o consumo de memória.

---
## Template Method - `ReservaRecorrente`

### Problema 

O sistema precisa suportar geração automática de reservas recorrentes — diária, semanal e mensal. O fluxo de geração é sempre o mesmo: iterar pelas datas, validar cada ocorrência e criar a reserva. Apenas o cálculo da próxima data varia entre os tipos de recorrência. 

### Solução 

`ReservaRecorrente` define o Template Method `gerarReservas()` como `final`, fixando o fluxo completo e impedindo que subclasses o alterem. O único ponto de variação — `calcularProximaData()` — é declarado abstrato e implementado por cada subclasse: `ReservaRecorrenteDiaria`, `ReservaRecorrenteSemanal` e `ReservaRecorrenteMensal`. 

### Outras alternativas 

Usar Strategy para encapsular a regra de recorrência. Descartado pois o Template Method é mais adequado quando o algoritmo completo pertence à hierarquia de classes — o fluxo de geração e a regra de recorrência são inseparáveis. Strategy faria mais sentido se a regra precisasse ser trocada em tempo de execução independentemente do objeto.

---
São estas todas as decisões de design no projeto que envolvem os *design patterns* aprendidos na disciplina.