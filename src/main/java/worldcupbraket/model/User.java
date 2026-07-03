package worldcupbraket.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worldcup_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;

    @Column(nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 0")
    private int casinoBalance;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private final List<PredictionModel> predictions = new ArrayList<>();

    protected User() {}

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<PredictionModel> getAllPredictions() {
        return this.predictions;
    }

    public PredictionModel getPredictionFromMatch(MatchModel match) {
        for (PredictionModel prediction : predictions) {
            if (prediction.getMatch().getId().equals(match.getId())) {
                return prediction;
            }
        }
        return null;
    }

    public void addPrediction(PredictionModel prediction) {
        predictions.add(prediction);
        prediction.setUser(this);
    }

    public void removePrediction(PredictionModel prediction) {
        predictions.removeIf(p -> p.getMatch().getId().equals(
                prediction.getMatch().getId()
        ));
        prediction.setUser(null);
        prediction.setMatch(null);
    }

    public String getPassword() {
        return password;
    }

    public int getCasinoBalance() {
        return casinoBalance;
    }

    public void adjustCasinoBalance(int delta) {
        this.casinoBalance += delta;
    }
}