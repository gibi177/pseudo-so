package modulos;

import processos.ProcessControlBlock;

/**
 * Trata as operações create e delete sobre os arquivos persistidos logicamente no disco.
 */
public class GerenciadorArquivos {

    public GerenciadorArquivos() {
        // Inicialização futura do mapa de ocupação do disco (blocos)
    }

    // Assinatura: Tenta criar um arquivo no modelo de alocação contígua (first-fit)
    public boolean criarArquivo(ProcessControlBlock pcb, String nomeArquivo, int tamanhoBlocos) {
        return false; // Stub
    }

    // Assinatura: Tenta deletar um arquivo validando a permissão do processo
    public boolean deletarArquivo(ProcessControlBlock pcb, String nomeArquivo) {
        return false; // Stub
    }
}