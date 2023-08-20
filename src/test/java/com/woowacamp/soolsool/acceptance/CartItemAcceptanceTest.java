package com.woowacamp.soolsool.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.woowacamp.soolsool.core.cart.dto.request.CartItemSaveRequest;
import com.woowacamp.soolsool.core.liquor.dto.LiquorDetailResponse;
import com.woowacamp.soolsool.core.liquor.dto.LiquorSaveRequest;
import com.woowacamp.soolsool.global.auth.dto.LoginRequest;
import com.woowacamp.soolsool.global.auth.dto.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("장바구니 : 인수 테스트")
class CartItemAcceptanceTest extends AcceptanceTest {

    private static final String BEARER_LITERAL = "Bearer ";

    @Test
    @DisplayName("성공 : 장바구니에 술 추가")
    void createMember() {
        // given
        String customerAccessToken = getCustomerAccessToken();
        String vendorAccessToken = getVendorAccessToken();

        LiquorSaveRequest liquorSaveRequest = new LiquorSaveRequest(
            "SOJU", "GYEONGSANGNAM_DO", "ON_SALE",
            "안동소주", "12000", "안동", "/soju.jpeg",
            120, 31.3, 300
        );
        String location = RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, vendorAccessToken)
            .body(liquorSaveRequest)
            .when().post("/liquors")
            .then().log().all()
            .extract().header("Location");
        Long liquorId = RestAssured
            .given().log().all()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get(location)
            .then().log().all()
            .extract().jsonPath().getObject("data", LiquorDetailResponse.class).getId();

        // when
        CartItemSaveRequest cartItemSaveRequest = new CartItemSaveRequest(liquorId, 1);
        ExtractableResponse<Response> response = RestAssured
            .given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, customerAccessToken)
            .body(cartItemSaveRequest)
            .when().post("/cart-items")
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private String getCustomerAccessToken() {
        LoginRequest loginRequest = new LoginRequest("woowafriends@naver.com",
            "woowa");

        return BEARER_LITERAL + RestAssured
            .given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(loginRequest).log().all()
            .when().post("/auth/login")
            .then().log().all()
            .extract().jsonPath().getObject("data", LoginResponse.class).getAccessToken();
    }

    private String getVendorAccessToken() {
        LoginRequest loginRequest = new LoginRequest("test@email.com",
            "test_password");

        return BEARER_LITERAL + RestAssured
            .given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(loginRequest).log().all()
            .when().post("/auth/login")
            .then().log().all()
            .extract().jsonPath().getObject("data", LoginResponse.class).getAccessToken();
    }
}