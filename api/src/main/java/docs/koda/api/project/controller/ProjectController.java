package docs.koda.api.project.controller;

import docs.koda.api.project.dto.*;
import docs.koda.api.project.service.ProjectService;
import docs.koda.api.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@RequestBody @Valid CreateProjectRequest req,
                                  @AuthenticationPrincipal User user,
                                  HttpServletRequest http) {
        return projectService.create(req, user, resolveIp(http));
    }

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal User user) {
        return projectService.list(user);
    }

    @GetMapping("/{slug}")
    public ProjectResponse get(@PathVariable String slug, @AuthenticationPrincipal User user) {
        return projectService.getBySlug(slug, user);
    }

    @PutMapping("/{slug}")
    public ProjectResponse update(@PathVariable String slug,
                                   @RequestBody @Valid UpdateProjectRequest req,
                                   @AuthenticationPrincipal User user) {
        return projectService.update(slug, req, user);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String slug, @AuthenticationPrincipal User user) {
        projectService.delete(slug, user);
    }

    @PostMapping("/{slug}/spec")
    public ProjectResponse uploadSpec(@PathVariable String slug,
                                       @RequestBody @Valid UploadSpecRequest req,
                                       @AuthenticationPrincipal User user) {
        return projectService.uploadSpec(slug, req.content(), user);
    }

    @GetMapping(value = "/{slug}/spec", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpec(@PathVariable String slug,
                                           @AuthenticationPrincipal User user) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(projectService.getSpec(slug, user));
    }

    private String resolveIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
    }
}
