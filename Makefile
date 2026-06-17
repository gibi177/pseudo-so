# /Makefile

# Variáveis
JAVAC = javac
JAVA = java
SRC_DIR = src
OUT_DIR = out
# Força o uso estrito do padrão Java 21 LTS
JFLAGS = -d $(OUT_DIR) --release 21

# Acha qualquer .java dentro de src/ e suas subpastas
SOURCES = $(shell find $(SRC_DIR) -name '*.java')

# Alvo padrão executado quando se digita apenas 'make'
all: compile

# Regra de compilação
compile:
	@echo "Compilando arquivos Java para a versão 21..."
	@mkdir -p $(OUT_DIR)
	$(JAVAC) $(JFLAGS) $(SOURCES)
	@echo "Compilação finalizada com sucesso."

# Limpa os binários gerados
clean:
	@echo "Limpando diretório de saída..."
	rm -rf $(OUT_DIR)
	@echo "Limpeza concluída."

# Executa o programa (exemplo genérico, precisará dos argumentos reais depois)
run: compile
	@echo "Executando o Dispatcher..."
	$(JAVA) -cp $(OUT_DIR) Dispatcher
