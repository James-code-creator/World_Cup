package worldcupbraket.domain;

import java.util.Objects;

public enum Phase {
    GroupPhase,
    KoPhase,
    Final;

    public static Phase getPhase(Match match) {
        if (match.group() != null) {
            return Phase.GroupPhase;
        } else if (Objects.equals(match.round(), "Final")) {
            return Phase.Final;
        }
        return Phase.KoPhase;
    }
}
