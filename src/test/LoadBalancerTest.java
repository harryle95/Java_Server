import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utility.http.HTTPResponse;
import utility.weatherJson.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoadBalancerTest {
    LoadBalancer loadBalancer;


    HTTPResponse getClientReceivedResponse(String hostname, int port, String id) throws IOException {
        GETClient client = GETClient.from_args(String.format("%s:%d %s", hostname, port, id).split(" "));
        client.run();
        return HTTPResponse.fromMessage(client.receivedMessages.get(0));
    }


    void setupHook() throws IOException {
        loadBalancer = new LoadBalancer(4567);
        new Thread(() -> {
            try {
                loadBalancer.start();
            } catch (IOException e) {
            }
        }).start();
    }

    @BeforeEach
    void setup() throws IOException {
        setupHook();
        loadBalancer.getBuiltinServer().setWAIT_TIME(1);
    }

    @AfterEach
    void shutdown() {
        try {
            loadBalancer.close();
        } catch (IOException e) {
        }
    }
}

class LoadBalancerWithFixtureTest extends LoadBalancerTest {
    Map<String, String> fixtureMap;

    List<String> fileNames;

    @BeforeEach
    void createFixture() throws IOException {
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
            parser.parseFile(Path.of(path));
            fixtureMap.put(path, parser.toString());
        }
    }

    void runContentServer(String hostname, int port, int fileIndex) throws IOException {
        ContentServer.main((hostname + ":" + port + " " + fileNames.get(fileIndex)).split(" "));
    }
}

class LoadBalancerGETPUTTest extends LoadBalancerWithFixtureTest {
    @Test
    void testOneGETOnePUTGivesSameResult() throws IOException {
        runContentServer("127.0.0.1", 4567, 0);
        HTTPResponse response = getClientReceivedResponse("127.0.0.1", 4567, "5000");
        assertEquals(fixtureMap.get(fileNames.get(0)), response.body);
    }

    @Test
    void testInterLeaveGETPUT() throws IOException {
        runContentServer("127.0.0.1", 4567, 0);
        HTTPResponse response = getClientReceivedResponse("127.0.0.1", 4567, "5000");
        assertEquals(fixtureMap.get(fileNames.get(0)), response.body);
        runContentServer("127.0.0.1", 4567, 1);
        response = getClientReceivedResponse("127.0.0.1", 4567, "5000");
        assertEquals(fixtureMap.get(fileNames.get(1)), response.body);
        runContentServer("127.0.0.1", 4567, 2);
        response = getClientReceivedResponse("127.0.0.1", 4567, "5000");
        assertEquals(fixtureMap.get(fileNames.get(1)), response.body);
    }

    @Test
    void testArchiveClearedAfterTimeOut() throws IOException, InterruptedException {
        loadBalancer.getBuiltinServer().setWAIT_TIME(1);
        for (int i = 0; i < fileNames.size(); i++) {
            runContentServer("127.0.0.1", 4567, i);
        }
        Thread.sleep(50);
        assertTrue(loadBalancer.getBuiltinServer().getArchive().get("/127.0.0.1").isEmpty());
    }

    @Test
    void testStaleArchiveRemoved() throws IOException {
        loadBalancer.getBuiltinServer().setWAIT_TIME(300);
        for (int i = 0; i < fileNames.size(); i++) {
            runContentServer("127.0.0.1", 4567, i);
        }
        assertFalse(loadBalancer.getBuiltinServer().getArchive().get("/127.0.0.1").containsKey(fileNames.get(0)));
        assertFalse(loadBalancer.getBuiltinServer().getArchive().get("/127.0.0.1").isEmpty());
    }

    @Test
    void testBackUpGenerated() throws IOException {
        File archive = new File(loadBalancer.getConfig().get("archiveDir"));
        File database = new File(loadBalancer.getConfig().get("databaseDir"));
        runContentServer("127.0.0.1", 4567, 0);
        loadBalancer.getBuiltinServer().getServerSnapshot().createSnapShot();
        // Check exists
        assertTrue(archive.exists());
        assertTrue(database.exists());
        // Remove created files
        archive.delete();
        database.delete();
    }
}

class LoadBalancerFailOverTest extends LoadBalancerWithFixtureTest {
    @Test
    void testWhenBuiltInServerDiesIsAliveIsFalse() throws IOException, InterruptedException {
        loadBalancer.getBuiltinServer().close();
        assertEquals(4568, loadBalancer.getLeader().getPort());
        Thread.sleep(100);
        assertFalse(loadBalancer.isAlive("127.0.0.1", 4568));
    }

}