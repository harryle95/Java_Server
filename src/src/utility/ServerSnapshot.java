package utility;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;


public class ServerSnapshot {
    public Logger logger;
    private final ConcurrentMap<String, String> database;
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<String,
            String>>> archive;

    public ServerSnapshot() throws IOException, ClassNotFoundException {
        logger = Logger.getLogger(this.getClass().getName());
        if (new File("src/backups/archive").exists()) {
            logger.info("Restoring archive from backup");
            FileInputStream archiveInStream = new FileInputStream("src/backups" +
                    "/archive");
            ObjectInputStream archiveIn = new ObjectInputStream(archiveInStream);
            archive = (ConcurrentMap<String, ConcurrentMap<String,
                    ConcurrentMap<String, String>>>) archiveIn.readObject();
            archiveIn.close();
            archiveInStream.close();
        } else {
            logger.info("Creating a new archive");
            archive = new ConcurrentHashMap<>();
        }

        if (new File("src/backups/database").exists()) {
            logger.info("Restoring database from backup");
            FileInputStream dbInStream = new FileInputStream("src/backups/database");
            ObjectInputStream dbIn = new ObjectInputStream(dbInStream);
            database = (ConcurrentMap<String, String>) dbIn.readObject();
            dbIn.close();
            dbInStream.close();
        } else {
            logger.info("Creating a new database");
            database = new ConcurrentHashMap<>();
        }
    }

    public ConcurrentMap<String, String> getDatabase() {
        return database;
    }

    public ConcurrentMap<String,
            ConcurrentMap<String, ConcurrentMap<String, String>>> getArchive() {
        return archive;
    }

    public void createSnapShot(){
        try {
            logger.info("Creating database snapshot");
            FileOutputStream dbOutStream = new FileOutputStream("src/backups/database");
            ObjectOutputStream dbOutObj = new ObjectOutputStream(dbOutStream);
            dbOutObj.writeObject(database);

            dbOutObj.close();
            dbOutStream.close();

            FileOutputStream archiveOutStream = new FileOutputStream("src/backups/archive");
            ObjectOutputStream archiveOutObj = new ObjectOutputStream(archiveOutStream);
            archiveOutObj.writeObject(archive);
            archiveOutObj.close();
            archiveOutStream.close();
        }catch (IOException e){
            logger.info("Fail to Create Snapshot: " + e.getMessage());
        }
    }
}
