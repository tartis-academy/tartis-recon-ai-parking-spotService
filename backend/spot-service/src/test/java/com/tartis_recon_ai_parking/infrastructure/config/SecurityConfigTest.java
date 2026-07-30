package com.tartis_recon_ai_parking.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// SEC-06 (mismo hallazgo que en tariff-service, revision 30/07): .with(jwt())
// en SpotRestAdapterTest no pasa por el JwtAuthenticationConverter real - inyecta
// la autenticacion ya construida directamente en el SecurityContext, saltandose
// tanto el JwtDecoder como este converter. Ese atajo es intencional (permite
// probar autorizacion por endpoint sin levantar un JWKS real), pero como efecto
// secundario ningun test de MockMvc verificaba que SecurityConfig conecta de
// verdad KeycloakRoleConverter dentro del JwtAuthenticationConverter que arma el
// filtro real. Este test cierra ese hueco sin MockMvc ni JWKS: instancia el bean
// tal cual lo construye SecurityConfig y comprueba que un JWT con roles de
// Keycloak produce authorities ROLE_*, que es exactamente lo que haria el
// filtro real en marcha.
class SecurityConfigTest {

    private final JwtAuthenticationConverter converter = new SecurityConfig().jwtAuthenticationConverter();

    @Test
    @DisplayName("El JwtAuthenticationConverter que registra SecurityConfig usa KeycloakRoleConverter, no el por defecto")
    void jwtAuthenticationConverterBean_ShouldUseKeycloakRoleConverter() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("ADMIN")));

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        // Ademas de ROLE_ADMIN, Spring Security 6.5+/7 anade automaticamente una
        // authority sintetica FACTOR_BEARER (tracking del "authentication factor"
        // usado, para step-up auth). No la genera KeycloakRoleConverter y no es
        // relevante para este test: solo nos importa que el rol del realm llega
        // como ROLE_*, sin usar el prefijo SCOPE_ del converter por defecto.
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN")
                .noneMatch(authority -> authority.startsWith("SCOPE_"));
    }

    @Test
    @DisplayName("Sin realm_access.roles, el converter real no aporta ningun ROLE_ (no usa el SCOPE_ por defecto de Spring)")
    void jwtAuthenticationConverterBean_ShouldReturnNoAuthorities_WhenNoRealmRoles() {
        Jwt jwt = buildJwt(null);

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        // FACTOR_BEARER sigue apareciendo (la anade el framework independientemente
        // de los roles del token); lo que prueba este caso es que sin
        // realm_access.roles no se cuela ningun ROLE_* ni SCOPE_* espureo.
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .noneMatch(authority -> authority.startsWith("ROLE_") || authority.startsWith("SCOPE_"));
    }

    private Jwt buildJwt(Map<String, Object> realmAccess) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", "test-user");

        if (realmAccess != null) {
            builder.claim("realm_access", realmAccess);
        }

        return builder.build();
    }
}
