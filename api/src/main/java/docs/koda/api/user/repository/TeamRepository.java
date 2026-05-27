package docs.koda.api.user.repository;

import docs.koda.api.user.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByOwnerId(UUID ownerId);
}
