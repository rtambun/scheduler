package io.github.rtambun.integration;

import io.github.rtambun.integration.container.KafkaCloseIncidentContainer;
import io.github.rtambun.integration.container.SchedulerRepositoryContainer;
import io.github.rtambun.integration.mockserver.IncidentProviderMockWebServer;
import io.github.rtambun.scheduler.SchedulerApplication;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@SpringBootTest(classes = SchedulerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
		SchedulerRepositoryContainer.Initializer.class,
		IncidentProviderMockWebServer.Initializer.class,
		KafkaCloseIncidentContainer.Initializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SchedulerApplicationTests {

	@BeforeAll
	public static void setEnvironment() throws IOException {
		SchedulerRepositoryContainer.startSchedulerRepositoryContainer();
		IncidentProviderMockWebServer
				.startIncidentProviderMockWebServer()
				.getMockWebServer()
				.enqueue(new MockResponse()
						.setHeader("Content-Type", "application/json")
						.setResponseCode(HttpStatus.OK.value())
						.setBody("{\"Count\":\"0\", \"incidents\":[]}"));
		KafkaCloseIncidentContainer.startKafkaCloseIncidentContainer();
	}

	@AfterAll
	public static void destroyEnvironment() throws IOException {
		SchedulerRepositoryContainer.stopSchedulerRepositoryContainer();
		IncidentProviderMockWebServer.stopIncidentProviderMockWebServer();
		KafkaCloseIncidentContainer.stopKafkaCloseIncidentContainer();
	}

	@Test
	void contextLoads() {
	}

}
