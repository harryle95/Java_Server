package utility;

import utility.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public abstract class SocketServer {
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    public final int port;

    protected final LamportClock clock;
    protected ServerSocket serverSocket;

    public Config getConfig() {
        return config;
    }

    protected Config config = new Config("src/config/server.properties");

    public boolean isUp() {
        return isUp;
    }

    protected boolean isUp = true;

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


    public void run() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        logger.info("Server listens to port " + port);
    }

    protected void pre_start_hook() {
    }

    protected void start_hook() throws IOException {
    }


    protected void pre_close_hook() {
    }

    protected void post_close_hook() {
    }

    public void start() throws IOException {
        pre_start_hook();
        while (true) {
            start_hook();
        }
    }

    public void close() throws IOException {
        logger.info("Initiating shutdown procedure " + this.getClass().getName());
        pre_close_hook();
        logger.info("Closing Server " + this.getClass().getName());
        serverSocket.close();
        logger.info(this.getClass().getName() + " is closed");
        isUp = false;
        post_close_hook();
    }
}
