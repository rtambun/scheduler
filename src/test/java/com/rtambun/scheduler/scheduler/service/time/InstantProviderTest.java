package com.rtambun.scheduler.scheduler.service.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class InstantProviderTest {

    @Test
    public void nowOk() {
        InstantProvider instantProvider = new InstantProvider();
        assertThat(instantProvider.now()).isNotNull();
    }

}