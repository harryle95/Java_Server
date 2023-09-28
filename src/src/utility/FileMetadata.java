package utility;

/**
 * Store PUT message metadata.
 * <p>
 * Used for deleting message that are not part of the 20 most recent updates
 *
 * @param remoteIP  IP of the server sending the message
 * @param fileName  Name of the file sent by the server
 * @param timestamp Lamport timestamp of when the message was received
 */
public record FileMetadata(String remoteIP, String fileName, String timestamp) {
}
