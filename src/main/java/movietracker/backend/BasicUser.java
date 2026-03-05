package movietracker.backend;

public class BasicUser extends User {
    private static final int RECOMMENDATION_LIMIT = 6;

    public BasicUser(String username, String password) {
        super(username, password, new Watchlist(), new History());
    }

    public BasicUser(String username, String password, Watchlist watchlist, History history) {
        super(username, password, watchlist, history);
    }

    @Override
    public String getUserType() {
        return "BasicUser";
    }
    @Override
    public int getRecommendationLimit() {
        return RECOMMENDATION_LIMIT;
    }

    @Override
    public boolean requiresPasswordHashing() {
        return false;
    }
}

