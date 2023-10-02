package utility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public abstract class SocketServer {
    protected final Logger logger;
    private final int port;
    protected ServerSocket serverSocket;


    public boolean isUp() {
        return isUp;
    }

    protected boolean isUp;

    public SocketServer(int port) {
        logger = Logger.getLogger(this.getClass().getName());
        this.port = port;
        this.isUp = true;
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

    public abstract void start() throws IOException;

    public void close() throws IOException {
        logger.info("Closing Server");
        serverSocket.close();
        logger.info("Server is closed");
        isUp = false;
    }
}
