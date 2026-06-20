package modulos;

import processos.ProcessControlBlock;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Mantém as interfaces e funções que operam sobre os processos, 
 * controlando as políticas de escalonamento (FIFO e Feedback).
 */
public class GerenciadorFilas {

    private final Queue<ProcessControlBlock> filaTempoReal;
    private final Queue<ProcessControlBlock> filaUsuario1;
    private final Queue<ProcessControlBlock> filaUsuario2;
    private final Queue<ProcessControlBlock> filaUsuario3;

    public GerenciadorFilas() {
        this.filaTempoReal = new LinkedList<>();
        this.filaUsuario1 = new LinkedList<>();
        this.filaUsuario2 = new LinkedList<>();
        this.filaUsuario3 = new LinkedList<>();
    }

    public void enfileirarProcesso(ProcessControlBlock pcb) {
        if (pcb.isConcluido()) {
            return;
        }

        int prioridade = pcb.getPrioridadeAtual();

        switch (prioridade) {
            case 0: filaTempoReal.add(pcb); break;
            case 1: filaUsuario1.add(pcb); break;
            case 2: filaUsuario2.add(pcb); break;
            case 3: filaUsuario3.add(pcb); break;
            default:
                throw new IllegalArgumentException("Prioridade inválida: " + prioridade);
        }
    }

    public ProcessControlBlock buscarProximoProcesso() {
        if (!filaTempoReal.isEmpty()) return filaTempoReal.poll();
        if (!filaUsuario1.isEmpty()) return filaUsuario1.poll();
        if (!filaUsuario2.isEmpty()) return filaUsuario2.poll();
        if (!filaUsuario3.isEmpty()) return filaUsuario3.poll();
        return null;
    }

    /**
     * Incrementa o contador de espera para todos os processos enfileirados e
     * aplica o aging aos processos que ultrapassaram o limite de tolerância[cite: 24].
     */
    public void aplicarAgingGlobal(int limiteStarvation) {
        // Filas a serem avaliadas (Tempo Real não sofre aging)
        Queue[] filasUsuario = {filaUsuario1, filaUsuario2, filaUsuario3};

        for (int i = 0; i < filasUsuario.length; i++) {
            Queue<ProcessControlBlock> filaAtiva = filasUsuario[i];
            int tamanhoOriginal = filaAtiva.size();

            for (int j = 0; j < tamanhoOriginal; j++) {
                ProcessControlBlock pcb = filaAtiva.poll();
                if (pcb != null) {
                    pcb.incrementarTempoEsperando();

                    // Se a fila atual (i) for a 1, 2 ou 3 (índices 0, 1, 2) e o processo estourou o limite de espera
                    if (i > 0 && pcb.getTempoEsperando() >= limiteStarvation) {
                        pcb.resetarTempoEsperando();
                        // Promove a prioridade (diminui o valor numérico) [cite: 24]
                        pcb.setPrioridadeAtual(pcb.getPrioridadeAtual() - 1);
                        enfileirarProcesso(pcb); // Reinsere na fila promovida
                    } else {
                        // Não promove, devolve para o final da mesma fila
                        filaAtiva.add(pcb);
                    }
                }
            }
        }
    }
}