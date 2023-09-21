OUTDIR = out/production/src
CLASSDIR = src/src

compile_server:
	javac $(CLASSDIR)/GreetServer.java -d $(OUTDIR)

compile_client:
	javac $(CLASSDIR)/GreetClient.java -d $(OUTDIR)
	javac $(CLASSDIR)/Main.java $(CLASSDIR)/GreetClient.java -d $(OUTDIR)

run_server: compile_server
	java -cp $(OUTDIR) GreetServer

run_client: compile_client
	java -cp $(OUTDIR) Main


.PHONY = clean
clean:
	clear