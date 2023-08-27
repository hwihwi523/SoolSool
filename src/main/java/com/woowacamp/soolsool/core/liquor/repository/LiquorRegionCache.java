package com.woowacamp.soolsool.core.liquor.repository;

import com.woowacamp.soolsool.core.liquor.domain.LiquorRegion;
import com.woowacamp.soolsool.core.liquor.domain.vo.LiquorRegionType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiquorRegionCache {

    private final LiquorRegionRepository liquorRegionRepository;

    @Cacheable(value = "liquorRegion", key = "#type")
    public Optional<LiquorRegion> findByType(final LiquorRegionType type) {
        return liquorRegionRepository.findByType(type);
    }
}