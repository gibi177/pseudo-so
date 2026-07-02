package modulos;

import processos.EstadoProcesso;
import processos.ProcessControlBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Trata a alocação e liberação dos recursos de E/S para os processos.
 *
 * Regras implementadas conforme a especificação:
 * - Recursos disponíveis: 1 scanner, 2 impressoras, 1 modem, 2 dispositivos SATA.
 * - Processos de tempo real não usam recursos de E/S.
 * - A alocação é exclusiva e sem preempção.
 * - Um processo só executa se todos os recursos que precisa estiverem disponíveis.
 */
public class GerenciadorRecursos {

    private static final int TOTAL_SCANNERS = 1;
    private static final int TOTAL_IMPRESSORAS = 2;
    private static final int TOTAL_MODEMS = 1;
    private static final int TOTAL_SATAS = 2;

    private int scannersDisponiveis;
    private int impressorasDisponiveis;
    private int modemsDisponiveis;
    private int satasDisponiveis;

    private final Map<Integer, AlocacaoRecursos> alocacoesPorProcesso;

    public GerenciadorRecursos() {
        this.scannersDisponiveis = TOTAL_SCANNERS;
        this.impressorasDisponiveis = TOTAL_IMPRESSORAS;
        this.modemsDisponiveis = TOTAL_MODEMS;
        this.satasDisponiveis = TOTAL_SATAS;
        this.alocacoesPorProcesso = new HashMap<>();
    }

    public boolean solicitarRecursos(ProcessControlBlock pcb) {
        if (pcb == null) {
            return false;
        }

        // Processos de tempo real não usam recursos de E/S
        if (pcb.getPrioridadeBase() == 0) {
            pcb.setEstadoAtual(EstadoProcesso.EXECUTANDO);
            return true;
        }

        // Se já possui recursos, mantém a posse até o término
        if (alocacoesPorProcesso.containsKey(pcb.getId())) {
            pcb.setEstadoAtual(EstadoProcesso.EXECUTANDO);
            return true;
        }

        int scannersNecessarios = normalizarBinario(
            pcb.getRequisicaoScanner(), "scanner", pcb.getId()
        );
        int impressorasNecessarias = normalizarBinario(
            pcb.getRequisicaoImpressora(), "impressora", pcb.getId()
        );
        int modemsNecessarios = normalizarBinario(
            pcb.getRequisicaoModem(), "modem", pcb.getId()
        );
        int satasNecessarios = normalizarFaixa(
            pcb.getRequisicaoDiscoSata(), 0, TOTAL_SATAS, "disco SATA", pcb.getId()
        );

        if (!haRecursosSuficientes(
                scannersNecessarios,
                impressorasNecessarias,
                modemsNecessarios,
                satasNecessarios)) {
            pcb.setEstadoAtual(EstadoProcesso.BLOQUEADO);
            return false;
        }

        scannersDisponiveis -= scannersNecessarios;
        impressorasDisponiveis -= impressorasNecessarias;
        modemsDisponiveis -= modemsNecessarios;
        satasDisponiveis -= satasNecessarios;

        alocacoesPorProcesso.put(
            pcb.getId(),
            new AlocacaoRecursos(
                scannersNecessarios,
                impressorasNecessarias,
                modemsNecessarios,
                satasNecessarios
            )
        );

        pcb.setEstadoAtual(EstadoProcesso.EXECUTANDO);
        return true;
    }

    public void liberarRecursos(ProcessControlBlock pcb) {
        if (pcb == null || pcb.getPrioridadeBase() == 0) {
            return;
        }

        AlocacaoRecursos alocacao = alocacoesPorProcesso.remove(pcb.getId());
        if (alocacao == null) {
            return;
        }

        scannersDisponiveis += alocacao.scanners;
        impressorasDisponiveis += alocacao.impressoras;
        modemsDisponiveis += alocacao.modems;
        satasDisponiveis += alocacao.satas;

        pcb.setEstadoAtual(EstadoProcesso.PRONTO);
    }

    private boolean haRecursosSuficientes(
            int scanners, int impressoras, int modems, int satas) {
        return scanners <= scannersDisponiveis
            && impressoras <= impressorasDisponiveis
            && modems <= modemsDisponiveis
            && satas <= satasDisponiveis;
    }

    private int normalizarBinario(int valor, String recurso, int pid) {
        return normalizarFaixa(valor, 0, 1, recurso, pid);
    }

    private int normalizarFaixa(
            int valor, int minimo, int maximo, String recurso, int pid) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(
                "PID " + pid + " requisitou quantidade inválida de "
                + recurso + ": " + valor
            );
        }
        return valor;
    }

    private static final class AlocacaoRecursos {
        private final int scanners;
        private final int impressoras;
        private final int modems;
        private final int satas;

        private AlocacaoRecursos(
                int scanners, int impressoras, int modems, int satas) {
            this.scanners = scanners;
            this.impressoras = impressoras;
            this.modems = modems;
            this.satas = satas;
        }
    }
}