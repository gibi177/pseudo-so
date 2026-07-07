import arquivos.OperacaoArquivo;
import arquivos.TipoOperacao;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import modulos.*;
import processos.*;
import util.LeitorEntrada;

/**
 * Classe principal responsável por despachar e inicializar o pseudo-SO.
 */
public class Dispatcher {

    private final ModuloProcessos processos;
    private final GerenciadorFilas filas;
    private final GerenciadorMemoria memoria;
    private final GerenciadorRecursos recursos;
    private final GerenciadorArquivos arquivos;

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
            System.err.println("Uso: ./dispatcher processes.txt files.txt string.txt");
            System.exit(1);
        }

        if (!arquivoValido(args[0]) || !arquivoValido(args[1]) || !arquivoValido(args[2])) {
            System.err.println("Erro: Arquivo inválido.");
            System.exit(1);
        }

        Dispatcher d = new Dispatcher();
        d.carregarDados(args[0], args[1], args[2]);
        d.executarSimulacao();
    }

    private void carregarDados(String p, String f, String s) {
        try {
            List<ProcessControlBlock> pcbs = LeitorEntrada.carregarProcessos(p, s);
            processos.carregarTodos(pcbs);

            LeitorEntrada.carregarSistemaArquivos(f, arquivos);

        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            System.exit(1);
        }
    }

    // ======================================================
    // LOOP PRINCIPAL
    // ======================================================
    private void executarSimulacao() {
        int tempo = 0;
        ProcessControlBlock cpu = null;
        int tempoQuantum = 0;

        while (!processos.isTodosConcluidos()) {
            // 1. chegada de processos
            for (ProcessControlBlock pcb : processos.getProcessosPorTempo(tempo)) {
                filas.enfileirarProcesso(pcb);
            }
            // 2. escalonamento
            if (cpu == null) {
                // Cria uma lista temporária para salvar quem não conseguiu I/O
                List<ProcessControlBlock> bloqueadosNesteCiclo = new ArrayList<>();
                
                ProcessControlBlock candidato = filas.buscarProximoProcesso();
                
                while (candidato != null) {
                    if (recursos.solicitarRecursos(candidato)) {
                        cpu = candidato;
                        cpu.resetarTempoEsperando();
                        tempoQuantum = 0;

                        if (cpu.getTempoExecutado() == 0) {
                            imprimirCabecalhoProcesso(cpu);
                        }
                        break; // Achou um processo apto, sai do loop
                    } else {
                        // Não conseguiu recurso. Guarda para devolver à fila depois!
                        bloqueadosNesteCiclo.add(candidato);
                    }
                    
                    candidato = filas.buscarProximoProcesso();
                }
                
                // Devolve todos os processos bloqueados para as filas
                for (ProcessControlBlock bloqueado : bloqueadosNesteCiclo) {
                    filas.enfileirarProcesso(bloqueado);
                }
            }

            // 3. execução
            if (cpu != null) {
                cpu.incrementarTempoExecutado();
                tempoQuantum++;

                System.out.println("P" + cpu.getId() + " instruction " + cpu.getTempoExecutado());
                executarMemoria(cpu);

                // término
                if (cpu.isConcluido()) {
                    System.out.println("P" + cpu.getId() + " return SIGINT");
                    memoria.liberarMemoria(cpu);
                    recursos.liberarRecursos(cpu);
                    processos.registrarProcessoConcluido();
                    cpu = null;

                } else if (cpu.getPrioridadeBase() > 0 && tempoQuantum >= QUANTUM_MAX) {
                    cpu.setPrioridadeAtual(Math.min(3, cpu.getPrioridadeAtual() + 1));
                    filas.enfileirarProcesso(cpu);
                    cpu = null;
                }
            }

            filas.aplicarAgingGlobal(LIMITE_STARVATION);
            tempo++;

            if (tempo > 50000) {
                break;
            }
        }

        executarSistemaArquivos();
        imprimirRelatorioFinal();
    }

    // ======================================================
    // MEMÓRIA
    // ======================================================
    private void executarMemoria(ProcessControlBlock pcb) {
        List<Integer> refs = pcb.getReferenciasMemoria();

        if (refs.isEmpty()) {
            return;
        }

        int paginasPorTick = (int) Math.ceil((double) refs.size() / pcb.getTempoCpu());
        int inicio = pcb.getProgramCounterMemoria();
        int fim = Math.min(inicio + paginasPorTick, refs.size());

        for (int i = inicio; i < fim; i++) {
            memoria.acessarPagina(pcb, refs.get(i)); 
            pcb.avancarProgramCounterMemoria();
        }
    }

    // ======================================================
    // SISTEMA DE ARQUIVOS
    // ======================================================
    private void executarSistemaArquivos() {
        System.out.println("\nSistema de arquivos =>\n");
        List<OperacaoArquivo> ops = arquivos.getFilaOperacoes();
        int contador = 1;

        for (OperacaoArquivo op : ops) {
            System.out.print("Operação " + contador + " => ");
            ProcessControlBlock pcb = processos.getProcessoPorId(op.getIdProcesso());

            if (pcb == null) {
                System.out.println("Falha");
                System.out.println("O processo " + op.getIdProcesso() + " não existe.\n");
                contador++;
                continue;
            }

            boolean sucesso;

            if (op.getTipoOperacao() == TipoOperacao.CRIAR) {
                GerenciadorArquivos.ResultadoOperacaoArquivo resultado = arquivos.criarArquivo(pcb, op.getNomeArquivo(), op.getTamanhoBlocos());

                switch (resultado) {
                    case SUCESSO:
                        System.out.println("Sucesso");
                        System.out.println("O processo " + pcb.getId() + 
                                        " criou o arquivo " + op.getNomeArquivo() + ".\n");
                        break;

                    case SEM_ESPACO:
                        System.out.println("Falha");
                        System.out.println("O processo " + pcb.getId() + 
                                        " não pode criar o arquivo " + op.getNomeArquivo() + 
                                        " (falta de espaço).\n");
                        break;

                    case JA_EXISTE:
                        System.out.println("Falha");
                        System.out.println("O processo " + pcb.getId() + 
                                        " não pode criar o arquivo " + op.getNomeArquivo() + 
                                        " (arquivo já existe).\n");
                        break;

                    case PROCESSO_INVALIDO:
                        System.out.println("Falha");
                        System.out.println("O processo " + pcb.getId() + 
                                        " não pode criar o arquivo " + op.getNomeArquivo() + 
                                        " (processo inválido).\n");
                        break;
                }
            } else if (op.getTipoOperacao() == TipoOperacao.DELETAR) {
                sucesso = arquivos.deletarArquivo(pcb, op.getNomeArquivo());
                if (sucesso) {
                    System.out.println("Sucesso");
                    System.out.println(
                        "O processo " + pcb.getId()
                        + " deletou o arquivo " + op.getNomeArquivo() + ".\n"
                    );
                } else {
                    System.out.println("Falha");
                    System.out.println(
                        "O processo " + pcb.getId()
                        + " não pode deletar o arquivo " + op.getNomeArquivo() + ".\n"
                    );
                }
            }
            contador++;
        }
        arquivos.imprimirMapaDisco();
    }

    // ======================================================
    // RELATÓRIO FINAL
    // ======================================================
    private void imprimirRelatorioFinal() {
        System.out.println("\nNúmero de Faltas de Páginas por processo:");

        for (int i = 0; i < processos.getTotalProcessos(); i++) {
            ProcessControlBlock p = processos.getProcessoPorId(i);
            if (p != null) {
                System.out.println("P" + p.getId() + " = " + p.getPageFaults() + " faltas de páginas");
            }
        }
    }

    private void imprimirCabecalhoProcesso(ProcessControlBlock pcb) {
        System.out.println("\ndispatcher =>");

        System.out.println("\tPID: " + pcb.getId());
        System.out.println("\tframes: " + pcb.getTamanhoWorkingSet());
        System.out.println("\tpriority: " + pcb.getPrioridadeBase());
        System.out.println("\ttime: " + pcb.getTempoCpu());

        System.out.println("\tprinters: " + (pcb.getRequisicaoImpressora() > 0 ? "1" : "0"));
        System.out.println("\tscanners: " + (pcb.getRequisicaoScanner() > 0 ? "1" : "0"));
        System.out.println("\tmodems: " + (pcb.getRequisicaoModem() > 0 ? "1" : "0"));
        System.out.println("\tdrives: " + (pcb.getRequisicaoDiscoSata() > 0 ? "1" : "0") + "\n");

        System.out.println("process " + pcb.getId() + " =>");
        System.out.println("P" + pcb.getId() + " STARTED");
    }

    private static boolean arquivoValido(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists() && arquivo.canRead();
    }

    private String formatarBlocos(int inicio, int tamanho) {
        if (tamanho == 1) return "bloco " + inicio;
        StringBuilder sb = new StringBuilder("blocos ");
        for (int i = 0; i < tamanho; i++) {
            sb.append(inicio + i);
            if (i < tamanho - 2) sb.append(", ");
            else if (i == tamanho - 2) sb.append(" e ");
        }
        return sb.toString();
    }
}
