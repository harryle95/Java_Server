import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        int port = 6666;
        GreetClient client = new GreetClient();
        client.startConnection("127.0.0.1",6666);
        client.sendMessage("hello server\n");
        client.stopConnection();
    }
}