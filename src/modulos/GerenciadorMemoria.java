package modulos;

import processos.ProcessControlBlock;

/**
 * Provê uma interface de abstração de memória RAM e gerencia a técnica de paginação.
 */
public class GerenciadorMemoria {

    public GerenciadorMemoria() {
        // Inicialização futura dos 20 frames (8 para Tempo Real, 12 para Usuário)
    }

    // Assinatura: Tenta acessar a página. Se não estiver na RAM, deve gerar page fault.
    public boolean acessarPagina(ProcessControlBlock pcb, int idPagina) {
        return true; // Stub: retorna true se a página está nos frames (hit), false se fault
    }
}
