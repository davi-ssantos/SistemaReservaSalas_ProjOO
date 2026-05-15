## Template Method - `ReservaRecorrente`

### Problema 

O sistema precisa suportar geração automática de reservas recorrentes — diária, semanal e mensal. O fluxo de geração é sempre o mesmo: iterar pelas datas, validar cada ocorrência e criar a reserva. Apenas o cálculo da próxima data varia entre os tipos de recorrência. 

### Solução 

`ReservaRecorrente` define o Template Method `gerarReservas()` como `final`, fixando o fluxo completo e impedindo que subclasses o alterem. O único ponto de variação — `calcularProximaData()` — é declarado abstrato e implementado por cada subclasse: `ReservaRecorrenteDiaria`, `ReservaRecorrenteSemanal` e `ReservaRecorrenteMensal`. 

### Outras alternativas 

Usar Strategy para encapsular a regra de recorrência. Descartado pois o Template Method é mais adequado quando o algoritmo completo pertence à hierarquia de classes — o fluxo de geração e a regra de recorrência são inseparáveis. Strategy faria mais sentido se a regra precisasse ser trocada em tempo de execução independentemente do objeto.