package docs.koda.api.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank String content,
        int sortOrder
) {}
