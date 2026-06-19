package util;

import processos.ProcessControlBlock;

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
}