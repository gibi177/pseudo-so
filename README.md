# Pseudo-Sistema Operacional Multiprogramado

## Descrição do Projeto

Este projeto consiste na implementação de um pseudo-sistema operacional
multiprogramado desenvolvido como trabalho prático da disciplina de
Sistemas Operacionais.

O objetivo principal é simular o funcionamento interno de um sistema
operacional, reproduzindo mecanismos fundamentais como:

-   Gerenciamento de processos;
-   Escalonamento de CPU;
-   Gerenciamento de memória virtual;
-   Controle de recursos de entrada e saída;
-   Prevenção de deadlocks;
-   Sistema de arquivos persistente.

A implementação foi realizada utilizando **Java 21 LTS**, seguindo uma
arquitetura modular inspirada na organização de um kernel real.

------------------------------------------------------------------------

# Arquitetura do Sistema

A arquitetura do pseudo-SO é dividida em módulos independentes
coordenados pelo componente central denominado **Dispatcher**.

O Dispatcher é responsável por controlar:

-   Relógio global da simulação;
-   Comunicação entre módulos;
-   Escalonamento de processos;
-   Evolução dos estados dos processos;
-   Execução das operações de memória;
-   Liberação de recursos.

A comunicação entre módulos ocorre exclusivamente através do Dispatcher,
reduzindo o acoplamento e garantindo determinismo durante a execução.

------------------------------------------------------------------------

# Estrutura dos Módulos

## Gerenciamento de Processos

Cada processo é representado por um **Process Control Block (PCB)**.

O PCB contém informações como:

-   PID;
-   Prioridade inicial;
-   Prioridade dinâmica;
-   Estado atual;
-   Tempo total de CPU;
-   Tempo executado;
-   Referências de memória;
-   Contador de instruções;
-   Número de page faults;
-   Tempo de espera em filas.

Os estados possíveis são:

-   `PRONTO`
-   `EXECUTANDO`
-   `BLOQUEADO`

------------------------------------------------------------------------

# Escalonamento da CPU

O escalonamento utiliza uma política baseada em **Multilevel Feedback
Queue (MLFQ)**.

O sistema possui quatro níveis de prioridade:

  Prioridade   Tipo         Política
  ------------ ------------ --------------------
  0            Tempo real   FIFO sem preempção
  1            Usuário      MLFQ
  2            Usuário      MLFQ
  3            Usuário      MLFQ

Processos de usuário possuem quantum de execução limitado.

Quando um processo ultrapassa o quantum:

1.  O processo sofre preempção;
2.  Sua prioridade é reduzida;
3.  Ele retorna ao final da fila correspondente.

------------------------------------------------------------------------

# Política de Aging

Para evitar starvation, foi implementado o mecanismo de envelhecimento.

O funcionamento é:

1.  Processos aguardando nas filas têm seu tempo de espera incrementado;
2.  Ao atingir o limite definido, o processo é promovido para uma fila
    de maior prioridade;
3.  O contador de espera é reiniciado.

Essa estratégia garante maior justiça na utilização da CPU.

------------------------------------------------------------------------

# Gerenciamento de Memória

A memória física simulada possui:

-   20 frames;
-   1 KB por frame.

A memória é dividida em:

-   8 frames reservados para processos de tempo real;
-   12 frames destinados aos processos de usuário.

O gerenciamento utiliza:

-   Memória virtual;
-   Paginação;
-   Working Set;
-   Substituição LRU.

------------------------------------------------------------------------

# Paginação e Working Set

Cada processo possui seu próprio conjunto de páginas residentes.

Quando uma página é acessada:

## Page Hit

Caso a página esteja carregada:

-   A página é considerada utilizada;
-   Sua posição na lista LRU é atualizada.

## Page Fault

Caso a página não esteja carregada:

-   O contador de falhas aumenta;
-   A página é carregada;
-   Caso necessário, uma página antiga é removida.

------------------------------------------------------------------------

# Algoritmo LRU

A substituição utiliza o algoritmo:

**Least Recently Used (LRU)**

A estrutura utilizada é uma `LinkedList`.

Funcionamento:

-   Primeiro elemento:
    -   Página menos recentemente utilizada.
-   Último elemento:
    -   Página utilizada mais recentemente.

Quando uma página é acessada, ela é movida para o final da lista.

------------------------------------------------------------------------

# Gerenciamento de Recursos

O módulo de recursos controla dispositivos de entrada e saída:

-   1 scanner;
-   2 impressoras;
-   1 modem;
-   2 discos SATA.

O acesso aos recursos utiliza sincronização através de:

``` java
synchronized
```

garantindo exclusão mútua durante alterações de estado.

------------------------------------------------------------------------

# Prevenção de Deadlocks

O sistema utiliza alocação atômica de recursos.

A política adotada é:

> Tudo ou nada.

Um processo somente recebe seus recursos quando todos estão disponíveis
simultaneamente.

