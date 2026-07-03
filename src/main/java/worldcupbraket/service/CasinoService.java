package worldcupbraket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import worldcupbraket.domain.CasinoSpinResult;
import worldcupbraket.domain.Player;
import worldcupbraket.model.User;
import worldcupbraket.model.UserRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
public class CasinoService {

    static final String[] SYMBOLS = {"⚽", "🏆", "🥅", "🎰", "💀"};
    static final String JACKPOT_SYMBOL = "🏆";

    private final UserRepository userRepository;
    private final WorldCupBraketService worldCupBraketService;
    private final SecureRandom random = new SecureRandom();

    public CasinoService(
            UserRepository userRepository,
            WorldCupBraketService worldCupBraketService
    ) {
        this.userRepository = userRepository;
        this.worldCupBraketService = worldCupBraketService;
    }

    public int getTotalPoints(String username) {
        User user = requireUser(username);
        Player player = worldCupBraketService.getInstance().getPlayer(username);
        int predictionPoints = player != null ? player.getPoints() : 0;
        return predictionPoints + user.getCasinoBalance();
    }

    @Transactional
    public CasinoSpinResult spin(String username, int bet) {
        if (bet <= 0) {
            throw new IllegalArgumentException("Bet must be at least 1 point");
        }

        User user = requireUser(username);
        Player player = worldCupBraketService.getInstance().getPlayer(username);
        int predictionPoints = player != null ? player.getPoints() : 0;
        int totalAvailable = predictionPoints + user.getCasinoBalance();

        if (bet > totalAvailable) {
            throw new IllegalStateException("Not enough points to bet " + bet);
        }

        List<String> reels = spinReels();
        int payout = calculatePayout(reels, bet);
        int netChange = payout - bet;

        user.adjustCasinoBalance(netChange);
        userRepository.saveAndFlush(user);

        return new CasinoSpinResult(
                reels,
                bet,
                payout,
                netChange,
                predictionPoints,
                user.getCasinoBalance(),
                predictionPoints + user.getCasinoBalance()
        );
    }

    List<String> spinReels() {
        List<String> reels = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            reels.add(SYMBOLS[random.nextInt(SYMBOLS.length)]);
        }
        return reels;
    }

    public static int calculatePayout(List<String> reels, int bet) {
        String first = reels.get(0);
        String second = reels.get(1);
        String third = reels.get(2);

        if (first.equals(second) && second.equals(third)) {
            if (first.equals(JACKPOT_SYMBOL)) {
                return bet * 10;
            }
            return bet * 5;
        }
        if (first.equals(second) || second.equals(third) || first.equals(third)) {
            return bet * 2;
        }
        return 0;
    }

    private User requireUser(String username) {
        User user = userRepository.findFirstByName(username);
        if (user == null) {
            throw new IllegalStateException("User not found: " + username);
        }
        return user;
    }
}
