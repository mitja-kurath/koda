package docs.koda.api.project.repository;

import docs.koda.api.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findBySlug(String slug);

    List<Project> findAllByTeamIdIn(Set<UUID> teamIds);

    boolean existsBySlug(String slug);
}
