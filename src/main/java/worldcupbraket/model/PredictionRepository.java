package worldcupbraket.model;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<PredictionModel, Long> {
    Example<? extends PredictionModel> findByScore1(int score1);
}