public class Message {

    private final long sequenceId;
    private final String payload;

    public Message(long sequenceId, String payload) {
        this.sequenceId = sequenceId;
        this.payload = payload;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public String getPayload() {
        return payload;
    }
}
