package modulos;

import processos.ProcessControlBlock;

/**
 * Mantém as interfaces e funções que operam sobre os processos, 
 * controlando as políticas de escalonamento (FIFO e Feedback).
 */
public class GerenciadorFilas {

    public GerenciadorFilas() {
        // Inicialização futura das estruturas de fila (Tempo Real e Múltiplas Filas)
    }

    // Assinatura: Enfileira um processo recém-chegado ou que sofreu preempção
    public void enfileirarProcesso(ProcessControlBlock pcb) {
        // Lógica futura de roteamento para a fila correta com base na prioridade
    }

    // Assinatura: Retorna o próximo processo a ganhar a CPU
    public ProcessControlBlock buscarProximoProcesso() {
        return null; // Stub
    }
}