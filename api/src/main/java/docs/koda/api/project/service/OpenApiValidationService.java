package docs.koda.api.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenApiValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAPI parseAndValidate(String content) {
        SwaggerParseResult result = new OpenAPIParser().readContents(content, null, null);
        List<String> messages = result.getMessages();
        if (result.getOpenAPI() == null) {
            String reason = (messages != null && !messages.isEmpty()) ? messages.get(0) : "Unable to parse spec";
            throw new IllegalArgumentException("Invalid OpenAPI spec: " + reason);
        }
        return result.getOpenAPI();
    }

    public String toJson(OpenAPI spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize spec", e);
        }
    }
}
