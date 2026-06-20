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

    public static void main(String[] args) {
        // Validação da quantidade de argumentos passados via terminal
        if (args.length != 3) {
            System.err.println("Erro: Quantidade de argumentos invalida.");
            System.err.println("Uso esperado: ./dispatcher <processes.txt> <files.txt> <string.txt>");
            System.exit(1);
        }

        String caminhoProcessos = args[0];
        String caminhoArquivos = args[1];
        String caminhoStrings = args[2];

        // Validação de existência e leitura dos arquivos
        if (!arquivoValido(caminhoProcessos) || !arquivoValido(caminhoArquivos) || !arquivoValido(caminhoStrings)) {
            System.err.println("Erro: Um ou mais arquivos de entrada nao existem ou nao possuem permissao de leitura.");
            System.exit(1);
        }

        // Instanciação e amarração da arquitetura (Fase 1 finalizada)
        ModuloProcessos processos = new ModuloProcessos();
        GerenciadorFilas filas = new GerenciadorFilas();
        GerenciadorMemoria memoria = new GerenciadorMemoria();
        GerenciadorRecursos recursos = new GerenciadorRecursos();
        GerenciadorArquivos arquivos = new GerenciadorArquivos();

        System.out.println("Pseudo-SO inicializado. Módulos carregados com sucesso.");

        try {
            // Processos e Memória
            List<ProcessControlBlock> pcbsLidos = LeitorEntrada.carregarProcessos(caminhoProcessos, caminhoStrings);
            processos.carregarTodos(pcbsLidos);
            System.out.println("Sucesso: " + processos.getTotalProcessos() + " processos carregados na memória de controle.");

            // Sistema de Arquivos
            LeitorEntrada.carregarSistemaArquivos(caminhoArquivos, arquivos);
            System.out.println("Sucesso: Sistema de arquivos estruturado e fila de disco criada.");

            // ... (setup inicial mantido)
            System.out.println("--------------------------------------------------");
            System.out.println("Iniciando simulação do Pseudo-SO...");

            int tempoAtual = 0;
            ProcessControlBlock processoNaCpu = null;

            final int LIMITE_STARVATION = 10;
            final int QUANTUM_MAX = 1; // Parametrização explícita exigida 
            int tempoNoQuantum = 0;    // Rastreador do tempo de CPU no contexto atual

            System.out.println("--------------------------------------------------");
            System.out.println("Iniciando simulação do Pseudo-SO...");

            // Loop Principal de Execução (Fase 3)
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
                        tempoNoQuantum = 0; // Reset do quantum ao realizar troca de contexto
                        
                        if (processoNaCpu.getTempoExecutado() == 0) {
                            System.out.println("dispatcher =>");
                            System.out.println("PID: " + processoNaCpu.getId());
                            System.out.println("frames: " + processoNaCpu.getTamanhoWorkingSet()); 
                            System.out.println("priority: " + processoNaCpu.getPrioridadeBase());
                            System.out.println("time: " + processoNaCpu.getTempoCpu());
                            System.out.println("printers: " + processoNaCpu.getRequisicaoImpressora());
                            System.out.println("scanners: " + processoNaCpu.getRequisicaoScanner());
                            System.out.println("modems: " + processoNaCpu.getRequisicaoModem());
                            System.out.println("drives: " + processoNaCpu.getRequisicaoDiscoSata());
                            System.out.println("process " + processoNaCpu.getId() + " =>");
                            System.out.println("P" + processoNaCpu.getId() + " STARTED");
                        }
                    }
                }

                // 3. Execução do Processo na CPU
                if (processoNaCpu != null) {
                    processoNaCpu.setEstadoAtual(EstadoProcesso.EXECUTANDO);
                    processoNaCpu.incrementarTempoExecutado();
                    tempoNoQuantum++; // Acumula o uso do milissegundo atual
                    
                    System.out.println("P" + processoNaCpu.getId() + " instruction " + processoNaCpu.getTempoExecutado());

                    // Verificação de Término
                    if (processoNaCpu.isConcluido()) {
                        System.out.println("P" + processoNaCpu.getId() + " return SIGINT");
                        processoNaCpu = null; // CPU ficará livre no próximo tick
                        
                    } else if (processoNaCpu.getPrioridadeBase() > 0) { 
                        // Validação explícita de preempção: apenas Usuário [cite: 18, 26]
                        
                        if (tempoNoQuantum >= QUANTUM_MAX) { // Controle estruturado do Quantum
                            processoNaCpu.setEstadoAtual(EstadoProcesso.PRONTO);
                            
                            // Realimentação: a prioridade decai (o valor numérico sobe) [cite: 20]
                            if (processoNaCpu.getPrioridadeAtual() < 3) {
                                processoNaCpu.setPrioridadeAtual(processoNaCpu.getPrioridadeAtual() + 1);
                            }
                            
                            filas.enfileirarProcesso(processoNaCpu);
                            processoNaCpu = null; // Cede a CPU
                        }
                    }
                    // Se prioridadeBase == 0 (Tempo Real), ele ignora o quantum e roda até acabar.
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
            
        } catch (Exception e) {
            System.err.println("Erro ao processar os arquivos de texto: " + e.getMessage());
            e.printStackTrace(); // Útil para depurar formatação na Fase 2
            System.exit(1);
        }
    }

    /**
     * Verifica se o arquivo existe e pode ser lido.
     */
    private static boolean arquivoValido(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists() && arquivo.canRead();
    }
}
