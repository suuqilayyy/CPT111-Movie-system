package movietracker.backend;

public final class UserManagerProvider {
    private static final UserManager INSTANCE = new UserManager();

    private UserManagerProvider() {
    }

    public static UserManager getInstance() {
        return INSTANCE;
    }
}
