package com.woowacamp.soolsool.core.liquor.repository.redisson;

import com.woowacamp.soolsool.core.liquor.domain.LiquorCtr;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiquorCtrRedisRepository {

    private static final String LIQUOR_CTR_LATEST_UPDATED = "LIQUOR_CTR_LATEST_UPDATED";
    private static final String LIQUOR_CTR_IMPRESSION_PREFIX = "LIQUOR_CTR_IMPRESSION:";
    private static final String LIQUOR_CTR_CLICK_PREFIX = "LIQUOR_CTR_CLICK:";

    private final RedissonClient redissonClient;

    public double getCtr(final Long liquorId) {
        return getLiquorCtr(liquorId).getCtr();
    }

    public void increaseImpression(final Long liquorId) {
        redissonClient
            .getAtomicLong(LIQUOR_CTR_IMPRESSION_PREFIX + liquorId)
            .incrementAndGetAsync();
        addLatestUpdated(liquorId);
    }

    public void increaseClick(final Long liquorId) {
        redissonClient
            .getAtomicLong(LIQUOR_CTR_CLICK_PREFIX + liquorId)
            .incrementAndGet();
        addLatestUpdated(liquorId);
    }

    private void addLatestUpdated(final Long liquorId) {
        redissonClient.getSet(LIQUOR_CTR_LATEST_UPDATED).add(liquorId);
    }

    public List<Long> getAndClearLatestUpdatedLiquorIds() {
        final List<Long> latestUpdatedLiquorIds = getLatestUpdatedLiquorIds();

        clearLatestUpdatedLiquorIds();

        return latestUpdatedLiquorIds;
    }

    private List<Long> getLatestUpdatedLiquorIds() {
        final RSet<Long> liquorIds = redissonClient.getSet(LIQUOR_CTR_LATEST_UPDATED);

        return liquorIds.stream().collect(Collectors.toUnmodifiableList());
    }

    private void clearLatestUpdatedLiquorIds() {
        redissonClient.getSet(LIQUOR_CTR_LATEST_UPDATED).clear();
    }

    public LiquorCtr getLiquorCtr(final Long liquorId) {
        final RAtomicLong impression = redissonClient.getAtomicLong(
            LIQUOR_CTR_IMPRESSION_PREFIX + liquorId);
        final RAtomicLong click = redissonClient.getAtomicLong(LIQUOR_CTR_CLICK_PREFIX + liquorId);

        return new LiquorCtr(liquorId, impression.get(), click.get());
    }
}
