import annotations.IgnoreCoverage;
import utility.LamportClock;
import utility.SocketClient;
import utility.SocketCommunicator;
import utility.SocketServer;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class LoadBalancer extends SocketServer {
    private int newPort;

    public AggregationServer getBuiltinServer() {
        return builtinServer;
    }

    private AggregationServer builtinServer;

    private final ScheduledExecutorService heartbeatPool = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService connectionPool = Executors.newCachedThreadPool();

    private final int HEARTBEAT_SCHEDULE = Integer.parseInt(config.get("HEARTBEAT_SCHEDULE", "30000"));

    private final List<ServerInfo> registry = new ArrayList<>();

    public ServerInfo getLeader() {
        return leader;
    }

    private ServerInfo leader;

    public boolean contains(String hostname, int port) {
        return registry.contains(new ServerInfo(hostname, port));
    }

    public void addServer(String hostname, int port) {
        logger.info("Adding new host to registry: " + hostname + ":" + port);
        if (isAlive(hostname, port))
            registry.add(new ServerInfo(hostname, port));
    }

    public synchronized void electLeader() throws IOException {
        logger.info("Electing new leader among connected servers");
        for (ServerInfo info : registry) {
            if (isAlive(info.hostname, info.port)) {
                leader = info;
                logger.info("Success: Selecting: " + info.hostname + ":" + info.port);
                return;
            }
        }
        logger.info("Not connecting to external server, creating a self-managed server.");
        startBuiltInServer();
        setLeader("127.0.0.1", newPort);
    }

    public synchronized void setLeader(String hostname, int port) {
        ServerInfo info = new ServerInfo(hostname, port);
        if (!contains(hostname, port)) {
            addServer(hostname, port);
        }
        leader = info;
    }

    public boolean isAlive(String hostname, int port) {
        try {
            GETClient.main((hostname + ":" + port).split(" "));
            logger.info("Success: server is healthy " + hostname + ":" + port);
            return true;
        } catch (IOException e) {
            logger.info("Failed: unable to get heartbeat from: " + hostname + ":" + port);
            return false;
        }
    }

    public LoadBalancer(int port) throws IOException {
        super(port);
        newPort = port + 1;
        startBuiltInServer();
        setLeader("127.0.0.1", newPort);
        run();
    }

    @IgnoreCoverage
    protected void pre_start_hook() {
        super.pre_start_hook();
        heartbeatPool.scheduleWithFixedDelay(() -> {
            if (!isAlive(leader.hostname, leader.port)) {
                try {
                    electLeader();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, HEARTBEAT_SCHEDULE, HEARTBEAT_SCHEDULE, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void start_hook() throws IOException {
        super.start_hook();
        Socket clientSocket = serverSocket.accept();
        connectionPool.execute(new ClientHandler(
                clientSocket,
                clock,
                new PrintWriter(clientSocket.getOutputStream(), true),
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))));
    }

    @IgnoreCoverage
    public static void main(String[] args) throws IOException {
        int port = getPort(args);
        SocketServer server = new LoadBalancer(port);
        server.start();
        server.close();
    }


    private void startBuiltInServer() {
        try {
            builtinServer = new AggregationServer(newPort);
            new Thread(() -> {
                try {
                    builtinServer.start();
                } catch (IOException e) {
                }
            }).start();
            addServer("127.0.0.1", newPort);
        } catch (IOException | ClassNotFoundException e) {
            newPort += 1;
            startBuiltInServer();
        }
    }

    @IgnoreCoverage
    public static class ServerInfo {
        private final String hostname;

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        private final int port;

        private final int hashCode;

        public ServerInfo(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
            this.hashCode = Objects.hash(this.hostname, this.port);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ServerInfo that = (ServerInfo) o;
            return hostname.equals(that.hostname) && port == that.port;
        }

        public int hashCode() {
            return hashCode;
        }
    }

    public class ClientHandler extends SocketCommunicator implements Runnable {

        SocketClient serverInterface;

        public ClientHandler(
                Socket clientSocket,
                LamportClock clock,
                PrintWriter out,
                BufferedReader in
        ) throws IOException {
            super(clientSocket, clock, out, in, "server");
            serverInterface = GETClient.from_args((leader.hostname + ":" + leader.port).split(" "));

        }


        public void handleRequest(String request) {
            try {
                serverInterface.send(HTTPRequest.fromMessage(request));
                HTTPResponse response = HTTPResponse.fromMessage(serverInterface.receive());
                send(response);
            } catch (IOException e) {
                logger.info("Error: server error.");
                HTTPResponse response = new HTTPResponse("1.1").setStatusCode("500").setReasonPhrase("Internal Server Error");
                send(response);
            }
        }

        @Override
        public void run() {

            while (true) {
                try {
                    String request = receive();
                    if (request == null)
                        break;
                    handleRequest(request);
                } catch (IOException e) {
                    logger.info("ERROR: runtime error: " + e);
                    break;
                }
            }
            try {
                serverInterface.close();
                close();
            } catch (IOException e) {
                logger.info("ERROR: unable to close server socket");
            }
        }
    }

    @Override
    protected void pre_close_hook() {
        super.pre_close_hook();
        logger.info("Closing load balancer connection pool");
        connectionPool.close();
        logger.info("Closing load balancer heartbeat pool");
        heartbeatPool.close();
        try {
            if (builtinServer != null && builtinServer.isUp())
                builtinServer.close();
        } catch (IOException e) {
            logger.info("Error: unable to close builtinServer: " + e);
        }
    }
}
