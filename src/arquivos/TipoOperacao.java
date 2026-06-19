package arquivos;

/**
 * Representa os tipos de operações suportadas pelo sistema de arquivos.
 */
public enum TipoOperacao {
    CRIAR(0),
    DELETAR(1);

    private final int codigo;

    TipoOperacao(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    /**
     * Converte o código lido do arquivo txt para o respectivo Enum.
     */
    public static TipoOperacao fromCodigo(int codigo) {
        for (TipoOperacao tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Código de operação inválido: " + codigo);
    }
}
