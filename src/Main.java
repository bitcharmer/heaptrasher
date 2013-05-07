import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/*
* This is a simple tool to enable some basic observations of GC behavior for single-thread allocation scenarios.
* It is based on simple heuristics that shapes object demography in a way to emulate real-world low latency systems as
* closely as possible.
* The probabilities used have been derived from a series of Monte-Carlo simulations for a single-threaded pricing system,
* but you are encouraged to play around with the values to experience the impact of changes for yourself.
*
* General principle is individual messages may have unilateral references to one another through String (char[])
* retention. Payload size may differ due to stochastic nature of its generation.
*
* Some of the messages are being retained in a central cache data structure to account for the natural process of
* tenuring messages to resemble "long-lived" market orders within a single order book. The cache is periodically
* stripped from a chunk of the oldest messages (as it happens with order books employed by pricing engines)
*
* To learn how to exploit this tool for low latency GC tuning please refer to README.md
* */

public class Main {

    // Change this if the test takes too long or too short on your system
    public static final int MAX_COUNT = 60 * 1000 * 1000;

    // The likelihood of consecutive messages to be referenced by one another
    public static final int REUSE_PREVIOUS_PAYLOAD_PROBABILITY = 30;

    // The likelihood of String retention / additional garbage
    public static final int GROW_PAYLOAD_PROBABILITY = 80;
    public static final int RE_GROW_PAYLOAD_PROBABILITY = 50;

    // Cache management parameters
    private static final int CACHE_CLEANUP_TRESHOLD = 1000;
    private static final int CACHE_CLEANUP_PROBABILITY = 30;
    private static final int CACHE_CLEANUP_CHUNK_SIZE = 10;

    private static final TreeMap<Long, Message> cache = new TreeMap<Long, Message>();
    private static final int REPORTING_INTERVAL = 1000 * 1000;

    private static final Random random = new Random();
    private static final AtomicLong sequence = new AtomicLong(0L);

    public static void main(String[] args) {
        Stats stats;
        if (args != null && args.length > 0 && "array".equalsIgnoreCase(args[0])) {
            stats = new ArrayStats(MAX_COUNT);
        } else {
            stats = new DirectBufferStats(MAX_COUNT);
        }
        final Message first = new Message(sequence.get(), "ROOT_MESSAGE");
        long currentSeq;
        long chunkStart = System.currentTimeMillis();

        while ((currentSeq = sequence.incrementAndGet()) < MAX_COUNT) {
            final long start = System.nanoTime();
            final Message next = nextMessage(first, currentSeq);
            cache.put(next.getSequenceId(), next);
            if (cache.size() > CACHE_CLEANUP_TRESHOLD && draw(CACHE_CLEANUP_PROBABILITY)) {
                for (int i = 0; i < CACHE_CLEANUP_CHUNK_SIZE; i++) {
                    cache.remove(cache.firstKey());
                }
            }
//  Allow for some OS-level thread preemption
//            LockSupport.parkNanos(1);
            stats.addObservation(System.nanoTime() - start);
            if (currentSeq % REPORTING_INTERVAL == 0) {
                System.out.println("Sequence: " + sequence);
                System.out.println("Messages/s: " + (REPORTING_INTERVAL * 1000 / (System.currentTimeMillis() - chunkStart)));
                chunkStart = System.currentTimeMillis();
            }
        }
        stats.printHistogram(6);
    }

    private static Message nextMessage(final Message previous, final long id) {
        final StringBuilder sb = new StringBuilder();
        if (draw(REUSE_PREVIOUS_PAYLOAD_PROBABILITY)) {
            // litter the heap with some loitering char arrays
            sb.append(previous.getPayload().substring(0, 10));
        }
        if (draw(GROW_PAYLOAD_PROBABILITY)) {
            // allocate even more space
            buildPayload(sb, RE_GROW_PAYLOAD_PROBABILITY, 10);
        }
        return new Message(id, sb.toString());
    }

    private static void buildPayload(final StringBuilder sb, final int baseProbability, final int step) {
        sb.append(UUID.randomUUID().toString());
        if (draw(baseProbability)) buildPayload(sb, baseProbability - step, step);
    }

    private static boolean draw(final int probability) {
        return random.nextInt(100) <= probability;
    }

}
