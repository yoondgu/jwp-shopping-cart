package woowacourse.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static woowacourse.fixture.Fixture.TEST_EMAIL;
import static woowacourse.fixture.Fixture.TEST_PASSWORD;
import static woowacourse.fixture.Fixture.TEST_USERNAME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import woowacourse.auth.dto.SignInRequestDto;
import woowacourse.auth.dto.SignInResponseDto;
import woowacourse.shoppingcart.dto.SignUpDto;
import woowacourse.shoppingcart.service.CustomerService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("로그인 정보를 받아서 토큰을 반환한다.")
    void login() {
        final SignUpDto signUpDto = new SignUpDto(TEST_EMAIL, TEST_PASSWORD, TEST_USERNAME);
        customerService.signUp(signUpDto);
        final SignInRequestDto signInDto = new SignInRequestDto(TEST_EMAIL, TEST_PASSWORD);

        final SignInResponseDto token = authService.login(signInDto);
        final String subject = authService.extractEmail(token.getAccessToken());

        assertThat(subject).isEqualTo(TEST_EMAIL);
    }

}