package com.kos.service.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImageStorageException extends RuntimeException {
    public ImageStorageException(String message) {
        super(message);
    }
    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
