package docs.koda.api.project.dto;

import docs.koda.api.project.domain.Project;
import docs.koda.api.project.domain.Project.Visibility;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Visibility visibility,
        boolean hasSpec,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getVisibility(), p.getSpecContent() != null,
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
