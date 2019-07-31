package com.github.fwi.db2rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6657565462243927741L;

	public NotFoundException(String msg) {
		this(msg, null);
	}

	public NotFoundException(Exception cause) {
		this(cause + StringUtils.EMPTY, null);
	}

	public NotFoundException(String msg, Exception cause) {
		super(msg, cause);
	}
}
