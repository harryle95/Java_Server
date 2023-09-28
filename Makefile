OUTDIR = out/production/Java_Server
TESTOUTDIR = out/test/src
CLASSDIR = src/src
UTILDIR = src/src/utility
TESTDIR  = src/test/
JUNITJAR = junit-platform-console-standalone-1.9.3.jar
MOCKITOJAR = mockito-core-5.5.0.jar
BYTEBUDDYJAR = byte-buddy-1.14.8.jar
BYTEBUDDYAGENTJAR = byte-buddy-agent-1.14.8.jar
MOCKITOJUNIT = mockito-junit-jupiter-5.5.0.jar
OBJENESIS = objenesis-3.3.jar
JARDIR = jar_files
JARFILES = $(JARDIR)/$(JUNITJAR):$(JARDIR)/$(MOCKITOJAR):$(JARDIR)/$(BYTEBUDDYJAR):$(JARDIR)/$(BYTEBUDDYAGENTJAR):$(JARDIR)/$(MOCKITOJUNIT):$(JARDIR)/$(OBJENESIS)



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

agg_server: compile_src
	java -cp $(OUTDIR) AggregationServer 4567

get_client: compile_src
	java -cp $(OUTDIR) GETClient 127.0.0.1:4567 A0

content_server: compile_src
	java -cp $(OUTDIR) ContentServer 127.0.0.1:4567 src/test/utility/json/resources/twoID.txt

.PHONY = clean
clean:
	rm -rf out
	clear