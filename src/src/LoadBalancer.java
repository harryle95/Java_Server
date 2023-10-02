import annotations.IgnoreCoverage;
import utility.SocketServer;

import java.io.IOException;


public class LoadBalancer extends SocketServer{

    public LoadBalancer(int port) {
        super(port);
    }

    @IgnoreCoverage
    public static void main(String[] args) throws IOException{
        int port = getPort(args);
        SocketServer server = new LoadBalancer(port);
        server.start();
        server.close();
    }

    @Override
    public void start() throws IOException {

    }
}
