package handlers;

import utility.FileMetadata;

import java.util.concurrent.ConcurrentMap;

public class RemoveEntryRunnable implements Runnable {
    private final FileMetadata popData;
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive;

    public RemoveEntryRunnable(
            FileMetadata metadata,
            ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String, String>>> archive) {
        this.popData = metadata;
        this.archive = archive;
    }

    @Override
    public void run() {
        String popIP = popData.remoteIP();
        String popFileName = popData.fileName();
        String popTS = popData.timestamp();
        if (archive.containsKey(popIP) && archive.get(popIP).containsKey(popFileName)) {
            String archiveTS = archive.get(popIP).get(popFileName).get("Timestamp");
            // If old data hasn't been updated since -> Remove
            if (popTS.equals(archiveTS)) {
                archive.get(popIP).remove(popFileName);
            }
        }
    }
}
