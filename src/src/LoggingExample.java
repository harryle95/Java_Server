import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class LoggingExample {
    static final Logger logger = Logger.getLogger(LoggingExample.class.getName());

    ExecutorService threadPool;

    public LoggingExample() throws IOException {
        FileHandler handler = new FileHandler("src/log/server.log", 50000, 10, true);
        handler.setFormatter(new XMLFormatter());
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        threadPool = Executors.newCachedThreadPool();
    }

    public void logOnMainThread(){
        logger.info("Logging method on main thread");
        System.out.println("Running method on main thread");
    }

    public void submit(Runnable task){
        threadPool.submit(task);
    }

    public static void main(String[] argv) throws IOException {
        logger.info("Starting main program");
        LoggingExample program = new LoggingExample();
        program.logOnMainThread();
        for (int i = 0; i < 10; i++){
            program.submit(new RunningThread());
        }

    }



}

class RunningThread implements Runnable {
    private static final Logger logger = Logger.getLogger(RunningThread.class.getName());
    @Override
    public void run() {
        long id = Thread.currentThread().threadId();
        logger.info("Logging Inside thread:" + id);
        System.out.println("Running method on thread: " + id);
    }
}