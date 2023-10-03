import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import utility.http.HTTPResponse;
import utility.weatherJson.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public abstract class BaseTest {
    String databaseBackUp = "src/resources/FileSystem/database.backup";
    String archiveBackUp = "src/resources/FileSystem/archive.backup";
    public Logger logger = Logger.getLogger(this.getClass().getName());
    String hostname = "127.0.0.1";
    int port = 4567;
    Map<String, String> fixtureMap;

    List<String> stationID;

    List<String> fileNames;

    ExecutorService threadPool = Executors.newCachedThreadPool();

    @BeforeEach
    void createFixture() throws IOException {
        stationID = new ArrayList<>();
        fixtureMap = new HashMap<>();
        fileNames = new ArrayList<>();
        Parser parser = new Parser();
        String prefixPath = "src/resources/WeatherData/SingleEntry/";
        fileNames.add(prefixPath + "Adelaide_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Adelaide_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Glenelg_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Glenelg_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "HenleyBeach_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "HenleyBeach_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Kilkenny_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Kilkenny_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Melbourne_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Melbourne_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "NorthAdelaide_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "NorthAdelaide_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Parkville_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Parkville_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Pennington_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Pennington_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Seaton_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Seaton_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "Semaphore_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "Semaphore_2023-07-15_16-30-00.txt");
        fileNames.add(prefixPath + "StClair_2023-07-15_16-00-00.txt");
        fileNames.add(prefixPath + "StClair_2023-07-15_16-30-00.txt");

        for (String path : fileNames) {
            parser.parseFile(Paths.get(path));
            fixtureMap.put(path, parser.toString());
            stationID.addAll(new ArrayList<>(parser.getContainer().keySet()));
        }
    }

    HTTPResponse getResponse(String id) throws IOException {
        GETClient client = GETClient.from_args(String.format("%s:%d %s", "127.0.0.1",
                4567, id).split(" "));
        client.run();
        return HTTPResponse.fromMessage(client.receivedMessages.get(0));
    }

    Callable<Object> putRequest(int id) {
        return Executors.callable(() -> {
            try {
                ContentServer.main(String.format("%s:%d %s",
                        hostname, port, fileNames.get(id)).split(" "));
            } catch (IOException e) {
                logger.info("Fails to make put request for file: " + fileNames.get(id));
            }
        });
    }

    abstract void setupHook();

    void setupNotDelete(){
        setupHook();
    }
    @BeforeEach
    void setup() throws IOException {
        Files.deleteIfExists(Paths.get(databaseBackUp));
        Files.deleteIfExists(Paths.get(archiveBackUp));
        setupNotDelete();
    }



    abstract void shutdownHook();

    @AfterEach
    void shutdown() {
        shutdownHook();
    }
}
