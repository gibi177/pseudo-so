package modulos;

import processos.ProcessControlBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mantém informações específicas do processo e atua como repositório global.
 */
public class ModuloProcessos {
    
    private List<ProcessControlBlock> processosCarregados;

    public ModuloProcessos() {
        this.processosCarregados = new ArrayList<>();
    }

    /**
     * Recebe a lista integral de processos lidos do disco e os armazena.
     * Utiliza cópia defensiva para evitar vazamento de referência.
     */
    public void carregarTodos(List<ProcessControlBlock> pcbs) {
        if (pcbs != null) {
            this.processosCarregados = new ArrayList<>(pcbs);
        }
    }

    /**
     * Retorna apenas os processos cujo tempo de inicialização coincide com o relógio (tick) atual.
     */
    public List<ProcessControlBlock> getProcessosPorTempo(int tempoAtual) {
        return processosCarregados.stream()
                .filter(p -> p.getTempoInicializacao() == tempoAtual)
                .collect(Collectors.toList());
    }

    /**
     * Busca um processo carregado pelo seu ID.
     * Retorna null se o processo não existir no domínio, permitindo validação segura.
     */
    public ProcessControlBlock getProcessoPorId(int idProcurado) {
        for (ProcessControlBlock pcb : processosCarregados) {
            if (pcb.getId() == idProcurado) {
                return pcb;
            }
        }
        return null; // Retorno null é esperado e tratado pelo Despachante
    }

    /**
     * Retorna a quantidade total de processos lidos.
     */
    public int getTotalProcessos() {
        return processosCarregados.size();
    }
}