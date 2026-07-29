package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Keycloak mete los roles del realm en el claim anidado "realm_access.roles",
// no en "scope"/"scp" que es lo que el JwtGrantedAuthoritiesConverter por defecto de
// Spring Security espera (y con el que antepone el prefijo SCOPE_, no ROLE_). Sin este
// converter, hasRole()/hasAuthority() en @PreAuthorize nunca encuentran
// coincidencia aunque el token traiga el rol correcto.
//
// Sustituye por completo al conversor por defecto en vez de combinarlo con las
// autoridades SCOPE_: la autorizacion de este proyecto es integramente por rol de
// realm (matriz SEC-03), no hay ningun flujo que dependa de scopes OAuth2. Revisar
// esta decision si algun dia un cliente bearer-only necesita autorizar por scope.
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !(realmAccess.get(ROLES_CLAIM) instanceof Collection<?> roles)) {
            // Cubre tanto la ausencia del claim como un formato inesperado (ej. si
            // "roles" no fuera una lista) sin lanzar ClassCastException: preferimos
            // autenticar sin permisos a romper el filtro de seguridad en produccion.
            return List.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(role -> !role.isBlank())
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }
}
