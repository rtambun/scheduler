package com.rtambun.scheduler.scheduler;

import com.rtambun.scheduler.scheduler.integration.SchedulerRepositoryContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = SchedulerApplication.class)
@ContextConfiguration(initializers = SchedulerRepositoryContainer.SchedulerRepositoryInitializer.class)
class SchedulerApplicationTests {

	@BeforeAll
	public static void setEnvironment() {
		SchedulerRepositoryContainer.startSchedulerRepositoryContainer();
	}

	@AfterAll
	public static void destroyEnvironment() {
		SchedulerRepositoryContainer.stopSchedulerRepositoryContainer();
	}

	@Test
	void contextLoads() {
	}

}
