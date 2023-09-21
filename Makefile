OUTDIR = out/production/src
CLASSDIR = src/src

compile_server:
	javac $(CLASSDIR)/GreetServer.java -d $(OUTDIR)

compile_client:
	javac -cp $(CLASSDIR) $(CLASSDIR)/GETClient.java -d $(OUTDIR)

run_server: compile_server
	java -cp $(OUTDIR) GreetServer

run_client: compile_client
	java -cp $(OUTDIR) GETClient

.PHONY = clean
clean:
	clear