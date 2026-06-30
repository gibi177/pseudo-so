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

  ```
   for (int i = indiceInicial; i < indiceInicial + tamanho; i++) {
       if (!"0".equals(disco[i])) {
           throw new IllegalStateException("Bloco " + i + " já está ocupado por " + disco[i]);
       }
       disco[i] = nomeArquivo;
   }

   metadadosAutoria.put(nomeArquivo, idAutor);
  ```

  }

  public void carregarFilaOperacoes(List<OperacaoArquivo> operacoes) {
  if (operacoes != null) {
  this.filaOperacoes = new ArrayList<>(operacoes);
  }
  }

  public List<OperacaoArquivo> getFilaOperacoes() {
  return Collections.unmodifiableList(filaOperacoes);
  }

  // ======================================================
  // CREATE (FIRST-FIT CONTÍGUO)
  // ======================================================
  public boolean criarArquivo(ProcessControlBlock pcb, String nomeArquivo, int tamanhoBlocos) {

  ```
   if (disco == null || tamanhoBlocos <= 0) return false;

   // Arquivo já existe
   if (metadadosAutoria.containsKey(nomeArquivo)) {
       return false;
   }

   int inicio = buscarFirstFit(tamanhoBlocos);

   // Não encontrou espaço contíguo suficiente
   if (inicio == -1) {
       return false;
   }

   // Alocar no disco
   for (int i = inicio; i < inicio + tamanhoBlocos; i++) {
       disco[i] = nomeArquivo;
   }

   // Registrar autoria
   metadadosAutoria.put(nomeArquivo, pcb.getId());

   return true;
  ```

  }

  // ======================================================
  // DELETE
  // ======================================================
  public boolean deletarArquivo(ProcessControlBlock pcb, String nomeArquivo) {

  ```
   if (disco == null) return false;

   // Arquivo não existe
   if (!metadadosAutoria.containsKey(nomeArquivo)) {
       return false;
   }

   int autor = metadadosAutoria.get(nomeArquivo);

   boolean ehTempoReal = pcb.getPrioridade() == 0;

   // Regra de permissão
   if (!ehTempoReal && autor != pcb.getId()) {
       return false;
   }

   // Remover do disco
   for (int i = 0; i < disco.length; i++) {
       if (nomeArquivo.equals(disco[i])) {
           disco[i] = "0";
       }
   }

   // Remover metadado
   metadadosAutoria.remove(nomeArquivo);

   return true;
  ```

  }

  // ======================================================
  // FIRST-FIT CONTÍGUO (SAFE)
  // ======================================================
  private int buscarFirstFit(int tamanho) {

  ```
   int contador = 0;

   for (int i = 0; i < disco.length; i++) {

       if ("0".equals(disco[i])) {
           contador++;
       } else {
           contador = 0;
       }

       if (contador == tamanho) {
           return i - tamanho + 1;
       }
   }

   return -1;
  ```

  }

  // ======================================================
  // DEBUG / SAÍDA FINAL
  // ======================================================
  public void imprimirMapaDisco() {
  System.out.println("Mapa de ocupação do disco:");

  ```
   for (String bloco : disco) {
       System.out.print(bloco + " ");
   }
   System.out.println();
  ```
  }
  }
