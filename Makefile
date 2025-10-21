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

build: lab2.jar 412alloc

# Compile all .java files at once into proper package folders
classes:
	mkdir -p $(BIN_DIR)
	$(JAVAC) -d $(BIN_DIR) $(SOURCES)

lab2.jar: classes
	$(JAR) cfe lab2.jar $(MAIN) -C $(BIN_DIR) .

412alloc:
	echo '#!/bin/bash' > 412alloc
	echo 'java -jar lab2.jar "$$@"' >> 412alloc
	chmod a+x 412alloc

clean:
	rm -rf $(BIN_DIR) lab2.jar 412fe

#create zy53.tar file under lab2-dist directory
dist:
	mkdir -p ../lab2-dist
	cp Makefile 412alloc README ../lab2-dist/
	cp -r src ../lab2-dist/
	cd ../lab2-dist && tar cvf ../zy53.tar .
	pwd
	mv ../zy53.tar ../l2auto/TarFileGoesHere/