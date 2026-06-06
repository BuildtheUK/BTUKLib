package org.btuk.http.exception;

import lombok.Getter;
import org.btuk.http.dto.ErrorResponse;

@Getter
public class BTUKApiException extends RuntimeException {
    private final int statusCode;
    private final ErrorResponse errorResponse;

    public BTUKApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = null;
    }

    public BTUKApiException(int statusCode, String message, ErrorResponse errorResponse) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    /**
     * Returns the error code from the ErrorResponse, if available.
     */
    public String getErrorCode() {
        return errorResponse != null && errorResponse.getErrorCode() != null
                ? errorResponse.getErrorCode().getValue()
                : null;
    }

    /**
     * Returns the parameters from the ErrorResponse, if available.
     */
    public java.util.List<String> getParameters() {
        return errorResponse != null ? errorResponse.getParameters() : null;
    }

    @Override
    public String toString() {
        return "BTUKApiException{" +
                "statusCode=" + statusCode +
                ", message='" + getMessage() + '\'' +
                ", errorResponse=" + errorResponse +
                '}';
    }
}
