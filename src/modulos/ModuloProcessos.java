package modulos;

import processos.ProcessControlBlock;
import java.util.ArrayList;
import java.util.List;

/**
 * Mantém informações específicas do processo e atua como repositório global.
 */
public class ModuloProcessos {
    
    private List<ProcessControlBlock> processosCarregados;

    public ModuloProcessos() {
        this.processosCarregados = new ArrayList<>();
    }

    // Assinatura: Adiciona os processos lidos do arquivo processes.txt
    public void registrarProcesso(ProcessControlBlock pcb) {
        // Lógica futura de armazenamento
    }

    // Assinatura: Retorna processos que devem ser inicializados no tempo atual (tick)
    public List<ProcessControlBlock> getProcessosPorTempo(int tempoAtual) {
        return new ArrayList<>(); // Stub
    }
}