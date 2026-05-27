package docs.koda.api.project.service;

import docs.koda.api.project.domain.Page;
import docs.koda.api.project.domain.Project;
import docs.koda.api.project.dto.PageRequest;
import docs.koda.api.project.dto.PageResponse;
import docs.koda.api.project.repository.PageRepository;
import docs.koda.api.project.repository.ProjectRepository;
import docs.koda.api.team.repository.TeamMemberRepository;
import docs.koda.api.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PageService {

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    private final PageRepository pageRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public PageResponse create(String projectSlug, PageRequest req, User user) {
        Project project = requireProject(projectSlug);
        requireAccess(user, project);
        String slug = ensureUniqueSlug(project, slugify(req.title()));
        Page page = Page.builder()
                .projectId(project.getId())
                .title(req.title())
                .slug(slug)
                .content(req.content())
                .sortOrder(req.sortOrder())
                .build();
        return PageResponse.from(pageRepository.save(page));
    }

    public List<PageResponse> list(String projectSlug, User user) {
        Project project = requireProject(projectSlug);
        requireAccess(user, project);
        return pageRepository.findAllByProjectIdOrderBySortOrderAsc(project.getId())
                .stream().map(PageResponse::from).toList();
    }

    public PageResponse get(String projectSlug, String pageSlug, User user) {
        Project project = requireProject(projectSlug);
        requireAccess(user, project);
        return PageResponse.from(requirePage(project, pageSlug));
    }

    @Transactional
    public PageResponse update(String projectSlug, String pageSlug, PageRequest req, User user) {
        Project project = requireProject(projectSlug);
        requireAccess(user, project);
        Page page = requirePage(project, pageSlug);
        page.setTitle(req.title());
        page.setContent(req.content());
        page.setSortOrder(req.sortOrder());
        return PageResponse.from(pageRepository.save(page));
    }

    @Transactional
    public void delete(String projectSlug, String pageSlug, User user) {
        Project project = requireProject(projectSlug);
        requireAccess(user, project);
        pageRepository.delete(requirePage(project, pageSlug));
    }

    // ── Public ───────────────────────────────────────────────────────────────

    public List<PageResponse> listPublic(String projectSlug) {
        Project project = requirePublicProject(projectSlug);
        return pageRepository.findAllByProjectIdOrderBySortOrderAsc(project.getId())
                .stream().map(PageResponse::from).toList();
    }

    public PageResponse getPublic(String projectSlug, String pageSlug) {
        Project project = requirePublicProject(projectSlug);
        return PageResponse.from(requirePage(project, pageSlug));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Project requireProject(String slug) {
        return projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Project requirePublicProject(String slug) {
        Project p = requireProject(slug);
        if (p.getVisibility() != Project.Visibility.PUBLIC) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return p;
    }

    private Page requirePage(Project project, String slug) {
        return pageRepository.findByProjectIdAndSlug(project.getId(), slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
    }

    private void requireAccess(User user, Project project) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private String slugify(String title) {
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = NON_SLUG.matcher(normalized.toLowerCase()).replaceAll("-")
                .replaceAll("^-+|-+$", "");
        return slug.substring(0, Math.min(64, slug.length()));
    }

    private String ensureUniqueSlug(Project project, String base) {
        if (!pageRepository.existsByProjectIdAndSlug(project.getId(), base)) return base;
        int i = 2;
        while (pageRepository.existsByProjectIdAndSlug(project.getId(), base + "-" + i)) i++;
        return base + "-" + i;
    }
}
