package com.diagnostic.core.exception;

/**
 * Base exception for diagnostic tool.
 * All diagnostic-related exceptions should extend this class.
 */
public class DiagnosticException extends RuntimeException {

    public DiagnosticException(String message) {
        super(message);
    }

    public DiagnosticException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiagnosticException(Throwable cause) {
        super(cause);
    }
}
