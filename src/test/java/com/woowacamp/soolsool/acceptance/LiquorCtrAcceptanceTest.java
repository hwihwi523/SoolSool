package com.woowacamp.soolsool.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.woowacamp.soolsool.acceptance.fixture.RestAuthFixture;
import com.woowacamp.soolsool.acceptance.fixture.RestLiquorCtrFixture;
import com.woowacamp.soolsool.acceptance.fixture.RestLiquorFixture;
import com.woowacamp.soolsool.acceptance.fixture.RestMemberFixture;
import com.woowacamp.soolsool.core.liquor.dto.liquorCtr.LiquorCtrDetailResponse;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("인수 테스트: /liquor-ctr")
class LiquorCtrAcceptanceTest extends AcceptanceTest {

    private static final String LIQUOR_CTR_IMPRESSION_PREFIX = "LIQUOR_CTR_IMPRESSION:";
    private static final String LIQUOR_CTR_CLICK_PREFIX = "LIQUOR_CTR_CLICK:";

    Long 새로;

    @Autowired
    RedissonClient redissonClient;

    @BeforeEach
    void setUpData() {
        RestMemberFixture.회원가입_최민족_판매자();

        String 최민족_토큰 = RestAuthFixture.로그인_최민족_판매자();
        새로 = RestLiquorFixture.술_등록_새로_판매중(최민족_토큰);

        initRedis();
    }

    @AfterEach
    void setOffRedis() {
        initRedis();
    }

    void initRedis() {
        redissonClient.getAtomicLong(LIQUOR_CTR_IMPRESSION_PREFIX + 새로).set(0L);
        redissonClient.getAtomicLong(LIQUOR_CTR_CLICK_PREFIX + 새로).set(0L);
    }

    @Test
    @DisplayName("주류 클릭률을 조회한다.")
    void getLiquorCtr() {
        /* given */
        RestLiquorCtrFixture.술_노출수_증가(List.of(새로));
        RestLiquorCtrFixture.술_노출수_증가(List.of(새로));
        RestLiquorCtrFixture.술_노출수_증가(List.of(새로));
        RestLiquorCtrFixture.술_클릭수_증가(새로);

        /* when */
        LiquorCtrDetailResponse response = RestAssured
            .given().log().all()
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE)
            .param("liquorId", 새로)
            .when().get("/api/liquor-ctr")
            .then().log().all()
            .extract().jsonPath().getObject("data", LiquorCtrDetailResponse.class);

        /* then */
        assertThat(response.getCtr()).isEqualTo(0.33);
    }
}
