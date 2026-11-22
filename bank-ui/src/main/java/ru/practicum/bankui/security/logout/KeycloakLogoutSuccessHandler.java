package ru.practicum.bankui.security.logout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * RP-Initiated Logout: после выхода из фронта завершает SSO-сессию в Keycloak.
 * Адрес end_session_endpoint задаётся явно, так как провайдер настроен без discovery по issuer-uri
 * (браузер и приложения обращаются к Keycloak по разным адресам).
 */
public class KeycloakLogoutSuccessHandler implements LogoutSuccessHandler {
    private final String endSessionUri;
    private final String clientId;

    public KeycloakLogoutSuccessHandler(String endSessionUri, String clientId) {
        this.endSessionUri = endSessionUri;
        this.clientId = clientId;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        UriComponentsBuilder logoutUri = UriComponentsBuilder.fromUriString(endSessionUri)
                .queryParam("client_id", clientId)
                .queryParam("post_logout_redirect_uri",
                        ServletUriComponentsBuilder.fromContextPath(request).path("/").toUriString());

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            logoutUri.queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue());
        }

        response.sendRedirect(logoutUri.encode().toUriString());
    }
}
