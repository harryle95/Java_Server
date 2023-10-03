package utility;

import utility.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public abstract class SocketServer {
    public final int port;
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    protected final LamportClock clock;
    protected ServerSocket serverSocket;
    protected Config config = new Config("src/config/server.properties");
    protected boolean isUp = true;

    public void setStartBreakSignal(boolean startBreakSignal) {
        this.startBreakSignal = startBreakSignal;
    }

    private boolean startBreakSignal = false;

    public SocketServer(int port) {
        this.port = port;
        clock = new LamportClock();

    }

    public static int getPort(String[] args) {
        int port;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 0) {
            port = 4567;
        } else {
            throw new RuntimeException("Usage: AggregationServer [port].");
        }
        return port;
    }

    public Config getConfig() {
        return config;
    }

    public boolean isUp() {
        return isUp;
    }

    public void run() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        logger.info("Server listens to port " + port);
    }

    protected void pre_start_hook() {
    }

    protected void start_hook() {
    }


    protected void pre_close_hook() {
    }

    protected void post_close_hook() {
    }

    public void start() {
        pre_start_hook();
        do {
            start_hook();
        } while (!startBreakSignal);
    }

    public void close() {
        logger.info("Initiating shutdown procedure " + this.getClass().getName());
        pre_close_hook();
        logger.info("Closing Server " + this.getClass().getName());
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.info("ERROR: fails to close server socket: " + e);
        }
        logger.info(this.getClass().getName() + " is closed");
        isUp = false;
        post_close_hook();
    }
}
