public interface Stats {

    void addObservation(long value);
    void printHistogram(int maxSigma);
}
