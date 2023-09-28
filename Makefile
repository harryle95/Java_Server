OUTDIR = out/production/src
TESTOUTDIR = out/test/src
CLASSDIR = src/src
UTILDIR = src/src/utility
TESTDIR  = src/test/
JUNITJAR = junit-platform-console-standalone-1.9.3.jar
MOCKITOJAR = mockito-core-5.5.0.jar
BYTEBUDDYJAR = byte-buddy-1.14.8.jar
BYTEBUDDYAGENTJAR = byte-buddy-agent-1.14.8.jar
MOCKITOJUNIT = mockito-junit-jupiter-5.5.0.jar
JARDIR = jar_files
JARFILES = $(JARDIR)/$(JUNITJAR):$(JARDIR)/$(MOCKITOJAR):$(JARDIR)/$(BYTEBUDDYJAR):$(JARDIR)/$(BYTEBUDDYAGENTJAR):$(JARDIR)/$(MOCKITOJUNIT)


make_dirs:
	mkdir -p $(OUTDIR) $(TESTOUTDIR)

compile_src:
	find $(CLASSDIR) -name "*.java" > sources.txt
	javac -d $(OUTDIR) -cp $(CLASSDIR) @sources.txt
	rm sources.txt

compile_test:
	find $(TESTDIR) -name "*.java" > sources.txt
	javac -d $(TESTOUTDIR) -cp $(JARFILES):$(CLASSDIR) @sources.txt
	rm sources.txt

run_test: compile_src compile_test
	java -jar $(JARDIR)/$(JUNITJAR) -cp $(JARFILES):$(TESTOUTDIR):$(OUTDIR) --scan-classpath


.PHONY = clean
clean:
	rm -rf out
	clear