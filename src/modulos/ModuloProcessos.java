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
     * Retorna a lista de processos cujo tempo de inicialização coincide com o tempo atual do relógio.
     */
    public List<ProcessControlBlock> getProcessosPorTempo(int tempoAtual) {
        List<ProcessControlBlock> processosChegando = new ArrayList<>();
        for (ProcessControlBlock pcb : processosCarregados) {
            if (pcb.getTempoInicializacao() == tempoAtual) {
                processosChegando.add(pcb);
            }
        }
        return processosChegando;
    }

    /**
     * Verifica se todos os processos carregados já consumiram todo o seu tempo de CPU.
     * Condição de parada para o loop principal do Despachante.
     */
    public boolean isTodosConcluidos() {
        for (ProcessControlBlock pcb : processosCarregados) {
            if (!pcb.isConcluido()) {
                return false;
            }
        }
        return true;
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