import java.nio.ByteBuffer;

public class DirectBufferStats implements Stats {

    private final ByteBuffer buffer;

    public DirectBufferStats(final int size) {
        buffer = ByteBuffer.allocateDirect(8 * size);
    }

    @Override
    public void addObservation(long value) {
        buffer.putLong(value);
    }

    @Override
    public void printHistogram(int maxSigma) {
        final long[] values = HistogramUtil.toLongArray(buffer);
        HistogramUtil.printHistogram(maxSigma, values);
    }

}
