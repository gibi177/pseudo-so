package modulos;

import processos.ProcessControlBlock;

/**
 * Trata a alocação e liberação dos recursos de E/S para os processos.
 */
public class GerenciadorRecursos {

    public GerenciadorRecursos() {
        // Inicialização futura dos semáforos/monitores para impressoras, scanners, etc.
    }

    // Assinatura: Tenta alocar os recursos solicitados pelo processo. 
    // Pode bloquear a thread/processo se o recurso estiver em uso.
    public boolean solicitarRecursos(ProcessControlBlock pcb) {
        return true; // Stub
    }

    // Assinatura: Libera todos os recursos retidos pelo processo.
    public void liberarRecursos(ProcessControlBlock pcb) {
        // Lógica futura de liberação
    }
}
