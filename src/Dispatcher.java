import modulos.ModuloProcessos;
import modulos.GerenciadorFilas;
import modulos.GerenciadorMemoria;
import modulos.GerenciadorRecursos;
import modulos.GerenciadorArquivos;
import processos.ProcessControlBlock;
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
