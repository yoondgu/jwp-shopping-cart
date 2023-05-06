package cart;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup({
        @Sql("/schema.sql"),
        @Sql("/data.sql")
})
// TODO 사용자 정보 테스트 픽스쳐로 상수화
public class CartItemIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인 사용자의 장바구니 아이템 조회 시 OK 응답코드를 반환한다")
    @Test
    void get() {
        RestAssured
                .given().log().all()
                .auth().preemptive().basic("dummy@gmail.com", "abcd1234")
                .when().get("/cartitems")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("인증되지 않은 사용자가 장바구니 아이템 조회 시 UNAUTHORIZED 응답코드를 반환한다")
    @Test
    void getUnauthorized() {
        RestAssured
                .given().log().all()
                .auth().preemptive().basic("unauthorized@gmail.com", "abcd1234")
                .when().get("/cartitems")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("로그인 사용자의 장바구니 아이템 등록 시 CREATED 응답코드를 반환한다")
    @Test
    void create() {
        RestAssured
                .given().log().all()
                .auth().preemptive().basic("dummy@gmail.com", "abcd1234")
                .when().post("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("로그인 사용자가 장바구니 아이템 중복 등록 시 BAD_REQUEST 응답코드를 반환한다")
    @Test
    void createDuplicated() {
        jdbcTemplate.update("INSERT INTO cart_items (member_id, product_id) VALUES (1, 1)");

        RestAssured
                .given().log().all()
                .auth().preemptive().basic("dummy@gmail.com", "abcd1234")
                .when().post("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("인증되지 않은 사용자가 장바구니 아이템 등록 시 UNAUTHORIZED 응답코드를 반환한다")
    @Test
    void createUnauthorized() {
        RestAssured
                .given().log().all()
                .auth().preemptive().basic("unauthorized@gmail.com", "abcd1234")
                .when().post("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("로그인 사용자의 장바구니 아이템 삭제 시 NO CONTENT 응답코드를 반환한다")
    @Test
    void delete() {
        jdbcTemplate.update("INSERT INTO cart_items (member_id, product_id) VALUES (1, 1)");

        RestAssured
                .given().log().all()
                .auth().preemptive().basic("dummy@gmail.com", "abcd1234")
                .when().delete("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("인증되지 않은 사용자가 장바구니 아이템 삭제 시 UNAUTHORIZED 응답코드를 반환한다")
    @Test
    void deleteUnauthorized() {
        jdbcTemplate.update("INSERT INTO cart_items (member_id, product_id) VALUES (1, 1)");

        RestAssured
                .given().log().all()
                .auth().preemptive().basic("unauthorized@gmail.com", "abcd1234")
                .when().delete("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("다른 사용자의 장바구니 아이템 삭제 시 FORBIDDEN 응답코드를 반환한다")
    @Test
    void deleteForbidden() {
        jdbcTemplate.update("INSERT INTO cart_items (member_id, product_id) VALUES (1, 1)");

        RestAssured
                .given().log().all()
                .auth().preemptive().basic("dummy2@gmail.com", "abcd5678")
                .when().delete("/cartitems/1")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
