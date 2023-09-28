package utility;

/**
 * Lamport Clock object
 */
public class LamportClock {
    private int timestamp;

    public LamportClock() {
        timestamp = 0;
    }

    /**
     * Get current timestamp without advancing clock
     *
     * @return timestamp
     */
    public int getTimeStamp() {
        return timestamp;
    }

    /**
     * Advance the clock and getTimestamp.
     * <p>
     * Used when sending message
     *
     * @return timestamp
     */
    public synchronized int advanceAndGetTimeStamp() {
        timestamp += 1;
        return timestamp;
    }

    /**
     * Advance the receiving timestamp and set to current
     * <p>
     * Used when receiving message
     *
     * @param timestamp timestamp in received message
     */
    public synchronized void advanceAndSetTimeStamp(int timestamp) {
        this.timestamp = Math.max(timestamp, this.timestamp) + 1;
    }
}
