import utility.LamportClock;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler extends SocketCommunicator implements Runnable {

    public ConnectionHandler(Socket socket, LamportClock clock) throws IOException {
        super(socket, clock, "server");
    }



    @Override
    public void run() {
        try {
            String message;
            while (true) {
                System.out.println("Before receive: " + clock.printTimestamp());
                message = receive();
                if (message == null)
                    break;
//                System.out.println(message);
                System.out.println("After receive: " + clock.printTimestamp());
                HTTPRequest request = HTTPRequest.fromMessage(message);
                // TODO: submit request to a task queue and get the Future as a CompletionService

                send(getResponse(request));
                System.out.println("After send: " + clock.printTimestamp());

            }
            System.out.println("Socket is closed");
            clientSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
