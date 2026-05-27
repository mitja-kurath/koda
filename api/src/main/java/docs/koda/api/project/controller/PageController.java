package docs.koda.api.project.controller;

import docs.koda.api.project.dto.PageRequest;
import docs.koda.api.project.dto.PageResponse;
import docs.koda.api.project.service.PageService;
import docs.koda.api.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectSlug}/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PageResponse create(@PathVariable String projectSlug,
                                @RequestBody @Valid PageRequest req,
                                @AuthenticationPrincipal User user) {
        return pageService.create(projectSlug, req, user);
    }

    @GetMapping
    public List<PageResponse> list(@PathVariable String projectSlug,
                                    @AuthenticationPrincipal User user) {
        return pageService.list(projectSlug, user);
    }

    @GetMapping("/{pageSlug}")
    public PageResponse get(@PathVariable String projectSlug,
                             @PathVariable String pageSlug,
                             @AuthenticationPrincipal User user) {
        return pageService.get(projectSlug, pageSlug, user);
    }

    @PutMapping("/{pageSlug}")
    public PageResponse update(@PathVariable String projectSlug,
                                @PathVariable String pageSlug,
                                @RequestBody @Valid PageRequest req,
                                @AuthenticationPrincipal User user) {
        return pageService.update(projectSlug, pageSlug, req, user);
    }

    @DeleteMapping("/{pageSlug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String projectSlug,
                        @PathVariable String pageSlug,
                        @AuthenticationPrincipal User user) {
        pageService.delete(projectSlug, pageSlug, user);
    }
}
