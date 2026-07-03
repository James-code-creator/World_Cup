package worldcupbraket;

import org.junit.jupiter.api.Test;
import worldcupbraket.service.CasinoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CasinoTests {

    @Test
    void jackpotPaysTenTimesBet() {
        int payout = CasinoService.calculatePayout(List.of("🏆", "🏆", "🏆"), 10);
        assertEquals(100, payout);
    }

    @Test
    void threeOfAKindPaysFiveTimesBet() {
        int payout = CasinoService.calculatePayout(List.of("⚽", "⚽", "⚽"), 10);
        assertEquals(50, payout);
    }

    @Test
    void twoOfAKindPaysDouble() {
        int payout = CasinoService.calculatePayout(List.of("⚽", "⚽", "🏆"), 10);
        assertEquals(20, payout);
    }

    @Test
    void noMatchPaysNothing() {
        int payout = CasinoService.calculatePayout(List.of("⚽", "🏆", "🥅"), 10);
        assertEquals(0, payout);
    }
}
