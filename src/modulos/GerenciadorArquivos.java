package modulos;

import arquivos.OperacaoArquivo;
import processos.ProcessControlBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trata as operações create e delete sobre os arquivos persistidos logicamente no disco.
 */
public class GerenciadorArquivos {

    public static final int AUTORIA_SISTEMA = -1;

    private String[] disco;
    private Map<String, Integer> metadadosAutoria;
    private List<OperacaoArquivo> filaOperacoes;

    public GerenciadorArquivos() {
        this.filaOperacoes = new ArrayList<>();
        this.metadadosAutoria = new HashMap<>();
    }

    public void inicializarDisco(int quantidadeBlocos) {
        this.disco = new String[quantidadeBlocos];
        Arrays.fill(this.disco, "0");
    }

    public void registrarBlocoOcupadoInicial(String nomeArquivo, int indiceInicial, int tamanho, int idAutor) {
        if (indiceInicial + tamanho > disco.length) {
            throw new IllegalArgumentException("Erro Crítico: Arquivo pré-existente '" + nomeArquivo + 
                "' tenta ocupar blocos além do tamanho total do disco.");
        }

        for (int i = indiceInicial; i < indiceInicial + tamanho; i++) {
            if (!"0".equals(disco[i])) {
                throw new IllegalStateException("Bloco " + i + " já está ocupado por " + disco[i]);
            }
            disco[i] = nomeArquivo;
        }
        
        metadadosAutoria.put(nomeArquivo, idAutor);
    }

    public void carregarFilaOperacoes(List<OperacaoArquivo> operacoes) {
        if (operacoes != null) {
            this.filaOperacoes = new ArrayList<>(operacoes);
        }
    }

    /**
     * Retorna uma cópia imutável da fila para o Despachante orquestrar.
     */
    public List<OperacaoArquivo> getFilaOperacoes() {
        return Collections.unmodifiableList(filaOperacoes);
    }

    // --- Stubs para Implementação Futura (Fase 6) ---

    public boolean criarArquivo(ProcessControlBlock pcb, String nomeArquivo, int tamanhoBlocos) {
        // Lógica futura de first-fit no array 'disco'
        // metadadosAutoria.put(nomeArquivo, pcb.getId());
        return false; // Stub
    }

    public boolean deletarArquivo(ProcessControlBlock pcb, String nomeArquivo) {
        // Lógica futura de validação de permissão e limpeza do array 'disco' com "0"
        
        // CORREÇÃO: Prevenção rigorosa do Metadata Leak
        // Isso garante que, se a exclusão física ocorrer, a exclusão lógica acompanha.
        metadadosAutoria.remove(nomeArquivo); 
        
        return false; // Stub
    }
}