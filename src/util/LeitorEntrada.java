package util;

import processos.ProcessControlBlock;
import modulos.GerenciadorArquivos;
import arquivos.OperacaoArquivo;
import arquivos.TipoOperacao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário responsável por fazer o parsing dos arquivos .txt de entrada 
 * e transformá-los em objetos de domínio do pseudo-SO.
 */
public class LeitorEntrada {

    /**
     * Lê os arquivos de processos e strings de memória lado a lado,
     * instanciando a lista de PCBs validada.
     */
    public static List<ProcessControlBlock> carregarProcessos(String caminhoProcessos, String caminhoStrings) throws IOException {
        List<ProcessControlBlock> pcbs = new ArrayList<>();

        // Lê todas as linhas. O filtro de linhas vazias foi removido para garantir 
        // a sincronia estrita de 1:1 entre os índices dos dois arquivos.
        List<String> linhasProcessos = Files.readAllLines(Path.of(caminhoProcessos));
        List<String> linhasStrings = Files.readAllLines(Path.of(caminhoStrings));

        // Validação de segurança: Limite máximo de 1000 processos
        if (linhasProcessos.size() >= 1000) {
            throw new IllegalStateException("Limite de 1000 processos excedido");
        }

        if (linhasProcessos.size() != linhasStrings.size()) {
            throw new IllegalStateException("Erro: O número de linhas lidas não corresponde entre os arquivos de processos e strings de referência.");
        }

        for (int i = 0; i < linhasProcessos.size(); i++) {
            String linhaProc = linhasProcessos.get(i).trim();
            
            // Ignora linhas que estejam simultaneamente em branco em ambos os arquivos (ex: quebra de linha no final do arquivo)
            if (linhaProc.isEmpty() && linhasStrings.get(i).trim().isEmpty()) {
                continue;
            }

            String[] dados = linhaProc.split(",");
            
            if (dados.length < 8) { 
                throw new IllegalArgumentException("Linha " + i + " mal formatada em processes.txt"); 
            }
            
            int id = i; // O ID deve sempre iniciar em 0
            
            int tempoInicializacao = Integer.parseInt(dados[0].trim());
            int prioridade = Integer.parseInt(dados[1].trim());
            int tempoCpu = Integer.parseInt(dados[2].trim());
            int tamanhoWorkingSet = Integer.parseInt(dados[3].trim());
            int reqImpressora = Integer.parseInt(dados[4].trim());
            int reqScanner = Integer.parseInt(dados[5].trim());
            int reqModem = Integer.parseInt(dados[6].trim());
            int reqSata = Integer.parseInt(dados[7].trim());

            // Sanitização de Domínio: Processos de tempo real não possuem I/O
            if (prioridade == 0) {
                reqImpressora = 0;
                reqScanner = 0;
                reqModem = 0;
                reqSata = 0;
            }

            ProcessControlBlock pcb = new ProcessControlBlock(
                id, tempoInicializacao, prioridade, tempoCpu, tamanhoWorkingSet,
                reqImpressora, reqScanner, reqModem, reqSata
            );

            // Mapeamento seguro: blinda o split contra strings vazias
            String linhaString = linhasStrings.get(i).trim();
            List<Integer> refsMemoria = new ArrayList<>();
            
            if (!linhaString.isEmpty()) {
                String[] paginas = linhaString.split(",");
                for (String pag : paginas) {
                    try {
                        refsMemoria.add(Integer.parseInt(pag.trim()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Erro ao converter referência de memória na linha " + i + ": '" + pag + "'", e);
                    }
                }
            }
            
            pcb.setReferenciasMemoria(refsMemoria);
            pcbs.add(pcb);
        }

        return pcbs;
    }

    /**
     * Lê o arquivo files.txt, extrai a ocupação inicial do disco e as operações pendentes.
     */
    public static void carregarSistemaArquivos(String caminhoArquivos, GerenciadorArquivos gerenciador) throws IOException {
        List<String> linhas = Files.readAllLines(Path.of(caminhoArquivos));
        
        if (linhas.isEmpty()) {
            throw new IllegalArgumentException("O arquivo files.txt está vazio.");
        }

        // Linha 1: Tamanho do disco
        int totalBlocos = Integer.parseInt(linhas.get(0).trim());
        gerenciador.inicializarDisco(totalBlocos);

        // Linha 2: Quantidade de segmentos (arquivos pré-existentes)
        int qtdSegmentos = Integer.parseInt(linhas.get(1).trim());

        // Linhas 3 até (qtdSegmentos + 2): Estado inicial do disco
        int linhaAtual = 2;
        for (int i = 0; i < qtdSegmentos; i++) {
            String[] dadosSegmento = linhas.get(linhaAtual).split(",");
            String nome = dadosSegmento[0].trim();
            int blocoInicial = Integer.parseInt(dadosSegmento[1].trim());
            int tamanho = Integer.parseInt(dadosSegmento[2].trim());
            
            // Usando a constante explícita em vez do Magic Number -1
            gerenciador.registrarBlocoOcupadoInicial(nome, blocoInicial, tamanho, GerenciadorArquivos.AUTORIA_SISTEMA);
            linhaAtual++;
        }

        // A partir da linha (qtdSegmentos + 2): Operações
        List<OperacaoArquivo> operacoes = new ArrayList<>();
        while (linhaAtual < linhas.size()) {
            String linhaStr = linhas.get(linhaAtual).trim();
            if (linhaStr.isEmpty()) {
                linhaAtual++;
                continue;
            }

            String[] dadosOperacao = linhaStr.split(",");
            int idProcesso = Integer.parseInt(dadosOperacao[0].trim());
            int codigoOp = Integer.parseInt(dadosOperacao[1].trim());
            
            // Traduz o inteiro 0 ou 1 para a Entidade de Domínio
            TipoOperacao tipoOperacao = TipoOperacao.fromCodigo(codigoOp);
            
            String nomeArq = dadosOperacao[2].trim();
            int tamBlocos = 0;
            
            // Fail-fast para tamanho ausente na operação de criar
            if (tipoOperacao == TipoOperacao.CRIAR) {
                if (dadosOperacao.length < 4) {
                    throw new IllegalArgumentException("Erro na linha " + (linhaAtual + 1) + 
                        ": Operacao de criacao sem tamanho definido para o arquivo " + nomeArq);
                }
                tamBlocos = Integer.parseInt(dadosOperacao[3].trim());
            }

            operacoes.add(new OperacaoArquivo(idProcesso, tipoOperacao, nomeArq, tamBlocos));
            linhaAtual++;
        }

        gerenciador.carregarFilaOperacoes(operacoes);
    }
}