package worldcupbraket.domain;

import java.util.List;

public record Score(
    List<Integer> ft,
    List<Integer> ht
){}
