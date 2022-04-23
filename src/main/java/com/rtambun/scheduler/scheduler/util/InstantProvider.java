package com.rtambun.scheduler.scheduler.util;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class InstantProvider implements IInstantProvider{

    @Override
    public Instant now() {
        return Instant.now();
    }
}
