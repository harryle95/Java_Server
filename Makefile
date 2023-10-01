OUTDIR = out/production/Java_Server
TESTOUTDIR = out/test/src
CLASSDIR = src/src
UTILDIR = src/src/utility
TESTDIR  = src/test/
JUNITJAR = junit-platform-console-standalone-1.9.3.jar
JARDIR = jar_files
JARFILES = $(JARDIR)/$(JUNITJAR)
SHELL := /usr/bin/bash
LOGGING_FLAG = -Djava.util.logging.config.file=src/config/logging.properties

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
	java -javaagent:jar_files/intellij-coverage-agent-1.0.737.jar=src/config/config.args -jar $(JARDIR)/$(JUNITJAR) -cp $(JARFILES):$(TESTOUTDIR):$(OUTDIR) --scan-classpath

build_report:
	javac -d out/report/ -cp out/report/:jar_files/intellij-coverage-reporter-1.0.737.jar:jar_files/freemarker-2.3.31.jar:jar_files/coverage-report-1.0.22.jar:jar_files/intellij-coverage-agent-1.0.737.jar src/report/ReportGenerator.java
	java -cp out/report/:jar_files/intellij-coverage-reporter-1.0.737.jar:jar_files/freemarker-2.3.31.jar:jar_files/coverage-report-1.0.22.jar:jar_files/intellij-coverage-agent-1.0.737.jar ReportGenerator

build_log:
	python3.10 src/script/log_report.py --source_dir src/log --dest_dir src/log/agg

agg_server: compile_src
	java $(LOGGING_FLAG) -cp $(OUTDIR) AggregationServer 4567

get_client: compile_src
	java $(LOGGING_FLAG)  -cp $(OUTDIR) GETClient 127.0.0.1:4567 A0

content_server: compile_src
	java $(LOGGING_FLAG) -cp $(OUTDIR) ContentServer 127.0.0.1:4567 src/test/utility/json/resources/twoID.txt

.PHONY = clean
clean:
	rm -rf out
	rm -rf report
	rm -rf src/backups/*
	rm -rf src/log/*
	clear