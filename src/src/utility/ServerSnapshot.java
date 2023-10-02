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

    public File getDatabaseDir() {
        return new File(databaseDir);
    }

    public File getArchiveDir() {
        return new File(archiveDir);
    }

    private final String databaseDir;

    private final String archiveDir;


    public ServerSnapshot(
            String databaseDir,
            String archiveDir
    ) throws IOException, ClassNotFoundException {
        this.databaseDir = databaseDir;
        this.archiveDir = archiveDir;
        logger = Logger.getLogger(this.getClass().getName());
        if (getArchiveDir().exists()) {
            logger.info("Restoring archive from backup");
            FileInputStream archiveInStream = new FileInputStream(archiveDir);
            ObjectInputStream archiveIn = new ObjectInputStream(archiveInStream);
            archive = (ConcurrentMap<String, ConcurrentMap<String,
                    ConcurrentMap<String, String>>>) archiveIn.readObject();
            archiveIn.close();
            archiveInStream.close();
        } else {
            logger.info("Creating a new archive");
            archive = new ConcurrentHashMap<>();
        }

        if (getDatabaseDir().exists()) {
            logger.info("Restoring database from backup");
            FileInputStream dbInStream = new FileInputStream(databaseDir);
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

    public void createSnapShot() {
        createSnapShot(databaseDir, archiveDir);
    }

    private void writeObject(OutputStream outputStream, Serializable obj) throws IOException {
        ObjectOutputStream objOutStream = new ObjectOutputStream(outputStream);
        objOutStream.writeObject(obj);
        objOutStream.close();
    }

    private void createFileSnapShot(String path, Serializable obj) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        writeObject(fileOutputStream, obj);
        fileOutputStream.close();
    }


    public void createSnapShot(String databaseDir, String archiveDir) {
        try {
            logger.info("Creating database snapshot");
            createFileSnapShot(databaseDir, (Serializable) database);
            createFileSnapShot(archiveDir, (Serializable) archive);
        } catch (IOException e) {
            logger.info("Fail to Create Snapshot: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
