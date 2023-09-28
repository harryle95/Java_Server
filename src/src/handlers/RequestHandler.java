package handlers;

import utility.FileMetadata;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;
import utility.json.Parser;
import utility.json.WeatherData;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestHandler implements Callable<HTTPResponse> {
    private final HTTPRequest request;
    private final int priority;
    private final LinkedBlockingQueue<FileMetadata> updateQueue;

    private final String remoteIP;
    private final ConcurrentMap<String, String> database;

    private final int freshUpdateCount;
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;

    public RequestHandler(
            HTTPRequest request,
            String remoteIP,
            int priority,
            LinkedBlockingQueue<FileMetadata> updateQueue,
            ConcurrentMap<String, String> database,
            int freshUpdateCount,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive
    ) {
        this.request = request;
        this.priority = priority;
        this.updateQueue = updateQueue;
        this.database = database;
        this.freshUpdateCount = freshUpdateCount;
        this.archive = archive;
        this.remoteIP = remoteIP;
    }


    public HTTPResponse handleGET() {
        // Empty GET request
        String stationID = request.getURIEndPoint();
        if (stationID == null)
            return new HTTPResponse("1.1")
                    .setStatusCode("204")
                    .setReasonPhrase("No Content")
                    .setHeader("Content-Type", "application/json")
                    .setBody("");
        // Station ID data is available
        if (database.containsKey(stationID))
            return new HTTPResponse("1.1")
                    .setStatusCode("200")
                    .setReasonPhrase("OK")
                    .setHeader("Content-Type", "application/json")
                    .setBody(database.get(stationID));
        // Station ID data unavailable
        return new HTTPResponse("1.1")
                .setStatusCode("404")
                .setReasonPhrase("Not Found")
                .setHeader("Content-Type", "application/json")
                .setBody("");
    }

    public HTTPResponse handlePUT() throws InterruptedException {
        // Remove updates older than 20 most recent
        removeStalePUTDataFromArchive();

        // Update archive
        addPUTDataToArchive();

        // Update database
        updateStationDatabase();

        // Return response
        return generateHTTPResponseToPUT();
    }

    private void updateStationDatabase() {
        Parser parser = new Parser();
        parser.parseMessage(request.body);
        Map<String, WeatherData> container = parser.getContainer();
        for (Map.Entry<String, WeatherData> weatherEntry : container.entrySet()) {
            String weatherData = weatherEntry.getValue().toString();
            database.put(weatherEntry.getKey(), weatherData);
        }
    }

    private HTTPResponse generateHTTPResponseToPUT() {
        // Response for newly connected host
        if (!archive.containsKey(remoteIP))
            return new HTTPResponse("1.1")
                    .setStatusCode("201")
                    .setReasonPhrase("Created")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Content-Length", request.header.get("Content-Length"))
                    .setBody(request.body);
        return new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setHeader("Content-Length", request.header.get("Content-Length"))
                .setBody(request.body);
    }

    private void removeStalePUTDataFromArchive() throws InterruptedException {
        String fileName = request.getURIEndPoint();
        // Add new metadata to updateQueue
        updateQueue.put(new FileMetadata(remoteIP, fileName, String.valueOf(priority)));
        while (updateQueue.size() > freshUpdateCount) {
            // Remove stale updates from the beginning of the queue
            FileMetadata popData = updateQueue.poll();
            if (popData != null) {
                Runnable removeEntryRunnable = new RemoveEntryRunnable(popData, archive);
                removeEntryRunnable.run();
            }
        }
    }

    private void addPUTDataToArchive() {
        ConcurrentMap<String, ConcurrentMap<String, String>>
                remoteEntry = archive.getOrDefault(remoteIP, new ConcurrentHashMap<>());
        ConcurrentMap<String, String> entry = new ConcurrentHashMap<>();
        entry.put("Value", request.body);
        entry.put("Timestamp", String.valueOf(priority));
        remoteEntry.put(request.getURIEndPoint(), entry);
        archive.put(remoteIP, remoteEntry);
    }


    @Override
    public HTTPResponse call() throws InterruptedException {
        HTTPResponse response;
        if (request.method.equals("GET"))
            response = handleGET();
        else if (request.method.equals("PUT"))
            response = handlePUT();
        else
            response = new HTTPResponse("1.1")
                    .setStatusCode("400")
                    .setReasonPhrase("Bad Request");
        return response;
    }

    public int getPriority() {
        return priority;
    }
}

