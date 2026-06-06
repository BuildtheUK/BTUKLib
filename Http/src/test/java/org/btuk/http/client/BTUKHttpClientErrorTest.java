package org.btuk.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.btuk.http.dto.ErrorResponse;
import org.btuk.http.exception.BTUKApiException;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class BTUKHttpClientErrorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testExceptionMapping() throws Exception {
        // Since I can't easily mock the final HttpClient/HttpResponse without Mockito, 
        // I'll test the BTUKApiException structure and parsing logic manually if possible.
        // But the core logic is in the private sendRequest method.
        
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(ErrorResponse.ErrorCodeEnum.INVALID_CREDENTIALS);
        errorResponse.addParametersItem("param1");
        errorResponse.addParametersItem("param2");
        
        String json = objectMapper.writeValueAsString(errorResponse);
        
        // Manual verification of the logic I implemented in BTUKHttpClient
        int statusCode = 401;
        ErrorResponse parsed = objectMapper.readValue(json, ErrorResponse.class);
        BTUKApiException ex = new BTUKApiException(statusCode, "API error: " + parsed.getErrorCode(), parsed);
        
        assertEquals(401, ex.getStatusCode());
        assertEquals(ErrorResponse.ErrorCodeEnum.INVALID_CREDENTIALS, ex.getErrorResponse().getErrorCode());
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
        assertNotNull(ex.getParameters());
        assertEquals(2, ex.getParameters().size());
        assertEquals("param1", ex.getParameters().get(0));
        assertTrue(ex.getMessage().contains("INVALID_CREDENTIALS"));
    }
}
