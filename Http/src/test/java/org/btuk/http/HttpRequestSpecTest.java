package org.btuk.http;

import org.btuk.http.client.HttpRequestSpec;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestSpecTest {

    @Test
    public void testGetSpec() {
        HttpRequestSpec<String> spec = HttpRequestSpec.get("/test", String.class);
        assertEquals("GET", spec.getMethod());
        assertEquals("/test", spec.getPath());
        assertEquals(String.class, spec.getResponseType());
        assertNull(spec.getBody());
    }

    @Test
    public void testPostSpecWithBodyParameters() {
        HttpRequestSpec<Map> spec = HttpRequestSpec.<Map>builder()
                .method("POST")
                .path("/create")
                .bodyParameter("key1", "value1")
                .bodyParameter("key2", 123)
                .responseType(Map.class)
                .build();
        
        assertEquals("POST", spec.getMethod());
        assertEquals("/create", spec.getPath());
        assertTrue(spec.getBody() instanceof Map);
        Map body = (Map) spec.getBody();
        assertEquals("value1", body.get("key1"));
        assertEquals(123, body.get("key2"));
    }

    @Test
    public void testBodyParameterMerging() {
        Map<String, Object> initialBody = new java.util.HashMap<>();
        initialBody.put("existing", "value");

        HttpRequestSpec<Map> spec = HttpRequestSpec.<Map>builder()
                .method("POST")
                .path("/create")
                .body(initialBody)
                .bodyParameter("new", "parameter")
                .responseType(Map.class)
                .build();

        assertTrue(spec.getBody() instanceof Map);
        Map body = (Map) spec.getBody();
        assertEquals("value", body.get("existing"));
        assertEquals("parameter", body.get("new"));
    }

    @Test
    public void testBodyParameterConflict() {
        assertThrows(IllegalStateException.class, () -> {
            HttpRequestSpec.builder()
                    .body("not a map")
                    .bodyParameter("key", "value")
                    .build();
        });
    }

    @Test
    public void testSpecBuilder() {
        HttpRequestSpec<Void> spec = HttpRequestSpec.<Void>builder()
                .method("PUT")
                .path("/update")
                .queryParameter("id", "1")
                .responseType(Void.class)
                .build();
        
        assertEquals("PUT", spec.getMethod());
        assertEquals("/update", spec.getPath());
        assertEquals("1", spec.getQueryParameters().get("id"));
    }
}
