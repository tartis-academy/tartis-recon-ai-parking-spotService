package com.tartis_recon_ai_parking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Tests de arranque de la aplicacion (SpotServiceApplication)")
class SpotServiceApplicationTests {

    @Test
    @DisplayName("Debe cargar correctamente el contexto de Spring Boot")
    void contextLoads() {
        // QUE HACE:
        // Inicia el contexto de la aplicacion Spring Boot vacio.
        
        // QUE DEBERIA HACER:
        // Si el contexto levanta sin lanzar excepciones (por problemas de dependencias, beans o configuracion),
        // la asercion asegura que el test pasa correctamente.
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Debe ejecutar el metodo main sin fallos")
    void mainMethodRunsSuccessfully() {
        // QUE HACE:
        // Ejecuta explicitamente el metodo main de la clase principal, pasando argumentos
        // mockeados (como "--server.port=0") para evitar colisiones de puerto en entorno local.
        SpotServiceApplication.main(new String[]{"--server.port=0"});
        
        // QUE DEBERIA HACER:
        // El proceso principal debe terminar de levantar o delegar el inicio de la app sin arrojar excepciones.
        assertThat(true).isTrue();
    }
}
