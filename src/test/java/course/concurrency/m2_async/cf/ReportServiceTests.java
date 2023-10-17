package course.concurrency.m2_async.cf;

import course.concurrency.m2_async.cf.report.ReportServiceCF;
import course.concurrency.m2_async.cf.report.ReportServiceExecutors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ReportServiceTests {

//    private ReportServiceVirtual reportService = new ReportServiceVirtual();

//    @ParameterizedTest
//    @ValueSource(ints = {2, 4, 6, 8, 10, 12, 16, 20, 24, 36, 48, 60, 80, 100, 120, 140, 160, 180})
    @Test
    public void testMultipleTasks() throws InterruptedException {
//        ReportServiceExecutors reportService = new ReportServiceExecutors(Executors.newWorkStealingPool(threads));
        ReportServiceCF reportService = new ReportServiceCF(ForkJoinPool.commonPool());
        int poolSize = Runtime.getRuntime().availableProcessors() * 3;
        int iterations = 5;

        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        for (int i = 0; i < poolSize; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
                for (int it = 0; it < iterations; it++) {
                    reportService.getReport();
                }
            });
        }

        long start = System.currentTimeMillis();
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();

//        System.out.println("WorkStealingPool parallelism=%d: %d {%d, %d}".formatted(
//            threads,
//            end - start,
//            threads,
//            end - start));
        System.out.println("CachedThreadPool: %d".formatted(end - start));
    }
}
