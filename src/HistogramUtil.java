import java.nio.ByteBuffer;
import java.util.Arrays;

public class HistogramUtil {

    public static final int SIZEOF_LONG = 8;
    private static final String LATENCY_MESSAGE = "% of message latency is less than:";


    public static void printHistogram(final int maxSigma, final long[] values) {
        final int length = values.length;
        Arrays.sort(values);
        System.out.println("Percentile histogram:\n---------------------------");
        printPercentile(50d, values[length / 2]);
        printPercentile(75d, values[length * 3 / 4]);
        double percentile = .9d;
        for (int i = 0; i < maxSigma; i++) {
            if (i > 0) percentile += (.9d / Math.pow(10d, i));
            int idx = (int) (length * percentile);
            printPercentile(percentile * 100, values[idx]);
        }
    }

    public static void printPercentile(final double percentile, final long value) {
        System.out.format("%.4f%s %d nanos%n", percentile, LATENCY_MESSAGE, value);
    }

    public static long[] toLongArray(final ByteBuffer buffer) {
        final long[] result = new long[buffer.capacity() / SIZEOF_LONG];
        for (int i = 0; i < buffer.capacity(); i += SIZEOF_LONG) {
            result[i / SIZEOF_LONG] = buffer.getLong(i);
        }
        return result;
    }


}
