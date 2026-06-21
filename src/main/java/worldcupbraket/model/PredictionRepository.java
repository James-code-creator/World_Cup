package worldcupbraket.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionRepository extends JpaRepository<PredictionModel, Long> {
    Optional<PredictionModel> findFirstByUser_IdAndMatch_IdOrderByIdDesc(
            Long userId,
            Long matchId
    );
}