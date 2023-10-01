package utility;

import utility.http.HTTPRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocketClient extends SocketCommunicator {
    private int retry = 0;

    public void setMAX_RETRY(int MAX_RETRY) {
        this.MAX_RETRY = MAX_RETRY;
    }

    private int SO_TIMEOUT = 5000;

    private int MAX_RETRY = 5;

    public SocketClient(
            Socket clientSocket,
            PrintWriter out,
            BufferedReader in) throws SocketException {
        super(clientSocket, new LamportClock(), out, in, "client");
        clientSocket.setSoTimeout(SO_TIMEOUT);
    }

    public String receive() throws IOException {
        // GETClient and ContentServer can resend messages up to 5 times
        logger.info("Receiving data from remote.");
        try {
            String message = super.receive();
            retry = 0;
            return message;
        } catch (SocketTimeoutException e) {
            retry += 1;
            logger.info("Retry attempt: " + retry);
            if (retry < MAX_RETRY) {
                send(HTTPRequest.fromMessage(sentMessages.get(sentMessages.size() - 1)));
                return receive();
            } else {
                retry = 0;
                throw new RuntimeException(e);
            }
        }
    }

    protected String hostname;

    protected int port;

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setSO_TIMEOUT(int SO_TIMEOUT) throws SocketException {
        this.SO_TIMEOUT = SO_TIMEOUT;
        clientSocket.setSoTimeout(SO_TIMEOUT);
    }
}