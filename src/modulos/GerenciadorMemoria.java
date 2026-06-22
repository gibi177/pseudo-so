package modulos;

import processos.ProcessControlBlock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class GerenciadorMemoria {

    // Alocação de frames no escopo local (Working Set) associado ao ID do processo
    private final Map<Integer, LinkedList<Integer>> alocacaoPorProcesso;
    
    // Rastreia quais processos já receberam a pré-carga obrigatória de 1 página
    private final Set<Integer> preCargaRealizada;

    // Mapa auxiliar para manter o controle de prioridades isolado neste módulo
    private final Map<Integer, Integer> prioridadePorProcesso;

    // Constantes de Particionamento Físico da RAM (20 frames totais)
    private static final int MAX_FRAMES_TR = 8;
    private static final int MAX_FRAMES_USER = 12;

    public GerenciadorMemoria() {
        this.alocacaoPorProcesso = new HashMap<>();
        this.preCargaRealizada = new HashSet<>();
        this.prioridadePorProcesso = new HashMap<>();
    }

    /**
     * Processa a requisição de uma página, aplicando o algoritmo LRU local
     * e contabilizando os page faults no PCB.
     */
    public void acessarPagina(ProcessControlBlock pcb, int idPagina) {
        int pid = pcb.getId();
        int prioridade = pcb.getPrioridadeBase();
        
        alocacaoPorProcesso.putIfAbsent(pid, new LinkedList<>());
        prioridadePorProcesso.putIfAbsent(pid, prioridade);
        
        LinkedList<Integer> framesLocais = alocacaoPorProcesso.get(pid);

        // Regra da Pré-carga: A primeira página é alocada na partição sem gerar fault.
        // Se a partição estiver cheia neste momento, forçará a quebra temporária do 
        // limite, já que o escopo LRU é estritamente local e a lista do processo está vazia.
        if (!preCargaRealizada.contains(pid)) {
            framesLocais.add(idPagina);
            preCargaRealizada.add(pid);
            return;
        }

        // Lógica de HIT (Página já está na RAM)
        if (framesLocais.contains(idPagina)) {
            framesLocais.remove((Integer) idPagina);
            framesLocais.addLast(idPagina);
            return;
        }

        // Lógica de MISS (Page Fault)
        pcb.registrarPageFault();

        // Estado Derivado: Calcula sob demanda para evitar corrupção de contadores
        boolean isParticaoGlobalCheia = (prioridade == 0 && calcularOcupacaoGlobal(0) >= MAX_FRAMES_TR) ||
                                        (prioridade > 0 && calcularOcupacaoGlobal(1) >= MAX_FRAMES_USER);

        // Verifica limite do Working Set local OU exaustão da partição física global
        if (framesLocais.size() >= pcb.getTamanhoWorkingSet() || isParticaoGlobalCheia) {
            // Evicção LRU local: Remove a página no início da lista
            if (!framesLocais.isEmpty()) {
                framesLocais.removeFirst();
            }
        } 

        framesLocais.addLast(idPagina);
    }

    /**
     * Calcula dinamicamente a quantidade de frames físicos ocupados na partição solicitada.
     * @param tipoPrioridade 0 para Tempo Real, > 0 para Usuário.
     */
    private int calcularOcupacaoGlobal(int tipoPrioridade) {
        int ocupacao = 0;
        for (Map.Entry<Integer, LinkedList<Integer>> entry : alocacaoPorProcesso.entrySet()) {
            int pid = entry.getKey();
            int prio = prioridadePorProcesso.get(pid);
            
            // Agrupa os frames da prioridade 1, 2 e 3 na mesma partição de Usuário
            boolean isProcessoTR = (prio == 0);
            boolean isBuscandoTR = (tipoPrioridade == 0);
            
            if (isProcessoTR == isBuscandoTR) {
                ocupacao += entry.getValue().size();
            }
        }
        return ocupacao;
    }

    /**
     * Libera todos os frames alocados por um processo que foi finalizado.
     * Como usamos estado derivado, não há risco de desincronia de contadores.
     */
    public void liberarMemoria(ProcessControlBlock pcb) {
        int pid = pcb.getId();
        alocacaoPorProcesso.remove(pid);
        preCargaRealizada.remove(pid);
        prioridadePorProcesso.remove(pid);
    }
}
