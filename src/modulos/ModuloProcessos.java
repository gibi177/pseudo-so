package modulos;

import processos.ProcessControlBlock;
import java.util.ArrayList;
import java.util.List;

/**
 * Mantém informações específicas do processo e atua como repositório global.
 */
public class ModuloProcessos {
    
    private List<ProcessControlBlock> processosCarregados;
    private int processosFinalizados = 0;

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

    public void registrarProcessoConcluido() {
        this.processosFinalizados++;
    }

    public boolean isTodosConcluidos() {
        return this.processosFinalizados == processosCarregados.size();
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
