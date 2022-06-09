package io.github.rtambun.scheduler.service.time;

import java.time.Instant;

public interface IInstantProvider {
    Instant now();
}
