OUTDIR = out/production/Java_Server
TESTOUTDIR = out/test
CLASSDIR = src/src
UTILDIR = src/src/utility
TESTDIR  = src/test
JUNITJAR = jar_files
JARFILE = junit-platform-console-standalone-1.9.3.jar

compile_utility:
	find $(UTILDIR) -name "*.java" > sources.txt
	javac -d $(OUTDIR) -cp $(CLASSDIR) @sources.txt
	rm sources.txt

compile_test:
	find $(TESTDIR) -name "*.java" > sources.txt
	javac -d $(TESTOUTDIR) -cp $(JUNITJAR)/*:$(CLASSDIR) @sources.txt
	rm sources.txt

run_test: compile_utility compile_test
	java -jar $(JUNITJAR)/$(JARFILE) -cp $(TESTOUTDIR) -cp $(OUTDIR) --scan-classpath


.PHONY = clean
clean:
	clear