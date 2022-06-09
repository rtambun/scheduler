package io.github.rtambun.scheduler.service.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InstantProviderTest {

    @Test
    public void nowOk() {
        InstantProvider instantProvider = new InstantProvider();
        assertThat(instantProvider.now()).isNotNull();
    }

}