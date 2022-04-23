package com.rtambun.scheduler.scheduler.util;

import java.time.Instant;

public interface IInstantProvider {
    Instant now();
}
