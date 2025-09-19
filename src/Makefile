# Compiler
JAVAC = javac
JAVA = java
JAR = jar

# Project files
SOURCES = $(wildcard *.java)
CLASSES = $(SOURCES:.java=.class)
MAIN = Main

# Targets
all: build

build: lab1.jar 412fe

lab1.jar: $(CLASSES)
	$(JAR) cfe lab1.jar $(MAIN) $(CLASSES)

%.class: %.java
	$(JAVAC) $<

412fe:
	echo '#!/bin/bash' > 412fe
	echo 'java -jar lab1.jar "$$@"' >> 412fe
	chmod a+x 412fe

clean:
	rm -f *.class lab1.jar 412fe