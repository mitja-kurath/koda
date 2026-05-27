package docs.koda.api.project.service;

import docs.koda.api.audit.domain.AuditLog;
import docs.koda.api.audit.service.AuditLogService;
import docs.koda.api.project.domain.Project;
import docs.koda.api.project.domain.Project.Visibility;
import docs.koda.api.project.dto.CreateProjectRequest;
import docs.koda.api.project.dto.ProjectResponse;
import docs.koda.api.project.dto.UpdateProjectRequest;
import docs.koda.api.project.repository.ProjectRepository;
import docs.koda.api.team.repository.TeamMemberRepository;
import docs.koda.api.user.domain.Team;
import docs.koda.api.user.domain.User;
import docs.koda.api.user.repository.TeamRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final OpenApiValidationService openApiValidationService;
    private final AuditLogService auditLogService;

    @Transactional
    public ProjectResponse create(CreateProjectRequest req, User user, String ip) {
        Team team = requireTeam(user);
        String slug = ensureUniqueSlug(slugify(req.name()));
        Project project = Project.builder()
                .name(req.name())
                .slug(slug)
                .description(req.description())
                .visibility(req.visibility())
                .teamId(team.getId())
                .build();
        projectRepository.save(project);
        auditLogService.log(AuditLog.Event.REGISTER_SUCCESS, user.getId(), ip,
                "project_created:" + slug);
        return ProjectResponse.from(project);
    }

    public List<ProjectResponse> list(User user) {
        Set<UUID> teamIds = teamMemberRepository.findAllByUserId(user.getId())
                .stream().map(m -> m.getTeamId()).collect(Collectors.toSet());
        return projectRepository.findAllByTeamIdIn(teamIds)
                .stream().map(ProjectResponse::from).toList();
    }

    public ProjectResponse getBySlug(String slug, User user) {
        Project project = requireProject(slug);
        requireAccess(user, project);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(String slug, UpdateProjectRequest req, User user) {
        Project project = requireProject(slug);
        requireOwnerOrAdmin(user, project);
        if (req.name() != null) project.setName(req.name());
        if (req.description() != null) project.setDescription(req.description());
        if (req.visibility() != null) project.setVisibility(req.visibility());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void delete(String slug, User user) {
        Project project = requireProject(slug);
        requireOwner(user, project);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse uploadSpec(String slug, String rawContent, User user) {
        Project project = requireProject(slug);
        requireOwnerOrAdmin(user, project);
        OpenAPI parsed = openApiValidationService.parseAndValidate(rawContent);
        String json = openApiValidationService.toJson(parsed);
        project.setSpecContent(json);
        project.setSpecTitle(parsed.getInfo() != null ? parsed.getInfo().getTitle() : null);
        project.setSpecVersion(parsed.getInfo() != null ? parsed.getInfo().getVersion() : null);
        return ProjectResponse.from(projectRepository.save(project));
    }

    public String getSpec(String slug, User user) {
        Project project = requireProject(slug);
        requireAccess(user, project);
        if (project.getSpecContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No spec uploaded yet");
        }
        return project.getSpecContent();
    }

    // ── Public access (no auth) ──────────────────────────────────────────────

    public ProjectResponse getPublicBySlug(String slug) {
        Project project = requireProject(slug);
        if (project.getVisibility() != Visibility.PUBLIC) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return ProjectResponse.from(project);
    }

    public String getPublicSpec(String slug) {
        Project project = requireProject(slug);
        if (project.getVisibility() != Visibility.PUBLIC) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        if (project.getSpecContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No spec uploaded yet");
        }
        return project.getSpecContent();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Project requireProject(String slug) {
        return projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Team requireTeam(User user) {
        return teamRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Team not found"));
    }

    private void requireAccess(User user, Project project) {
        if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private void requireOwnerOrAdmin(User user, Project project) {
        teamMemberRepository.findByTeamIdAndUserId(project.getTeamId(), user.getId())
                .filter(m -> m.getRole() == docs.koda.api.team.domain.TeamMember.Role.OWNER
                        || m.getRole() == docs.koda.api.team.domain.TeamMember.Role.ADMIN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions"));
    }

    private void requireOwner(User user, Project project) {
        teamMemberRepository.findByTeamIdAndUserId(project.getTeamId(), user.getId())
                .filter(m -> m.getRole() == docs.koda.api.team.domain.TeamMember.Role.OWNER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can perform this action"));
    }

    private String slugify(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return NON_SLUG.matcher(normalized.toLowerCase()).replaceAll("-")
                .replaceAll("^-+|-+$", "")
                .substring(0, Math.min(64, normalized.length()));
    }

    private String ensureUniqueSlug(String base) {
        if (!projectRepository.existsBySlug(base)) return base;
        int i = 2;
        while (projectRepository.existsBySlug(base + "-" + i)) i++;
        return base + "-" + i;
    }
}
