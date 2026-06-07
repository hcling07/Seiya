package seiya;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Test
    void commandLinePortOverridesEnvironmentConfiguration() {
        assertEquals(9090, Main.webPort(new String[] {"web", "9090"}, "10000"));
    }

    @Test
    void usesEnvironmentPortWhenCommandLinePortIsMissing() {
        assertEquals(10000, Main.webPort(new String[] {"web"}, "10000"));
    }

    @Test
    void defaultsToLocalPortWhenNoPortIsConfigured() {
        assertEquals(8080, Main.webPort(new String[] {"web"}, null));
    }
}
