package org.devnull.client.spring.error

class KeyNotFoundException extends RuntimeException {
    KeyNotFoundException(String message, Throwable e = null) {
        super(message, e)
    }
}
