package com.woowacamp.soolsool.core.liquor.service;

import static com.woowacamp.soolsool.core.liquor.code.LiquorErrorCode.NOT_LIQUOR_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.woowacamp.soolsool.core.liquor.dto.LiquorDetailResponse;
import com.woowacamp.soolsool.core.liquor.dto.LiquorElementResponse;
import com.woowacamp.soolsool.core.liquor.dto.LiquorModifyRequest;
import com.woowacamp.soolsool.core.liquor.dto.LiquorSaveRequest;
import com.woowacamp.soolsool.core.liquor.repository.LiquorBrewCache;
import com.woowacamp.soolsool.core.liquor.repository.LiquorQueryDslRepository;
import com.woowacamp.soolsool.core.liquor.repository.LiquorRegionCache;
import com.woowacamp.soolsool.core.liquor.repository.LiquorStatusCache;
import com.woowacamp.soolsool.global.config.MultipleCacheManagerConfig;
import com.woowacamp.soolsool.global.config.QuerydslConfig;
import com.woowacamp.soolsool.global.exception.SoolSoolException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import({LiquorService.class, LiquorBrewCache.class, LiquorStatusCache.class,
    LiquorRegionCache.class, LiquorQueryDslRepository.class,
    QuerydslConfig.class, MultipleCacheManagerConfig.class})
@DisplayName("통합 테스트: LiquorService")
class LiquorServiceIntegrationTest {

    @Autowired
    LiquorService liquorService;

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql", "/liquor.sql", "/liquor-ctr.sql",
        "/receipt-type.sql", "/receipt.sql",
        "/order-type.sql", "/order.sql"
    })
    @DisplayName("상품 상세 정보를 조회한다.")
    void liquorDetail() {
        /* given */
        Long 새로 = 1L;

        /* when */
        LiquorDetailResponse response = liquorService.liquorDetail(새로);

        /* then */
        assertAll(
            () -> assertThat(response.getId()).isEqualTo(1L),
            () -> assertThat(response.getName()).isEqualTo("새로"),
            () -> assertThat(response.getBrand()).isEqualTo("롯데"),
            () -> assertThat(response.getImageUrl()).isEqualTo("/soju-url"),
            () -> assertThat(response.getAlcohol()).isEqualTo(12.0),
            () -> assertThat(response.getVolume()).isEqualTo(300),
            () -> assertThat(response.getStock()).isEqualTo(100)
        );
    }

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql", "/liquor.sql", "/liquor-ctr.sql",
        "/receipt-type.sql", "/receipt.sql",
        "/order-type.sql", "/order.sql"
    })
    @DisplayName("특정 상품과 함께 많이 구매된 상품 목록을 조회한다.")
    void liquorPurchasedTogether() {
        /* given */
        Long 새로 = 1L;

        /* when */
        List<LiquorElementResponse> response = liquorService.liquorPurchasedTogether(새로);

        /* then */
        assertThat(response).hasSize(1);
    }

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql"
    })
    @DisplayName("liquor를 저장한다.")
    void saveLiquorTest() {
        // given
        LiquorSaveRequest liquorSaveRequest = new LiquorSaveRequest(
            "SOJU", "GYEONGGI_DO", "ON_SALE",
            "새로", "3000", "브랜드", "/url",
            12.0, 300);

        // when & then
        assertThatCode(() -> liquorService.saveLiquor(liquorSaveRequest))
            .doesNotThrowAnyException();
    }

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql", "/liquor.sql", "/liquor-ctr.sql"
    })
    @DisplayName("liquor를 수정한다.")
    void modifyLiquorTest() {
        // given
        LiquorDetailResponse target = liquorService.liquorDetail(1L);
        LiquorModifyRequest liquorModifyRequest = new LiquorModifyRequest(
            "BERRY", "GYEONGGI_DO", "ON_SALE",
            "새로2", "3000", "브랜드", "/url",
            100, 12.0, 300,
            LocalDateTime.now().plusYears(10L)
        );

        // when
        liquorService.modifyLiquor(target.getId(), liquorModifyRequest);

        // then
        LiquorDetailResponse liquor = liquorService.liquorDetail(1L);

        assertThat(liquor.getName()).isEqualTo(liquorModifyRequest.getName());
    }

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql", "/liquor.sql"
    })
    @DisplayName("liquor Id가 존재하지 않을 때, 수정 시 에러를 반환한다.")
    void modifyLiquorTestFailWithNoExistId() {
        // given
        Long liquorId = 999L;
        LiquorModifyRequest liquorModifyRequest = new LiquorModifyRequest(
            "BERRY", "GYEONGGI_DO", "ON_SALE",
            "새로2", "3000", "브랜드", "/url",
            100, 12.0, 300,
            LocalDateTime.now().plusYears(10L)
        );

        // when & then
        assertThatCode(() -> liquorService.modifyLiquor(liquorId, liquorModifyRequest))
            .isInstanceOf(SoolSoolException.class)
            .hasMessage(NOT_LIQUOR_FOUND.getMessage());
    }

    @Test
    @Sql({
        "/member-type.sql", "/member.sql",
        "/liquor-type.sql", "/liquor.sql"
    })
    @DisplayName("liquor를 삭제한다.")
    void deleteLiquorTest() {
        // given

        // when
        liquorService.deleteLiquor(1L);

        // then
        assertThatCode(() -> liquorService.liquorDetail(1L))
            .isExactlyInstanceOf(SoolSoolException.class)
            .hasMessage("술이 존재하지 않습니다.");
    }
}
