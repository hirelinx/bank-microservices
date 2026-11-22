package ru.practicum.bankui.security.logout;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakLogoutSuccessHandlerTest {
    private static final String END_SESSION_URI = "http://localhost:8180/realms/bank-realm/protocol/openid-connect/logout";

    private final KeycloakLogoutSuccessHandler handler = new KeycloakLogoutSuccessHandler(END_SESSION_URI, "bank-ui");

    @Test
    void redirectsToKeycloakEndSessionEndpointWithIdTokenHint() throws Exception {
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", "ivanov", "preferred_username", "ivanov"));
        DefaultOidcUser user = new DefaultOidcUser(AuthorityUtils.NO_AUTHORITIES, idToken, "preferred_username");
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(user, user.getAuthorities(), "keycloak");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/logout");
        request.setServerName("localhost");
        request.setServerPort(8080);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onLogoutSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .startsWith(END_SESSION_URI + "?")
                .contains("client_id=bank-ui")
                .contains("post_logout_redirect_uri=http://localhost:8080/")
                .contains("id_token_hint=id-token-value");
    }
}
