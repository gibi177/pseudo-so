package arquivos;

/**
 * Representa uma intenção de operação (create ou delete) lida do files.txt.
 * Classe imutável de domínio.
 */
public class OperacaoArquivo {
    
    private final int idProcesso;
    private final TipoOperacao tipoOperacao; // Substituição do int (0 e 1) pelo Enum
    private final String nomeArquivo;
    private final int tamanhoBlocos; // Será 0 em caso de exclusão

    public OperacaoArquivo(int idProcesso, TipoOperacao tipoOperacao, String nomeArquivo, int tamanhoBlocos) {
        this.idProcesso = idProcesso;
        this.tipoOperacao = tipoOperacao;
        this.nomeArquivo = nomeArquivo;
        this.tamanhoBlocos = tamanhoBlocos;
    }

    public int getIdProcesso() { return idProcesso; }
    public TipoOperacao getTipoOperacao() { return tipoOperacao; }
    public String getNomeArquivo() { return nomeArquivo; }
    public int getTamanhoBlocos() { return tamanhoBlocos; }
}