Caso contrário:

-   Nenhum recurso é reservado;
-   O processo permanece aguardando.

Essa estratégia evita a condição de espera circular necessária para
deadlocks.

------------------------------------------------------------------------

# Sistema de Arquivos

O sistema de arquivos utiliza uma implementação baseada em:

-   Disco virtual;
-   Alocação contígua;
-   Algoritmo First-Fit.

O disco é representado por um vetor:

    String[] disco

Cada posição representa um bloco.

Valores:

-   `"0"` → bloco livre;
-   Nome do arquivo → bloco ocupado.

------------------------------------------------------------------------

# Criação de Arquivos

A criação utiliza First-Fit:

1.  Percorre o disco sequencialmente;
2.  Procura uma sequência contínua de blocos livres;
3.  Reserva os blocos encontrados;
4.  Atualiza os metadados.

------------------------------------------------------------------------

# Exclusão de Arquivos

O sistema controla permissões através do PID do processo criador.

Regras:

-   Processos de tempo real possuem privilégio administrativo;
-   Processos comuns só podem excluir arquivos próprios.

Após exclusão:

-   Os blocos retornam ao estado livre.

------------------------------------------------------------------------

# Fluxo de Execução

Cada ciclo do Dispatcher executa:

1.  Verificação de novos processos;
2.  Inserção nas filas;
3.  Seleção do próximo processo;
4.  Solicitação de recursos;
5.  Execução de CPU;
6.  Atualização da memória;
7.  Tratamento de finalização ou preempção;
8.  Aplicação de aging;
9.  Atualização do relógio global.

Ao final da simulação:

-   Operações de arquivos são executadas;
-   Estado do disco é exibido;
-   Estatísticas de page faults são apresentadas.

------------------------------------------------------------------------

# Estrutura de Diretórios

Exemplo da organização do projeto:

    ProjetoSO/
    │
    ├── src/
    │   ├── processos/
    │   │   └── ProcessControlBlock.java
    │   │
    │   ├── modulos/
    │   │   ├── Dispatcher.java
    │   │   ├── GerenciadorFilas.java
    │   │   ├── GerenciadorMemoria.java
    │   │   ├── GerenciadorRecursos.java
    │   │   └── GerenciadorArquivos.java
    │   │
    │   └── util/
    │       └── LeitorEntrada.java
    │
    ├── build.sh
    ├── Makefile
    └── README.md

------------------------------------------------------------------------

# Compilação e Execução

## Requisitos

-   Java 21 ou superior;
-   Sistema operacional compatível com execução de scripts shell.

## Compilação

Execute:

``` bash
make
```

ou:

``` bash
./build.sh
```

------------------------------------------------------------------------

# Execução

Após compilação:

``` bash
./dispatcher processes.txt files.txt string.txt
```

O sistema iniciará a simulação e exibirá:

-   Evolução dos processos;
-   Eventos de escalonamento;
-   Falhas de página;
-   Operações de arquivos;
-   Estado final do disco.

------------------------------------------------------------------------

# Tecnologias Utilizadas

  Tecnologia     Uso
  -------------- ---------------------------
  Java 21 LTS    Implementação principal
  Git            Controle de versão
  Makefile       Automação de compilação
  Shell Script   Automatização de execução

------------------------------------------------------------------------

# Integrantes

## Felipe Lopes Gibin Duarte

Responsável por:

-   Escalonamento MLFQ;
-   Aging;
-   Gerenciamento de memória;
-   Controle de recursos;
-   Estrutura inicial do Dispatcher.

## Bernardo Gomes Rodrigues

Responsável por:

-   PCB;
-   Estados dos processos;
-   Controle do ciclo de vida;
-   Leitor de entrada.

## Isaac Silva

Responsável por:

-   Sistema de arquivos;
-   Algoritmo First-Fit;
-   Controle de permissões;
-   Documentação técnica.

------------------------------------------------------------------------

# Inteligência Artificial

Durante o desenvolvimento foi utilizada inteligência artificial
generativa como ferramenta auxiliar para:

-   Revisão de código;
-   Identificação de melhorias estruturais;
-   Apoio na documentação;
-   Análise de possíveis problemas lógicos.

Todas as decisões arquiteturais, implementação dos algoritmos e
validação final foram realizadas pela equipe.

------------------------------------------------------------------------

# Conceitos Aplicados

O projeto aplica conceitos fundamentais de Sistemas Operacionais:

-   Process Control Block;
-   Escalonamento preemptivo;
-   Multilevel Feedback Queue;
-   Aging;
-   Memória virtual;
-   Paginação;
-   Working Set;
-   LRU;
-   Sincronização;
-   Deadlock;
-   Sistemas de arquivos.

------------------------------------------------------------------------

# Referências

-   Stallings, W. *Operating Systems: Internals and Design Principles.*
-   Silberschatz, A.; Galvin, P.; Gagne, G. *Operating System Concepts.*
