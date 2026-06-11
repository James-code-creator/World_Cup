package worldcupbraket;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "worldcup_results")
public record Result (
        Match Match,
        int Score1,
        int Score2
) {}
