import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class GreetServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void run(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server listening at port: " + port);
    }

    public void start() throws IOException {
        try (
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {


            System.out.println("Spawning a new socket: " + clientSocket.getLocalPort());
            System.out.println("Accepting Connection from: " + clientSocket.getRemoteSocketAddress());
            String clientInput, serverInput;
            Scanner scanner = new Scanner(System.in);
            while ((clientInput = in.readLine()) != null) {
                if (!clientInput.isEmpty())
                    System.out.println(">>" + clientInput);
                if ((serverInput = scanner.nextLine()) != null) {
                    out.println(serverInput);
                    out.flush();
                    if (serverInput.equals("exit")) {
                        break;
                    }
                }
                if (clientInput.equals("exit")) {
                    break;
                }
            }
            scanner.close();
        } catch (SocketException e){
            System.out.println("Connection aborted. Client is disconnected");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        int port;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 0) {
            port = 4567;
        } else {
            throw new RuntimeException("Usage: AggregationServer [port].");
        }
        try {
            GreetServer server = new GreetServer();
            server.run(port);
            server.start();
        } catch (BindException e) {
            System.out.println("Port already in use: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}