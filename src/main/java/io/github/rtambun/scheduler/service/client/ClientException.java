package io.github.rtambun.scheduler.service.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClientException extends RuntimeException {

    public static final int SERVICE_RESPONSE_STATUS_NON_2XX = 1;
    public static final int SERVICE_REQUEST_FAILED = 2;

    private int status;
}
