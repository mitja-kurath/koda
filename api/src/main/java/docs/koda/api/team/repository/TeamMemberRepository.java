package docs.koda.api.team.repository;

import docs.koda.api.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    List<TeamMember> findAllByUserId(UUID userId);

    List<TeamMember> findAllByTeamId(UUID teamId);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    void deleteByTeamIdAndUserId(UUID teamId, UUID userId);

}
