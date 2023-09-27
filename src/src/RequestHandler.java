import utility.LamportClock;
import utility.http.HTTPRequest;
import utility.http.HTTPResponse;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

public class RequestHandler implements Callable<HttpResponse> {
    private final HTTPRequest request;
    private final int priority;

    private final ConcurrentMap<String, String> database;

    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;

    public RequestHandler(
            HTTPRequest request,
            LamportClock clock,
            ConcurrentMap<String, String> database,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive
    ) {
        this.request = request;
        priority = clock.printTimestamp();
        this.database = database;
        this.archive = archive;
    }

    private HTTPResponse handleGET(HTTPRequest request) {
        // Empty GET request
        if (request.uri.equals("/"))
            return new HTTPResponse("1.1")
                    .setStatusCode("204")
                    .setReasonPhrase("No Content")
                    .setHeader("Content-Type", "application/json")
                    .setBody("");
        // Station ID provided
        String stationID = request.uri.substring(1);
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

    private HTTPResponse handlePUT(HTTPRequest request) {
        String body = request.body;
        // TODO: perform correct PUT
        return new HTTPResponse("1.1")
                .setStatusCode("200")
                .setReasonPhrase("OK")
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }


    private HTTPResponse getResponse(HTTPRequest request) {
        HTTPResponse response;
        if (request.method.equals("GET"))
            response = handleGET(request);
        else if (request.method.equals("PUT"))
            response = handlePUT(request);
        else
            response = new HTTPResponse("1.1").setStatusCode("400").setReasonPhrase("Bad Request");
        return response;
    }

    @Override
    public HttpResponse call() throws Exception {
        return null;
    }
}
