This simple tool allows for observing the impact of GC configuration changes on GC latency for single-threaded scenarios.
It is a part of presentation which took place on 23rd April 2013 at Warsaw JUG. The whole lecture is available on WJUG's Youtube channel: http://www.youtube.com/watch?v=0DAZcaOZEag.
You can also find some slides in ./doc subdirectory.
The point of this code is solely to demonstrate how tweaking some GC parameters yields better latency for young collections.

Best way to observe the influence is either:
1) run the tests with "-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
or:
2) Make VisualVM attach to Java process and resort to GCViewer and Visual GC plugins
You can find the GCViewer plugin sources at https://github.com/bitcharmer/gcperf and the binary in ./plugins subdirectory.

The individual test runs are described below:

1) Java 6, Array-backed statistics (heavy heap usage)
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:+UseParallelGC Main array

2) Java 7, Array-backed statistics (heavy heap usage)
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:+UseParallelGC Main array

3) Java 7, DirectBuffer statistics (off-heap storage), JVM sizes generations automatically
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:+UseParallelGC Main direct

4) Java 7, DirectBuffer statistics (off-heap storage), Manual generation sizing
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:MaxNewSize=3g -XX:NewSize=3g -XX:+UseParallelGC Main direct

5) Java 7, DirectBuffer statistics (off-heap storage), Manual generation sizing, using GC Task affinity, binding GC threads to CPU cores, overriding default GC threads number
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:MaxNewSize=3g -XX:NewSize=3g -XX:ParallelGCThreads=7 -XX:+UseGCTaskAffinity -XX:+BindGCTaskThreadsToCPUs Main direct

6) same as 5) but with ParGCCardsPerStrideChunk increased
-XX:+UnlockDiagnosticVMOptions -Xmx4g -Xms4g -XX:MaxNewSize=3g -XX:NewSize=3g -XX:ParallelGCThreads=7 -XX:+UseGCTaskAffinity -XX:+BindGCTaskThreadsToCPUs -XX:ParGCCardsPerStrideChunk=32768 Main direct

*** IMPORTANT NOTE ***
ParallelGCThreads should be set to at most the number of CPUs available in your system - 1 (so 7 for an 8-core environment).

*** IMPORTANT NOTE ***
Please bear in mind this test code does not cover the warm-up phase so you'll need to give your VM a few moments before you start measuring anything.