package docs.koda.api.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UploadSpecRequest(@NotBlank String content) {}
