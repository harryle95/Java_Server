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
CONTENT_FILE ?= Adelaide_2023-07-15_16-30-00.txt
RESOURCE_DIR ?= src/resources/WeatherData/Composite
PORT ?= 4567
CONTENT_ID ?= 5000


make_dirs:
	@mkdir -p $(OUTDIR) $(TESTOUTDIR)
	@mkdir -p src/log

compile_src: make_dirs
	@find $(CLASSDIR) -name "*.java" > sources.txt
	@javac -d $(OUTDIR) -cp $(CLASSDIR) @sources.txt
	@rm sources.txt

compile_test: make_dirs
	@find $(TESTDIR) -name "*.java" > sources.txt
	@javac -d $(TESTOUTDIR) -cp $(JARFILES):$(CLASSDIR) @sources.txt
	@rm sources.txt

run_test: compile_src compile_test
	@java $(LOGGING_FLAG) -javaagent:jar_files/intellij-coverage-agent-1.0.737.jar=src/config/config.args -jar $(JARDIR)/$(JUNITJAR) -cp $(JARFILES):$(TESTOUTDIR):$(OUTDIR) --scan-classpath

build_report:
	javac -d out/report/ -cp out/report/:jar_files/intellij-coverage-reporter-1.0.737.jar:jar_files/freemarker-2.3.31.jar:jar_files/coverage-report-1.0.22.jar:jar_files/intellij-coverage-agent-1.0.737.jar src/report/ReportGenerator.java
	java -cp out/report/:jar_files/intellij-coverage-reporter-1.0.737.jar:jar_files/freemarker-2.3.31.jar:jar_files/coverage-report-1.0.22.jar:jar_files/intellij-coverage-agent-1.0.737.jar ReportGenerator

build_log:
	python3 src/script/log_report.py --source_dir src/log --dest_dir src/log/agg

agg_server: compile_src
	@java $(LOGGING_FLAG) -cp $(OUTDIR) AggregationServer $(PORT)

get_client: compile_src
	@java -cp $(OUTDIR) GETClient 127.0.0.1:$(PORT) $(CONTENT_ID)

content_server: compile_src
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) $(RESOURCE_DIR)/$(CONTENT_FILE)

load_balancer: compile_src
	@java $(LOGGING_FLAG) -cp $(OUTDIR) LoadBalancer $(PORT)

upload_all: compile_src
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/Composite/Adelaide_2023-07-15_16-30-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/Composite/Glenelg_2023-07-15_16-30-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Adelaide_2023-07-15_16-00-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Adelaide_2023-07-15_16-30-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-00-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-30-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Adelaide_2023-07-15_16-00-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Adelaide_2023-07-15_16-30-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-00-00.txt &
	@java -cp $(OUTDIR) ContentServer 127.0.0.1:$(PORT) src/resources/WeatherData/SingleEntry/Glenelg_2023-07-15_16-30-00.txt &

.PHONY = clean
clean:
	rm -rf out
	rm -rf report
	rm -rf src/resources/FileSystem/*.backup
	rm -rf src/log/*
	clear