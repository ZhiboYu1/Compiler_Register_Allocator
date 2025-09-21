# Compiler
JAVAC = javac
JAVA = java
JAR = jar

SRC_DIR = src
BIN_DIR = bin
# Find all Java sources recursively
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Main class
MAIN = Main

# Targets
all: build

build: lab1.jar 412fe

# Compile all .java files at once into proper package folders
classes:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)

lab1.jar: classes
	$(JAR) cfe lab1.jar $(MAIN) -C $(BIN_DIR)

412fe:
	echo '#!/bin/bash' > 412fe
	echo 'java -jar lab1.jar "$$@"' >> 412fe
	chmod a+x 412fe

clean:
	rm -rf $(BIN_DIR) lab1.jar 412fe
