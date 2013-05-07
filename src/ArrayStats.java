import java.util.concurrent.atomic.AtomicInteger;

public class ArrayStats implements Stats {

    private final long[] values;
    private AtomicInteger index = new AtomicInteger(0);

    public ArrayStats(final int size) {
        values = new long[size];
    }

    @Override
    public void addObservation(final long value) {
        values[index.getAndIncrement()] = value;
    }

    @Override
    public void printHistogram(int maxSigma) {
        HistogramUtil.printHistogram(maxSigma, values);
    }

}
