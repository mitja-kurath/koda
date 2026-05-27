package docs.koda.api.project.controller;

import docs.koda.api.project.dto.PageResponse;
import docs.koda.api.project.dto.ProjectResponse;
import docs.koda.api.project.service.PageService;
import docs.koda.api.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final ProjectService projectService;
    private final PageService pageService;

    @GetMapping("/{slug}")
    public ProjectResponse getProject(@PathVariable String slug) {
        return projectService.getPublicBySlug(slug);
    }

    @GetMapping(value = "/{slug}/spec", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpec(@PathVariable String slug) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectService.getPublicSpec(slug));
    }

    @GetMapping("/{slug}/pages")
    public List<PageResponse> listPages(@PathVariable String slug) {
        return pageService.listPublic(slug);
    }

    @GetMapping("/{slug}/pages/{pageSlug}")
    public PageResponse getPage(@PathVariable String slug, @PathVariable String pageSlug) {
        return pageService.getPublic(slug, pageSlug);
    }
}
