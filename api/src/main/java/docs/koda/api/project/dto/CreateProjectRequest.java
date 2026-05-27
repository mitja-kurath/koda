package docs.koda.api.project.dto;

import docs.koda.api.project.domain.Project.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull Visibility visibility
) {}
