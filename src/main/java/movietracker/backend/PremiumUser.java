package movietracker.backend;

//PremiumUser represents a premium user account with enhanced features.Extends User class.

public class PremiumUser extends User {
    private static final int RECOMMENDATION_LIMIT = 16;

    public PremiumUser(String username, String password) {
        super(username, password, new Watchlist(), new History());
    }

    public PremiumUser(String username, String password, Watchlist watchlist, History history) {
        super(username, password, watchlist, history);
    }

    @Override
    public String getUserType() {
        return "PremiumUser";
    }
    @Override
    public int getRecommendationLimit() {
        return RECOMMENDATION_LIMIT;
    }

    @Override
    public boolean requiresPasswordHashing() {
        return true;
    }
}

