package worldcupbraket.model;

import jakarta.persistence.*;


@Entity
@Table(name = "worldcup_matchresult")
public class MatchResultModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public int score1;
    public int score2;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchModel match;

    protected MatchResultModel() {
    }

    public MatchResultModel(MatchModel match, int score1, int score2) {
        this.match = match;
        this.score1 = score1;
        this.score2 = score2;
    }

    public void setMatch(MatchModel match) {
        this.match = match;
    }

    public MatchModel getMatch() {
        return this.match;
    }
}