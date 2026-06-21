import modulos.ModuloProcessos;
import modulos.GerenciadorFilas;
import modulos.GerenciadorMemoria;
import modulos.GerenciadorRecursos;
import modulos.GerenciadorArquivos;
import processos.ProcessControlBlock;
import processos.EstadoProcesso;
import util.LeitorEntrada;

import java.io.File;
import java.util.List;

/**
 * Classe principal responsável por despachar e inicializar o pseudo-SO.
 * Ponto de entrada da aplicação.
 */
public class Dispatcher {

    // Gerenciadores encapsulados como propriedades de instância
    private final ModuloProcessos processos;
    private final GerenciadorFilas filas;
    private final GerenciadorMemoria memoria;
    private final GerenciadorRecursos recursos;
    private final GerenciadorArquivos arquivos;

    // Constantes de Escalonamento
    private static final int LIMITE_STARVATION = 10;
    private static final int QUANTUM_MAX = 1;

    public Dispatcher() {
        this.processos = new ModuloProcessos();
        this.filas = new GerenciadorFilas();
        this.memoria = new GerenciadorMemoria();
        this.recursos = new GerenciadorRecursos();
        this.arquivos = new GerenciadorArquivos();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Erro: Quantidade de argumentos invalida.");
            System.err.println("Uso esperado: ./dispatcher <processes.txt> <files.txt> <string.txt>");
            System.exit(1);
        }

        if (!arquivoValido(args[0]) || !arquivoValido(args[1]) || !arquivoValido(args[2])) {
            System.err.println("Erro: Um ou mais arquivos de entrada nao existem ou nao possuem permissao de leitura.");
            System.exit(1);
        }

        Dispatcher despachante = new Dispatcher();
        despachante.carregarDados(args[0], args[1], args[2]);
        despachante.executarSimulacao();
    }

    private void carregarDados(String caminhoProcessos, String caminhoArquivos, String caminhoStrings) {
        try {
            System.out.println("Pseudo-SO inicializado. Módulos carregados com sucesso.");
            
            List<ProcessControlBlock> pcbsLidos = LeitorEntrada.carregarProcessos(caminhoProcessos, caminhoStrings);
            processos.carregarTodos(pcbsLidos);
            System.out.println("Sucesso: " + processos.getTotalProcessos() + " processos carregados na memória de controle.");

            LeitorEntrada.carregarSistemaArquivos(caminhoArquivos, arquivos);
            System.out.println("Sucesso: Sistema de arquivos estruturado e fila de disco criada.");
            
        } catch (Exception e) {
            System.err.println("Erro ao processar os arquivos de texto: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Motor principal do Pseudo-SO. Controla o ciclo de CPU, preempção e impressão de saídas.
     */
    private void executarSimulacao() {
        System.out.println("--------------------------------------------------");
        System.out.println("Iniciando simulação do Pseudo-SO...");

        int tempoAtual = 0;
        ProcessControlBlock processoNaCpu = null;
        int tempoNoQuantum = 0; 

        while (!processos.isTodosConcluidos()) {
            
            // 1. Acorda processos e enfileira
            List<ProcessControlBlock> recemChegados = processos.getProcessosPorTempo(tempoAtual);
            for (ProcessControlBlock pcb : recemChegados) {
                filas.enfileirarProcesso(pcb);
            }

            // 2. Escalonamento e Atribuição de CPU
            if (processoNaCpu == null) {
                processoNaCpu = filas.buscarProximoProcesso();
                
                if (processoNaCpu != null) {
                    processoNaCpu.resetarTempoEsperando();
                    tempoNoQuantum = 0; 
                    
                    if (processoNaCpu.getTempoExecutado() == 0) {
                        imprimirCabecalhoProcesso(processoNaCpu); // Extraído para manter o código limpo
                    }
                }
            }

            // 3. Execução do Processo na CPU
                if (processoNaCpu != null) {
                    processoNaCpu.setEstadoAtual(EstadoProcesso.EXECUTANDO);
                    processoNaCpu.incrementarTempoExecutado();
                    tempoNoQuantum++; 
                    
                    System.out.println("P" + processoNaCpu.getId() + " instruction " + processoNaCpu.getTempoExecutado());

                    // Verificação de Término
                    if (processoNaCpu.isConcluido()) {
                        System.out.println("P" + processoNaCpu.getId() + " return SIGINT");
                        
                        // CORREÇÃO: Notifica o módulo global para atualizar o contador de término
                        processos.registrarProcessoConcluido(); 
                        
                        processoNaCpu = null; 
                        
                    } else if (processoNaCpu.getPrioridadeBase() > 0) { 
                        // Preempção apenas para processos de Usuário [cite: 26]
                        if (tempoNoQuantum >= QUANTUM_MAX) { 
                            processoNaCpu.setEstadoAtual(EstadoProcesso.PRONTO);
                            
                            if (processoNaCpu.getPrioridadeAtual() < 3) {
                                processoNaCpu.setPrioridadeAtual(processoNaCpu.getPrioridadeAtual() + 1); // Realimentação [cite: 20]
                            }
                            
                            filas.enfileirarProcesso(processoNaCpu);
                            processoNaCpu = null; 
                        }
                    }
                }

            // 4. Mecanismo de Prevenção de Starvation
            filas.aplicarAgingGlobal(LIMITE_STARVATION);

            tempoAtual++;
            
            if (tempoAtual > 50000) { 
                System.err.println("Timeout de segurança atingido. Possivel deadlock.");
                break;
            }
        }

        System.out.println("Simulação concluída no tempo: " + tempoAtual);
    }

    private void imprimirCabecalhoProcesso(ProcessControlBlock pcb) {
        System.out.println("dispatcher =>");
        System.out.println("PID: " + pcb.getId());
        System.out.println("frames: " + pcb.getTamanhoWorkingSet()); 
        System.out.println("priority: " + pcb.getPrioridadeBase());
        System.out.println("time: " + pcb.getTempoCpu());
        System.out.println("printers: " + pcb.getRequisicaoImpressora());
        System.out.println("scanners: " + pcb.getRequisicaoScanner());
        System.out.println("modems: " + pcb.getRequisicaoModem());
        System.out.println("drives: " + pcb.getRequisicaoDiscoSata());
        System.out.println("process " + pcb.getId() + " =>");
        System.out.println("P" + pcb.getId() + " STARTED");
    }

    private static boolean arquivoValido(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists() && arquivo.canRead();
    }
}
