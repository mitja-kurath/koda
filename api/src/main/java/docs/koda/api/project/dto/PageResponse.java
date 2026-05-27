package docs.koda.api.project.dto;

import docs.koda.api.project.domain.Page;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageResponse(
        UUID id,
        UUID projectId,
        String title,
        String slug,
        String content,
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PageResponse from(Page p) {
        return new PageResponse(p.getId(), p.getProjectId(), p.getTitle(), p.getSlug(),
                p.getContent(), p.getSortOrder(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
