package docs.koda.api.project.dto;

import docs.koda.api.project.domain.Project.Visibility;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
        @Size(max = 255) String name,
        String description,
        Visibility visibility
) {}
