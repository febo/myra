SRC=src/main/java
OUTPUT=target
PACKAGES=\
	myra\
	myra/interval\
	myra/rule\
	myra/rule/function\
	myra/rule/irl\
	myra/rule/pittsburgh\
	myra/rule/shell\
	myra/rule/shell/command\
	myra/url

SRC_DIR=$(addprefix $(SRC)/,$(PACKAGES))
JAVA_FILES=$(foreach sdir,$(SRC_DIR),$(wildcard $(sdir)/*.java))
CLASS_FILES=$(JAVA_FILES:.java=.class)

JAVAC=javac
JAR=jar
JAR_FILE=myra.jar

.PHONY: all
all: jar 

.PHONY: clean
clean:
	@echo "--> Removing generated files "
	@rm -rf $(OUTPUT)/classes $(JAR_FILE)
	@echo "[done]"

jar: $(CLASS_FILES) 
	@echo "--> Building jar file"
	$(JAR) cf $(OUTPUT)/$(JAR_FILE) -C $(OUTPUT)/classes/ .
	@echo "[done]"

%.class: %.java
	@mkdir -p $(OUTPUT)/classes
	@echo "Compiling $<"
	@$(JAVAC) -source 7 -target 7 -Xlint:-options -cp $(SRC) -d $(OUTPUT)/classes $<
