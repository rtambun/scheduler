package io.github.rtambun.integration.mockserver;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class IncidentProviderMockWebServer {

    private final MockWebServer mockWebServer;
    private static String baseUrl;
    private static String pathUrl;

    private static IncidentProviderMockWebServer incidentProviderMockWebServer;

    public static IncidentProviderMockWebServer startIsmsMockWebServer() throws IOException {
        if (incidentProviderMockWebServer == null) {
            incidentProviderMockWebServer = new IncidentProviderMockWebServer();
        }
        return incidentProviderMockWebServer;
    }

    public static void stopIsmsMockWebServer() throws IOException {
        if (incidentProviderMockWebServer != null) {
            incidentProviderMockWebServer.mockWebServer.shutdown();
            incidentProviderMockWebServer = null;
        }
    }

    public MockWebServer getMockWebServer() {
        return mockWebServer;
    }

    private IncidentProviderMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        pathUrl = "/incident/query";
    }

    public static class Initializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String incidentProviderUri = "incident.close.provider.url=" + baseUrl;
            String incidentProviderUrl = "incident.close.provider.path=" + pathUrl;
            String incidentProviderTimeOut = "incident.close.provider.timeout=1000";
            TestPropertyValues.of(incidentProviderUri).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(incidentProviderUrl).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(incidentProviderTimeOut).applyTo(applicationContext.getEnvironment());
        }
    }

}
