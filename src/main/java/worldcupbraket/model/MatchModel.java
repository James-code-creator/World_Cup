package worldcupbraket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "worldcup_match")
public class MatchModel {
    @Id
    private Long id;

    String round;
    String date;
    String time;
    String team1;
    String team2;
    @Column(name = "group_name")
    String group;
    String ground;

    public MatchModel(
        Long id,
        String round,
        String date,
        String time,
        String team1,
        String team2,
        String group,
        String ground
    ) {
        this.id = id;
        this.round = round;
        this.date = date;
        this.time = time;
        this.team1 = team1;
        this.team2 = team2;
        this.group = group;
        this.ground = ground;
    }

    public MatchModel() {}

    public Long getId() {
        return id;
    }

    public String getRound() {
        return round;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public String getGroup() {
        return group;
    }

    public String getGround() {
        return ground;
    }
}
