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
    
    // Limite arquitetural definido na especificação 
    private static final int CAPACIDADE_MAXIMA = 1000;

    public GerenciadorFilas() {
        this.filaTempoReal = new LinkedList<>();
        this.filaUsuario1 = new LinkedList<>();
        this.filaUsuario2 = new LinkedList<>();
        this.filaUsuario3 = new LinkedList<>();
    }

    /**
     * Retorna o total de processos atualmente aguardando em todas as filas.
     */
    public int getTotalProcessosEnfileirados() {
        return filaTempoReal.size() + filaUsuario1.size() + filaUsuario2.size() + filaUsuario3.size();
    }

    public void enfileirarProcesso(ProcessControlBlock pcb) {
        if (pcb.isConcluido()) {
            return;
        }

        // Aplicação do padrão Fail-Fast para evitar o Paradoxo do Processo Zumbi
        if (getTotalProcessosEnfileirados() >= CAPACIDADE_MAXIMA) {
            throw new IllegalStateException("Falha crítica: Limite arquitetural de " + CAPACIDADE_MAXIMA + 
                                            " processos nas filas atingido. O SO não pode despachar o PID " + pcb.getId() + ".");
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
     * aplica o aging aos processos que ultrapassaram o limite de tolerância.
     */
    public void aplicarAgingGlobal(int limiteStarvation) {
        Queue[] filasUsuario = {filaUsuario1, filaUsuario2, filaUsuario3};

        for (int i = 0; i < filasUsuario.length; i++) {
            // ... (restante do método permanece idêntico)
            Queue<ProcessControlBlock> filaAtiva = filasUsuario[i];
            int tamanhoOriginal = filaAtiva.size();

            for (int j = 0; j < tamanhoOriginal; j++) {
                ProcessControlBlock pcb = filaAtiva.poll();
                if (pcb != null) {
                    pcb.incrementarTempoEsperando();

                    if (i > 0 && pcb.getTempoEsperando() >= limiteStarvation) {
                        pcb.resetarTempoEsperando();
                        pcb.setPrioridadeAtual(pcb.getPrioridadeAtual() - 1); // Promove prioridade [cite: 24]
                        enfileirarProcesso(pcb); 
                    } else {
                        filaAtiva.add(pcb);
                    }
                }
            }
        }
    }
}
