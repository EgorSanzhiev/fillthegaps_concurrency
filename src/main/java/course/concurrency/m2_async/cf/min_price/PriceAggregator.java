package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;

import static java.lang.Math.max;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        final var threadPool = newFixedThreadPool(max(shopIds.size(), 100));
        final var minPrice = shopIds.stream()
            .map(shopId -> supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), threadPool)
                .orTimeout(2950, MILLISECONDS)
                .exceptionally(this::logAndGetDefaultPrice))
            .reduce((first, second) -> first.thenCombine(second, Math::min))
            .orElse(completedFuture(Double.MAX_VALUE))
            .join();

        return minPrice == Double.MAX_VALUE ? Double.NaN : minPrice;
    }

    private double logAndGetDefaultPrice(Throwable ex) {
        ex.printStackTrace();
        return Double.MAX_VALUE;
    }
}
