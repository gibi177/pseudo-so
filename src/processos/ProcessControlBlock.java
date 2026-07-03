package processos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * PCB (Process Control Block).
 * Mantém todas as informações específicas e o estado atual de um processo.
 * Atualizado com cópias defensivas e identidade imutável baseada no ID.
 */
public class ProcessControlBlock {

    // Atributos de Entrada Estáticos (lidos do arquivo processes.txt)
    private final int id; // Deve iniciar em 0 e ir até a quantidade de processos - 1
    private final int tempoInicializacao;
    private final int prioridadeBase;
    private final int tempoCpu;
    private final int tamanhoWorkingSet;
    private final int requisicaoImpressora;
    private final int requisicaoScanner;
    private final int requisicaoModem;
    private final int requisicaoDiscoSata;

    // Atributos de Estado Interno e Controle Dinâmico
    private EstadoProcesso estadoAtual;
    private int prioridadeAtual; // Manipulada pelo escalonador (aging/realimentação)
    private int pageFaults;
    private int tempoExecutado; // Quantidade de ciclos de CPU já consumidos
    private int tempoEsperando; // Rastreia starvation: tempo contínuo aguardando na fila

    // Rastreamento de Referências de Memória (lidos do arquivo string.txt)
    private List<Integer> referenciasMemoria;
    private int programCounterMemoria; // Ponteiro que rastreia qual instrução/página deve ser lida

    /**
     * Construtor para inicializar o PCB com os dados brutos de entrada.
     */
    public ProcessControlBlock(int id, int tempoInicializacao, int prioridadeBase, int tempoCpu,
                               int tamanhoWorkingSet, int requisicaoImpressora, int requisicaoScanner,
                               int requisicaoModem, int requisicaoDiscoSata) {
        
        this.id = id;
        this.tempoInicializacao = tempoInicializacao;
        this.prioridadeBase = prioridadeBase;
        this.tempoCpu = tempoCpu;
        this.tamanhoWorkingSet = tamanhoWorkingSet;
        this.requisicaoImpressora = requisicaoImpressora;
        this.requisicaoScanner = requisicaoScanner;
        this.requisicaoModem = requisicaoModem;
        this.requisicaoDiscoSata = requisicaoDiscoSata;

        // Configuração inicial do simulador conforme modelo de 3 estados.
        this.estadoAtual = EstadoProcesso.PRONTO;
        this.prioridadeAtual = prioridadeBase;
        this.pageFaults = 0;
        this.tempoExecutado = 0;
        
        this.referenciasMemoria = new ArrayList<>();
        this.programCounterMemoria = 0;

        this.tempoEsperando = 0;
    }

    // Getters
    public int getId() { return id; }
    public int getTempoInicializacao() { return tempoInicializacao; }
    public int getPrioridadeBase() { return prioridadeBase; }
    public int getTempoCpu() { return tempoCpu; }
    public int getTamanhoWorkingSet() { return tamanhoWorkingSet; }
    public int getRequisicaoImpressora() { return requisicaoImpressora; }
    public int getRequisicaoScanner() { return requisicaoScanner; }
    public int getRequisicaoModem() { return requisicaoModem; }
    public int getRequisicaoDiscoSata() { return requisicaoDiscoSata; }
    public int getTempoEsperando() { return tempoEsperando; }
    
    public EstadoProcesso getEstadoAtual() { return estadoAtual; }
    public int getPrioridadeAtual() { return prioridadeAtual; }
    public int getPageFaults() { return pageFaults; }
    public int getTempoExecutado() { return tempoExecutado; }
    public int getProgramCounterMemoria() { return programCounterMemoria; }
    public List<Integer> getReferenciasMemoria() { return Collections.unmodifiableList(referenciasMemoria); }

    // Setters e Modificadores Dinâmicos de Estado

    public void setEstadoAtual(EstadoProcesso estadoAtual) {
        this.estadoAtual = estadoAtual;
    }

    public void setPrioridadeAtual(int prioridadeAtual) {
        this.prioridadeAtual = prioridadeAtual;
    }

    /**
     * Define as referências de memória fazendo uma cópia defensiva da lista original.
     */
    public void setReferenciasMemoria(List<Integer> referenciasMemoria) {
        this.referenciasMemoria = referenciasMemoria != null ? new ArrayList<>(referenciasMemoria) : new ArrayList<>();
    }

    public void incrementarTempoEsperando() {
        this.tempoEsperando++;
    }

    public void resetarTempoEsperando() {
        this.tempoEsperando = 0;
    }

    public void incrementarTempoExecutado() {
        this.tempoExecutado++;
    }

    public void registrarPageFault() {
        this.pageFaults++;
    }

    /**
     * Incrementa o ponteiro da próxima página a ser lida pelo processo.
     */
    public void avancarProgramCounterMemoria() {
        this.programCounterMemoria++;
    }

    /**
     * Obtém a página atual da string de referência com base no programCounterMemoria.
     * Retorna Optional.empty() caso o processo não possua mais páginas mapeadas.
     */
    public Optional<Integer> getPaginaAtual() {
        if (programCounterMemoria >= 0 && programCounterMemoria < referenciasMemoria.size()) {
            return Optional.of(referenciasMemoria.get(programCounterMemoria));
        }
        return Optional.empty();
    }

    /**
     * Verifica se o processo concluiu o seu tempo total de processador requerido.
     */
    public boolean isConcluido() {
        return this.tempoExecutado >= this.tempoCpu;
    }
}
