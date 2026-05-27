package docs.koda.api.project.repository;

import docs.koda.api.project.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageRepository extends JpaRepository<Page, UUID> {

    List<Page> findAllByProjectIdOrderBySortOrderAsc(UUID projectId);

    Optional<Page> findByProjectIdAndSlug(UUID projectId, String slug);

    boolean existsByProjectIdAndSlug(UUID projectId, String slug);
}
