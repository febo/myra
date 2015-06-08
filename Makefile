SRC=src/main/java
OUTPUT=target
PACKAGES=\
	myra\
	myra/interval\
	myra/rule\
	myra/rule/function\
	myra/rule/irl\
	myra/rule/pittsburgh\
	myra/rule/pittsburgh/unordered\
	myra/rule/shell\
	myra/rule/shell/command\
	myra/tree\
	myra/util

SRC_DIR=$(addprefix $(SRC)/,$(PACKAGES))
JAVA_FILES=$(foreach sdir,$(SRC_DIR),$(wildcard $(sdir)/*.java))
CLASS_FILES=$(JAVA_FILES:.java=.class)

JAVAC=javac
JAR=jar
JAR_FILE=myra-`grep -m 1 '<version>' pom.xml | cut -d '>' -f 2 | cut -d '<' -f 1`.jar
GIT_FILE=$(OUTPUT)/classes/git.properties

.PHONY: all
all: jar 

.PHONY: clean
clean:
	@echo "--> Removing generated files "
	@rm -rf $(OUTPUT)/classes $(OUTPUT)/test-classes $(OUTPUT)/$(JAR_FILE)
	@echo "[done]"

jar: $(CLASS_FILES) 
	@echo "--> Building jar file"
	@echo "git.commit.id.describe-short=`git describe --always --abbrev --dirty=-DEV`" > $(GIT_FILE)
	@echo "git.commit.id.abbrev=`git rev-parse --short HEAD`" >> $(GIT_FILE)
	@echo "git.commit.id.describe=`git describe --always --dirty=-DEV`" >> $(GIT_FILE)
	@echo "git.commit.id=`git rev-parse HEAD`" >> $(GIT_FILE)
	@$(JAR) cf $(OUTPUT)/$(JAR_FILE) -C $(OUTPUT)/classes/ .
	@echo "[done]"

%.class: %.java
	@mkdir -p $(OUTPUT)/classes
	@echo "Compiling $<"
	@$(JAVAC) -source 7 -target 7 -Xlint:-options -cp $(SRC) -d $(OUTPUT)/classes $<
