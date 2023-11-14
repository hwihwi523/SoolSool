package com.woowacamp.soolsool.core.liquor.repository.redisson;

import com.woowacamp.soolsool.core.liquor.domain.LiquorCtr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiquorCtrRedisRepository {

    private static final String LIQUOR_CTR_IMPRESSION_PREFIX = "LIQUOR_CTR_IMPRESSION:";
    private static final String LIQUOR_CTR_CLICK_PREFIX = "LIQUOR_CTR_CLICK:";

    private final RedissonClient redissonClient;

    public double getCtr(final Long liquorId) {
        final RAtomicLong impression = redissonClient.getAtomicLong(
            LIQUOR_CTR_IMPRESSION_PREFIX + liquorId);
        final RAtomicLong click = redissonClient.getAtomicLong(LIQUOR_CTR_CLICK_PREFIX + liquorId);

        return new LiquorCtr(liquorId, impression.get(), click.get()).getCtr();
    }

    public void increaseImpression(final Long liquorId) {
        redissonClient
            .getAtomicLong(LIQUOR_CTR_IMPRESSION_PREFIX + liquorId)
            .incrementAndGetAsync();
    }

    public void increaseClick(final Long liquorId) {
        redissonClient
            .getAtomicLong(LIQUOR_CTR_CLICK_PREFIX + liquorId)
            .incrementAndGet();
    }
}
