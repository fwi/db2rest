package com.github.fwi.db2rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -4247494724377002953L;

    public BadRequestException(String msg) {
        this(msg, null);
    }

    public BadRequestException(Exception cause) {
        this(cause + StringUtils.EMPTY, null);
    }

    public BadRequestException(String msg, Exception cause) {
        super(msg, cause);
    }

}
