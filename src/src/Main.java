import java.io.IOException;
import java.util.Scanner;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        int port;
        GreetClient client = new GreetClient();
        client.startConnection("127.0.0.1", 4567);
        Scanner scanner = new Scanner(System.in);
        client.sendMessage("Hello");
        String input, output;
        while ((output = client.getResponse()) != null) {
            if (!output.isEmpty())
                System.out.println(">>"+output);
            if ((input = scanner.nextLine()) != null) {
                client.sendMessage(input);
                if (input.equals("exit"))
                    break;
            }
        }


        client.stopConnection();
    }
}