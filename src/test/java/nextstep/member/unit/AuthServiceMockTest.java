package nextstep.member.unit;

import nextstep.auth.application.AuthService;
import nextstep.auth.application.dto.TokenRequest;
import nextstep.auth.application.dto.TokenResponse;
import nextstep.member.application.JwtTokenProvider;
import nextstep.member.application.MemberService;
import nextstep.member.domain.Member;
import nextstep.member.domain.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static nextstep.common.constants.ErrorConstant.INVALID_EMAIL_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceMockTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MemberService memberService;

    private static final String EMAIL = "admin@email.com";
    private static final String PASSWORD = "password";

    @Test
    @DisplayName("토큰 생성 실패-비밀번호 미일치")
    void login_mismatchPwd() {
        // given
        final Member member = new Member(EMAIL, PASSWORD, 20, List.of(RoleType.ROLE_ADMIN.name()));
        when(memberService.findByEmail(EMAIL)).thenReturn(member);

        // when
        // then
        assertThatThrownBy(() -> authService.login(new TokenRequest(EMAIL, "new" + PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_EMAIL_PASSWORD);
    }

    @Test
    @DisplayName("토큰 생성")
    void login() {
        // given
        final Member member = new Member(EMAIL, PASSWORD, 20, List.of(RoleType.ROLE_ADMIN.name()));
        when(memberService.findByEmail(EMAIL)).thenReturn(member);
        when(jwtTokenProvider.createToken(member.getEmail(), member.getRoles())).thenReturn("token");

        // when
        final TokenResponse token = authService.login(new TokenRequest(EMAIL, PASSWORD));

        // then
        assertThat(token.getAccessToken()).isNotBlank();
    }
}
