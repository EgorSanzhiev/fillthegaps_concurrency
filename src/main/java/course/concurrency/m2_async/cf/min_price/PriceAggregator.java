package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PriceAggregator {

    private static final int THREAD_COUNT = 100;

    private PriceRetriever priceRetriever = new PriceRetriever();
    private final ExecutorService threadPool = newFixedThreadPool(THREAD_COUNT);

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        return shopIds.stream()
            .map(shopId -> supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), threadPool)
                .orTimeout(2950, MILLISECONDS)
                .exceptionally(this::logAndGetDefaultPrice))
            .reduce((first, second) -> first.thenCombine(second, this::minWithNanLast))
            .orElse(completedFuture(Double.NaN))
            .join();
    }

    private double logAndGetDefaultPrice(Throwable ex) {
        ex.printStackTrace();
        return Double.NaN;
    }

    private Double minWithNanLast(Double first, Double second) {
        if (first.isNaN()) {
            return second;
        }

        if (second.isNaN()) {
            return first;
        }

        return Math.min(first, second);
    }
}
