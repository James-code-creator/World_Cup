package worldcupbraket.domain;

import java.util.List;

public record CasinoSpinResult(
        List<String> reels,
        int bet,
        int payout,
        int netChange,
        int predictionPoints,
        int casinoBalance,
        int totalPoints
) {
    public boolean isWin() {
        return netChange > 0;
    }

    public boolean isJackpot() {
        return payout >= bet * 10;
    }
}
