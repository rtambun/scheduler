package com.rtambun.scheduler.scheduler.service.time;

import java.time.Instant;

public interface IInstantProvider {
    Instant now();
}
