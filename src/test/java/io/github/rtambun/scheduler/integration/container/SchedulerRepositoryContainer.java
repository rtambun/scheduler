package io.github.rtambun.scheduler.integration.container;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class SchedulerRepositoryContainer extends ElasticsearchContainer {

    //see https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/ for reference of elastic search
    //version for your spring boot
    private static final String ELASTICSEARCH_VERSION = "7.15.2";
    private static final String ELASTICSEARCH_REGISTRY  = "docker.elastic.co/elasticsearch/elasticsearch";

    public static final String ELASTICSEARCH_HOST = "localhost";
    public static final String ELASTICSEARCH_USERNAME = "elastic";
    public static final String ELASTICSEARCH_PASSWORD = "password";
    public static final long MINUTES_BEFORE_NOW = 5;

    private static SchedulerRepositoryContainer schedulerRepositoryContainer;

    public static void startSchedulerRepositoryContainer() {
        if (schedulerRepositoryContainer == null) {
            schedulerRepositoryContainer = new SchedulerRepositoryContainer();
            schedulerRepositoryContainer.start();
        }
    }

    public static void stopSchedulerRepositoryContainer() {
        schedulerRepositoryContainer.stop();
        schedulerRepositoryContainer = null;
    }

    private SchedulerRepositoryContainer() {
        super(DockerImageName.parse(ELASTICSEARCH_REGISTRY).withTag(ELASTICSEARCH_VERSION));
        withPassword(ELASTICSEARCH_PASSWORD);
    }

    public static class SchedulerRepositoryInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {


        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String elasticSearchPort = "elasticsearch.port=" +
                    schedulerRepositoryContainer.getMappedPort(9200);
            String elasticSearchHost = "elasticsearch.host=" + ELASTICSEARCH_HOST;
            String elasticSearchUserName = "elasticsearch.username=" + ELASTICSEARCH_USERNAME;
            String elasticSearchPassword = "elasticsearch.password=" + ELASTICSEARCH_PASSWORD;
            String minutesBeforeNow = "incident.close.minutes.before=" + MINUTES_BEFORE_NOW;

            TestPropertyValues.of(elasticSearchPort).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchHost).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchUserName).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(elasticSearchPassword).applyTo(applicationContext.getEnvironment());
            TestPropertyValues.of(minutesBeforeNow).applyTo(applicationContext.getEnvironment());
        }
    }

}
