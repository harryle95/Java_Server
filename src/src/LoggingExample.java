import java.io.IOException;
import java.util.logging.*;

public class LoggingExample {
    private static final Logger logger = Logger.getLogger("MainThreadLogger");

    public LoggingExample() throws IOException {
        FileHandler handler = new FileHandler("src/log/server.log", 50000, 10, false);
        handler.setFormatter(new XMLFormatter());
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
    }

    public void logOnMainThread(){
        logger.info("Logging method on main thread");
        System.out.println("Running method on main thread");
    }

    public static void main(String[] argv) throws IOException {
        logger.info("Starting main program");
        LoggingExample program = new LoggingExample();
        program.logOnMainThread();
        new Thread(new RunningThread()).start();
    }

    static class RunningThread implements Runnable {
        @Override
        public void run() {
            logger.info("Logging Inside another thread");
            System.out.println("Running method on thread");
        }
    }

}