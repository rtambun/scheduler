package io.github.rtambun.integration.mockserver;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class IsmsMockWebServer {

    private MockWebServer mockWebServer;
    private static String baseUrl;

    private static IsmsMockWebServer ismsMockWebServer;

    public static IsmsMockWebServer startIsmsMockWebServer() throws IOException {
        if (ismsMockWebServer == null) {
            ismsMockWebServer = new IsmsMockWebServer();
        }
        return ismsMockWebServer;
    }

    public static void stopIsmsMockWebServer() throws IOException {
        if (ismsMockWebServer != null) {
            ismsMockWebServer.mockWebServer.shutdown();
            ismsMockWebServer = null;
        }
    }

    public MockWebServer getMockWebServer() {
        return mockWebServer;
    }

    private IsmsMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String ismsEpUri = "isms.ep.uri=" + baseUrl;
            String ismsTimeOut = "isms.ep.timeout=100";
            TestPropertyValues.of(ismsEpUri).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(ismsTimeOut).applyTo(applicationContext.getEnvironment());
        }
    }

}
